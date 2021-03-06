package vn.com.irtech.eport.web.controller.om;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.com.irtech.eport.common.config.ServerConfig;
import vn.com.irtech.eport.common.constant.EportConstants;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.page.PageAble;
import vn.com.irtech.eport.common.utils.StringUtils;
import vn.com.irtech.eport.logistic.domain.LogisticGroup;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentComment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
import vn.com.irtech.eport.logistic.service.ILogisticGroupService;
import vn.com.irtech.eport.logistic.service.IShipmentCommentService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.logistic.service.IShipmentService;
import vn.com.irtech.eport.system.domain.SysUser;
import vn.com.irtech.eport.web.controller.AdminBaseController;
import vn.com.irtech.eport.web.mqtt.MqttService;

@Controller
@RequestMapping("/om/document")
public class DocumentGatheringController extends AdminBaseController  {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentGatheringController.class); 

	private String PREFIX = "om/document";
	
	@Autowired
	private IShipmentService shipmentService;
	
	@Autowired
	private ILogisticGroupService logisticGroupService;
	
	@Autowired
	private ICatosApiService catosApiService;
	
	@Autowired
	private IShipmentDetailService shipmentDetailService;
	
	@Autowired
	private IShipmentCommentService shipmentCommentService;
	
	@Autowired
	private MqttService mqttService;
	
	@Autowired
	private ServerConfig serverConfig;
	
	@GetMapping()
	public String getViewDocument(@RequestParam(required = false) Long sId, ModelMap mmap) {
		
		if (sId != null) {
			mmap.put("sId", sId);
		}
		mmap.put("domain", serverConfig.getUrl());
		// Get list logistic group
		LogisticGroup logisticGroup = new LogisticGroup();
	    logisticGroup.setGroupName("Chọn đơn vị Logistics");
	    logisticGroup.setId(0L);
	    LogisticGroup logisticGroupParam = new LogisticGroup();
	    logisticGroupParam.setDelFlag("0");
	    List<LogisticGroup> logisticGroups = logisticGroupService.selectLogisticGroupList(logisticGroupParam);
	    logisticGroups.add(0, logisticGroup);
	    mmap.put("logisticGroups", logisticGroups);
	    
	    // Get list vslNm : vslNmae : voyNo
	    List<ShipmentDetail> berthplanList = catosApiService.selectVesselVoyageBerthPlanWithoutOpe();
	    if(berthplanList == null) {
	    	berthplanList = new ArrayList<>();
	    }
	    ShipmentDetail shipmentDetail = new ShipmentDetail();
	    shipmentDetail.setVslAndVoy("Chọn tàu chuyến");
	    berthplanList.add(0, shipmentDetail);
	    mmap.put("vesselAndVoyages", berthplanList);
		return PREFIX + "/index";
	}
	
	@GetMapping("/confirmation")
	public String getConfirmationForm() {
		return PREFIX + "/confirmation";
	}
	
	@PostMapping("/shipments")
	@ResponseBody
	public AjaxResult getShipments(@RequestBody PageAble<Shipment> param) {
		startPage(param.getPageNum(), param.getPageSize(), param.getOrderBy());
		AjaxResult ajaxResult = AjaxResult.success();
		Shipment shipment = param.getData();
		if (shipment == null) {
			shipment = new Shipment();
		}
		shipment.setServiceType(EportConstants.SERVICE_PICKUP_FULL);
		shipment.setEdoFlg(EportConstants.DO_TYPE_CARRIER_DO);
		Map<String, Object> params = shipment.getParams();
		if (params == null) {
			params = new HashMap<>();
		}
		params.put("processStatus", "Y");
		shipment.setParams(params);
		List<Shipment> shipments = shipmentService.selectShipmentListByWithShipmentDetailFilter(shipment);
		ajaxResult.put("shipments", getDataTable(shipments));
		return ajaxResult;
	}
	
	@GetMapping("/shipment/{shipmentId}/shipmentDetails")
	@ResponseBody
	public AjaxResult getShipmentDetails(@PathVariable("shipmentId") Long shipmentId) {
		AjaxResult ajaxResult = AjaxResult.success();
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentId);
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
		ajaxResult.put("shipmentDetails", shipmentDetails);
		return ajaxResult;
	}
	
	@PostMapping("/confirmation")
	@ResponseBody
	@Transactional
	public AjaxResult submitConfirmation(String doStatus, String content, String shipmentDetailIds, Long logisticGroupId) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, logisticGroupId);
		for (ShipmentDetail shipmentDetail : shipmentDetails) {
			if (shipmentDetail.getPaymentStatus().equals("N")) {
				return error("Không thể xác nhận chứng từ gốc cho container chưa thanh toán. Vui lòng kiểm tra lại.");
			}
		}
		String containers = "";
		for (ShipmentDetail shipmentDetail : shipmentDetails) {
			containers += shipmentDetail.getContainerNo() + ",";
			shipmentDetail.setDoStatus(doStatus);
			shipmentDetail.setUpdateBy(getUser().getLoginName());
			shipmentDetailService.updateShipmentDetail(shipmentDetail);
		}
		ShipmentDetail shipmentDetail = shipmentDetails.get(0);
		Shipment shipment = shipmentService.selectShipmentById(shipmentDetail.getShipmentId());
		
		// Send release container request to robot
		containers = containers.substring(0, containers.length()-1);
		mqttService.sendReleaseTerminalHoldForRobot(containers, shipmentDetail, shipment.getServiceType());
		
		if (StringUtils.isNotEmpty(content)) {

			ShipmentComment shipmentComment = new ShipmentComment();
			shipmentComment.setLogisticGroupId(shipmentDetail.getLogisticGroupId());
			shipmentComment.setShipmentId(shipmentDetail.getShipmentId());
			shipmentComment.setUserId(getUserId());
			shipmentComment.setUserType(EportConstants.COMMENTOR_DNP_STAFF);
			shipmentComment.setUserAlias(getUser().getDept().getDeptName());
			shipmentComment.setCommentTime(new Date());
			shipmentComment.setContent(content);
			shipmentComment.setTopic(EportConstants.TOPIC_COMMENT_OM_DOCUMENT);
			shipmentComment.setServiceType(shipment.getServiceType());
			shipmentCommentService.insertShipmentComment(shipmentComment);
		}
 		return success("Thu chứng từ gốc thành công");
	}
	
	@PostMapping("/shipment/comment")
	@ResponseBody
	public AjaxResult addNewCommentToSend(@RequestBody ShipmentComment shipmentComment) {
		SysUser user = getUser();
		shipmentComment.setCreateBy(user.getUserName());
		shipmentComment.setUserId(user.getUserId());
		shipmentComment.setUserType(EportConstants.COMMENTOR_DNP_STAFF);
		shipmentComment.setUserAlias(user.getDept().getDeptName());
		shipmentComment.setUserName(user.getUserName());
		shipmentComment.setCommentTime(new Date());
		shipmentComment.setResolvedFlg(true);
		shipmentCommentService.insertShipmentComment(shipmentComment);
		
		// Add id to make background grey (different from other comment)
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentCommentId", shipmentComment.getId());
		return ajaxResult;
	}
}
