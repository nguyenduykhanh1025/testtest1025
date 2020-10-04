package vn.com.irtech.eport.logistic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.annotation.RepeatSubmit;
import vn.com.irtech.eport.common.config.Global;
import vn.com.irtech.eport.common.config.ServerConfig;
import vn.com.irtech.eport.common.constant.EportConstants;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.text.Convert;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.common.enums.OperatorType;
import vn.com.irtech.eport.common.exception.file.InvalidExtensionException;
import vn.com.irtech.eport.common.utils.DateUtils;
import vn.com.irtech.eport.common.utils.StringUtils;
import vn.com.irtech.eport.common.utils.file.FileUploadUtils;
import vn.com.irtech.eport.common.utils.file.MimeTypeUtils;
import vn.com.irtech.eport.framework.custom.queue.listener.CustomQueueService;
import vn.com.irtech.eport.framework.web.service.DictService;
import vn.com.irtech.eport.logistic.domain.LogisticAccount;
import vn.com.irtech.eport.logistic.domain.OtpCode;
import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentComment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.domain.ShipmentImage;
import vn.com.irtech.eport.logistic.dto.ServiceSendFullRobotReq;
import vn.com.irtech.eport.logistic.form.ContainerServiceForm;
import vn.com.irtech.eport.logistic.listener.MqttService;
import vn.com.irtech.eport.logistic.listener.MqttService.EServiceRobot;
import vn.com.irtech.eport.logistic.listener.MqttService.NotificationCode;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
import vn.com.irtech.eport.logistic.service.IOtpCodeService;
import vn.com.irtech.eport.logistic.service.IProcessBillService;
import vn.com.irtech.eport.logistic.service.IShipmentCommentService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.logistic.service.IShipmentImageService;
import vn.com.irtech.eport.logistic.service.IShipmentService;

@Controller
@RequestMapping("/logistic/send-cont-full")
public class LogisticSendContFullController extends LogisticBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(LogisticSendContFullController.class);
    
    private final String PREFIX = "logistic/sendContFull";
	
	@Autowired
	private IShipmentService shipmentService;

	@Autowired
	private IShipmentDetailService shipmentDetailService;

	@Autowired
	private IOtpCodeService otpCodeService;

	@Autowired
	private MqttService mqttService;
	
	@Autowired
	private IProcessBillService processBillService;
	
	@Autowired
	private CustomQueueService customQueueService;

	@Autowired
	private ICatosApiService catosApiService;
	
	@Autowired
    private IShipmentCommentService shipmentCommentService;
	
	@Autowired
	private IShipmentImageService shipmentImageService;
	
	@Autowired
	private ServerConfig serverConfig;
	
	@Autowired
	private DictService dictService;
	
    @GetMapping()
	public String sendContFull(@RequestParam(required = false) Long sId, ModelMap mmap) {
    	if (sId != null) {
			mmap.put("sId", sId);
		}
    	mmap.put("oprListBookingCheck", dictService.getListTag("opr_list_booking_check"));
		return PREFIX + "/index";
	}

	@GetMapping("/shipment/add")
	public String add(ModelMap mmap) {
		List<String> oprCodeList = catosApiService.getOprCodeList();
		oprCodeList.add(0, "Chọn OPR");
		
		mmap.put("taxCode", getGroup().getMst());
		mmap.put("oprCodeList", oprCodeList);
		mmap.put("oprListBookingCheck", dictService.getListTag("opr_list_booking_check"));
		return PREFIX + "/add";
	}

	@GetMapping("/shipment/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		Shipment shipment = shipmentService.selectShipmentById(id);
		if (verifyPermission(shipment.getLogisticGroupId())) {
			mmap.put("shipment", shipment);
			mmap.put("taxCode", getGroup().getMst());
		}
		List<String> oprCodeList = catosApiService.getOprCodeList();
		oprCodeList.add(0, "Chọn OPR");
		
		ShipmentImage shipmentImage = new ShipmentImage();
		shipmentImage.setShipmentId(id);
		List<ShipmentImage> shipmentImages = shipmentImageService.selectShipmentImageList(shipmentImage);
		mmap.put("shipmentFiles", shipmentImages);
		mmap.put("oprListBookingCheck", dictService.getListTag("opr_list_booking_check"));
		mmap.put("oprCodeList", oprCodeList);
        return PREFIX + "/edit";
	}

	@GetMapping("/otp/cont-list/confirmation/{shipmentDetailIds}")
	public String checkContListBeforeVerify(@PathVariable("shipmentDetailIds") String shipmentDetailIds, ModelMap mmap) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());
		mmap.put("creditFlag", getGroup().getCreditFlag());
		mmap.put("taxCode", getGroup().getMst());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			mmap.put("shipmentDetails", shipmentDetails);
		}
		return PREFIX + "/checkContListBeforeVerify";
	}

	@GetMapping("/otp/verification/{shipmentDetailIds}/{creditFlag}/{taxCode}/{shipmentId}")
	public String verifyOtpForm(@PathVariable("shipmentDetailIds") String shipmentDetailIds, @PathVariable("shipmentId") Long shipmentId,
			@PathVariable("creditFlag") boolean creditFlag, @PathVariable("taxCode") String taxCode, ModelMap mmap) {
		mmap.put("shipmentDetailIds", shipmentDetailIds);
		mmap.put("numberPhone", getUser().getMobile());
		mmap.put("shipmentId", "-");
		mmap.put("creditFlag", creditFlag);
		mmap.put("taxCode", taxCode);
		mmap.put("shipmentId", shipmentId);
		return PREFIX + "/verifyOtp";
	}

	@GetMapping("/payment/{processOrderIds}")
	public String paymentForm(@PathVariable("processOrderIds") String processOrderIds, ModelMap mmap) {
		String shipmentDetailIds = "";
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByProcessIds(processOrderIds);
		for (ShipmentDetail shipmentDetail : shipmentDetails) {
			shipmentDetailIds += shipmentDetail.getId() + ",";
		}
		if (!"".equalsIgnoreCase(shipmentDetailIds)) {
			shipmentDetailIds.substring(0, shipmentDetailIds.length()-1);
		}
		mmap.put("shipmentDetailIds", shipmentDetailIds);
		mmap.put("processBills", processBillService.selectProcessBillListByProcessOrderIds(processOrderIds));
		return PREFIX + "/paymentForm";
	}

	@GetMapping("/custom-status/{shipmentDetailIds}")
	public String checkCustomStatus(@PathVariable String shipmentDetailIds, ModelMap mmap) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			if (verifyPermission(shipmentDetails.get(0).getLogisticGroupId())) {
				mmap.put("shipmentId", shipmentDetails.get(0).getShipmentId());
				mmap.put("contList", shipmentDetails);
			}
		}
		return PREFIX + "/checkCustomStatus";
	}
	
	@GetMapping("/shipment-detail/{shipmentDetailId}/cont/{containerNo}/sztp/{sztp}/detail")
	public String getShipmentDetailInputForm(@PathVariable("shipmentDetailId") Long shipmentDetailId, @PathVariable("containerNo") String containerNo, @PathVariable("sztp") String sztp, ModelMap mmap) {
		mmap.put("containerNo", containerNo);
		mmap.put("sztp", sztp);
		mmap.put("shipmentDetailId", shipmentDetailId);
		return PREFIX + "/detail";
	}

	@Log(title = "Tạo Lô Hạ Hàng", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment")
	@Transactional
    @ResponseBody
    public AjaxResult addShipment(@RequestBody Shipment shipment) {
		LogisticAccount user = getUser();
		if (StringUtils.isNotEmpty(shipment.getBookingNo())) {
			shipment.setBookingNo(shipment.getBookingNo().toUpperCase());
		}
		shipment.setLogisticAccountId(user.getId());
		shipment.setLogisticGroupId(user.getGroupId());
		shipment.setCreateBy(user.getFullName());
		shipment.setServiceType(EportConstants.SERVICE_DROP_FULL);
		shipment.setStatus(EportConstants.SHIPMENT_STATUS_INIT);
		
		boolean attachBooking = false;
		// List opr need to attach booking (domestic container)
		List<String> oprList = dictService.getListTag("opr_list_booking_check");
		if (oprList.contains(shipment.getOpeCode())) {
			attachBooking = true;
		}
		
		if (shipmentService.insertShipment(shipment) == 1) {
			if (attachBooking) {
				ShipmentImage shipmentImage = new ShipmentImage();
	        	shipmentImage.setShipmentId(shipment.getId());
	        	Map<String, Object> map = new HashMap<>();
	        	map.put("ids", Convert.toStrArray(shipment.getParams().get("ids").toString()));
	        	shipmentImage.setParams(map);
	            shipmentImageService.updateShipmentImageByIds(shipmentImage);
			}
			return success("Thêm lô thành công");
		}
		return error("Có lỗi khi thực hiện thêm lô, vui lòng thử lại");
	}

	@PostMapping("/unique/booking-no")
	@ResponseBody
	public AjaxResult checkBookingNoUnique(@RequestBody ContainerServiceForm inputForm) {
		String bookingNo = inputForm.getBookingNo();
		if(StringUtils.isAllBlank(bookingNo)) {
			return error("Hãy nhập số Booking");
		}
		Shipment shipment = new Shipment();
		shipment.setLogisticGroupId(getUser().getGroupId());
		shipment.setBookingNo(bookingNo);
		shipment.setServiceType(EportConstants.SERVICE_DROP_FULL);
//		if(catosApiService.checkBookingNoForSendFReceiveE(bookingNo, "F").intValue() == 0) {
//			return error("Booking No này chưa có trong hệ thống. Vui lòng liên hệ OM để tạo !");
//		}
		if (shipmentService.checkBillBookingNoUnique(shipment) == 0) {
			return success();
		}
		return error(String.format("Số Booking '%s' đã tồn tại!", bookingNo));
	}
	
	@PostMapping("/shipment/{shipmentId}")
    @ResponseBody
    public AjaxResult editShipment(@RequestBody Shipment input, @PathVariable Long shipmentId) {
		LogisticAccount user = getUser();
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (verifyPermission(shipment.getLogisticGroupId())) {
			// Chi update cac item cho phep
			// Kiem tra co update so luong co giam -> kiem tra xem so luong details 
			if(input.getContainerAmount() < shipment.getContainerAmount()) {
				ShipmentDetail shipmentSearch = new ShipmentDetail();
				shipmentSearch.setShipmentId(shipmentId);
				long contNumber = shipmentDetailService.countShipmentDetailList(shipmentSearch);
				if(contNumber > input.getContainerAmount()) {
					return error("Không thể chỉnh sửa số lượng container nhỏ hơn danh sách khai báo.");
				}
			}
			shipment.setContainerAmount(input.getContainerAmount());
			shipment.setRemark(input.getRemark());
			shipment.setUpdateBy(user.getFullName());
			// Update OPR, Booking when status is 1 or 2
			if(shipment.getStatus().equals(EportConstants.SHIPMENT_STATUS_INIT)) {
				shipment.setOpeCode(input.getOpeCode());
				if (StringUtils.isNotEmpty(input.getBookingNo())) {
					shipment.setBookingNo(input.getBookingNo().toUpperCase());
				}
			} else if (shipment.getStatus().equals(EportConstants.SHIPMENT_STATUS_SAVE)) {
				shipment.setOpeCode(input.getOpeCode());
			}
			
			boolean attachBooking = false;
			// List opr need to attach booking (domestic container)
			List<String> oprList = dictService.getListTag("opr_list_booking_check");
			if (oprList.contains(shipment.getOpeCode())) {
				attachBooking = true;
			}
			
			if (shipmentService.updateShipment(shipment) == 1) {
				ShipmentImage shipmentImage = new ShipmentImage();
	        	shipmentImage.setShipmentId(shipment.getId());
	        	Map<String, Object> map = new HashMap<>();
	        	map.put("ids", Convert.toStrArray(input.getParams().get("ids").toString()));
	        	shipmentImage.setParams(map);
	            shipmentImageService.updateShipmentImageByIds(shipmentImage);
				return success("Chỉnh sửa lô thành công");
			}
		}
		return error("Chỉnh sửa lô thất bại");
	}

	@GetMapping("/shipment/{shipmentId}/shipment-detail")
	@ResponseBody
	public AjaxResult listShipmentDetail(@PathVariable Long shipmentId) {
		AjaxResult ajaxResult = AjaxResult.success();
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (verifyPermission(shipment.getLogisticGroupId())) {
			ShipmentDetail shipmentDetail = new ShipmentDetail();
			shipmentDetail.setShipmentId(shipmentId);
			List<ShipmentDetail> shipmentDetails = shipmentDetailService.getShipmentDetailListForSendFReceiveE(shipmentDetail);
			if (shipmentDetails != null) {
				ajaxResult.put("shipmentDetails", shipmentDetails);
			} else {
				ajaxResult = AjaxResult.error("Không tìm thấy thông tin lô hàng, vui lòng kiểm tra lại.");
			}
		} else {
			ajaxResult = AjaxResult.error("Lô không tồn tại, vui lòng kiểm tra lại.");
		}
		
		return ajaxResult;
	}

	@Log(title = "Khai Báo Cont", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/{shipmentId}/shipment-detail")
	@ResponseBody
	public AjaxResult saveShipmentDetail(@RequestBody List<ShipmentDetail> shipmentDetails, @PathVariable("shipmentId") Long shipmentId) {
		// Verify shipment
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		boolean attachBooking = false;
		// List opr need to attach booking (domestic container)
		List<String> oprList = dictService.getListTag("opr_list_booking_check");
		if (oprList.contains(shipment.getOpeCode())) {
			attachBooking = true;
		}
		if (!verifyPermission(shipment.getLogisticGroupId())) {
			return AjaxResult.error("Không tìm thấy lô, xin vui lòng kiểm tra lại");
		}
		// Check if list have one or more items
		if (shipmentDetails != null && shipmentDetails.size() > 0) {
			LogisticAccount user = getUser();
			boolean updateShipment = true;
			String containerNos = "";
			for(ShipmentDetail sd : shipmentDetails) {
				if(sd.getId() == null) {
					containerNos += sd.getContainerNo() + ",";
				}
			}
			// Lay danh sach container da lam lenh booking
			//TODO tam thoi remove logic check cont da ha hang tren catos
//			if(!"".equals(containerNos)) {
//				List<String> contReservedList = shipmentDetailService.checkContainerReserved(containerNos.substring(0, containerNos.length() - 1));
//				if (contReservedList.size() > 0) {
//					// Thong bao loi khong the khai bao cho cac conts nay
//					AjaxResult ajaxResult = AjaxResult.error();
//					ajaxResult.put("conts", contReservedList);
//					return ajaxResult;
//				}
//			}
			// Danh sachs shipment details co 2 truong hop: co container da tao tu truoc (id != null) va container vua tao (id ==null)
			for (ShipmentDetail shipmentDetail : shipmentDetails) {
				// ? Vi sao update process status = null ??
				shipmentDetail.setProcessStatus(null);
				// Giu lai cac thong tin khong the overwrite kih update
				shipmentDetail.setShipmentId(shipmentId);
				shipmentDetail.setLogisticGroupId(user.getGroupId());
				shipmentDetail.setServiceType(EportConstants.SERVICE_DROP_FULL);
				// T/H container da ton tai
				if (shipmentDetail.getId() != null) {
					updateShipment = false;
					shipmentDetail.setUpdateBy(user.getFullName());
					if (shipmentDetailService.updateShipmentDetail(shipmentDetail) != 1) {
						return error("Lưu khai báo thất bại từ container: " + shipmentDetail.getContainerNo());
					}
				} else {
					shipmentDetail.setCreateBy(user.getFullName());
					shipmentDetail.setStatus(1);
					shipmentDetail.setPaymentStatus("N");
					shipmentDetail.setUserVerifyStatus("N");
					shipmentDetail.setProcessStatus("N");
					shipmentDetail.setCustomStatus("N");
					shipmentDetail.setFinishStatus("N");
					shipmentDetail.setFe("F");
					if (attachBooking) {
						shipmentDetail.setDoStatus("N");
					}
					if (shipmentDetailService.insertShipmentDetail(shipmentDetail) != 1) {
						return error("Lưu khai báo thất bại từ container: " + shipmentDetail.getContainerNo());
					}
				}
			}
			if (updateShipment) {
				shipment.setUpdateBy(getUser().getFullName());
				shipment.setStatus(EportConstants.SHIPMENT_STATUS_SAVE);
				shipmentService.updateShipment(shipment);
			}
			return success("Lưu khai báo thành công");
		}
		return error("Lưu khai báo thất bại");
	}

	@Log(title = "Xóa Khai Báo Cont", businessType = BusinessType.DELETE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/{shipmentId}/shipment-detail/delete")
	@Transactional
	@ResponseBody
	public AjaxResult deleteShipmentDetail(@PathVariable("shipmentId") Long shipmentId, @RequestBody ContainerServiceForm inputForm) {
		// check shipment permission
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (!verifyPermission(shipment.getLogisticGroupId())) {
			return AjaxResult.error("Không tìm thấy lô, xin vui lòng kiểm tra lại");
		}
		String shipmentDetailIds = inputForm.getIds();
		// delete shipment details
		if (shipmentDetailIds != null) {
			// just delete shipmentIds for shipment has been verified before
			shipmentDetailService.deleteShipmentDetailByIds(shipmentId, shipmentDetailIds);
			ShipmentDetail shipmentDetail = new ShipmentDetail();
			shipmentDetail.setShipmentId(shipmentId);
			// Reset status shipment to INIT if deleted all shipment details
			if (shipmentDetailService.countShipmentDetailList(shipmentDetail) == 0) {
				shipment.setStatus(EportConstants.SHIPMENT_STATUS_INIT);
				shipmentService.updateShipment(shipment);
			}
			return success("Xóa khai báo thành công");
		}
		return error("Xóa khai báo thất bại");
	}

	@Log(title = "Xác Nhận OTP", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/otp/{otp}/verification")
	@ResponseBody
	@RepeatSubmit
	public AjaxResult verifyOtp(@PathVariable String otp, String shipmentDetailIds, String taxCode, boolean creditFlag) {
		try {
			Long.parseLong(otp);
		} catch (Exception e) {
			return error("Mã OTP nhập vào không hợp lệ!");
		}
		OtpCode otpCode = new OtpCode();
		otpCode.setTransactionId(shipmentDetailIds);
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.MINUTE, -5);
		otpCode.setCreateTime(cal.getTime());
		otpCode.setOtpCode(otp);
		if (otpCodeService.verifyOtpCodeAvailable(otpCode) != 1) {
			return error("Mã OTP không chính xác hoặc đã hết hạn, xin vui lòng kiểm tra lại.");
		}
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());
		AjaxResult validateResult = validateShipmentDetailList(shipmentDetails);
		Integer code = (Integer)validateResult.get("code");
		if (code != 0) {
			return validateResult;
		}
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			AjaxResult ajaxResult = null;
			Shipment shipment = shipmentService.selectShipmentById(shipmentDetails.get(0).getShipmentId());
			if (!EportConstants.SHIPMENT_STATUS_PROCESSING.equals(shipment.getStatus())) {
				shipment.setStatus(EportConstants.SHIPMENT_STATUS_PROCESSING);
				shipment.setUpdateBy(getUser().getFullName());
				shipmentService.updateShipment(shipment);
			}
			ProcessOrder processOrder = shipmentDetailService.makeOrderSendCont(shipmentDetails, shipment, taxCode, creditFlag);
			if (processOrder != null) {
				ServiceSendFullRobotReq serviceRobotReq = new ServiceSendFullRobotReq(processOrder, shipmentDetails);
				try {
					if (!mqttService.publishMessageToRobot(serviceRobotReq, EServiceRobot.SEND_CONT_FULL)) {
						ajaxResult = AjaxResult.warn("Yêu cầu đang được xử lý, xin vui lòng đợi trong giây lát.");
						ajaxResult.put("processId", processOrder.getId());
						return ajaxResult;
					}
				} catch (Exception e) {
					logger.warn(e.getMessage());
					return error("Có lỗi xảy ra trong quá trình xác thực!");
				}
				
				ajaxResult =  AjaxResult.success("Yêu cầu đang được xử lý, xin vui lòng đợi trong giây lát.");
				ajaxResult.put("processId", processOrder.getId());
				return ajaxResult;
			}
		}
		return error("Có lỗi xảy ra trong quá trình xác thực!");
	}

	@Log(title = "Thanh Toán Hạ Hàng", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/payment/{shipmentDetailIds}")
	@ResponseBody
	public AjaxResult payment(@PathVariable String shipmentDetailIds) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			for (ShipmentDetail shipmentDetail : shipmentDetails) {
				shipmentDetail.setStatus(3); // TODO Chuyen thanh dung EportConstants
				shipmentDetail.setPaymentStatus("Y");
				if ("VN".equals(shipmentDetail.getDischargePort().substring(0,2))) {
					shipmentDetail.setStatus(4);
					shipmentDetail.setCustomStatus("Y");
				}
				shipmentDetailService.updateShipmentDetail(shipmentDetail);
			}
			return success("Thanh toán thành công");
		}
		return error("Có lỗi xảy ra trong quá trình thanh toán.");
	}

	@Log(title = "Khai Báo Hải Quan", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/custom-status/shipment-detail")
	@ResponseBody
	public AjaxResult checkCustomStatus(@RequestParam(value = "declareNos") String declareNoList, @RequestParam(value = "shipmentDetailIds") String shipmentDetailIds) {
		// FIXME chuyen params thanh POST REQUEST BODY
		if (StringUtils.isNotEmpty(declareNoList)) {
			List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());
			if (CollectionUtils.isNotEmpty(shipmentDetails)) {
				for (ShipmentDetail shipmentDetail : shipmentDetails) {
					// Save declare no list to shipment detail
					shipmentDetail.setCustomsNo(declareNoList);
					shipmentDetail.setCustomScanTime(new Date());
					shipmentDetailService.updateShipmentDetail(shipmentDetail);
					customQueueService.offerShipmentDetail(shipmentDetail);
				}
				return success();
			}
		}
		return error();
	}
	
	@GetMapping("/berthplan/ope-code/list")
	@ResponseBody
	public AjaxResult getOpeCodeList() {
		AjaxResult ajaxResult = success();
		List<String> opeCodeList = catosApiService.selectOpeCodeListInBerthPlan();
		if(opeCodeList.size() > 0 ) {
			ajaxResult.put("opeCodeList", opeCodeList);
			return ajaxResult;
		}
		return error();
		
	}
	
	@GetMapping("/berthplan/ope-code/{opeCode}/vessel-voyage/list")
	@ResponseBody
	public AjaxResult getVesselVoyageList(@PathVariable String opeCode) {
		AjaxResult ajaxResult = success();
		List<ShipmentDetail> berthplanList = catosApiService.selectVesselVoyageBerthPlan(opeCode);
		if(berthplanList.size() > 0) {
			List<String> vesselAndVoyages = new ArrayList<String>();
			for(ShipmentDetail i : berthplanList) {
				vesselAndVoyages.add(i.getVslAndVoy());
			}
			ajaxResult.put("berthplanList", berthplanList);
			ajaxResult.put("vesselAndVoyages", vesselAndVoyages);
			return ajaxResult;
		}
		return error();
	}
	
	@GetMapping("/berthplan/vessel-voyage/list")
	@ResponseBody
	public AjaxResult getVesselVoyageListWithoutOpeCode() {
		AjaxResult ajaxResult = success();
		List<ShipmentDetail> berthplanList = catosApiService.selectVesselVoyageBerthPlanWithoutOpe();
		if(berthplanList != null && berthplanList.size() > 0) {
			List<String> vesselAndVoyages = new ArrayList<String>();
			for(ShipmentDetail i : berthplanList) {
				vesselAndVoyages.add(i.getVslAndVoy());
			}
			ajaxResult.put("berthplanList", berthplanList);
			ajaxResult.put("vesselAndVoyages", vesselAndVoyages);
			return ajaxResult;
		}
		return error();
	}
	
	@GetMapping("/containerNo/{containerNo}/sztp")
	@ResponseBody
	public AjaxResult getSztpByContainerNo(@PathVariable("containerNo") String containerNo) {
		String sztp = catosApiService.getSztpByContainerNo(containerNo);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("sztp", sztp);
		return ajaxResult;
	}
	
	@PostMapping("/shipment/comment")
	@ResponseBody
	public AjaxResult addNewCommentToSend(@RequestBody ShipmentComment shipmentComment) {
		LogisticAccount user = getUser();
		shipmentComment.setCreateBy(user.getUserName());
		shipmentComment.setLogisticGroupId(user.getGroupId());
		shipmentComment.setUserId(getUserId());
		shipmentComment.setUserType(EportConstants.COMMENTOR_LOGISTIC);
		shipmentComment.setUserAlias(getGroup().getGroupName());
		shipmentComment.setUserName(user.getUserName());
		shipmentComment.setServiceType(EportConstants.SERVICE_DROP_FULL);
		shipmentComment.setCommentTime(new Date());
		shipmentComment.setSeenFlg(true);
		shipmentCommentService.insertShipmentComment(shipmentComment);
		
		// Send notification to om
		try {
			mqttService.sendNotificationApp(NotificationCode.NOTIFICATION_OM, shipmentComment.getTopic(), shipmentComment.getContent(), "", EportConstants.NOTIFICATION_PRIORITY_MEDIUM);
		} catch (MqttException e) {
			logger.error("Fail to send message om notification app: " + e);
		}
		
		// Add id to make background grey (different from other comment)
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentCommentId", shipmentComment.getId());
		return ajaxResult;
	}
	
	@PostMapping("/shipment-detail/validation")
	@ResponseBody
	public AjaxResult validateShipmentDetail(String shipmentDetailIds) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds, getUser().getGroupId());	
		AjaxResult validateResult = validateShipmentDetailList(shipmentDetails);
		return validateResult;
	}
	
	public AjaxResult validateShipmentDetailList(List<ShipmentDetail> shipmentDetails) {
		if (CollectionUtils.isEmpty(shipmentDetails)) {
			return error("Không tìm thấy thông tin chi tiết lô đã chọn.");
		}
		
		// validate
		ShipmentDetail shipmentDetailReference = shipmentDetails.get(0);
		for (int i=0; i<shipmentDetails.size(); i++) {
			if (StringUtils.isEmpty(shipmentDetails.get(i).getContainerNo())) {
				return error("Hàng " + (i + 1) + ": Quý khách chưa nhập số container!");
			}
			if (StringUtils.isEmpty(shipmentDetails.get(i).getConsignee())) {
                return error("Hàng " + (i + 1) + ": Quý khách chưa chọn chủ hàng!");
            } 
			if (StringUtils.isEmpty(shipmentDetails.get(i).getVslNm())) {
                return error("Hàng " + (i + 1) + ": Quý khách chưa chọn tàu!");
            } 
			if (StringUtils.isEmpty(shipmentDetails.get(i).getSztp())) {
                return error("Hàng " + (i + 1) + ": Quý khách chưa chọn kích thước!");
            } 
			if (shipmentDetails.get(i).getWgt() == null) {
                return error("Hàng " + (i + 1) + ": Quý khách chưa nhập trọng lượng!");
            } 
			if (shipmentDetails.get(i).getWgt() > 99999) {
                return error("Hàng " + (i + 1) + ": Trọng lượng không được quá 5 chữ số!");
            } 
			if (StringUtils.isEmpty(shipmentDetails.get(i).getDischargePort())) {
				return error("Hàng " + (i + 1) + ": Quý khách chưa chọn cảng dỡ hàng!");
              
            } 
			if (!shipmentDetailReference.getConsignee().equals(shipmentDetails.get(i).getConsignee())) {
                return error("Tên chủ hàng không được khác nhau!");
                
            } 
			if (!shipmentDetailReference.getVslNm().equals(shipmentDetails.get(i).getVslNm())) {
                return error("Tàu và Chuyến không được khác nhau!");
                
            } 
			if (!shipmentDetailReference.getVoyNo().equals(shipmentDetails.get(i).getVoyNo())) {
                return error("Tàu và Chuyến không được khác nhau!");
                
            } 
			if (!shipmentDetailReference.getDischargePort().equals(shipmentDetails.get(i).getDischargePort())) {
                return error("Cảng dỡ hàng không được khác nhau!");
                
            } 
		}
		
		// validate consignee exist in catos
		if (catosApiService.checkConsigneeExistInCatos(shipmentDetailReference.getConsignee()) == 0) {
			return error("Tên chủ hàng quý khách nhập không đúng, vui lòng chọn tên chủ hàng từ trong danh sách của hệ thống gợi ý.");
		}
		
		// validate pod exist in catos
		if (catosApiService.checkPodExistIncatos(shipmentDetailReference.getDischargePort()) == 0) {
			return error("Cảng dỡ hàng quý khách nhập không đúng, vui lòng chọn cảng từ trong dánh sách của hệ thống gợi ý.");
		}
		return success();
	}
	
	@PostMapping("/file")
	@ResponseBody
	public AjaxResult saveFile(MultipartFile file) throws IOException, InvalidExtensionException {
		String basePath = String.format("%s/%s", Global.getUploadPath() + "/booking", getUser().getGroupId());
		String now = DateUtils.dateTimeNow();
		String fileName = String.format("file%s.%s", now, FileUploadUtils.getExtension(file));
        String filePath = FileUploadUtils.upload(basePath, fileName, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        
        ShipmentImage shipmentImage = new ShipmentImage();
        shipmentImage.setPath(filePath);
        shipmentImage.setCreateTime(DateUtils.getNowDate());
        shipmentImage.setCreateBy(getUser().getFullName());
        shipmentImageService.insertShipmentImage(shipmentImage);
        
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("shipmentFileId", shipmentImage.getId());
        return ajaxResult;
	}
	
	@DeleteMapping("/booking/file")
	@ResponseBody
	public AjaxResult deleteFile(Long id) throws IOException {
		ShipmentImage shipmentImageParam = new ShipmentImage();
		shipmentImageParam.setId(id);
		ShipmentImage shipmentImage = shipmentImageService.selectShipmentImageById(shipmentImageParam);
		String[] fileArr = shipmentImage.getPath().split("/");
		File file = new File(Global.getUploadPath() + "/booking/" + getUser().getGroupId() + "/" + fileArr[fileArr.length-1]);
		if (file.delete()) {
			shipmentImageService.deleteShipmentImageById(id);
		}
		return success();
	}
	
	@GetMapping("/shipments/{shipmentId}/shipment-images")
	@ResponseBody
	public AjaxResult getShipmentImages(@PathVariable("shipmentId") Long shipmentId) {
		AjaxResult ajaxResult = AjaxResult.success();
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (verifyPermission(shipment.getLogisticGroupId())) {
			ShipmentImage shipmentImage = new ShipmentImage();
			shipmentImage.setShipmentId(shipmentId);
			List<ShipmentImage> shipmentImages = shipmentImageService.selectShipmentImageList(shipmentImage);
			for (ShipmentImage shipmentImage2 : shipmentImages) {
				shipmentImage2.setPath(serverConfig.getUrl() + shipmentImage2.getPath());
			}
			ajaxResult.put("shipmentFiles", shipmentImages);
			return ajaxResult;
		}
		return error();
	}
}