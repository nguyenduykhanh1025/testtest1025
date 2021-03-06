package vn.com.irtech.eport.logistic.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.config.Global;
import vn.com.irtech.eport.common.config.ServerConfig;
import vn.com.irtech.eport.common.constant.EportConstants;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.page.PageAble;
import vn.com.irtech.eport.common.core.page.TableDataInfo;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.common.enums.OperatorType;
import vn.com.irtech.eport.common.exception.file.InvalidExtensionException;
import vn.com.irtech.eport.common.utils.DateUtils;
import vn.com.irtech.eport.common.utils.file.FileUploadUtils;
import vn.com.irtech.eport.common.utils.file.MimeTypeUtils;
import vn.com.irtech.eport.framework.web.service.ConfigService;
import vn.com.irtech.eport.framework.web.service.DictService;
import vn.com.irtech.eport.logistic.domain.LogisticAccount;
import vn.com.irtech.eport.logistic.domain.OtpCode;
import vn.com.irtech.eport.logistic.domain.PaymentHistory;
import vn.com.irtech.eport.logistic.domain.ProcessBill;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentComment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
import vn.com.irtech.eport.logistic.service.INapasApiService;
import vn.com.irtech.eport.logistic.service.IOtpCodeService;
import vn.com.irtech.eport.logistic.service.IPaymentHistoryService;
import vn.com.irtech.eport.logistic.service.IProcessBillService;
import vn.com.irtech.eport.logistic.service.IProcessOrderService;
import vn.com.irtech.eport.logistic.service.IShipmentCommentService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.logistic.service.IShipmentService;
import vn.com.irtech.eport.system.domain.SysDictData;
import vn.com.irtech.eport.system.dto.PartnerInfoDto;

@Controller
@RequestMapping("/logistic")
public class LogisticCommonController extends LogisticBaseController {

	private static final Logger logger = LoggerFactory.getLogger(LogisticCommonController.class);

	private final String PREFIX = "logistic";

	@Autowired
	private IShipmentDetailService shipmentDetailService;

	@Autowired
	ICatosApiService catosApiService;

	@Autowired
	private IShipmentService shipmentService;

	@Autowired
	private IOtpCodeService otpCodeService;

	@Autowired
	private ConfigService configService;

	@Autowired
	private IProcessBillService processBillService;

	@Autowired
	private INapasApiService napasApiService;

	@Autowired
	private IProcessOrderService processOrderService;

	@Autowired
	private IPaymentHistoryService paymentHistoryService;

	@Autowired
	private IShipmentCommentService shipmentCommentService;

	@Autowired
	private ServerConfig serverConfig;

	@Autowired
	private DictService dictDataService;

	@GetMapping("/company/{taxCode}")
	@ResponseBody
	public AjaxResult getGroupNameByTaxCode(@PathVariable String taxCode) throws Exception {
		AjaxResult ajaxResult = AjaxResult.success();
		if (taxCode == null || "".equals(taxCode)) {
			return error();
		}
		PartnerInfoDto partner = catosApiService.getGroupNameByTaxCode(taxCode);
		String groupName = partner.getGroupName();
		String address = partner.getAddress();
		if (address != null) {
			ajaxResult.put("address", address);
		}
		if (groupName != null) {
			ajaxResult.put("groupName", groupName);
		} else {
			ajaxResult = AjaxResult.error();
		}
		return ajaxResult;
	}

	@PostMapping("/shipments")
	@ResponseBody
	public TableDataInfo listShipment(@RequestBody PageAble<Shipment> param) {
		startPage(param.getPageNum(), param.getPageSize(), param.getOrderBy());
		LogisticAccount user = getUser();
		Shipment shipment = param.getData();
		shipment.setLogisticGroupId(user.getGroupId());
		List<Shipment> shipments = shipmentService.selectShipmentListForRegister(shipment);
		return getDataTable(shipments);
	}

	@Log(title = "Xóa Lô", businessType = BusinessType.DELETE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment/remove")
	@ResponseBody
	public AjaxResult remove(Long id) {
		Shipment shipment = shipmentService.selectShipmentById(id);
		LogisticAccount user = getUser();
		if (shipment != null && shipment.getLogisticGroupId().equals(user.getGroupId())) {
			if (shipment.getStatus() != null && Integer.parseInt(shipment.getStatus()) < Integer.parseInt(EportConstants.SHIPMENT_STATUS_PROCESSING)) {
				// Delete shipment detail by shipment id
				ShipmentDetail shipmentDetailParam = new ShipmentDetail();
				shipmentDetailParam.setShipmentId(id);
				shipmentDetailParam.setLogisticGroupId(user.getGroupId());
				shipmentDetailService.deleteShipmentDetailByCondition(shipmentDetailParam);
				return toAjax(shipmentService.deleteShipmentById(id));
			} else {
				return error("Không thể xóa lô đang làm lệnh");
			}
		}
		return error();
	}

	@GetMapping("/otp/{shipmentDetailIds}")
	@ResponseBody
	public AjaxResult sendOTP(@PathVariable String shipmentDetailIds) {
		// LogisticGroup lGroup = getGroup();
		OtpCode otpCode = new OtpCode();
		Random rd = new Random();
		long rD = rd.nextInt(900000) + 100000;
		String tDCode = Long.toString(rD);
		otpCodeService.deleteOtpCodeByShipmentDetailIds(shipmentDetailIds);

		otpCode.setTransactionId(shipmentDetailIds);
		otpCode.setPhoneNumber(getUser().getMobile());
		otpCode.setOtpCode(tDCode);
		otpCode.setOtpType("1");

		Calendar cal = Calendar.getInstance();
		Date now = new Date();
		cal.setTime(now);
		cal.add(Calendar.MINUTE, +5);
		otpCode.setExpiredTime(cal.getTime());
		otpCodeService.insertSysOtp(otpCode);
		// FIXME Get message template from SysConfigService, using String.format
		// to replace place holders
		String[] shipmentDetailIdArr = shipmentDetailIds.split(",");
		ShipmentDetail shipmentDetail = shipmentDetailService.selectShipmentDetailById(Long.parseLong(shipmentDetailIdArr[0]));
		String content = configService.getKey("otp.format");
		content = content.replace("{shipmentId}", shipmentDetail.getShipmentId().toString()).replace("{otp}", tDCode);
		String response = "";
		try {
			response = otpCodeService.postOtpMessage(getUser().getMobile(), content);
			System.out.println(response);
			logger.debug("OTP Send Response: " + response);
		} catch (IOException ex) {
			// process the exception
			logger.error(ex.getMessage());
		}
		return AjaxResult.success();
	}

	@GetMapping("/source/taxCode/consignee")
	@ResponseBody
	public AjaxResult getConsigneeList() {
		AjaxResult ajaxResult = success();
		List<String> listConsignee = shipmentDetailService.getConsigneeList();
		ajaxResult.put("consigneeList", listConsignee);
		return ajaxResult;
	}

	@GetMapping("/source/specialService")
	@ResponseBody
	public AjaxResult getConsigneeListWithoutTaxCode() {
		AjaxResult ajaxResult = success();
		List<String> specialService = dictDataService.getListTag("special.service");
		List<SysDictData> sysDictDatas = dictDataService.getType("special.service");
		ajaxResult.put("specialService", specialService);
		ajaxResult.put("specialServiceDictData", sysDictDatas);
		return ajaxResult;
	}

	@GetMapping("/source/consignee")
	@ResponseBody
	public AjaxResult getSpecialService() {
		AjaxResult ajaxResult = success();
		List<String> listConsignee = shipmentDetailService.getConsigneeListWithoutTaxCode();
		ajaxResult.put("consigneeList", listConsignee);
		return ajaxResult;
	}

	@GetMapping("/source/option")
	@ResponseBody
	public AjaxResult getField() {
		AjaxResult ajaxResult = success();

		List<String> listVessel = shipmentDetailService.getVesselCodeList();
		List<String> opeCodeList = shipmentDetailService.getOpeCodeList();

		ajaxResult.put("vslNmList", listVessel);
		ajaxResult.put("opeCodeList", opeCodeList);

		return ajaxResult;
	}

	@GetMapping("/vessel/{vslNm}/voyages")
	@ResponseBody
	public AjaxResult getVoyages(@PathVariable String vslNm) {
		AjaxResult ajaxResult = success();
		List<String> voyages = shipmentDetailService.getVoyageNoList(vslNm);
		ajaxResult.put("voyages", voyages);
		return ajaxResult;
	}

	@GetMapping("/payment/napas/{processOrderIds}")
	public String napasPaymentForm(@PathVariable String processOrderIds, ModelMap mmap) {
		String[] processOrderIdsArr = processOrderIds.split(",");

		// return error when logistic didn't own process order
		if (processOrderIdsArr.length != processOrderService.checkLogisticOwnedProcessOrder(getUser().getGroupId(),
				processOrderIdsArr)) {
			return "error/unauth";
		}

		String orderId = "Order";
		for (String id : processOrderIdsArr) {
			orderId += "-" + id;
		}
		orderId = orderId + "-" + DateUtils.dateTimeNow();

		// return error when get total bill null
		Long total = processBillService.getSumOfTotalBillList(processOrderIdsArr);
		if (total == null) {
			return "error/500";
		}

		// check if process order is on payment transaction
		PaymentHistory paymentHistoryParam = new PaymentHistory();
		paymentHistoryParam.setProccessOrderIds(processOrderIds);
		paymentHistoryParam.setStatus("1");
		List<PaymentHistory> paymentHistories = paymentHistoryService.selectPaymentHistoryList(paymentHistoryParam);
		PaymentHistory paymentHistory;
		if (CollectionUtils.isEmpty(paymentHistories)) {
			paymentHistory = new PaymentHistory();
			paymentHistory.setUserId(getUserId());
			paymentHistory.setProccessOrderIds(processOrderIds);
			paymentHistory.setShipmentId(
					processOrderService.getShipmentIdByProcessOrderId(Long.valueOf(processOrderIdsArr[0])));
			paymentHistory.setAmount(total);
			paymentHistory.setStatus("0");
			paymentHistory.setOrderId(orderId);
			paymentHistory.setCreateBy(getUser().getFullName());
			paymentHistory.setLogisticGroupId(getUser().getGroupId());
			paymentHistoryService.insertPaymentHistory(paymentHistory);
		} else {
			return "error/unauth";
		}

		mmap.put("resultUrl", configService.getKey("napas.payment.result"));
		mmap.put("referenceOrder", "Thanh toan " + orderId);
		mmap.put("clientIp", getUserIp());
		mmap.put("data",
				napasApiService.getDataKey(getUserIp(), "deviceId", orderId, total, napasApiService.getAccessToken()));

		return PREFIX + "/napas/napasPaymentForm";
	}

	@Log(title = "Thanh Toán Napas", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@RequestMapping(value = "/payment/result", consumes = "application/x-www-form-urlencoded;charset=UTF-8")
	@Transactional
	public String getPaymentResult(@RequestParam("napasResult") String result, ModelMap mmap) {
		JSONObject json = JSONObject.parseObject(result);
		String dataBase64 = json.getString("data");

		boolean isError = true;

		// checksum
		String checksumString = json.getString("checksum");
		if (checksumString.equalsIgnoreCase(DigestUtils.sha256Hex(dataBase64 + configService.getKey("napas.client.secret")))) {

			// decode base
			JSONObject decodeData = JSONObject.parseObject(new String(Base64.getDecoder().decode(dataBase64)));

			// result (Success or Failed)
			String resultStatus = decodeData.getJSONObject("paymentResult").getString("result");

			if ("SUCCESS".equalsIgnoreCase(resultStatus)) {
				// order id
				String orderId = decodeData.getJSONObject("paymentResult").getJSONObject("order").getString("id");
				PaymentHistory paymentHistoryParam = new PaymentHistory();
				paymentHistoryParam.setOrderId(orderId);
				List<PaymentHistory> paymentHistories = paymentHistoryService.selectPaymentHistoryList(paymentHistoryParam);
				if (!paymentHistories.isEmpty()) {
					PaymentHistory paymentHistory = paymentHistories.get(0);

					// Update payment history
					paymentHistory.setUpdateBy(getUser().getFullName());
					paymentHistory.setStatus("1");
					paymentHistoryService.updatePaymentHistory(paymentHistory);

					// Update shipment detail
					List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByProcessIds(paymentHistory.getProcessOrderIds());
					for (ShipmentDetail shipmentDetail : shipmentDetails) {
						shipmentDetail.setPaymentStatus("Y");
						shipmentDetail.setStatus(shipmentDetail.getStatus() + 1);
						if (shipmentDetail.getCustomStatus() != null && "N".equals(shipmentDetail.getCustomStatus())
								&& shipmentDetail.getDischargePort() != null
								&& shipmentDetail.getDischargePort().length() > 2
								&& "VN".equals(shipmentDetail.getDischargePort().substring(0, 2))) {
							shipmentDetail.setCustomStatus("R");
							shipmentDetail.setStatus(shipmentDetail.getStatus() + 1);
						}
						shipmentDetailService.updateShipmentDetail(shipmentDetail);
					}

					// Update bill
					processBillService.updateBillListByProcessOrderIds(paymentHistory.getProcessOrderIds());

					isError = false;
				}
			}
		}
		if (isError) {
			mmap.put("result", "ERROR");
		} else {
			mmap.put("result", "SUCCESS");
		}
		return PREFIX + "/napas/resultForm";
	}

	@Log(title = "Thanh Toán Napas", businessType = BusinessType.INSERT, operatorType = OperatorType.MOBILE)
	@RequestMapping(value = "/payment/mobile/result", consumes = "application/x-www-form-urlencoded;charset=UTF-8")
	@Transactional
	public String getPaymentMobileResult(@RequestParam("napasResult") String result, ModelMap mmap) {
		JSONObject json = JSONObject.parseObject(result);
		String dataBase64 = json.getString("data"); 
		boolean isError = true; 
		// checksum
		String checksumString = json.getString("checksum");
		if (checksumString.equalsIgnoreCase(DigestUtils.sha256Hex(dataBase64 + configService.getKey("napas.client.secret")))) { 
			// decode base
			JSONObject decodeData = JSONObject.parseObject(new String(Base64.getDecoder().decode(dataBase64))); 
			// result (Success or Failed)
			String resultStatus = decodeData.getJSONObject("paymentResult").getString("result");

			if ("SUCCESS".equalsIgnoreCase(resultStatus)) {
				// order id
				String orderId = decodeData.getJSONObject("paymentResult").getJSONObject("order").getString("id");
				PaymentHistory paymentHistoryParam = new PaymentHistory();
				paymentHistoryParam.setOrderId(orderId);
				List<PaymentHistory> paymentHistories = paymentHistoryService.selectPaymentHistoryList(paymentHistoryParam);
				if (!paymentHistories.isEmpty()) {
					PaymentHistory paymentHistory = paymentHistories.get(0); 
					// Update payment history
					paymentHistory.setUpdateBy(getUser().getFullName());
					paymentHistory.setStatus("1");
					paymentHistoryService.updatePaymentHistory(paymentHistory); 
					// Update shipment detail
					List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByProcessIds(paymentHistory.getProcessOrderIds());
					for (ShipmentDetail shipmentDetail : shipmentDetails) {
						shipmentDetail.setPaymentStatus("Y");
						shipmentDetail.setStatus(shipmentDetail.getStatus() + 1);
						if (shipmentDetail.getCustomStatus() != null && "N".equals(shipmentDetail.getCustomStatus())
								&& shipmentDetail.getDischargePort() != null
								&& shipmentDetail.getDischargePort().length() > 2
								&& "VN".equals(shipmentDetail.getDischargePort().substring(0, 2))) {
							shipmentDetail.setCustomStatus("R");
							shipmentDetail.setStatus(shipmentDetail.getStatus() + 1);
						}
						shipmentDetailService.updateShipmentDetail(shipmentDetail);
					} 
					// Update bill
					processBillService.updateBillListByProcessOrderIds(paymentHistory.getProcessOrderIds());

					isError = false;
				}
			}
		}
		if (isError) {
			mmap.put("result", "ERROR");
		} else {
			mmap.put("result", "SUCCESS");
		}
		return PREFIX + "/napas/resultMobiletForm";
	}

	@PostMapping("/pods")
	@ResponseBody
	public AjaxResult getPODs(@RequestBody ShipmentDetail shipmentDetail) {
		AjaxResult ajaxResult = success();
		List<String> listPOD = new ArrayList<String>();
		if (shipmentDetail != null) {
			// String year =
			// catosApiService.getYearByVslCodeAndVoyNo(shipmentDetail.getVslNm(),
			// shipmentDetail.getVoyNo());
			// if(year != null) {
			// shipmentDetail.setYear(year);
			// }
			listPOD = catosApiService.getPODList(shipmentDetail);
			ajaxResult.put("dischargePorts", listPOD);
			return ajaxResult;
		}
		return error();
	}

	@GetMapping("/size/container/list")
	@ResponseBody
	public AjaxResult getSztps() {
		return AjaxResult.success(dictDataService.getType("sys_size_container_eport"));
	}

	@GetMapping("/shipment/{shipmentId}/napas")
	public String napasShiftingPaymentForm(@PathVariable("shipmentId") Long shipmentId, ModelMap mmap) {

		List<ProcessBill> processBills = processBillService.getBillShiftingContByShipmentId(shipmentId,getUser().getGroupId());

		if (processBills.isEmpty()) {
			return "error/500";
		}

		Long total = 0L;
		Long idTemp = processBills.get(0).getProcessOrderId();
		String orderId = "Order-" + idTemp;
		String processOrderIds = idTemp + ",";
		for (ProcessBill processBill : processBills) {
			total += processBill.getVatAfterFee();
			if (!idTemp.equals(processBill.getProcessOrderId())) {
				idTemp = processBill.getProcessOrderId();
				orderId += "-" + idTemp;
				processOrderIds += idTemp + ",";
			}
		}
		orderId = orderId + "-" + DateUtils.dateTimeNow();
		processOrderIds.substring(0, processOrderIds.length() - 1);

		// check if process order is on payment transaction
		PaymentHistory paymentHistoryParam = new PaymentHistory();
		paymentHistoryParam.setProccessOrderIds(processOrderIds);
		paymentHistoryParam.setStatus("0");
		PaymentHistory paymentHistory;
		paymentHistory = new PaymentHistory();
		paymentHistory.setUserId(getUserId());
		paymentHistory.setProccessOrderIds(processOrderIds);
		paymentHistory.setShipmentId(shipmentId);
		paymentHistory.setAmount(total);
		paymentHistory.setStatus("0");
		paymentHistory.setOrderId(orderId);
		paymentHistory.setCreateBy(getUser().getFullName());
		paymentHistory.setLogisticGroupId(getUser().getGroupId());
		paymentHistoryService.insertPaymentHistory(paymentHistory);
		mmap.put("resultUrl", configService.getKey("napas.payment.shifting.result"));
		mmap.put("referenceOrder", "Thanh toan " + orderId);
		mmap.put("clientIp", getUserIp());
		mmap.put("data",
				napasApiService.getDataKey(getUserIp(), "deviceId", orderId, total, napasApiService.getAccessToken()));

		return PREFIX + "/napas/napasPaymentForm";
	}

	@RequestMapping(value = "/payment/shifting/result", consumes = "application/x-www-form-urlencoded;charset=UTF-8")
	@Transactional
	public String getPaymentShiftingResult(@RequestParam("napasResult") String result, ModelMap mmap) {
		JSONObject json = JSONObject.parseObject(result);
		String dataBase64 = json.getString("data");

		boolean isError = true;

		// checksum
		String checksumString = json.getString("checksum");
		if (checksumString.equalsIgnoreCase(DigestUtils.sha256Hex(dataBase64 + configService.getKey("napas.client.secret")))) { 
			// decode base
			JSONObject decodeData = JSONObject.parseObject(new String(Base64.getDecoder().decode(dataBase64))); 
			// result (Success or Failed)
			String resultStatus = decodeData.getJSONObject("paymentResult").getString("result"); 
			if ("SUCCESS".equalsIgnoreCase(resultStatus)) {
				// order id
				String orderId = decodeData.getJSONObject("paymentResult").getJSONObject("order").getString("id");
				PaymentHistory paymentHistoryParam = new PaymentHistory();
				paymentHistoryParam.setOrderId(orderId);
				List<PaymentHistory> paymentHistories = paymentHistoryService.selectPaymentHistoryList(paymentHistoryParam);
				if (CollectionUtils.isNotEmpty(paymentHistories)) {
					PaymentHistory paymentHistory = paymentHistories.get(0);

					// Update payment history
					paymentHistory.setUpdateBy(getUser().getFullName());
					paymentHistory.setStatus("1");
					paymentHistoryService.updatePaymentHistory(paymentHistory);

					// Update shipment detail
					ShipmentDetail shipmentDetailParam = new ShipmentDetail();
					shipmentDetailParam.setShipmentId(paymentHistory.getShipmentId());
					shipmentDetailParam.setPreorderPickup("Y");
					List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetailParam);
					for (ShipmentDetail shipmentDetail : shipmentDetails) {
						shipmentDetail.setPrePickupPaymentStatus("Y");
						shipmentDetailService.updateShipmentDetail(shipmentDetail);
					} 
					// Update bill
					processBillService.updateBillListByProcessOrderIds(paymentHistory.getProcessOrderIds());

					isError = false;
				}
			}
		}
		if (isError) {
			mmap.put("result", "ERROR");
		} else {
			mmap.put("result", "SUCCESS");
		}
		return PREFIX + "/napas/resultForm";
	}

	@GetMapping("/ope-code/{opeCode}/vessel-code/list")
	@ResponseBody
	public AjaxResult getVesselBerthPlanByOpeCode(@PathVariable String opeCode) {
		AjaxResult ajaxResult = success();
		List<String> vesselList = catosApiService.selectVesselCodeBerthPlan(opeCode);
		if (vesselList.size() > 0) {
			ajaxResult.put("vessels", vesselList);
			return ajaxResult;
		}
		return error();
	}

	@GetMapping("/taxCode/{taxCode}/delegate/payment/permission")
	@ResponseBody
	public AjaxResult checkDelegatePermission(@PathVariable("taxCode") String taxCode) {
		if (taxCode.equalsIgnoreCase(getGroup().getMst())) {
			return success();
		}
		if (logisticGroupService.checkDelegatePermission(taxCode, getGroup().getMst(),
				EportConstants.DELEGATE_PERMISSION_PAYMENT) > 0) {
			return success();
		}
		return error();
	}

	@PostMapping("/comment/list")
	@ResponseBody
	public AjaxResult getCommentList(@RequestBody ShipmentComment shipmentComment) {
		if (shipmentComment == null) {
			return error("Invalid input!");
		}
		shipmentComment.setLogisticGroupId(getUser().getGroupId());
		List<ShipmentComment> shipmentComments = shipmentCommentService.selectShipmentCommentListCustom(shipmentComment);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentComments", shipmentComments);
		return ajaxResult;
	}

	@PostMapping("/comment/update")
	@ResponseBody
	public AjaxResult updateCommentSeenFlg(@RequestBody ShipmentComment shipmentComment) {
		if (shipmentComment == null || shipmentComment.getShipmentId() == null) {
			return error("Invalid input!");
		}

		ShipmentComment shipmentCommentParam = new ShipmentComment();
		shipmentCommentParam.setShipmentId(shipmentComment.getShipmentId());
		shipmentCommentParam.setLogisticGroupId(getUser().getGroupId());
		shipmentCommentParam.setSeenFlg(true);
		shipmentCommentService.updateFlgShipmentComment(shipmentCommentParam);
		return success();
	}

	@GetMapping("/comment/amount")
	@ResponseBody
	public AjaxResult getNumberOfComment() {
		ShipmentComment shipmentComment = new ShipmentComment();
		shipmentComment.setUserType(EportConstants.COMMENTOR_DNP_STAFF);
		shipmentComment.setLogisticGroupId(getUser().getGroupId());
		shipmentComment.setSeenFlg(false);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentCommentAmount", shipmentCommentService.selectCountCommentListUnSeen(shipmentComment));
		return ajaxResult;
	}

	@GetMapping("/comment/notifications")
	@ResponseBody
	public AjaxResult getListCommentShipmentForGeneral() {
		startPage();
		ShipmentComment shipmentComment = new ShipmentComment();
		shipmentComment.setUserType(EportConstants.COMMENTOR_DNP_STAFF);
		shipmentComment.setLogisticGroupId(getUser().getGroupId());
		List<ShipmentComment> shipmentComments = shipmentCommentService.selectShipmentCommentListForNotification(shipmentComment);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentComments", shipmentComments);
		Long total = new PageInfo<ShipmentComment>(shipmentComments).getTotal();
		ajaxResult.put("total", total);
		return ajaxResult;
	}

	@GetMapping("/comment/notification/all")
	@ResponseBody
	public AjaxResult getFullListShipmentComment() {
		ShipmentComment shipmentComment = new ShipmentComment();
		shipmentComment.setUserType(EportConstants.COMMENTOR_DNP_STAFF);
		shipmentComment.setLogisticGroupId(getUser().getGroupId());
		List<ShipmentComment> shipmentComments = shipmentCommentService.selectShipmentCommentListForNotification(shipmentComment);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentComments", shipmentComments);
		return ajaxResult;
	}

	@PostMapping("/shipment/{shipmentId}/file/attach")
	@ResponseBody
	public AjaxResult postAttachFile(MultipartFile file, @PathVariable("shipmentId") Long shipmentId)
			throws IOException, InvalidExtensionException {
		String basePath = String.format("%s/%s", Global.getUploadPath() + "/comment", shipmentId);
		String now = DateUtils.dateTimeNow();
		String fileName = String.format("file%s.%s", now, FileUploadUtils.getExtension(file));
		String filePath = FileUploadUtils.upload(basePath, fileName, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("fileLink", serverConfig.getUrl() + filePath);
		return ajaxResult;
	}
}
