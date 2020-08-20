package vn.com.irtech.eport.logistic.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vn.com.irtech.eport.common.config.Global;
import vn.com.irtech.eport.logistic.domain.PickupHistory;
import vn.com.irtech.eport.logistic.domain.ProcessBill;
import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.Shipment;
import vn.com.irtech.eport.logistic.domain.ShipmentDetail;
import vn.com.irtech.eport.logistic.service.ICatosApiService;
@Service
public class CatosApiServiceImpl implements ICatosApiService {
	
	private static final Logger logger = LoggerFactory.getLogger(CatosApiServiceImpl.class);
	
	@Override
	public Shipment getOpeCodeCatosByBlNo(String blNo) {
		String url = Global.getApiUrl() + "/shipmentDetail/getOpeCodeCatosByBlNo/" + blNo;
		logger.debug("Call CATOS API :{}", url);
        RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url, Shipment.class);
	}

	@Override
	public Shipment getGroupNameByTaxCode(String taxCode) {
    	String url = Global.getApiUrl() + "/shipmentDetail/getGroupNameByTaxCode/"+taxCode;
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		Shipment shipment = restTemplate.getForObject(url, Shipment.class);
		return  shipment;
	}

	@Override
	public ProcessOrder getYearBeforeAfter(String vessel, String voyage) {
        String url = Global.getApiUrl() + "/processOrder/getYearBeforeAfter/"+vessel+"/"+voyage;
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url, ProcessOrder.class);
	}

	@Override
	public List<String> checkContainerReserved(String containerNos) {
		String url = Global.getApiUrl() + "/shipmentDetail/checkContReserved/" + containerNos;
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		List<String> listCont = response.getBody();
		return listCont;
	}

	@Override
	public List<String> getPODList(ShipmentDetail shipmentDetail) {
		String url = Global.getApiUrl() + "/shipmentDetail/getPODList";
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity httpEntity = new HttpEntity<ShipmentDetail>(shipmentDetail);
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<String>>(){});
		List<String> pods = response.getBody();
		return pods;
	}

	@Override
	public List<String> getVesselCodeList() {
		String url = Global.getApiUrl() + "/shipmentDetail/getVesselCodeList";
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		List<String> listVessel = response.getBody();
		return listVessel;
	}

	@Override
	public List<String> getConsigneeList() {
		String url = Global.getApiUrl() + "/shipmentDetail/getConsigneeList";
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		List<String> listConsignee = response.getBody();
		return listConsignee;
	}

	@Override
	public List<String> getOpeCodeList() {
		String url = Global.getApiUrl() + "/shipmentDetail/getOpeCodeList";
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		List<String> listOpeCode = response.getBody();
		return listOpeCode;
	}

	@Override
	public List<String> getVoyageNoList(String vesselCode) {
		String url = Global.getApiUrl() + "/shipmentDetail/getVoyageNoList/" + vesselCode;
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		List<String> listVoyage = response.getBody();
		return listVoyage;
	}

	@Override
	public int getCountContByBlNo(String blNo) {
		String url = Global.getApiUrl() + "/shipmentDetail/getCountContByBlNo/" + blNo;
		logger.debug("Call CATOS API :{}", url);
		RestTemplate restTemplate = new RestTemplate();
		Integer count = restTemplate.getForObject(url, Integer.class);
		return count.intValue();
	}

	@Override
	public Boolean checkCustomStatus(String containerNo, String voyNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/check/custom/" + containerNo + "/" + voyNo;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Boolean rs = restTemplate.getForObject(url, Boolean.class);
			return rs;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			return false;
		}
	}

	@Override
	public List<ShipmentDetail> getCoordinateOfContainers(String blNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/getCoordinateOfContainers/" + blNo;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<ShipmentDetail>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ShipmentDetail>>() {});
			List<ShipmentDetail> coordinates = response.getBody();
			return coordinates;
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while call CATOS Api", e);
			return null;
		}
	}

	@Override
	public List<ShipmentDetail> selectShipmentDetailsByBLNo(String blNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/list/" + blNo;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<ShipmentDetail>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ShipmentDetail>>() {});
			List<ShipmentDetail> shipmentDetails = response.getBody();
			return shipmentDetails;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while call CATOS Api", e);
			return null;
		}

	}

	@Override
	public ShipmentDetail selectShipmentDetailByContNo(String blNo, String containerNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/containerInfor/" + blNo + "/" + containerNo;
			logger.debug("Call CATOS API: {}", url);
			RestTemplate restTemplate = new RestTemplate();
			ShipmentDetail shipmentDetail = restTemplate.getForObject(url, ShipmentDetail.class);
			return shipmentDetail;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while call CATOS Api", e);
			return null;
		}
	}

	@Override
	public List<String> selectVesselCodeBerthPlan(String opeCode) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/berthplan/vessel-code/list/ope-code/" + opeCode ;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
			List<String> vslCodes = response.getBody();
			return vslCodes;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public String getYearByVslCodeAndVoyNo(String vslCode, String voyNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/vessel-code/"+ vslCode +"/voyage/" + voyNo + "/year" ;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			String year = restTemplate.getForObject(url, String.class);
			return year;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> selectOpeCodeListInBerthPlan() {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/berthplan/ope-code/list" ;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
			List<String> opeCodes = response.getBody();
			return opeCodes;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ShipmentDetail> selectVesselVoyageBerthPlan(String opeCode) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/berthplan/ope-code/"+ opeCode +"/vessel-voyage/list" ;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<ShipmentDetail>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ShipmentDetail>>() {});
			List<ShipmentDetail> vesselAndVoyages = response.getBody();
			return vesselAndVoyages;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ProcessBill> getUnitBillByShipmentDetailsForReserve(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/unit-bill/list/send-cont";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity httpEntity = new HttpEntity<ShipmentDetail>(shipmentDetail);
			ResponseEntity<List<ProcessBill>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<ProcessBill>>() {});
			List<ProcessBill> bills= response.getBody();
			return bills;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ProcessBill> getUnitBillByShipmentDetailsForInventory(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/unit-bill/list/receive-cont";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity httpEntity = new HttpEntity<ShipmentDetail>(shipmentDetail);
			ResponseEntity<List<ProcessBill>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<ProcessBill>>() {});
			List<ProcessBill> bills= response.getBody();
			return bills;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ProcessBill> getUnitBillByShipmentDetailsForReceiveSSR(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/unit-bill/list/receive-cont/ssr";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity httpEntity = new HttpEntity<ShipmentDetail>(shipmentDetail);
			ResponseEntity<List<ProcessBill>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<ProcessBill>>() {});
			List<ProcessBill> bills= response.getBody();
			return bills;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ProcessBill> getUnitBillByShipmentDetailsForSendSSR(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/unit-bill/list/send-cont/ssr";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity httpEntity = new HttpEntity<ShipmentDetail>(shipmentDetail);
			ResponseEntity<List<ProcessBill>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<ProcessBill>>() {});
			List<ProcessBill> bills= response.getBody();
			return bills;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer checkBookingNoForSendFReceiveE(String bookingNo, String fe) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/check/booking-no/"+ bookingNo + "/fe/" + fe;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Integer result = restTemplate.getForObject(url, Integer.class);
			return result;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ShipmentDetail getInforSendFReceiveE(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/infor/send-full-receive-e";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ShipmentDetail result = restTemplate.postForObject(url, shipmentDetail, ShipmentDetail.class);
			return result;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer getIndexContForSsrByContainerNo(String containerNo) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/index/container-master-ssr/" + containerNo;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Integer index = restTemplate.getForObject(url, Integer.class);
			return index;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}
	/***
	 * input: vslNm, voyNo, year, sztp,booking
	 * @param shipmentDetail
	 * @return
	 */
	@Override
	public Integer getIndexBooking(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/booking/index";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Integer index = restTemplate.postForObject(url, shipmentDetail, Integer.class);
			return index;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}
	/***
	 * input: blNo, containerNo
	 */

	@Override
	public PickupHistory getLocationForReceiveF(PickupHistory pickupHistory) {
		try {
			String blNo = pickupHistory.getShipment().getBlNo();
			String containerNo = pickupHistory.getContainerNo();
			String url = Global.getApiUrl() + "/shipmentDetail/location/bl-no/" + blNo +"/container-no/" + containerNo;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ShipmentDetail location = restTemplate.getForObject(url, ShipmentDetail.class);
			if(location != null) {
				pickupHistory.setBlock(location.getBlock());
				pickupHistory.setBay(location.getBay());
				pickupHistory.setLine(String.valueOf(location.getRow()));
				pickupHistory.setTier(String.valueOf(location.getTier()));
			}
			return pickupHistory;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}
	/***
	 * input: containerNo, blNo, bookingNo, vslNm, voyNo, sztp, opeCode, fe
	 */

	@Override
	public String checkContainerStatus(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/container-status";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			String containerStatus = restTemplate.postForObject(url, shipmentDetail, String.class);
			return containerStatus;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getBlockList(String keyword) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/block/list/keyword/" + keyword;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
			List<String> blockList = response.getBody();
			return blockList;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getAreaList(String keyword) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/area/list/keyword/" + keyword;
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
			List<String> areaList = response.getBody();
			return areaList;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Boolean checkContReserved(ShipmentDetail shipmentDetail) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/check/reserved";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Boolean rs = restTemplate.postForObject(url, shipmentDetail, Boolean.class);
			return rs;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer checkTheNumberOfContainersNotOrderedForReceiveContEmpty(String bookingNo, String sztp) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/receive-cont-empty/container-amount/booking-no/" + bookingNo + "/sztp/" + sztp + "/check/not-ordered";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Integer rs = restTemplate.getForObject(url, Integer.class);
			return rs;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer checkTheNumberOfContainersNotOrderedForSendContFull(String bookingNo, String sztp) {
		try {
			String url = Global.getApiUrl() + "/shipmentDetail/send-cont-full/container-amount/booking-no/" + bookingNo + "/sztp/" + sztp + "/check/not-ordered";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			Integer rs = restTemplate.getForObject(url, Integer.class);
			return rs;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get list consignee with tax code
	 * 
	 * @param shipment
	 * @return	List<shipment>
	 */
	@Override
	public List<Shipment> getListConsigneeWithTaxCode(Shipment shipment) {
		try {
			String url = Global.getApiUrl() + "/consignee/list";
			logger.debug("Call CATOS API :{}", url);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity httpEntity = new HttpEntity<Shipment>(shipment);
			ResponseEntity<List<Shipment>> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<Shipment>>() {});
			List<Shipment> shipments= response.getBody();
			return shipments;
		} catch (Exception e) {
			logger.error("Error while call CATOS Api", e);
			e.printStackTrace();
			return null;
		}
	}
}
