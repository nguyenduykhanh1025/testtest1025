package vn.com.irtech.api.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class UnitBillEntity {

	private String invoiceNo;

	private Long exchangeFee;
	
	private Integer vatRate;
	
	private Long vatAfterFee;
	
	private String containerNo;
	
	private String sztp;
	

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public Long getExchangeFee() {
		return exchangeFee;
	}

	public void setExchangeFee(Long exchangeFee) {
		this.exchangeFee = exchangeFee;
	}

	public Integer getVatRate() {
		return vatRate;
	}

	public void setVatRate(Integer vatRate) {
		this.vatRate = vatRate;
	}

	public Long getVatAfterFee() {
		return vatAfterFee;
	}

	public void setVatAfterFee(Long amount) {
		this.vatAfterFee = amount;
	}

	public String getContainerNo() {
		return containerNo;
	}

	public void setContainerNo(String containerNo) {
		this.containerNo = containerNo;
	}

	public String getSztp() {
		return sztp;
	}

	public void setSztp(String sztp) {
		this.sztp = sztp;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
				.append("invoiceNo", getInvoiceNo())
	            .append("exchangeFee", getExchangeFee())
	            .append("vatRate", getVatRate())
	            .append("vatAfterFee", getVatAfterFee())
	            .append("containerNo", getContainerNo())
	            .append("sztp", getSztp())
	            .toString();
	}
	
}
