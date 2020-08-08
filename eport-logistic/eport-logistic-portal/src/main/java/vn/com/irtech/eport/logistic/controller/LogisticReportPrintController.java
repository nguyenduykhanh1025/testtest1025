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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
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

	/**
	 * Print Process Order
	 */
	@GetMapping("/shipment/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		mmap.put("shipmentId", id);
		return prefix + "/processOrder";
	}

	@GetMapping("/processOrder/{shipmentId}")
	public void jasperReport(@PathVariable("shipmentId") Long shipmentId, HttpServletResponse response) {
		// First check permission for this shipmentId
		Shipment shipment = shipmentService.selectShipmentById(shipmentId);
		if(shipment == null || shipment.getLogisticGroupId() == null || shipment.getLogisticGroupId().equals(getUser().getLogisticGroup().getId()))
		{
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
		final JasperReport report = (JasperReport) JRLoader
				.loadObject(this.getClass().getResourceAsStream("/report/equipment_interchange_order.jasper"));

		// Fetching the shipmentDetails from the data source.
		List<ExporterInputItem> jpList = new ArrayList<>();
		//final JRBeanCollectionDataSource params = new JRBeanCollectionDataSource(shipmentDetails);
		ShipmentDetail shipmentDetail = new ShipmentDetail();
		shipmentDetail.setShipmentId(shipmentDetails.get(0).getShipmentId());
		shipmentDetail.setPaymentStatus("Y");
		List<Long> commands = shipmentDetailService.getCommandListInBatch(shipmentDetail);
		if(commands.size()>0) {
			for(Long i : commands) {
				List<ShipmentDetail> list = new ArrayList<ShipmentDetail>();
				for(ShipmentDetail j : shipmentDetails) {
					if(j.getProcessOrderId().equals(i)) {
						list.add(j);
					}
				}
				if(list.size()>0) {
					//final JRBeanCollectionDataSource params = new JRBeanCollectionDataSource(list);
			        final Map<String, Object> parameters = new HashMap<>();
			        parameters.put("user", getGroup().getGroupName());
			        parameters.put("qrCode", "123");
			        parameters.put("billingBooking", (list.get(0).getBlNo() != null? list.get(0).getBlNo():"") +"/"+(list.get(0).getBookingNo() != null ? list.get(0).getBookingNo() : ""));
			        parameters.put("consignee", list.get(0).getConsignee());
			        parameters.put("vslName", list.get(0).getVslName());
			        parameters.put("voyCarrier", list.get(0).getVoyCarrier());
			        parameters.put("opeCode", list.get(0).getOpeCode());
			        parameters.put("invoiceNo", list.get(0).getInvoiceNo());
			        parameters.put("list", list);
			        Shipment shipment = shipmentService.selectShipmentById(shipmentDetails.get(0).getShipmentId());
			        parameters.put("remark", (shipment.getRemark() != null) ? shipment.getRemark() : "");
			        if(shipment.getServiceType().intValue() == 1) {
				        parameters.put("serviceType", "Truck Pickup");
			        }
			        if(shipment.getServiceType().intValue() == 2) {
				        parameters.put("serviceType", "Truck Drop Off");
			        }
			        if(shipment.getServiceType().intValue() == 3) {
				        parameters.put("serviceType", "Truck Empty Pickup");
			        }
			        if(shipment.getServiceType().intValue() == 4) {
				        parameters.put("serviceType", "Truck Full Drop");
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
		//exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jpList);
		exporter.setExporterInput(new SimpleExporterInput(jpList));
		//exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, out); 
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
		exporter.exportReport();
		//JasperExportManager.exportReportToPdfStream(print, out);
	}
}
