package vn.com.irtech.eport.framework.mqtt.listener;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Processor;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.framework.web.service.ConfigService;
import vn.com.irtech.eport.framework.web.service.MqttService;
import vn.com.irtech.eport.framework.web.service.WebSocketService;
import vn.com.irtech.eport.framework.web.service.MqttService.EServiceRobot;
import vn.com.irtech.eport.framework.web.service.MqttService.NotificationCode;
import vn.com.irtech.eport.logistic.domain.ProcessHistory;
import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.dto.ServiceSendFullRobotReq;
import vn.com.irtech.eport.logistic.service.IProcessBillService;
import vn.com.irtech.eport.logistic.service.IProcessHistoryService;
import vn.com.irtech.eport.logistic.service.IProcessOrderService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.system.domain.SysRobot;
import vn.com.irtech.eport.system.service.ISysRobotService;

@Component
public class RobotResponseHandler implements IMqttMessageListener{
	
	private static final Logger logger = LoggerFactory.getLogger(RobotResponseHandler.class);
	
	@Autowired
	private ISysRobotService robotService;
	
	@Autowired
	private IProcessOrderService processOrderService;

	@Autowired
	private IShipmentDetailService shipmentDetailService;
	
	@Autowired
	private IProcessBillService processBillService;
	
	@Autowired
	private WebSocketService webSocketService;
	
	@Autowired
	private IProcessHistoryService processHistoryService;

	@Autowired
	private MqttService mqttService;

	@Autowired
	private ConfigService configService;

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		logger.info("Receive message subject : " + topic);
		String messageContent = new String(message.getPayload());
		logger.info("Receive message content : " + messageContent);
		Map<String, Object> map = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			map = new Gson().fromJson(messageContent, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		String uuId = map.get("id") == null ? null : map.get("id").toString();
		if (uuId == null) {
			return;
		}

		String orderNo = map.get("orderNo") == null ? null : map.get("orderNo").toString();
		String orderType = map.get("serviceType") == null ? null : map.get("serviceType").toString();
		Integer serviceType = Integer.parseInt(orderType);
		SysRobot sysRobot = robotService.selectRobotByUuId(uuId);

		if (sysRobot == null) {
			return;
		}

		String status = map.get("status") == null ? null : map.get("status").toString();
		String result = map.get("result") == null ? null : map.get("result").toString();
		String receiptId = map.get("receiptId") == null ? null : map.get("receiptId").toString();
		String invoiceNo = map.get("invoiceNo") == null ? "" : map.get("invoiceNo").toString(); 

		if (receiptId != null) {
			if ("0".equals(status)) {
				if (serviceType == 1 || serviceType == 2 || serviceType == 3 || serviceType == 4 || serviceType == 5) {
					this.updateShipmentDetail(result, receiptId, invoiceNo, uuId, orderNo);
				}	
				switch (serviceType) {
					case 6:
						this.updateChangeVesselOrder(result, receiptId, uuId);
						break;
					case 7:
						this.updateCreateBookingOrder(result, receiptId, uuId);
						break;
					case 9:
						this.updateExtensionDateOrder(result, receiptId, uuId);
						break;
					default:
						break;
				}
				this.sendMessageWebsocket(result, receiptId);
				status = this.assignNewProcessOrder(sysRobot);
			} else if ("1".equals(status)) {
				// SAVE HISTORY ROBOT START MAKE-ORDER
				this.updateHistory(receiptId, uuId);
			}
		}

		robotService.updateRobotStatusByUuId(uuId, status);
		
	}
	
	/**
	 * update shipment detail
	 * 
	 * @param result:   "success/error"
	 * @param receiptId
	 */
	@Transactional
	private void updateShipmentDetail(String result, String receiptId, String invoiceNo, String uuId, String orderNo) {
		// INIT PROCESS HISTORY
		Long id = Long.parseLong(receiptId);
		ProcessHistory processHistory = new ProcessHistory();
		processHistory.setProcessOrderId(id);
		processHistory.setRobotUuid(uuId);
		processHistory.setStatus(2); // FINISH

		ProcessOrder processOrder = new ProcessOrder();

		if ("success".equalsIgnoreCase(result)) {
			// GET LIST SHIPMENT DETAIL BY PROCESS ORDER ID
			ShipmentDetail shipmentDetail = new ShipmentDetail();
			shipmentDetail.setProcessOrderId(Long.parseLong(receiptId));
			List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
			
			// GET PROCESS ORDER AND SET NEW STATUS TO UPDATE
			try {
				processOrder = processOrderService.selectProcessOrderById(id);
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
			
			processOrder.setOrderNo(orderNo);
			processOrder.setInvoiceNo(invoiceNo);
			processOrder.setStatus(2); // FINISH		
			processOrder.setResult("S"); // RESULT SUCESS	
			processOrderService.updateProcessOrder(processOrder);

			// SAVE BILL TO PROCESS BILL BY INVOICE NO
			if (invoiceNo != null && !invoiceNo.equals("")) {
				processBillService.saveProcessBillByInvoiceNo(processOrder);
			} else if (processOrder.getServiceType() != 5) {
				processBillService.saveProcessBillWithCredit(shipmentDetails, processOrder);
			}

			// UPDATE STATUS OF SHIPMENT DETAIL AFTER MAKE ORDER SUCCESS
			if (processOrder.getServiceType() != 5) {
				shipmentDetailService.updateProcessStatus(shipmentDetails, "Y", invoiceNo, processOrder);
			}

			// SET RESULT FOR HISTORY SUCCESS
			processHistory.setResult("S");
		} else {
			// Send notification for om
			try {
				mqttService.sendNotification(NotificationCode.NOTIFICATION_OM, "Lỗi lệnh số " + processOrder.getId(), configService.getKey("domain.admin.name") + "/om/executeCatos/detail/" + processOrder.getId());
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

			// INIT PROCESS ORDER TO UPDATE
			processOrder = new ProcessOrder();
			processOrder.setId(id);
			processOrder.setResult("F"); // RESULT FAILED
			processOrder.setStatus(0); // BACK TO WAITING STATUS FOR OM HANDLE
			if (orderNo != null) {
				processOrder.setOrderNo(orderNo);
			}
			processOrderService.updateProcessOrder(processOrder);
			
			ShipmentDetail shipmentDetail = new ShipmentDetail();
			shipmentDetail.setProcessOrderId(Long.parseLong(receiptId));
			List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
			for (ShipmentDetail shipmentDetail2 : shipmentDetails) {
				shipmentDetail2.setProcessStatus("E");
			}

			// SET RESULT FOR HISTORY FAILED
			processHistory.setResult("F");
		}
		processHistoryService.insertProcessHistory(processHistory);
	}

	private void updateHistory(String receiptId, String uuId) {
		Long id = Long.parseLong(receiptId);
		ProcessHistory processHistory = new ProcessHistory();
		processHistory.setProcessOrderId(id);
		processHistory.setRobotUuid(uuId);
		processHistory.setStatus(1); // START
		processHistory.setResult("S"); // RESULT SUCCESS
		processHistoryService.insertProcessHistory(processHistory);
	}
	
	private void sendMessageWebsocket(String result, String receiptId) {
		AjaxResult ajaxResult= null;
		if ("success".equalsIgnoreCase(result)) {
			ajaxResult = AjaxResult.success("Làm lệnh thành công!");
		} else {
			ajaxResult = AjaxResult.error("Làm lệnh thất bại, quý khách vui lòng liên hệ với bộ phận OM để được hỗ trợ thêm.");
		}
		
		webSocketService.sendMessage("/" + receiptId + "/response", ajaxResult);
	}

	public String assignNewProcessOrder(SysRobot robot) throws MqttException {
		// Get service types of robot
		String serviceTypes = "";

		if (robot.getIsReceiveContFullOrder()) {
			serviceTypes += 1 + ",";
		}
		if (robot.getIsReceiveContEmptyOrder()) {
			serviceTypes += 3 + ",";
		}
		if (robot.getIsSendContFullOrder()) {
			serviceTypes += 4 + ",";
		}
		if (robot.getIsSendContEmptyOrder()) {
			serviceTypes += 2 + ",";
		}
		if (robot.getIsShiftingContOrder()) {
			serviceTypes += 5 + ",";
		}
		if (robot.getIsChangeVesselOrder()) {
			serviceTypes += 6 + ",";
		}
		if (robot.getIsCreateBookingOrder()) {
			serviceTypes += 7 + ",";
		}
		if (robot.getIsExtensionDateOrder()) {
			serviceTypes += 9 + ",";
		}

		if (serviceTypes.length() > 0) {
			serviceTypes = serviceTypes.substring(0, serviceTypes.length()-1);
		}

		// Find process order for robot
		ProcessOrder reqProcessOrder = processOrderService.findProcessOrderForRobot(serviceTypes);
		if (reqProcessOrder != null) {
			if (processOrderService.updateProcessOrder(reqProcessOrder) == 1) {
				ShipmentDetail shipmentDetail = new ShipmentDetail();
				shipmentDetail.setProcessOrderId(reqProcessOrder.getId());
				List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
				ServiceSendFullRobotReq req = new ServiceSendFullRobotReq(reqProcessOrder, shipmentDetails);

				// Send order to robot
				switch (reqProcessOrder.getServiceType()) {
					case 1:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.RECEIVE_CONT_FULL, robot.getUuId());
						break;
					case 2:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.SEND_CONT_EMPTY, robot.getUuId());
						break;
					case 3:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.RECEIVE_CONT_EMPTY, robot.getUuId());
						break;
					case 4:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.SEND_CONT_FULL, robot.getUuId());
						break;
					case 5:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.SHIFTING_CONT, robot.getUuId());
						break;
					case 6:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.CHANGE_VESSEL, robot.getUuId());
						break;
					case 7:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.CREATE_BOOKING, robot.getUuId());
						break;
					case 9:
						mqttService.publicMessageToDemandRobot(req, EServiceRobot.EXTENSION_DATE, robot.getUuId());
						break;
				}
				return "1";
			} 
		}
		return "0";
	}
	
	@Transactional
	private void updateChangeVesselOrder(String result, String receiptId, String uuId) {
		// INIT PROCESS HISTORY
		Long id = Long.parseLong(receiptId);
		ProcessHistory processHistory = new ProcessHistory();
		processHistory.setProcessOrderId(id);
		processHistory.setRobotUuid(uuId);
		processHistory.setStatus(2); // FINISH

		ProcessOrder processOrder = processOrderService.selectProcessOrderById(id);
		if ("success".equalsIgnoreCase(result)) {
			Map<String, Object> map = new Gson().fromJson(processOrder.getProcessData(), Map.class);
			List<Long> shipmentDetailIds = (List<Long>) map.get("shipmentDetailIds");
			for (Long shipmentDetailId : shipmentDetailIds) {
				ShipmentDetail shipmentDetail = new ShipmentDetail();
				shipmentDetail.setId(shipmentDetailId);
				shipmentDetail.setVslNm(processOrder.getVessel());
				shipmentDetail.setVoyNo(processOrder.getVoyage());
				// TODO : Update carrier code
				
				shipmentDetailService.updateShipmentDetail(shipmentDetail);
			}
			processOrder.setStatus(2); // FINISH		
			processOrder.setResult("S"); // RESULT SUCESS	
			processOrderService.updateProcessOrder(processOrder);
			processHistory.setResult("S");
		} else {
			// Send notification for om
			try {
				mqttService.sendNotification(NotificationCode.NOTIFICATION_OM, "Lỗi lệnh số " + receiptId, configService.getKey("domain.admin.name") + "/om/executeCatos/detail/" + receiptId);
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

			// INIT PROCESS ORDER TO UPDATE
			processOrder = new ProcessOrder();
			processOrder.setId(id);
			processOrder.setResult("F"); // RESULT FAILED
			processOrder.setStatus(0); // BACK TO WAITING STATUS FOR OM HANDLE
			processOrderService.updateProcessOrder(processOrder);

			// SET RESULT FOR HISTORY FAILED
			processHistory.setResult("F");
		}
		processHistoryService.insertProcessHistory(processHistory);
	}
	
	private void updateCreateBookingOrder(String result, String receiptId, String uuId) {
		
	}
	
	private void updateExtensionDateOrder(String result, String receiptId, String uuId) {
		// INIT PROCESS HISTORY
		Long id = Long.parseLong(receiptId);
		ProcessHistory processHistory = new ProcessHistory();
		processHistory.setProcessOrderId(id);
		processHistory.setRobotUuid(uuId);
		processHistory.setStatus(2); // FINISH

		ProcessOrder processOrder = processOrderService.selectProcessOrderById(id);
		if ("success".equalsIgnoreCase(result)) {
			Map<String, Object> map = new Gson().fromJson(processOrder.getProcessData(), Map.class);
			List<Long> shipmentDetailIds = (List<Long>) map.get("shipmentDetailIds");
			for (Long shipmentDetailId : shipmentDetailIds) {
				ShipmentDetail shipmentDetail = new ShipmentDetail();
				shipmentDetail.setId(shipmentDetailId);
				shipmentDetail.setExpiredDem(processOrder.getPickupDate());
				shipmentDetailService.updateShipmentDetail(shipmentDetail);
			}
			processOrder.setStatus(2); // FINISH		
			processOrder.setResult("S"); // RESULT SUCESS	
			processOrderService.updateProcessOrder(processOrder);
			processHistory.setResult("S");
		} else {
			// Send notification for om
			try {
				mqttService.sendNotification(NotificationCode.NOTIFICATION_OM, "Lỗi lệnh số " + receiptId, configService.getKey("domain.admin.name") + "/om/executeCatos/detail/" + receiptId);
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

			// INIT PROCESS ORDER TO UPDATE
			processOrder = new ProcessOrder();
			processOrder.setId(id);
			processOrder.setResult("F"); // RESULT FAILED
			processOrder.setStatus(0); // BACK TO WAITING STATUS FOR OM HANDLE
			processOrderService.updateProcessOrder(processOrder);

			// SET RESULT FOR HISTORY FAILED
			processHistory.setResult("F");
		}
		processHistoryService.insertProcessHistory(processHistory);
	}
}
