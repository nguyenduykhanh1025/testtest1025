package vn.com.irtech.eport.api.mqtt.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

import vn.com.irtech.eport.api.consts.BusinessConsts;
import vn.com.irtech.eport.api.consts.MessageConsts;
import vn.com.irtech.eport.api.consts.MqttConsts;
import vn.com.irtech.eport.api.form.CheckinReq;
import vn.com.irtech.eport.api.form.DriverDataRes;
import vn.com.irtech.eport.api.form.DriverRes;
import vn.com.irtech.eport.api.form.GateInFormData;
import vn.com.irtech.eport.api.form.MeasurementDataReq;
import vn.com.irtech.eport.api.form.PickupHistoryDataRes;
import vn.com.irtech.eport.api.message.MessageHelper;
import vn.com.irtech.eport.api.mqtt.service.MqttService;
import vn.com.irtech.eport.common.exception.BusinessException;
import vn.com.irtech.eport.logistic.domain.PickupHistory;
import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
import vn.com.irtech.eport.logistic.service.IPickupHistoryService;
import vn.com.irtech.eport.logistic.service.IProcessOrderService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.system.domain.SysRobot;
import vn.com.irtech.eport.system.service.ISysRobotService;

@Component
public class CheckinHandler implements IMqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(CheckinHandler.class);

	@Autowired
	private MqttService mqttService;
	
	@Autowired
	private IProcessOrderService processOrderService;

	@Autowired
	private IPickupHistoryService pickupHistoryService;
	
	@Autowired 
	private ISysRobotService robotService;

	@Autowired
	private IShipmentDetailService shipmentDetailService;
	
	@Autowired
	private ICatosApiService catosApiService;

	// time wait mc input postion
	private static final Long TIME_OUT_WAIT_MC = 6000L;
	
	private static final Integer RETRY_WAIT_MC = 20;
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		logger.info("Receive message subject : " + topic);
		String messageContent = new String(message.getPayload());
		logger.info("Receive message content : " + messageContent);

		CheckinReq checkinReq = null;
		String gateId = topic.replace(MqttConsts.BASE_TOPIC + "/gate/", "").replace("/request", "");

		try {
			checkinReq = new Gson().fromJson(messageContent, CheckinReq.class);
		} catch (Exception ex) {
			logger.warn(ex.getMessage());
			responseSmartGate(gateId, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0022));
			return;
		}

		try {
			checkinReq = new Gson().fromJson(messageContent, CheckinReq.class);
			
			if (vn.com.irtech.eport.common.utils.StringUtils.isEmpty(checkinReq.getSessionId())) {
				List<PickupHistoryDataRes> pickupHistoryDataRes = autoRecognizePickup(checkinReq.getInput());
				if (CollectionUtils.isNotEmpty(pickupHistoryDataRes)) {
					checkinReq.setData(pickupHistoryDataRes);
				} else {
					sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0026), checkinReq.getSessionId(), gateId);
					return;
				}
			}
			
			// Response to driver for waiting in progress
			sendMessageResult(BusinessConsts.IN_PROGRESS, null, MessageHelper.getMessage(MessageConsts.E0020), checkinReq.getSessionId(), null);
			
			// Compare contNo, truckNo, chassisNo, weight 
			if (validateDataWithInput(checkinReq, gateId)) {
				
				// Check if container is deliverable
				if (!checkGateOrderDoable(checkinReq)) {
					sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0021), checkinReq.getSessionId(), gateId);
				}
				
				// Get yard position or area, request to mc if not
				List<DriverDataRes> data = getDriverDataResponse(checkinReq);

				// Call and wait robot
				if (CollectionUtils.isNotEmpty(data)) {
					data.get(0).setWgt(checkinReq.getInput().get(0).getWeight());
					if (!sendGateInOrderToRobot(data, checkinReq.getSessionId(), gateId)) {
						sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0021), checkinReq.getSessionId(), gateId);
					}
				} else {
					sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0021), checkinReq.getSessionId(), gateId);
				}
			}
		} catch (Exception e) {
			sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0024), checkinReq.getSessionId(), gateId);
		}
	}

	/**
	 * Response checkin result to driver
	 * 
	 * @param driverRes
	 * @param sessionId
	 * @throws Exception
	 */
	private void responseDriver(DriverRes driverRes, String sessionId) throws Exception {
		String msg = new Gson().toJson(driverRes);
		mqttService.publish(MqttConsts.DRIVER_RES_TOPIC.replace("+", sessionId), new MqttMessage(msg.getBytes()));
	}

	/**
	 * Response checkin result to smart gate
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void responseSmartGate(String gateId, String result, String message) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("result", result);
		map.put("message", message);
		String msg = new Gson().toJson(map);
		mqttService.publish(MqttConsts.SMART_GATE_RES_TOPIC.replace("+", gateId), new MqttMessage(msg.getBytes()));
	}

	/**
	 * Request MC input position for container
	 * 
	 * @param pickupHistoryId
	 */
	private void requestMC(Long pickupHistoryId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("pickupHistoryId", pickupHistoryId.toString());
		String msg = new Gson().toJson(map);
		mqttService.sendMessageToMc(msg);
	}

	/**
	 * Get yard position from pickup
	 * @param pickupHistory
	 */
	private String getYardPostion(PickupHistory pickupHistory) {
		if (!StringUtils.isEmpty(pickupHistory.getArea())) {
			return pickupHistory.getArea();
		}
		
		String yardPosition = "";
		yardPosition += (pickupHistory.getBlock() != null)?pickupHistory.getBlock():StringUtils.EMPTY;
		yardPosition += "-" + ((pickupHistory.getBay() != null)?pickupHistory.getBay():StringUtils.EMPTY);
		yardPosition += "-" + ((pickupHistory.getLine() != null)?pickupHistory.getLine():StringUtils.EMPTY);
		yardPosition += "-" + ((pickupHistory.getTier() != null)?pickupHistory.getTier():StringUtils.EMPTY);
		return yardPosition;
	}
	
	/**
	 * get data response for driver
	 * @param checkinReq
	 * @return
	 * @throws Exception
	 */
	private List<DriverDataRes> getDriverDataResponse(CheckinReq checkinReq) throws Exception{
		List<DriverDataRes> result = new ArrayList<>();
		List<DriverDataRes> dataWithoutYardPostion = new ArrayList<>();
		
		for (PickupHistoryDataRes pickupHistoryDataRes : checkinReq.getData()) {
			PickupHistory pickupHistory = pickupHistoryService
					.selectPickupHistoryById(pickupHistoryDataRes.getPickupHistoryId());
			
			// pickup is not exist
			if (pickupHistory == null) {
				throw new BusinessException(MessageHelper.getMessage(MessageConsts.E0007));
			}
			
			// Get position for send container case
			if (pickupHistory.getShipment().getServiceType() == 1) {
				// TODO : Get position
				pickupHistory = catosApiService.getLocationForReceiveF(pickupHistory);
			}
			
			DriverDataRes driverDataRes = new DriverDataRes();
			driverDataRes.setPickupHistoryId(pickupHistory.getId());
			driverDataRes.setContNo(pickupHistory.getContainerNo());
			driverDataRes.setSztp(pickupHistory.getShipmentDetail().getSztp());
			driverDataRes.setFe(pickupHistory.getShipmentDetail().getFe());
			driverDataRes.setTruckNo(pickupHistory.getTruckNo());
			driverDataRes.setChassisNo(pickupHistory.getChassisNo());
			

			// pickup has not position
			if (!checkPickupHistoryHasPosition(pickupHistory)) {
				dataWithoutYardPostion.add(driverDataRes);
			}else {
				driverDataRes.setYardPosition(getYardPostion(pickupHistory));
			}
			
			result.add(driverDataRes);
		}
		
		if (!CollectionUtils.isEmpty(dataWithoutYardPostion)) {
			for (DriverDataRes data : dataWithoutYardPostion) {
				// Request MC input position
				requestMC(data.getPickupHistoryId());
			}
		} else {
			return result;
		}
		
		// wait MC
		for (int i = 1; i<= RETRY_WAIT_MC; i++) {
			logger.debug("Wait " + TIME_OUT_WAIT_MC  + " miliseconds");
			Thread.sleep(TIME_OUT_WAIT_MC);
			logger.debug("Check db");
			for (int j = 0; j < dataWithoutYardPostion.size(); j ++) {
				DriverDataRes data = dataWithoutYardPostion.get(j);
				PickupHistory pickupHistory = pickupHistoryService
						.selectPickupHistoryById(data.getPickupHistoryId());
				if (checkPickupHistoryHasPosition(pickupHistory)) {
					data.setYardPosition(getYardPostion(pickupHistory));
					dataWithoutYardPostion.remove(j);
				}
			}
			
			if (dataWithoutYardPostion.size() == 0) {
				break;
			}
		}
		
		if (dataWithoutYardPostion.size() > 0) {
			throw new BusinessException(MessageHelper.getMessage(MessageConsts.E0019));
		}
		
		return result;
	}
	
	/**
	 * Validate data input from gate
	 * 
	 * @param checkinReq
	 * @return Boolean
	 */
	private Boolean validateDataWithInput(CheckinReq checkinReq, String gateId) {
		int contValidate = checkinReq.getData().size();
		boolean validTruckNo = false;
		boolean validChasissNo = false;

		// TODO : Check weight
		for (PickupHistoryDataRes pickupHistoryDataRes : checkinReq.getData()) {
			for (MeasurementDataReq measurementDataReq : checkinReq.getInput()) {

				// Get Pickup History
				PickupHistory pickupHistory = pickupHistoryService.selectPickupHistoryById(pickupHistoryDataRes.getPickupHistoryId());
				if (pickupHistory.getShipmentDetailId() == null) {

					// Auto pickup
					ShipmentDetail shipmentDetail = shipmentDetailService.getContainerWithYardPosition(pickupHistory.getShipmentId());
					if (shipmentDetail == null) {
						sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0025), checkinReq.getSessionId(), gateId);
						return false;
					} else {
						contValidate--;
					}

				} else if (pickupHistoryDataRes.getContNo().equalsIgnoreCase(measurementDataReq.getContNo())) {
					contValidate--;
				}
				if (pickupHistoryDataRes.getTruckNo().equalsIgnoreCase(measurementDataReq.getTruckNo())) {
					validTruckNo = true;
				}
				if (pickupHistoryDataRes.getChassisNo().equalsIgnoreCase(measurementDataReq.getChassisNo())) {
					validChasissNo = true;
				}
			}
		}
		if (contValidate <= 0 && validTruckNo && validChasissNo) {
			return true;
		}
		sendMessageResult(BusinessConsts.FINISH, BusinessConsts.FAIL, MessageHelper.getMessage(MessageConsts.E0023), checkinReq.getSessionId(), gateId);
		return false;
	}
	
	/**
	 * Send gate in order to robot
	 * 
	 * @param driverDatareses
	 * @param sessionId
	 * @param gateId
	 * @return Boolean
	 */
	@Transactional
	private Boolean sendGateInOrderToRobot(List<DriverDataRes> driverDatareses, String sessionId, String gateId) {
		// Send gate in order to robot
		try {
			GateInFormData gateInFormData = new GateInFormData();
			List<PickupHistory> pickupIn = new ArrayList<>();
			List<PickupHistory> pickupOut = new ArrayList<>();
			PickupHistory pickupTemp = null;
			Long wgt = 0L;
			for (DriverDataRes driverDataRes : driverDatareses) {
				pickupTemp = pickupHistoryService.selectPickupHistoryById(driverDataRes.getPickupHistoryId());
				if (pickupTemp.getShipment().getServiceType()%2 == 1) {
					pickupOut.add(pickupTemp);
				} else {
					pickupIn.add(pickupTemp);
				}
				wgt += Long.parseLong(driverDataRes.getWgt());
			}
			if (pickupIn.size() == 0) {
				gateInFormData.setPickupOut(pickupOut);
				gateInFormData.setModule("OUT");
				gateInFormData.setContNumberOut(pickupOut.size());
			} else if (pickupOut.size() == 0) {
				gateInFormData.setPickupIn(pickupIn);
				gateInFormData.setModule("IN");
				gateInFormData.setContNumberIn(pickupIn.size());
			} else {
				gateInFormData.setPickupIn(pickupIn);
				gateInFormData.setPickupOut(pickupOut);
				gateInFormData.setModule("INOUT");
				gateInFormData.setContNumberIn(pickupIn.size());
				gateInFormData.setContNumberOut(pickupOut.size());
			}
			DriverDataRes driverData = driverDatareses.get(0);
			gateInFormData.setGatePass(pickupTemp.getGatePass());
			gateInFormData.setTruckNo(driverData.getTruckNo());
			gateInFormData.setChassisNo(driverData.getChassisNo());
			gateInFormData.setWgt(wgt.toString());
			gateInFormData.setSessionId(sessionId);
			gateInFormData.setGateId(gateId);
			
			
			ProcessOrder processOrder = new ProcessOrder();
			processOrder.setShipmentId(pickupTemp.getShipmentId());
			processOrder.setServiceType(8);
			processOrder.setLogisticGroupId(pickupTemp.getLogisticGroupId());
			processOrder.setStatus(1);
			processOrder.setRobotUuid(gateId);
			processOrderService.insertProcessOrder(processOrder);
			
			
			gateInFormData.setReceiptId(processOrder.getId());
			for (DriverDataRes driverDataRes : driverDatareses) {
				PickupHistory pickupHistory = pickupHistoryService.selectPickupHistoryById(driverDataRes.getPickupHistoryId());
				pickupHistory.setProcessOrderId(processOrder.getId());
				pickupHistoryService.updatePickupHistory(pickupHistory);
			}
			String msg = new Gson().toJson(gateInFormData);
			SysRobot robot = new SysRobot();
			robot.setStatus("0");
			robot.setIsGateInOrder(true);
			SysRobot sysRobot = robotService.findFirstRobot(robot);
			if (sysRobot != null) {
				robotService.updateRobotStatusByUuId(sysRobot.getUuId(), "1");
				mqttService.sendMessageToRobot(msg, sysRobot.getUuId());
			}
			
		} catch (Exception e) {
			logger.error("Error when send order gate in: " + e);
			return false;
		}
		return true;
	}
	
	/**
	 * Check pickup history has position
	 * 
	 * @param pickupHistory
	 * @return Boolean
	 */
	private Boolean checkPickupHistoryHasPosition(PickupHistory pickupHistory) {
		if (pickupHistory.getArea() != null) {
			return true;
		}
		if (pickupHistory.getBlock() != null && pickupHistory.getBay() != null
			&& pickupHistory.getLine() != null && pickupHistory.getTier() != null) {
				return true;
			}
		return false;
	}
	
	private Boolean checkGateOrderDoable(CheckinReq checkinReq) {
		// TODO : Validate on the things need to make a gate-in order
		return true;
	}
	
	/**
	 * Send message result for driver and smart app (Progress/Finish)
	 * 
	 * @param status
	 * @param result
	 * @param message
	 * @param sessionId
	 * @param gateId
	 */
	private void sendMessageResult(String status, String result, String message, String sessionId, String gateId) {
		if (sessionId != null) {
			DriverRes driverRes = new DriverRes();
			driverRes.setStatus(status);
			driverRes.setMsg(message);
			try {
				responseDriver(driverRes, sessionId);
			} catch (Exception e) {
				logger.error("Error send message to driver: " + e);
			}
		}
		if (gateId != null) {
			try {
				responseSmartGate(gateId, result, message);
			} catch (Exception e) {
				logger.error("Error send message to smart app: " + e);
			}
		}
	}
	
	/**
	 * Get pickup history when not scan qr code
	 * 
	 * @param measurementDataReqs
	 * @return List<PickupHistoryDataRes>
	 */
	private List<PickupHistoryDataRes> autoRecognizePickup(List<MeasurementDataReq> measurementDataReqs) {
		List<PickupHistoryDataRes> pickupHistoryDataReses = new ArrayList<>();
		for (MeasurementDataReq measurementDataReq : measurementDataReqs) {
			if (vn.com.irtech.eport.common.utils.StringUtils.isNotEmpty(measurementDataReq.getContNo())) {
				// Send container
				// Case driver has picked up
				PickupHistory pickupHistoryParam = new PickupHistory();
				pickupHistoryParam.setStatus(0);
				pickupHistoryParam.setTruckNo(measurementDataReq.getTruckNo());
				pickupHistoryParam.setChassisNo(measurementDataReq.getChassisNo());
				pickupHistoryParam.setContainerNo(measurementDataReq.getContNo());
				// Get pickup history by container no, truck no, chassis no
				List<PickupHistory> pickupHistories = pickupHistoryService.selectPickupHistoryList(pickupHistoryParam);
				if (CollectionUtils.isNotEmpty(pickupHistories)) {
					// Add pickup history info to list data return
					PickupHistory pickupHistory = pickupHistories.get(0);
					PickupHistoryDataRes pickupHistoryDataRes = new PickupHistoryDataRes();
					pickupHistoryDataRes.setChassisNo(pickupHistory.getChassisNo());
					pickupHistoryDataRes.setTruckNo(pickupHistory.getTruckNo());
					pickupHistoryDataRes.setServiceType(pickupHistory.getShipment().getServiceType());
					pickupHistoryDataRes.setContNo(pickupHistory.getContainerNo());
					if (pickupHistoryDataRes.getServiceType() == 1 || pickupHistoryDataRes.getServiceType() == 4) {
						pickupHistoryDataRes.setFe("F");
					} else {
						pickupHistoryDataRes.setFe("E");
					}
					pickupHistoryDataRes.setPickupHistoryId(pickupHistory.getId());
					pickupHistoryDataRes.setShipmentDetailId(pickupHistory.getShipmentDetailId());
					pickupHistoryDataRes.setShipmentId(pickupHistory.getShipmentId());
					pickupHistoryDataRes.setSztp(pickupHistory.getShipmentDetail().getSztp());
					pickupHistoryDataRes.setVessel(pickupHistory.getShipmentDetail().getVslNm());
					pickupHistoryDataRes.setVoyage(pickupHistory.getShipmentDetail().getVoyNo());
					pickupHistoryDataRes.setWeight(pickupHistory.getShipmentDetail().getWgt());
					pickupHistoryDataReses.add(pickupHistoryDataRes);
				} else {
					// TODO : Case driver has not picked up
					return null;
				}
				
			}
		}
		
		// Check receive container by shipment id
		PickupHistory pickupHistoryParam = new PickupHistory();
		pickupHistoryParam.setStatus(0);
		pickupHistoryParam.setTruckNo(measurementDataReqs.get(0).getTruckNo());
		pickupHistoryParam.setChassisNo(measurementDataReqs.get(0).getChassisNo());
		List<PickupHistory> pickupHistories = pickupHistoryService.selectPickupHistoryList(pickupHistoryParam);
		if (CollectionUtils.isNotEmpty(pickupHistories)) {
			for (PickupHistory pickupHistory : pickupHistories) {
				// Check to filter pickup history that not have container no yet
				if (pickupHistory.getShipmentDetailId() == null) {
					// Add pickup history info to list data return
					PickupHistoryDataRes pickupHistoryDataRes = new PickupHistoryDataRes();
					pickupHistoryDataRes.setChassisNo(pickupHistory.getChassisNo());
					pickupHistoryDataRes.setTruckNo(pickupHistory.getTruckNo());
					pickupHistoryDataRes.setServiceType(pickupHistory.getShipment().getServiceType());
					if (pickupHistoryDataRes.getServiceType() == 1) {
						pickupHistoryDataRes.setFe("F");
					} else {
						pickupHistoryDataRes.setFe("E");
					}
					pickupHistoryDataRes.setPickupHistoryId(pickupHistory.getId());
					pickupHistoryDataRes.setShipmentId(pickupHistory.getShipmentId());
					pickupHistoryDataRes.setVessel(pickupHistory.getShipmentDetail().getVslNm());
					pickupHistoryDataRes.setVoyage(pickupHistory.getShipmentDetail().getVoyNo());
					pickupHistoryDataRes.setWeight(pickupHistory.getShipmentDetail().getWgt());
					pickupHistoryDataReses.add(pickupHistoryDataRes);
				}
			}
		}
		return pickupHistoryDataReses;
	}
}
