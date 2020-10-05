package vn.com.irtech.eport.web.controller.om;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.config.ServerConfig;
import vn.com.irtech.eport.common.constant.Constants;
import vn.com.irtech.eport.common.constant.EportConstants;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.page.PageAble;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.common.enums.OperatorType;
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

@Controller
@RequestMapping("/om/support/custom-receive-full")
public class SupportCustomReiceiveFullController  extends OmBaseController{
	protected final Logger logger = LoggerFactory.getLogger(SupportCustomReiceiveFullController.class);
    private final String PREFIX = "om/support/customReceiveFull"; 
    
    @Autowired
    private IShipmentDetailService shipmentDetailService;
    
    @Autowired
    private ILogisticGroupService logisticGroupService;
    
    @Autowired
    private IShipmentService shipmentService;
    
    @Autowired 
    private IShipmentCommentService shipmentCommentService;
    
    @Autowired
    private ICatosApiService catosApiService;
    
    @Autowired
    private ServerConfig serverConfig;
    
    @GetMapping("/view")
    public String getViewSupportReceiveFull(@RequestParam(required = false) Long sId, ModelMap mmap)
    {
    	if (sId != null) {
			mmap.put("sId", sId);
		}
    	mmap.put("domain", serverConfig.getUrl());
    	// Get list logistic group
		LogisticGroup logisticGroup = new LogisticGroup();
	    logisticGroup.setGroupName("Tất cả khách hàng");
	    logisticGroup.setId(null);
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
        return PREFIX + "/customReceiveFull";
    }
    
    @PostMapping("/shipments")
	@ResponseBody
	public AjaxResult getListOrder(@RequestBody PageAble<Shipment> param) {
        startPage(param.getPageNum(), param.getPageSize(), param.getOrderBy());
        Shipment shipment = param.getData();
        if (shipment == null) {
        	shipment = new Shipment();
        }
        AjaxResult ajaxResult = AjaxResult.success();
        shipment.setServiceType(Constants.RECEIVE_CONT_FULL);
		List<Shipment> shipments = shipmentService.getShipmentsForSupportCustom(shipment);
		ajaxResult.put("shipments", getDataTable(shipments));
		return ajaxResult;
    }
    
    @GetMapping("/shipmentId/{shipmentId}/shipmentDetails")
	@ResponseBody
	public AjaxResult getshipmentDetails(@PathVariable Long shipmentId) {
    	AjaxResult ajaxResult = success();
    	ShipmentDetail shipmentDetail = new ShipmentDetail();
    	shipmentDetail.setShipmentId(shipmentId);
        List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
        if (shipmentDetails.size() > 0) {
        	ajaxResult.put("shipmentDetails", shipmentDetails);
        	return ajaxResult;
        }
		return error();
    }
    @GetMapping("/confirm-result-notification/shipmentId/{shipmentId}")
    public String confirmResultNotification(@PathVariable("shipmentId") Long shipmentId, ModelMap mmap) {
    	mmap.put("shipmentId", shipmentId);
    	return PREFIX + "/confirmResultNotification";
    }
    @Log(title = "Hỗ trợ Hải Quan", businessType = BusinessType.INSERT, operatorType = OperatorType.MANAGE)
    @PostMapping("/confirm-result-notification")
    @ResponseBody
    public AjaxResult sendNotification(@RequestBody ShipmentComment shipmentComment) {
    	Shipment shipment = shipmentService.selectShipmentById(shipmentComment.getShipmentId());
    	if(shipment == null) {
    		return error();
    	}
    	shipmentDetailService.resetCustomStatus(shipment.getId());
    	if(shipmentComment == null || shipmentComment.getContent() == null ||
    			shipmentComment.getContent() == "") {
	    	shipmentComment.setLogisticGroupId(shipment.getLogisticGroupId());
	    	shipmentComment.setUserId(getUserId());
	    	shipmentComment.setUserType("S");// S: DNP Staff
	    	shipmentComment.setUserName(getUser().getUserName());
	    	shipmentComment.setUserAlias(getUser().getUserName());//TODO get tạm username
	    	shipmentComment.setCommentTime(new Date());
	    	shipmentComment.setCreateTime(new Date());
	    	shipmentComment.setCreateBy(getUser().getUserName());
	    	shipmentComment.setTopic(Constants.RECEIVE_CONT_FULL_CUSTOM_SUPPORT);
			shipmentComment.setServiceType(shipment.getServiceType());
	    	shipmentCommentService.insertShipmentComment(shipmentComment);
    	}
    	return success();
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
		shipmentComment.setServiceType(EportConstants.SERVICE_PICKUP_FULL);
		shipmentComment.setCommentTime(new Date());
		shipmentComment.setResolvedFlg(true);
		shipmentCommentService.insertShipmentComment(shipmentComment);
		
		// Add id to make background grey (different from other comment)
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentCommentId", shipmentComment.getId());
		return ajaxResult;
	}
}
