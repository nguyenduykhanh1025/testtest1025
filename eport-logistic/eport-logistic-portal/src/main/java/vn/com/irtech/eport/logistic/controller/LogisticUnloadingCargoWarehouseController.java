package vn.com.irtech.eport.logistic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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

import vn.com.irtech.eport.carrier.domain.EdoHouseBill;
import vn.com.irtech.eport.carrier.service.ICarrierGroupService;
import vn.com.irtech.eport.carrier.service.IEdoHouseBillService;
import vn.com.irtech.eport.carrier.service.IEdoService;
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
import vn.com.irtech.eport.logistic.domain.CfsHouseBill;
import vn.com.irtech.eport.logistic.domain.LogisticAccount;
import vn.com.irtech.eport.logistic.domain.OtpCode;
import vn.com.irtech.eport.logistic.domain.ProcessBill;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentComment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.domain.ShipmentImage;
import vn.com.irtech.eport.logistic.form.ContainerServiceForm;
import vn.com.irtech.eport.logistic.listener.MqttService;
import vn.com.irtech.eport.logistic.listener.MqttService.NotificationCode;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
import vn.com.irtech.eport.logistic.service.ICfsHouseBillService;
import vn.com.irtech.eport.logistic.service.IOtpCodeService;
import vn.com.irtech.eport.logistic.service.IProcessBillService;
import vn.com.irtech.eport.logistic.service.IShipmentCommentService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.logistic.service.IShipmentImageService;
import vn.com.irtech.eport.logistic.service.IShipmentService;
import vn.com.irtech.eport.system.dto.ContainerInfoDto;
import vn.com.irtech.eport.system.service.ISysConfigService;

@Controller
@RequiresPermissions("logistic:order")
@RequestMapping("/logistic/unloading-cargo-warehouse")
public class LogisticUnloadingCargoWarehouseController extends LogisticBaseController {
	private static final Logger logger = LoggerFactory.getLogger(LogisticUnloadingCargoWarehouseController.class);

	private final String PREFIX = "logistic/unloadingCargoWarehouse";

	@Autowired
	private IShipmentService shipmentService;

	@Autowired
	private IShipmentDetailService shipmentDetailService;

	@Autowired
	private IOtpCodeService otpCodeService;

	@Autowired
	private IProcessBillService processBillService;

	@Autowired
	private CustomQueueService customQueueService;

	@Autowired
	private ISysConfigService configService;

	@Autowired
	private MqttService mqttService;

	@Autowired
	private ICatosApiService catosApiService;

	@Autowired
	private IEdoService edoService;

	@Autowired
	private ICarrierGroupService carrierGroupService;

	@Autowired
	private IEdoHouseBillService edoHouseBillService;

	@Autowired
	private IShipmentCommentService shipmentCommentService;

	@Autowired
	private DictService dictService;

	@Autowired
	private ServerConfig serverConfig;

	@Autowired
	private ICfsHouseBillService cfsHouseBillService;

	@Autowired
	private IShipmentImageService shipmentImageService;

	@GetMapping()
	public String receiveContFull(@RequestParam(required = false) Long sId, ModelMap mmap) {
		if (sId != null) {
			mmap.put("sId", sId);
		}
		List<String> emptyDepots = new ArrayList<>();
		String danangPortName = configService.selectConfigByKey("danang.port.name");
		if (danangPortName != null) {
			emptyDepots.add(danangPortName);
		}
		emptyDepots.add("Cảng Khác");
		mmap.put("emptyDepots", emptyDepots);
		mmap.put("domain", serverConfig.getUrl());
		return PREFIX + "/index";
	}

	@GetMapping("/shipment/add")
	public String add(ModelMap mmap) {
		mmap.put("taxCode", getGroup().getMst());
		return PREFIX + "/add";
	}

	@GetMapping("/shipment/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		Shipment shipment = shipmentService.selectShipmentById(id);
		if (verifyPermission(shipment.getLogisticGroupId())) {
			mmap.put("shipment", shipment);
			mmap.put("taxCode", getGroup().getMst());
		}
		return PREFIX + "/edit";
	}

	@GetMapping("/custom-status/{shipmentDetailIds}")
	public String checkCustomStatus(@PathVariable String shipmentDetailIds, ModelMap mmap) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			mmap.put("shipmentId", shipmentDetails.get(0).getShipmentId());
			mmap.put("contList", shipmentDetails);
		}
		return PREFIX + "/checkCustomStatus";
	}

	@GetMapping("/otp/cont-list/confirmation/{shipmentDetailIds}")
	public String checkContListBeforeVerify(@PathVariable("shipmentDetailIds") String shipmentDetailIds,
			ModelMap mmap) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());
		mmap.put("creditFlag", getGroup().getCreditFlag());
		mmap.put("taxCode", getGroup().getMst());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			if (("Cảng Tiên Sa").equals(shipmentDetails.get(0).getEmptyDepot())) {
				mmap.put("sendContEmpty", true);
			}
			mmap.put("shipmentDetails", shipmentDetails);
		} else {
			mmap.put("sendContEmpty", false);
		}
		return PREFIX + "/checkContListBeforeVerify";
	}

	@GetMapping("/otp/verification/{shipmentDetailIds}/{creditFlag}/{taxCode}/{isSendContEmpty}/{shipmentId}")
	public String verifyOtpForm(@PathVariable("shipmentDetailIds") String shipmentDetailIds,
			@PathVariable("shipmentId") Long shipmentId, @PathVariable("creditFlag") boolean creditFlag,
			@PathVariable("isSendContEmpty") boolean isSendContEmpty, @PathVariable("taxCode") String taxCode,
			ModelMap mmap) {
		mmap.put("shipmentDetailIds", shipmentDetailIds);
		mmap.put("numberPhone", getUser().getMobile());
		mmap.put("creditFlag", creditFlag);
		mmap.put("isSendContEmpty", isSendContEmpty);
		mmap.put("taxCode", taxCode);
		mmap.put("shipmentId", shipmentId);
		return PREFIX + "/verifyOtp";
	}

	@GetMapping("/payment/{shipmentDetailIds}")
	public String paymentForm(@PathVariable("shipmentDetailIds") String shipmentDetailIds, ModelMap mmap) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			shipmentDetailIds = "";
			for (ShipmentDetail shipmentDetail : shipmentDetails) {
				shipmentDetailIds += shipmentDetail.getId() + ",";
			}
			shipmentDetailIds = shipmentDetailIds.substring(0, shipmentDetailIds.length() - 1);
			mmap.put("shipmentDetailIds", shipmentDetailIds);

			// Get process bill
			ProcessBill processBillParam = new ProcessBill();
			processBillParam.setPaymentStatus("N");
			Map<String, Object> params = new HashMap<>();
			params.put("shipmentDetailIds", Convert.toStrArray(shipmentDetailIds));
			processBillParam.setParams(params);
			List<ProcessBill> processBills = processBillService.selectProcessBillList(processBillParam);
			mmap.put("processBills", processBills);
		}
		return PREFIX + "/paymentForm";
	}

	@GetMapping("/req/cancel/confirmation")
	public String getCancelConfirmationForm() {
		return PREFIX + "/confirmRequestCancel";
	}

	@GetMapping("/row/{rowIndex}/calendar")
	public String getCalendarInputForm(@PathVariable("rowIndex") Integer rowIndex, ModelMap mmap) {
		mmap.put("rowIndex", rowIndex);
		return PREFIX + "/calendar";
	}

	@Log(title = "Thêm Lô Bốc Hàng", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment")
	@Transactional
	@ResponseBody
	public AjaxResult addShipment(Shipment shipmentInput) {
		Shipment shipment = new Shipment();
		// Check case house bill
		if (StringUtils.isNotEmpty(shipmentInput.getHouseBill())) {
			if (edoHouseBillService.getContainerAmountWithOrderNumber(shipmentInput.getHouseBill(),
					shipmentInput.getOrderNumber()) != 0) {
				// Shipment is house bill case => edo
				shipment.setEdoFlg(EportConstants.DO_TYPE_CARRIER_EDO);
			}
		} else if (edoService.getContainerAmountWithOrderNumber(shipmentInput.getBlNo(),
				shipmentInput.getOrderNumber()) != 0) {
			// Case edo with master bill
			shipment.setEdoFlg(EportConstants.DO_TYPE_CARRIER_EDO);
		} else {
			// If shipmen is not house bill or master bill with valid order number => DO
			// type
			shipment.setEdoFlg(EportConstants.DO_TYPE_CARRIER_DO);
		}

		LogisticAccount user = getUser();
		shipment.setEdoFlg(shipmentInput.getEdoFlg());
		shipment.setOpeCode(shipmentInput.getOpeCode());
		shipment.setOrderNumber(shipmentInput.getOrderNumber());
		shipment.setBlNo(shipmentInput.getBlNo());
		shipment.setHouseBill(shipmentInput.getHouseBill());
		shipment.setContainerAmount(shipmentInput.getContainerAmount());
		shipment.setRemark(shipmentInput.getRemark());
		shipment.setLogisticAccountId(user.getId());
		shipment.setLogisticGroupId(user.getGroupId());
		shipment.setCreateBy(user.getFullName());
		shipment.setServiceType(EportConstants.SERVICE_UNLOADING_CARGO_WAREHOUSE);
		shipment.setStatus(EportConstants.SHIPMENT_STATUS_INIT);
		if (shipmentService.insertShipment(shipment) == 1) {
			return success("Thêm lô thành công");
		}
		return error("Thêm lô thất bại");
	}

	@Log(title = "Chỉnh Sửa Lô", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment/{shipmentId}")
	@ResponseBody
	public AjaxResult editShipment(Shipment shipment, @PathVariable Long shipmentId) {
		LogisticAccount user = getUser();
		Shipment referenceShipment = shipmentService.selectShipmentById(shipmentId);
		// check if current user own shipment
		if (verifyPermission(referenceShipment.getLogisticGroupId())) {
			// Chi update cac item cho phep
			referenceShipment.setUpdateBy(user.getFullName());
			referenceShipment.setRemark(shipment.getRemark());

			// Can change bill of lading when status = initialize => another item change
			// accordingly
			if (EportConstants.SHIPMENT_STATUS_INIT.equals(referenceShipment.getStatus())) {
				if (StringUtils.isNotEmpty(shipment.getBlNo())) {
					referenceShipment.setBlNo(shipment.getBlNo().toUpperCase());
					referenceShipment.setContainerAmount(shipment.getContainerAmount());
				}

				if (StringUtils.isNotEmpty(shipment.getHouseBill())) {
					referenceShipment.setHouseBill(shipment.getHouseBill().toUpperCase());
				}
				referenceShipment.setOpeCode(shipment.getOpeCode());
				referenceShipment.setEdoFlg(shipment.getEdoFlg());
			}

			if (shipmentService.updateShipment(referenceShipment) == 1) {
				return success("Chỉnh sửa lô thành công");
			}
		}
		return error("Chỉnh sửa lô thất bại");
	}

	@GetMapping("/shipment/{shipmentId}/shipment-detail")
	@ResponseBody
	public AjaxResult listShipmentDetail(@PathVariable Long shipmentId) {
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		// check if shipment exist and allow permission
		if (shipment == null || !verifyPermission(shipment.getLogisticGroupId())) {
			return error("Không tìm thấy lô");
		}
		// get list shipment detail by Id
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentId);
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.getShipmentDetailList(shipmentDetail);
		// auto load containers detail for eDO for first time
		if ("1".equals(shipment.getEdoFlg()) && shipmentDetails.size() == 0) {
			if (StringUtils.isNotEmpty(shipment.getHouseBill())) {
				shipmentDetails = shipmentDetailService.getShipmentDetailFromHouseBill(shipment.getHouseBill());
			} else {
				// get infor from edi
				shipmentDetails = shipmentDetailService.getShipmentDetailsFromEDIByBlNo(shipment.getBlNo());
			}
			// get infor from catos
			// List<ShipmentDetail> shipmentDetailsCatos =
			// catosApiService.selectShipmentDetailsByBLNo(shipment.getBlNo());
			Map<String, ContainerInfoDto> catosDetailMap = getCatosShipmentDetail(shipment.getBlNo());
			// Get opecode, sealNo, wgt, pol, pod
			ContainerInfoDto catos = null;
			for (ShipmentDetail detail : shipmentDetails) {
				catos = catosDetailMap.get(detail.getContainerNo());
				if (catos != null) {
					// Overwrite information from CATOS
					detail.setSztp(catos.getSztp());
					// Block-Bay-Row-Tier
					detail.setLocation(catos.getLocation());
					detail.setContainerRemark(catos.getRemark());
					detail.setOpeCode(catos.getPtnrCode() + ": " + catos.getPtnrName());
					detail.setVslNm(catos.getVslCd() + ": " + catos.getVslNm());// overwrite VSL_CD:VSL_NM from CATOS
					detail.setVoyCarrier(catos.getInVoy()); // Carrier voyage name on booking
					detail.setVoyNo(catos.getUserVoy());
					detail.setSealNo(catos.getSealNo1());
					detail.setWgt(catos.getWgt());
					detail.setLoadingPort(catos.getPol()); // overwrite from CATOS
					detail.setDischargePort(catos.getPod()); // overwrite from CATOS
				}
			}
		}

		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("shipmentDetails", shipmentDetails);
		return ajaxResult;
	}

	@Log(title = "Khai Báo Cont", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment-detail")
	@Transactional
	@ResponseBody
	public AjaxResult saveShipmentDetail(@RequestBody List<ShipmentDetail> shipmentDetails) {

		if (CollectionUtils.isNotEmpty(shipmentDetails)) {
			String dnPortName = configService.selectConfigByKey("danang.port.name");
			LogisticAccount user = getUser();
			ShipmentDetail firstDetail = shipmentDetails.get(0);
			Long shipmentId = firstDetail.getShipmentId();
			if (shipmentId == null) {
				return error("Không tìm thấy lô cần thêm chi tiết.");
			}
			Shipment shipment = shipmentService.selectShipmentById(shipmentId);
			if (shipment == null || !verifyPermission(shipment.getLogisticGroupId())) {
				return error("Không tìm thấy lô, vui lòng kiểm tra lại thông tin.");
			}
			Shipment shipmentSendEmpty = new Shipment();
			boolean updateShipment = true;

			// eDO Map to replace when save info
			Map<String, ShipmentDetail> edoDetailMap = new HashMap<>();
			if ("1".equals(shipment.getEdoFlg())) {
				// get infor from edo
				List<ShipmentDetail> edoDetails = null;
				if (StringUtils.isNotEmpty(shipment.getHouseBill())) {
					edoDetails = shipmentDetailService.getShipmentDetailFromHouseBill(shipment.getHouseBill());
				} else {
					// get infor from edi
					edoDetails = shipmentDetailService.getShipmentDetailsFromEDIByBlNo(shipment.getBlNo());
				}
				for (ShipmentDetail sd : edoDetails) {
					edoDetailMap.put(sd.getContainerNo(), sd);
				}
			}
			// lay consignee taxcode tu catos
			String taxCode = catosApiService.getTaxCodeBySnmGroupName(firstDetail.getConsignee());
			if (StringUtils.isBlank(taxCode)) {
				return error(String.format("Không tìm thấy chủ hàng '%s', <br/>Vui lòng chọn chủ hàng từ danh sách.",
						firstDetail.getConsignee()));
			}
			// ShipmentDetail catosSearch = new ShipmentDetail();
			// catosSearch.setBlNo(shipment.getBlNo());
			// create to search infor from catos

			// Get container list for BL from catos
			Map<String, ContainerInfoDto> catosMap = getCatosShipmentDetail(shipment.getBlNo());
			ContainerInfoDto ctnrInfo = null;
			ShipmentDetail shipmentDetail = null;
			for (ShipmentDetail inputDetail : shipmentDetails) {
				shipmentDetail = new ShipmentDetail();
				// New record
				if (inputDetail.getId() == null) {
					// Setting from input screen
					shipmentDetail.setContainerNo(inputDetail.getContainerNo());
					shipmentDetail.setConsignee(inputDetail.getConsignee());
					shipmentDetail.setEmptyDepot(inputDetail.getEmptyDepot());
					shipmentDetail.setExpiredDem(inputDetail.getExpiredDem());
					shipmentDetail.setDetFreeTime(inputDetail.getDetFreeTime());
					shipmentDetail.setDateReceipt(inputDetail.getDateReceipt());
					shipmentDetail.setSpecialService(EportConstants.SPECIAL_SERVICE_UNLOAD_WAREHOUSE);
					// default value
					shipmentDetail.setShipmentId(shipmentId);
					shipmentDetail.setBlNo(shipment.getBlNo());
					shipmentDetail.setLogisticGroupId(user.getGroupId());
					shipmentDetail.setCreateBy(user.getFullName());
					shipmentDetail.setFe("F");
					shipmentDetail.setPaymentStatus("N");
					shipmentDetail.setUserVerifyStatus("N");
					shipmentDetail.setDateReceiptStatus("N");
					shipmentDetail.setProcessStatus("N");
					if (EportConstants.DO_TYPE_CARRIER_DO.equals(shipment.getEdoFlg())) {
						shipmentDetail.setDoStatus("N");
					} else {
						shipmentDetail.setDoStatus("Y");
					}
					shipmentDetail.setPreorderPickup("N");
					shipmentDetail.setDoStatus("N");
					shipmentDetail.setFinishStatus("N");
					// search catos infor for specified container and replace infor
					ctnrInfo = catosMap.get(shipmentDetail.getContainerNo());
					if (ctnrInfo != null) {
						shipmentDetail.setSztp(ctnrInfo.getSztp());
						// shipmentDetail.setSztpDefine(catos.getSztpDefine()); // TODO
						shipmentDetail.setCarrierName(ctnrInfo.getPtnrName());
						shipmentDetail.setVslName(ctnrInfo.getVslNm());
						shipmentDetail.setVslNm(ctnrInfo.getVslCd());
						shipmentDetail.setVoyNo(ctnrInfo.getCallSeq());
						shipmentDetail.setVslAndVoy(ctnrInfo.getUserVoy());
						shipmentDetail.setOpeCode(ctnrInfo.getPtnrCode());
						shipmentDetail.setSealNo(ctnrInfo.getSealNo1());
						shipmentDetail.setWgt(ctnrInfo.getWgt());
						shipmentDetail.setVoyCarrier(ctnrInfo.getInVoy());
						shipmentDetail.setLoadingPort(ctnrInfo.getPol());
						shipmentDetail.setDischargePort(ctnrInfo.getPod());
						shipmentDetail.setCargoType(ctnrInfo.getCargoType());
						shipmentDetail.setLocation(
								ctnrInfo.getLocation() != null ? ctnrInfo.getLocation() : ctnrInfo.getArea());
						shipmentDetail.setContainerRemark(ctnrInfo.getRemark());
						shipmentDetail.setYear(ctnrInfo.getCallYear());
					}
					// edo
					if ("1".equals(shipment.getEdoFlg())) {
						// TODO khai bao bien lai gon hon
						if (edoDetailMap.get(inputDetail.getContainerNo()) != null) {
							shipmentDetail
									.setExpiredDem(edoDetailMap.get(inputDetail.getContainerNo()).getExpiredDem());
							shipmentDetail
									.setDetFreeTime(edoDetailMap.get(inputDetail.getContainerNo()).getDetFreeTime());
						}
					}
					// Hàng cont nội
					if ("VN".equalsIgnoreCase(shipmentDetail.getLoadingPort().substring(0, 2))) {
						shipmentDetail.setCustomStatus(null);
						shipmentDetail.setStatus(2);
						shipmentDetail.setTaxCode(taxCode);
						shipmentDetail.setConsigneeByTaxCode(firstDetail.getConsignee());
					} else {
						shipmentDetail.setCustomStatus("N");
						shipmentDetail.setStatus(1);
						// set null to taxcode and consignee, update when custom process
						shipmentDetail.setTaxCode(null);
						shipmentDetail.setConsigneeByTaxCode(null);
					}
					if (shipmentDetailService.insertShipmentDetail(shipmentDetail) != 1) {
						return error("Lưu khai báo thất bại từ container: " + shipmentDetail.getContainerNo());
					}

					// Update case
				} else {
					// set null to taxcode and consignee
					ShipmentDetail shipmentDetailReference = shipmentDetailService
							.selectShipmentDetailById(inputDetail.getId());
					// validate shipment detail, in-case edit ID from client
					if (!shipmentDetailReference.getLogisticGroupId().equals(user.getGroupId())) {
						return error("Không tìm thấy thông tin, vui lòng kiểm tra lại");
					}
					// Update khi nguoi dung chua lam lenh.
					if ("N".equals(shipmentDetailReference.getUserVerifyStatus())) {
						updateShipment = false;
						shipmentDetailReference.setUpdateBy(user.getFullName());
						shipmentDetailReference.setConsignee(inputDetail.getConsignee());
						shipmentDetailReference.setEmptyDepot(inputDetail.getEmptyDepot());
						// T/h la container domestic, update taxcode, consignee theo thong tin nguoi
						// dung nhap
						if ("VN".equalsIgnoreCase(shipmentDetailReference.getLoadingPort().substring(0, 2))) {
							shipmentDetailReference.setTaxCode(taxCode);
							shipmentDetailReference.setConsigneeByTaxCode(inputDetail.getConsignee());
						}
						// check if not eDO
						if (!"1".equals(shipment.getEdoFlg())) {
							shipmentDetailReference.setExpiredDem(inputDetail.getExpiredDem());
							shipmentDetailReference.setDetFreeTime(inputDetail.getDetFreeTime());
						}
						shipmentDetailReference.setUpdateTime(new Date());
						shipmentDetailReference.setDateReceipt(inputDetail.getDateReceipt());
						if (shipmentDetailService.updateShipmentDetail(shipmentDetailReference) != 1) {
							return error("Lưu khai báo thất bại từ container: " + inputDetail.getContainerNo());
						}
					}
				}
			}
			if (updateShipment && shipment != null && shipment.getId() != null) {
				shipment.setUpdateTime(new Date());
				shipment.setUpdateBy(getUser().getFullName());
				shipment.setStatus(EportConstants.SHIPMENT_STATUS_SAVE);
				shipmentService.updateShipment(shipment);
			}
			return success("Lưu khai báo thành công");
		}
		return error("Lưu khai báo thất bại");
	}

	@Log(title = "Xóa Khai Báo Cont", businessType = BusinessType.DELETE, operatorType = OperatorType.LOGISTIC)
	@DeleteMapping("/shipment/{shipmentId}/shipment-detail/{shipmentDetailIds}")
	@Transactional
	@ResponseBody
	public AjaxResult deleteShipmentDetail(@PathVariable Long shipmentId,
			@PathVariable("shipmentDetailIds") String shipmentDetailIds) {
		if (shipmentDetailIds != null) {
			// kiem tra co the xoa hay khong. Sau khi lam lenh thi khong the khai bao
			List<ShipmentDetail> details = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
					getUser().getGroupId());
			for (ShipmentDetail detail : details) {
				// Check if user is verified. delete when user not verified
				if (!"Y".equals(detail.getUserVerifyStatus())) {
					shipmentDetailService.deleteShipmentDetailById(detail.getId());
				}
			}
			ShipmentDetail shipmentDetail = new ShipmentDetail();
			shipmentDetail.setShipmentId(shipmentId);
			if (shipmentDetailService.countShipmentDetailList(shipmentDetail) == 0) {
				Shipment shipment = new Shipment();
				shipment.setId(shipmentId);
				shipment.setStatus("1");
				shipment.setUpdateBy(getUser().getFullName());
				shipmentService.updateShipment(shipment);
			}
			return success("Lưu khai báo thành công");
		}
		return error("Lưu khai báo thất bại");
	}

	@PostMapping("/shipment-detail/bl-no/cont/info")
	@ResponseBody
	public ShipmentDetail getContInfo(@RequestBody ShipmentDetail shipmentDetail) {
		if (StringUtils.isNotEmpty(shipmentDetail.getBlNo())
				&& StringUtils.isNotEmpty(shipmentDetail.getContainerNo())) {
			// TODO su dung form tra ve gia tri can thiet, khong tra ve het
			ShipmentDetail shipmentDetailResult = catosApiService.selectShipmentDetailByContNo(shipmentDetail);
			return shipmentDetailResult;
		}
		return null;
	}

	@Log(title = "Check Hải Quan", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/custom-status/shipment-detail")
	@ResponseBody
	public AjaxResult checkCustomStatus(@RequestParam(value = "declareNos") String declareNoList,
			@RequestParam(value = "shipmentDetailIds") String shipmentDetailIds) {
		if (StringUtils.isNotEmpty(declareNoList)) {
			List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
					getUser().getGroupId());
			// flag mapping custom No
			if (CollectionUtils.isNotEmpty(shipmentDetails)) {
				for (ShipmentDetail shipmentDetail : shipmentDetails) {
					// Save declareNoList to shipment detail
					shipmentDetail.setCustomsNo(declareNoList);
					shipmentDetail.setCustomScanTime(new Date());
					shipmentDetailService.updateShipmentDetail(shipmentDetail);

					customQueueService.offerShipmentDetail(shipmentDetail);
					// }
				}
				return success();
			}
		}
		return error();
	}

	@Log(title = "Xác Nhận OTP", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/otp/{otp}/verification")
	@ResponseBody
	@RepeatSubmit
	public AjaxResult verifyOtp(@PathVariable("otp") String otp, String shipmentDetailIds, String taxCode,
			boolean creditFlag) {
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
			return error("Mã OTP không chính xác, hoặc đã hết hiệu lực!");
		}
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());
		AjaxResult validateResult = validateShipmentDetailList(shipmentDetails);
		Integer code = (Integer) validateResult.get("code");
		if (code != 0) {
			return validateResult;
		}
		if (!CollectionUtils.isEmpty(shipmentDetails)) {
			Shipment shipment = shipmentService.selectShipmentById(shipmentDetails.get(0).getShipmentId());
			// Neu khong phai status la "Dang lam lenh" thi update thanh dang lam lenh
			if (!EportConstants.SHIPMENT_STATUS_PROCESSING.equals(shipment.getStatus())) {
				shipment.setStatus(EportConstants.SHIPMENT_STATUS_PROCESSING);
				shipment.setUpdateTime(new Date());
				shipment.setUpdateBy(getUser().getFullName());
				shipmentService.updateShipment(shipment);
			}
			// Đổi opeCode operateCode -> groupCode. VD Hang tau CMA: CMA,CNC,APL.. -> CMA
			String oprParent = dictService.getLabel("carrier_parent_child_list", shipmentDetails.get(0).getOpeCode());
			if (StringUtils.isNotEmpty(oprParent)) {
				for (ShipmentDetail shpDtl : shipmentDetails) {
					shpDtl.setOpeCode(oprParent);
					shpDtl.setUpdateBy(getUser().getUserName());
				}
			}

			// Create list req for order receive cont empty
			shipmentDetailService.makeOrderUnloadingCargo(shipmentDetails, shipment, taxCode, creditFlag,
					getUser().getMobile());

			// Request manual order for om
			String title = "Yêu cầu làm lệnh bốc hàng rút ruột cfs";
			String msg = "Yêu cầu bốc hàng rút ruột cho container B/L No " + shipment.getBlNo();
			try {
				mqttService.sendNotificationApp(NotificationCode.NOTIFICATION_OM, title, msg,
						configService.selectConfigByKey("domain.admin.name") + EportConstants.URL_OM_UNLOADING_CARGO,
						EportConstants.NOTIFICATION_PRIORITY_HIGH);
			} catch (MqttException e) {
				logger.error("Error when push request make unloading cargo order: " + e);
			}

			return success();
		}

		return error("Có lỗi xảy ra trong quá trình xác thực!");
	}

	/**
	 * Get all consignee in catos and load in grid view
	 * 
	 * @return
	 */
	@GetMapping("/consignees")
	@ResponseBody
	public AjaxResult getField() {
		AjaxResult ajaxResult = success();
		List<String> listConsignee = shipmentDetailService.getConsigneeList();
		ajaxResult.put("consigneeList", listConsignee);
		return ajaxResult;
	}

	/**
	 * Check B/L no is exist for current logistic
	 * 
	 * @param inputForm
	 * @return
	 */
	@PostMapping("/shipment/bl-no")
	@ResponseBody
	public AjaxResult checkShipmentInforByBlNo(@RequestBody ContainerServiceForm inputForm) {
		String blNo = inputForm.getBlNo();
		if (StringUtils.isNotEmpty(blNo)) {
			blNo = blNo.toUpperCase();
		}
		AjaxResult ajaxResult = new AjaxResult();
		Shipment shipment = new Shipment();
		// check bill unique
		shipment.setServiceType(EportConstants.SERVICE_UNLOADING_CARGO_WAREHOUSE);
		shipment.setLogisticGroupId(getUser().getGroupId());
		shipment.setBlNo(blNo);
		if (shipmentService.checkBillBookingNoUnique(shipment) != 0) {
			return error("Số bill đã tồn tại");
		}

		// check opeCode
		String opeCode = edoService.getOpeCodeByBlNo(blNo);
		// Long containerAmount = edoService.getCountContainerAmountByBlNo(blNo);

		// Check edo with master bill
		if (opeCode != null) {
			// Check if carrier group support edo depend on do type defining in carrier
			// group admin
			if (EportConstants.DO_TYPE_CARRIER_EDO.equalsIgnoreCase(carrierGroupService.getDoTypeByOpeCode(opeCode))) {
				shipment.setEdoFlg("1");
				shipment.setOpeCode(opeCode);
				// shipment.setContainerAmount(containerAmount);
				ajaxResult = success();
				ajaxResult.put("shipment", shipment);
				return ajaxResult;
			}
		} else {
			// Check edo with house bill
			EdoHouseBill edoHouseBill = edoHouseBillService.getEdoHouseBillByBlNo(blNo);
			if (edoHouseBill != null) {
				// Check if carrier group support edo depend on do type defining in carrier
				// group admin
				if (EportConstants.DO_TYPE_CARRIER_EDO
						.equalsIgnoreCase(carrierGroupService.getDoTypeByOpeCode(edoHouseBill.getCarrierCode()))) {
					shipment.setEdoFlg("1");
					shipment.setOpeCode(edoHouseBill.getCarrierCode());
					shipment.setHouseBill(blNo);
					shipment.setBlNo(edoService.getBlNoByHouseBillId(edoHouseBill.getId()));
					// shipment.setContainerAmount(containerAmount);
					ajaxResult = success();
					ajaxResult.put("shipment", shipment);
					return ajaxResult;
				}
			}
		}

		// check do
		Shipment shipCatos = null;
		try {
			shipCatos = catosApiService.getOpeCodeCatosByBlNo(blNo);
		} catch (Exception e) {
			logger.error("Error when get ope code catos by bl no: " + e);
		}
		if (shipCatos != null) {
			String edoFlg = carrierGroupService.getDoTypeByOpeCode(shipCatos.getOpeCode());
			if (edoFlg == null) {
				return error(
						"Mã hãng tàu:" + shipCatos.getOpeCode() + " không có trong hệ thống. Vui lòng liên hệ Cảng!");
			}
			// if(edoFlg.equals("1")){
			// return error("Bill này là eDO nhưng không có dữ liệu trong eport. Vui lòng
			// liên hệ Cảng!");
			// }
			shipment.setEdoFlg(edoFlg);
			ajaxResult = success();
			shipment.setOpeCode(shipCatos.getOpeCode());
			shipment.setContainerAmount(shipCatos.getContainerAmount());
			ajaxResult.put("shipment", shipment);
			return ajaxResult;
		}

		ajaxResult = error("Số bill không tồn tại!");
		return ajaxResult;
	}

	/**
	 * Check order number match with bl no for edo (master bill, house bill)
	 * 
	 * @param shipment
	 * @return AjaxResult
	 */
	@PostMapping("/orderNumber/check")
	@ResponseBody
	public AjaxResult checkOrderNumber(@RequestBody Shipment shipment) {
		int containerAmount = 0;
		// Check for house bill case
		if (StringUtils.isNotEmpty(shipment.getHouseBill())) {
			containerAmount = edoHouseBillService.getContainerAmountWithOrderNumber(shipment.getHouseBill(),
					shipment.getOrderNumber());
		} else {
			// Check for master bill case
			containerAmount = edoService.getContainerAmountWithOrderNumber(shipment.getBlNo(),
					shipment.getOrderNumber());
		}
		if (containerAmount != 0) {
			AjaxResult ajaxResult = AjaxResult.success();
			ajaxResult.put("containerAmount", containerAmount);
			return ajaxResult;
		}
		return error("Mã nhận container không chính xác.");
	}

	@GetMapping("/shipment/{shipmentId}/delegate/permission")
	@ResponseBody
	public AjaxResult checkDelegatePermission(@PathVariable Long shipmentId) {
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (shipment == null || !shipment.getLogisticGroupId().equals(getUser().getGroupId())) {
			return error("Lô không tồn tai, vui lòng kiểm tra lại.");
		}
		// check DO or eDO (0 is DO no need to validate
		// Get tax code of consignee own this shipment
		String taxCode = shipmentDetailService.selectConsigneeTaxCodeByShipmentId(shipmentId);
		// taxcode from HQ null -> chua thong quan, cont nội: chưa chọn chủ hàng
		if (taxCode == null) {
			return error("Không xác định được chủ hàng, vui lòng kiểm tra lại.");
		}
		// if logistic is consignee -> pass
		String myTaxcode = getGroup().getMst();
		if (taxCode.equalsIgnoreCase(myTaxcode)) {
			// Pass
			return success();
		}
		// Check if logistic can make order for this shipment
		if (logisticGroupService.checkDelegatePermission(taxCode, myTaxcode,
				EportConstants.DELEGATE_PERMISSION_PROCESS) > 0) {
			return success();
		}
		return error(
				"Bạn chưa có ủy quyền từ chủ hàng để thực hiện lô hàng này. Hãy liên hệ với Cảng để thêm ủy quyền.");
	}

	@GetMapping("/shipment/{shipmentId}/custom/notification")
	@ResponseBody
	public AjaxResult sendNotificationCustomError(@PathVariable("shipmentId") String shipmentId) {
		try {
			mqttService.sendNotification(NotificationCode.NOTIFICATION_OM_CUSTOM, "Lỗi hải quan lô " + shipmentId,
					configService.selectConfigByKey("domain.admin.name") + "/om/support/custom/" + shipmentId);
		} catch (MqttException e) {
			logger.error("Gửi thông báo lỗi hải quan cho om: " + e);
		}
		return success();
	}

	/**
	 * Get empty depot location base on sztp and opr in dictionary
	 * 
	 * @param sztp
	 * @param opr
	 * @return empty depot location string (DANALOG 01, DANALOG 02, Tiên Sa)
	 */
	private String getEmptyDepotLocation(String sztp, String opr) {
		String emptyDepotRule = dictService.getLabel("empty_depot_location", opr);
		if (StringUtils.isNotEmpty((emptyDepotRule))) {
			String[] emptyDepotArr = emptyDepotRule.split(",");
			int length = emptyDepotArr.length;
			for (int i = 0; i < length; i++) {
				if (sztp.equalsIgnoreCase(emptyDepotArr[i])) {
					String emptyDepotLocation = emptyDepotArr[length - 1];
					return emptyDepotLocation;
				}
			}
		}
		String danangDepotName = configService.selectConfigByKey("danang.depot.name");
		return danangDepotName;
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
		shipmentComment.setServiceType(EportConstants.SERVICE_UNLOADING_CARGO_WAREHOUSE);
		shipmentComment.setCommentTime(new Date());
		shipmentComment.setSeenFlg(true);
		shipmentCommentService.insertShipmentComment(shipmentComment);

		// Send notification to om
		try {
			mqttService.sendNotificationApp(NotificationCode.NOTIFICATION_OM, shipmentComment.getTopic(),
					shipmentComment.getContent(), "", EportConstants.NOTIFICATION_PRIORITY_MEDIUM);
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
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());

		AjaxResult validateResult = validateShipmentDetailList(shipmentDetails);
		return validateResult;
	}

	private AjaxResult validateShipmentDetailList(List<ShipmentDetail> shipmentDetails) {
		if (CollectionUtils.isEmpty(shipmentDetails)) {
			return error("Không tìm thấy thông tin chi tiết lô đã chọn.");
		}
		// validate
		ShipmentDetail shipmentDetailReference = shipmentDetails.get(0);
		String containerNos = "";
		String userVoy = shipmentDetailReference.getVslAndVoy();
		// List sztp can register on eport get from dictionary
		// All sztp that not in this list is invalid
		List<String> sztps = dictService.getListTag("sys_size_container_eport");
		for (int i = 0; i < shipmentDetails.size(); i++) {
			if (StringUtils.isEmpty(shipmentDetails.get(i).getContainerNo())) {
				return error("Hàng " + (i + 1) + ": Chưa nhập số container!");
			}
			if (shipmentDetails.get(i).getDateReceipt() == null) {
				return error("Hàng " + (i + 1) + ": Chưa đăng ký ngày rút hàng!");
			}
			if (shipmentDetailReference.getExpiredDem() == null) {
				return error("Hàng " + (i + 1) + ": Chưa nhập hạn lệnh!");
			}
			if (StringUtils.isEmpty(shipmentDetails.get(i).getConsignee())) {
				return error("Hàng " + (i + 1) + ": Chưa chọn chủ hàng!");
			}
			if (StringUtils.isEmpty(shipmentDetails.get(i).getEmptyDepot())) {
				return error("Hàng " + (i + 1) + ": Chưa chọn nơi hạ vỏ!");
			}
			if (!shipmentDetailReference.getConsignee().equals(shipmentDetails.get(i).getConsignee())) {
				return error("Tên chủ hàng không được khác nhau!");
			}
			// Validate sztp
			if (!sztps.contains(shipmentDetails.get(i).getSztp())) {
				return error(
						"Kích thước " + shipmentDetails.get(i).getSztp() + " không được phép làm lệnh trên eport.");
			}

			containerNos += shipmentDetails.get(i).getContainerNo() + ",";

			// Check house bill
			List<CfsHouseBill> cfsHouseBills = cfsHouseBillService
					.selectCfsHouseBillByIdShipmentDetail(shipmentDetails.get(i).getId());
			if (CollectionUtils.isEmpty(cfsHouseBills)) {
				ShipmentImage shipmentImageParam = new ShipmentImage();
				shipmentImageParam.setShipmentDetailId(shipmentDetails.get(i).getId().toString());
				List<ShipmentImage> shipmentImages = shipmentImageService.selectShipmentImageList(shipmentImageParam);
				if (CollectionUtils.isEmpty(shipmentImages)) {
					return error("Chưa nhập house bill cho container " + shipmentDetails.get(i).getContainerNo() + ".");
				}
			}

			if (!checkRegisterTime(shipmentDetails.get(i).getDateReceipt())) {
				return error("Hàng " + (i + 1)
						+ ": Thời gian đăng ký rút hàng hiện tại không hợp lệ, quý khách vui lòng chọn thời gian đăng ký rút hàng khác. ");
			}
		}
		// trim last ','
		if (containerNos.length() > 0) {
			containerNos = containerNos.substring(0, containerNos.length() - 1);
		}
		// validate consignee exist in catos
		if (catosApiService.checkConsigneeExistInCatos(shipmentDetailReference.getConsignee()) == 0) {
			return error(
					"Tên chủ hàng quý khách nhập không đúng, vui lòng chọn tên chủ hàng từ trong danh sách của hệ thống gợi ý.");
		}
		// kiem tra uy quyen
		if (logisticGroupService.checkDelegatePermission(shipmentDetailReference.getTaxCode(), getGroup().getMst(),
				EportConstants.DELEGATE_PERMISSION_PROCESS) == 0) {
			return error(
					"Bạn chưa có ủy quyền từ chủ hàng để thực hiện lô hàng này. Hãy liên hệ với Cảng để thêm ủy quyền.");
		}
		// kiem tra container da duoc lam lenh trong catos
		List<ContainerInfoDto> pickupResult = catosApiService.getContainerPickup(containerNos, userVoy);
		String pickuped = "";
		if (pickupResult != null && pickupResult.size() > 0) {
			for (ContainerInfoDto dto : pickupResult) {
				pickuped += dto.getCntrNo() + ", ";
			}
			// trim last ','
			pickuped = StringUtils.substring(pickuped, 0, -2);
			return error("Các container sau đã làm lệnh nâng hạ.<br/>Vui lòng kiểm tra lại:<br>" + pickuped);
		}

		return success();
	}

	/**
	 * Create Map of ContainerNumber-> ShipmentDetail from catos
	 * 
	 * @param blNo
	 * @return
	 */
	private Map<String, ContainerInfoDto> getCatosShipmentDetail(String blNo) {
		// get infor from catos
		List<ContainerInfoDto> shipmentDetailsCatos = catosApiService.selectShipmentDetailsByBLNo(blNo);
		Map<String, ContainerInfoDto> detailMap = new HashMap<>();
		// Get opecode, sealNo, wgt, pol,pod
		if (shipmentDetailsCatos != null) {
			for (ContainerInfoDto detail : shipmentDetailsCatos) {
				if ("F".equals(detail.getFe())) {
					detailMap.put(detail.getCntrNo(), detail);
				}
			}
		}
		return detailMap;
	}

	@PostMapping("/order-cancel/shipment-detail")
	@ResponseBody
	public AjaxResult reqCancelOrderContainer(String shipmentDetailIds, String contReqRemark) {
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailByIds(shipmentDetailIds,
				getUser().getGroupId());
		if (CollectionUtils.isNotEmpty(shipmentDetails)) {

			// Validate before send req cancel order
			for (ShipmentDetail shipmentDetail : shipmentDetails) {
				if (!"Y".equalsIgnoreCase(shipmentDetail.getProcessStatus())) {
					return error("Container quý khách chọn chưa được làm lệnh.");
				}
			}

			// Update status req cancel order
			for (ShipmentDetail shipmentDetail : shipmentDetails) {
				shipmentDetail.setProcessStatus(EportConstants.PROCESS_STATUS_SHIPMENT_DETAIL_DELETE);
				shipmentDetail.setUpdateBy(getUser().getUserName());
				shipmentDetailService.updateShipmentDetail(shipmentDetail);
			}

			ShipmentDetail shipmentDetail = shipmentDetails.get(0);
			// Write comment for topic supply container
			if (StringUtils.isNotEmpty(contReqRemark)) {
				ShipmentComment shipmentComment = new ShipmentComment();
				shipmentComment.setLogisticGroupId(getUser().getGroupId());
				shipmentComment.setShipmentId(shipmentDetail.getShipmentId());
				shipmentComment.setUserId(getUserId());
				shipmentComment.setUserType(EportConstants.COMMENTOR_LOGISTIC);
				shipmentComment.setUserAlias(getGroup().getGroupName());
				shipmentComment.setCommentTime(new Date());
				shipmentComment.setContent(contReqRemark);
				shipmentComment.setTopic(EportConstants.TOPIC_COMMENT_CANCEL_PICKUP_FULL);
				shipmentComment.setServiceType(shipmentDetail.getServiceType());
				shipmentCommentService.insertShipmentComment(shipmentComment);
			}

			// Set up data to send app notificaton
			String title = "ePort: Yêu cầu huỷ lệnh nhận container hàng.";
			String msg = "Có yêu cầu huỷ lệnh nhận container hàng cho Bill: " + shipmentDetail.getBlNo()
					+ ", Hãng tàu: " + shipmentDetail.getOpeCode() + ", Trucker: " + getGroup().getGroupName()
					+ ", Chủ hàng: " + shipmentDetails.get(0).getConsignee();
			try {
				mqttService.sendNotificationApp(NotificationCode.NOTIFICATION_OM, title, msg,
						configService.selectConfigByKey("domain.admin.name") + EportConstants.URL_CANCEL_ORDER_SUPPORT,
						EportConstants.NOTIFICATION_PRIORITY_LOW);
			} catch (MqttException e) {
				logger.error("Error when push request cancel order pickup full: " + e);
			}
			return success("Đã yêu cầu hủy lệnh, quý khách vui lòng đợi bộ phận thủ tục xử lý.");
		}

		return error("Yêu cầu hủy lệnh thất bại, quý khách vui lòng liên hệ bộ phận thủ tục để được hỗ trợ thêm.");
	}

	@GetMapping("/shipment-detail/{shipmentDetailId}/house-bill")
	public String getCfsHouseBill(@PathVariable("shipmentDetailId") Long shipmentDetailId, ModelMap mmap) {
		ShipmentDetail shipmentDetail = shipmentDetailService.selectShipmentDetailById(shipmentDetailId);
		if (shipmentDetail != null && getUser().getGroupId().equals(shipmentDetail.getLogisticGroupId())) {
			mmap.put("masterBill", shipmentDetail.getBlNo());
			mmap.put("containerNo", shipmentDetail.getContainerNo());
			mmap.put("shipmentDetailId", shipmentDetailId);

			// Get image file from shipment detail id
			ShipmentImage shipmentImage = new ShipmentImage();
			shipmentImage.setShipmentDetailId(shipmentDetailId.toString());
			List<ShipmentImage> shipmentImages = shipmentImageService.selectShipmentImageList(shipmentImage);
			if (CollectionUtils.isNotEmpty(shipmentImages)) {
				for (ShipmentImage shipmentImage2 : shipmentImages) {
					shipmentImage2.setPath(serverConfig.getUrl() + shipmentImage2.getPath());
				}
			}
			mmap.put("shipmentImages", shipmentImages);
		}
		return PREFIX + "/houseBill";
	}

	@GetMapping("shipment-detail/{shipmentDetailId}/house-bills")
	@ResponseBody
	public AjaxResult getHouseBillList(@PathVariable("shipmentDetailId") Long shipmentDetailId) {
		CfsHouseBill cfsHouseBillParam = new CfsHouseBill();
		cfsHouseBillParam.setShipmentDetailId(shipmentDetailId);
		cfsHouseBillParam.setLogisticGroupId(getUser().getGroupId());
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("cfsHouseBills", cfsHouseBillService.selectCfsHouseBillList(cfsHouseBillParam));
		return ajaxResult;
	}

	// SAVE OR EDIT SHIPMENT DETAIL
	@Log(title = "Lưu Khai Báo House Bill", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/shipment-detail/{shipmentDetailId}/house-bills")
	@Transactional
	@ResponseBody
	public AjaxResult saveHouseBill(@RequestBody List<CfsHouseBill> cfsHouseBills,
			@PathVariable("shipmentDetailId") Long shipmentDetailId) {
		if (shipmentDetailId == null) {
			return error("Không xác định được thông tin container.");
		}
		ShipmentDetail shipmentDetail = shipmentDetailService.selectShipmentDetailById(shipmentDetailId);
		if (shipmentDetail == null) {
			return error("Không xác định được thông tin container.");
		}
		if ("Y".equalsIgnoreCase(shipmentDetail.getUserVerifyStatus())) {
			return error("Container đã được xác nhận làm lệnh, không thể chỉnh sửa thông tin chi tiết.");
		}
		if (CollectionUtils.isNotEmpty(cfsHouseBills)) {
			LogisticAccount user = getUser();
			for (CfsHouseBill inputHouseBill : cfsHouseBills) {
				if (inputHouseBill.getId() != null) {
					inputHouseBill.setShipmentDetailId(shipmentDetailId);
					inputHouseBill.setUpdateBy(user.getUserName());
					cfsHouseBillService.updateCfsHouseBill(inputHouseBill);
				} else {
					inputHouseBill.setLogisticGroupId(user.getGroupId());
					inputHouseBill.setShipmentDetailId(shipmentDetailId);
					inputHouseBill.setCreateBy(user.getUserName());
					cfsHouseBillService.insertCfsHouseBill(inputHouseBill);
				}
			}
			return success("Lưu khai báo thành công");
		}
		return error("Lưu khai báo thất bại");
	}

	// DELETE SHIPMENT DETAIL
	@Log(title = "Xóa Khai Báo House Bill", businessType = BusinessType.DELETE, operatorType = OperatorType.LOGISTIC)
	@DeleteMapping("/house-bills")
	@Transactional
	@ResponseBody
	public AjaxResult deleteHouseBills(String houseBillIds) {
		if (houseBillIds != null) {
			CfsHouseBill cfsHouseBillParam = new CfsHouseBill();
			Map<String, Object> params = new HashMap<>();
			params.put("ids", Convert.toStrArray(houseBillIds));
			cfsHouseBillParam.setParams(params);
			cfsHouseBillParam.setLogisticGroupId(getUser().getGroupId());
			List<CfsHouseBill> cfsHouseBills = cfsHouseBillService.selectCfsHouseBillList(cfsHouseBillParam);
			if (CollectionUtils.isNotEmpty(cfsHouseBills)) {
				Long currentShipmentDetailId = cfsHouseBills.get(0).getShipmentDetailId();
				houseBillIds = "";
				for (CfsHouseBill cfsHouseBill : cfsHouseBills) {
					if (!cfsHouseBill.getShipmentDetailId().equals(currentShipmentDetailId)) {
						return error("Không thể xóa thông tin chi tiết của nhiều container cùng 1 lúc.");
					}
					houseBillIds += cfsHouseBill.getId() + ",";
				}
			}
			if (StringUtils.isEmpty(houseBillIds)) {
				return error("Không tìm thấy thông tin chi tiết nào cần xóa.");
			}
			cfsHouseBillService.deleteCfsHouseBillByIds(houseBillIds.substring(0, houseBillIds.length() - 1));
			return success("Lưu khai báo thành công");
		}
		return error("Lưu khai báo thất bại");
	}

	@PostMapping("/shipment-detail/{shipmentDetailId}/file")
	@ResponseBody
	public AjaxResult saveShipmentDetailFile(MultipartFile file,
			@PathVariable("shipmentDetailId") Long shipmentDetailId) throws IOException, InvalidExtensionException {

		ShipmentDetail shipmentDetail = shipmentDetailService.selectShipmentDetailById(shipmentDetailId);
		if (shipmentDetail == null) {
			return error("Không xác định được thông tin container cần đính kèm.");
		}

		String basePath = String.format("%s/%s",
				Global.getUploadPath() + "/" + getUser().getGroupId() + "/shipment-detail", shipmentDetailId);
		String now = DateUtils.dateTimeNow();
		String fileName = String.format("file%s.%s", now, FileUploadUtils.getExtension(file));
		String filePath = FileUploadUtils.upload(basePath, fileName, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

		ShipmentImage shipmentImage = new ShipmentImage();
		shipmentImage.setPath(filePath);
		shipmentImage.setShipmentId(shipmentDetail.getShipmentId());
		shipmentImage.setShipmentDetailId(shipmentDetailId.toString());
		shipmentImage.setCreateTime(DateUtils.getNowDate());
		shipmentImage.setCreateBy(getUser().getFullName());
		shipmentImageService.insertShipmentImage(shipmentImage);

		AjaxResult ajaxResult = AjaxResult.success();
		shipmentImage.setPath(serverConfig.getUrl() + shipmentImage.getPath());
		ajaxResult.put("shipmentFile", shipmentImage);
		return ajaxResult;
	}

	@DeleteMapping("/shipment-detail/{shipmentDetailId}/file")
	@ResponseBody
	public AjaxResult deleteShipmentDetailFile(Long id, @PathVariable("shipmentDetailId") Long shipmentDetailId)
			throws IOException {
		// TODO: Validate permission before delete file

		ShipmentImage shipmentImageParam = new ShipmentImage();
		shipmentImageParam.setId(id);
		ShipmentImage shipmentImage = shipmentImageService.selectShipmentImageById(shipmentImageParam);
		String[] fileArr = shipmentImage.getPath().split("/");
		File file = new File(Global.getUploadPath() + "/" + getUser().getGroupId() + "/shipment-detail/"
				+ shipmentDetailId + "/" + fileArr[fileArr.length - 1]);
		if (file.delete()) {
			shipmentImageService.deleteShipmentImageById(id);
		}
		return success();
	}

	private Boolean checkRegisterTime(Date registerDate) {
		long registerTimeMillis = ((registerDate.getHours() * 60 * 60) + (registerDate.getMinutes() * 60)) * 1000;
		long timeShift1 = 6 * 60 * 60 * 1000;
		long timeShift2 = 12 * 60 * 60 * 1000;
		long timeShift3 = 18 * 60 * 60 * 1000;
		long timeShift4 = 24 * 60 * 60 * 1000;
		long limitTimeMillis = ((8 * 60 * 60) + (30 * 60)) * 1000;
		Date now = new Date();
		long extraBeforeShiftMillis = 30 * 60 * 1000 * 3;
		long currentTimeMillis = ((now.getHours() * 60 * 60) + (now.getMinutes() * 60)) * 1000;
		long diffInMillies = Math.abs(registerDate.getTime() - now.getTime());
		if (diffInMillies < 0) {
			return false;
		}

		if (diffInMillies < limitTimeMillis) {
			if (registerTimeMillis < timeShift1) {
				if (currentTimeMillis <= timeShift1 || timeShift4 - currentTimeMillis < extraBeforeShiftMillis) {
					return false;
				}
			} else if (registerTimeMillis < timeShift2) {
				if (timeShift1 - currentTimeMillis < extraBeforeShiftMillis) {
					return false;
				}

			} else if (registerTimeMillis < timeShift3) {
				if (timeShift2 - currentTimeMillis < extraBeforeShiftMillis) {
					return false;
				}
			} else if (registerTimeMillis < timeShift4) {
				if (timeShift3 - currentTimeMillis < extraBeforeShiftMillis) {
					return false;
				}
			}
		}
		return true;
	}
}
