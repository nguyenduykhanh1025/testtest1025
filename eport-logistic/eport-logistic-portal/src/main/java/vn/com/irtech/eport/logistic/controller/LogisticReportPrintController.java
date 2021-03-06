package vn.com.irtech.eport.logistic.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import vn.com.irtech.eport.carrier.domain.Edo;
import vn.com.irtech.eport.carrier.domain.EdoHouseBill;
import vn.com.irtech.eport.carrier.service.IEdoHouseBillService;
import vn.com.irtech.eport.common.constant.EportConstants;
import vn.com.irtech.eport.logistic.domain.LogisticGroup;
import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.service.IProcessOrderService;
import vn.com.irtech.eport.logistic.service.IShipmentDetailService;
import vn.com.irtech.eport.logistic.service.IShipmentService;

/**
 * Logistic Report Print Controller
 * 
 * @author admin
 * @date 2020-06-16
 */
@Controller
@RequestMapping("/logistic/print")
public class LogisticReportPrintController extends LogisticBaseController {

	private static final Logger logger = LoggerFactory.getLogger(LogisticReportPrintController.class);

	private String prefix = "logistic/print";

	@Autowired
	private IShipmentService shipmentService;

	@Autowired
	private IShipmentDetailService shipmentDetailService;

	@Autowired
	private IEdoHouseBillService edoHouseBillService;
	
	@Autowired
	private IProcessOrderService processOrderService;

	/**
	 * Print Process Order
	 */
	@GetMapping("/shipment/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		mmap.put("shipmentId", id);
		return prefix + "/processOrder";
	}

	/**
	 * Print Receipt for 4 type register
	 */
	@GetMapping("receipt/shipment/{shipmentId}")
	public String view(@PathVariable("shipmentId") Long shipmentId, ModelMap mmap) {
		mmap.put("shipmentId", shipmentId);
		return prefix + "/receipt";
	}

	/**
	 * Print House Bill
	 */
	@GetMapping("/house-bill/{houseBillNo}")
	public String viewHouseBill(@PathVariable("houseBillNo") String houseBillNo, ModelMap mmap) {
		mmap.put("houseBillNo", houseBillNo);
		return prefix + "/houseBill";
	}

	/**
	 * Print Packing List
	 */
	@GetMapping("/shipment/{id}/packing-list")
	public String packingList(@PathVariable("id") Long id, ModelMap mmap) {
		mmap.put("shipmentId", id);
		return prefix + "/packingList";
	}

	@GetMapping("/processOrder/{shipmentId}")
	public void jasperReport(@PathVariable("shipmentId") Long shipmentId, HttpServletResponse response) {
		// First check permission for this shipmentId
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (shipment == null || shipment.getLogisticGroupId() == null
				|| !shipment.getLogisticGroupId().equals(getUser().getGroupId())) {
			logger.error("Error when print Order for shipment: " + shipmentId);
			return;
		}
		// get shipment detail list
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentId);
		shipmentDetail.setPaymentStatus("Y");
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.getShipmentDetailForPrint(shipmentDetail);
		try {
			response.setContentType("application/pdf");
			createPdfReport(shipmentDetails, response.getOutputStream());
		} catch (final Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createPdfReport(final List<ShipmentDetail> shipmentDetails, OutputStream out) throws JRException {
		// Fetching the report file from the resources folder.
		// FIXME Build jasper
//		final JasperReport report = (JasperReport) JRLoader
//				.loadObject(this.getClass().getResourceAsStream("/report/equipment_interchange_order.jasper"));

		JasperReport report = JasperCompileManager
				.compileReport(this.getClass().getResourceAsStream("/report/jrxml/equipment_interchange_order.jrxml"));

		// Fetching the shipmentDetails from the data source.
		List<ExporterInputItem> jpList = new ArrayList<>();
		// final JRBeanCollectionDataSource params = new
		// JRBeanCollectionDataSource(shipmentDetails);
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentDetails.get(0).getShipmentId());
		shipmentDetail.setPaymentStatus("Y");
		Shipment shipmentRef = shipmentService.selectShipmentById(shipmentDetail.getShipmentId());
		List<Long> commands = shipmentDetailService.getProcessOrderIdListByShipment(shipmentDetail);
		if (commands.size() > 0) {
			for (Long cmd : commands) {
				// Get process order object by id to get create time (datetime start make order)
				ProcessOrder processOrder = processOrderService.selectProcessOrderById(cmd);
				
				List<ShipmentDetail> list = new ArrayList<ShipmentDetail>();
				for (ShipmentDetail detail : shipmentDetails) {
					if (detail.getProcessOrderId().equals(cmd)) {
						if (shipmentRef.getServiceType() == EportConstants.SERVICE_PICKUP_EMPTY) {
							detail.setCargoType("Empty");
						}
						list.add(detail);
					}
				}
				if (list.size() > 0) {
					ShipmentDetail detail = list.get(0);
					// final JRBeanCollectionDataSource params = new
					// JRBeanCollectionDataSource(list);
					final Map<String, Object> parameters = new HashMap<>();
					parameters.put("user", getGroup().getGroupName());
					parameters.put("qrCode", detail.getOrderNo());
					// put time start make order
					parameters.put("orderCreateTime", processOrder.getCreateTime());
					parameters.put("orderNo", detail.getOrderNo());
					parameters.put("billingBooking",
							(detail.getBlNo() != null ? detail.getBlNo() : "") + "/"
									+ (detail.getBookingNo() != null ? detail.getBookingNo() : "") + "/"
									+ (detail.getOrderNo() != null ? detail.getOrderNo() : ""));
					parameters.put("payerName", detail.getPayerName());
					parameters.put("vslName", detail.getVslName());
					parameters.put("voyCarrier", detail.getVoyCarrier());
					parameters.put("opeCode", detail.getOpeCode());
					parameters.put("invoiceNo", detail.getInvoiceNo());
					parameters.put("list", list);
					LogisticGroup logisticGroup = logisticGroupService.selectLogisticGroupById(getUser().getGroupId());
					parameters.put("groupName", logisticGroup.getGroupName());
					Shipment shipment = shipmentService.selectShipmentById(detail.getShipmentId());
					parameters.put("remark", (detail.getContainerRemark() != null) ? detail.getContainerRemark() : "");
					parameters.put("emptyDepot", detail.getEmptyDepot());
					parameters.put("expiredDem", detail.getExpiredDem());
					parameters.put("etd", detail.getEtd());
					parameters.put("eta", detail.getEta());
//					try {
//						File file = new File("target/classes/static/img/logo_gray.jpeg");
//						parameters.put("pathBackground", file.getPath());
//					} catch (Exception e) {
//						logger.error("Path background report error",e.getMessage());
//					}
					if (shipment.getServiceType().intValue() == 1) {
						parameters.put("serviceType", "Truck Pickup");
					}
					if (shipment.getServiceType().intValue() == 2) {
						parameters.put("serviceType", "Truck Drop Off");
					}
					if (shipment.getServiceType().intValue() == 3) {
						parameters.put("serviceType", "Truck Empty Pickup");
					}
					if (shipment.getServiceType().intValue() == 4) {
						parameters.put("serviceType", "Truck Drop Full");
					}
					final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
					jpList.add(new SimpleExporterInputItem(print));
				}
			}
		}

		// Adding the additional parameters to the pdf.
//        final Map<String, Object> parameters = new HashMap<>();
//        parameters.put("user", getGroup().getGroupName());
		// Filling the report with the shipmentDetail data and additional parameters
		// information.
//		final JasperPrint print = JasperFillManager.fillReport(report, parameters, params);
		// Export DPF to output stream
		JRPdfExporter exporter = new JRPdfExporter();
		// exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jpList);
		exporter.setExporterInput(new SimpleExporterInput(jpList));
		// exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, out);
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
		exporter.exportReport();
		// JasperExportManager.exportReportToPdfStream(print, out);
	}

	@GetMapping("create-receipt/shipment/{shipmentId}")
	public void receipt(@PathVariable("shipmentId") Long shipmentId, HttpServletResponse response) {
		// First check permission for this shipmentId
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if (shipment == null || shipment.getLogisticGroupId() == null
				|| !shipment.getLogisticGroupId().equals(getUser().getGroupId())) {
			logger.error("Error when print Receipt for shipment: " + shipmentId);
			return;
		}
		// get shipment detail list
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentId);
		shipmentDetail.setPaymentStatus("Y");
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.getShipmentDetailForPrint(shipmentDetail);
		try {
			response.setContentType("application/pdf");
			createReceipt(shipmentDetails, response.getOutputStream());
		} catch (final Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createReceipt(final List<ShipmentDetail> shipmentDetails, OutputStream out) throws JRException {
		// Fetching the report file from the resources folder.
		final JasperReport report = (JasperReport) JRLoader
				.loadObject(this.getClass().getResourceAsStream("/report/receipt.jasper"));
		Shipment shipment = shipmentService.selectShipmentById(shipmentDetails.get(0).getShipmentId());
		if (shipmentDetails.size() > 0 && shipment != null) {
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("shipmentId", shipment.getId());
			parameters.put("customer", ""); // FIXME Payer
			parameters.put("mst", ""); // FIXME Taxcode
			parameters.put("address", ""); // FIXME Payer Address
			parameters.put("list", shipmentDetails);
//			try {
//				File file = new File("target/classes/static/img/logo_gray.jpeg");
//				parameters.put("pathBackground", file.getPath());
//			} catch (Exception e) {
//				logger.error("Path background report error",e.getMessage());
//			}
			if (shipment.getServiceType().intValue() == 1) {
				parameters.put("serviceType", "Nhận container có hàng từ Cảng");
			}
			if (shipment.getServiceType().intValue() == 2) {
				parameters.put("serviceType", "Hạ container rỗng vào Cảng");
			}
			if (shipment.getServiceType().intValue() == 3) {
				parameters.put("serviceType", "Nhận container rỗng từ Cảng");
			}
			if (shipment.getServiceType().intValue() == 4) {
				parameters.put("serviceType", "Hạ container có hàng vào Cảng");
			}
			final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
			JasperExportManager.exportReportToPdfStream(print, out);
		}
	}

	@GetMapping("create-house-bill/{houseBillNo}")
	public void jasperReportHouseBill(@PathVariable("houseBillNo") String houseBillNo, HttpServletResponse response) {
		EdoHouseBill edoHouseBill = new EdoHouseBill();
		edoHouseBill.setHouseBillNo(houseBillNo);
		edoHouseBill.setLogisticGroupId(getUser().getGroupId());
		// edoHouseBill.setLogisticAccountId(getUser().getId());
		List<EdoHouseBill> houseBillList = edoHouseBillService.selectEdoHouseBillList(edoHouseBill);
		if (houseBillList.isEmpty()) {
			logger.error("Error when print HouseBill: " + houseBillNo);
			return;
		}
		try {
			response.setContentType("application/pdf");
			createHouseBillReport(houseBillList, response.getOutputStream());
		} catch (final Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createHouseBillReport(final List<EdoHouseBill> houseBillList, OutputStream out) throws JRException {
		// Fetching the report file from the resources folder.
//		final JasperReport report = (JasperReport) JRLoader
//				.loadObject(this.getClass().getResourceAsStream("/report/house_bill.jasper"));

		JasperReport report = JasperCompileManager
				.compileReport(this.getClass().getResourceAsStream("/report/jrxml/house_bill.jrxml"));

		// Fetching the shipmentDetails from the data source.
		// final JRBeanCollectionDataSource params = new
		// JRBeanCollectionDataSource(shipmentDetails);

		// Adding the additional parameters to the pdf.
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("consignee", houseBillList.get(0).getConsignee2());
		parameters.put("businessUnit", houseBillList.get(0).getEdo().getBusinessUnit());
		parameters.put("vessel/voy", houseBillList.get(0).getVessel() + " / " + houseBillList.get(0).getVoyNo());
		parameters.put("orderNumber", houseBillList.get(0).getOrderNumber());
		parameters.put("pol", houseBillList.get(0).getEdo().getPol());
		parameters.put("pod", houseBillList.get(0).getEdo().getPod());
		parameters.put("masterBillNo", houseBillList.get(0).getMasterBillNo());
		parameters.put("houseBillNo", houseBillList.get(0).getHouseBillNo());
		parameters.put("fileCreateTime", houseBillList.get(0).getEdo().getFileCreateTime());
//		try {
//			File file = new File("target/classes/static/img/logo_gray.jpeg");
//			parameters.put("pathBackground", file.getPath());
//		} catch (Exception e) {
//			logger.error("Path background report error",e.getMessage());
//		}
		List<Edo> edoList = new ArrayList<Edo>();
		for (EdoHouseBill i : houseBillList) {
			edoList.add(i.getEdo());
		}
		parameters.put("list", edoList);
		final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
//		final JasperPrint print = JasperFillManager.fillReport(report, parameters, params);
		// Export DPF to output stream
		JasperExportManager.exportReportToPdfStream(print, out);
	}

	@GetMapping("create-packing-list/{shipmentId}")
	public void packingListReport(@PathVariable("shipmentId") Long shipmentId, HttpServletResponse response) {
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentId);
		List<ShipmentDetail> shipmentDetails = shipmentDetailService.selectShipmentDetailList(shipmentDetail);
		if (shipmentDetails.isEmpty()) {
			logger.error("Error when print packing list: " + shipmentId);
			return;
		}
		try {
			response.setContentType("application/pdf");
			createPackingListReport(shipmentDetails, response.getOutputStream());
		} catch (final Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createPackingListReport(final List<ShipmentDetail> shipmentDetailList, OutputStream out)
			throws JRException {
		// Fetching the report file from the resources folder.
		final JasperReport report = (JasperReport) JRLoader
				.loadObject(this.getClass().getResourceAsStream("/report/packinglist.jasper"));

		// Fetching the shipmentDetails from the data source.
		// final JRBeanCollectionDataSource params = new
		// JRBeanCollectionDataSource(shipmentDetails);

		// Adding the additional parameters to the pdf.
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("bookingNo", shipmentDetailList.get(0).getBookingNo());
		parameters.put("consignee", shipmentDetailList.get(0).getConsignee());
		parameters.put("etd", shipmentDetailList.get(0).getEtd());
		parameters.put("feederName",
				shipmentDetailList.get(0).getVslName() + " - " + shipmentDetailList.get(0).getVoyCarrier());
		parameters.put("voy", shipmentDetailList.get(0).getVoyCarrier());
		parameters.put("pol", "VNDAD");
		parameters.put("pod", shipmentDetailList.get(0).getDischargePort());
		parameters.put("logisticGroup", getGroup().getGroupName());

		parameters.put("table", shipmentDetailList);
		final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
//		final JasperPrint print = JasperFillManager.fillReport(report, parameters, params);
		// Export DPF to output stream
		JasperExportManager.exportReportToPdfStream(print, out);
	}
}
