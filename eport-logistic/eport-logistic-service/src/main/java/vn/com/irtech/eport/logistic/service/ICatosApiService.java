package vn.com.irtech.eport.logistic.service;

import java.util.List;

import vn.com.irtech.eport.logistic.domain.ProcessOrder;
import vn.com.irtech.eport.logistic.domain.Shipment;

public interface ICatosApiService {

	public Shipment getOpeCodeCatosByBlNo(String blNo);
	
	public String getGroupNameByTaxCode(String taxCode);
	
	public ProcessOrder getYearBeforeAfter(String vessel, String voyage);
	
	public List<String> checkContainerReserved(String containerNos);
	
	public List<String> getPODList();
	
	public List<String> getVesselCodeList();
	
	public List<String> getConsigneeList();
	
	public List<String> getOpeCodeList();
	
	public List<String> getVoyageNoList(String vesselCode);
	
	public int getCountContByBlNo(String blNo);
}