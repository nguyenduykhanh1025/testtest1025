package vn.com.irtech.api.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import java.util.Date;

/**
 * Shipment Details Object shipment_detail
 * 
 * @author admin
 * @date 2020-06-05
 */
public class ShipmentDetailEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** Ma Lo */
    private Long shipmentId;

    /** null */
    private Long logisticGroupId;

    /** Ma DK */
    private String registerNo;

    /** Container Number */
    private String containerNo;

    /** Container Status (S,D) */
    private String containerStatus;

    /** Size Type */
    private String sztp;

    /** FE */
    private String fe;

    /** Booking Number */
    private String bookingNo;

    /** BL number */
    private String blNo;

    /** Seal Number */
    private String sealNo;

    /** Shipper/consignee */
    private String consignee;

    /** Han Lenh */
    private Date expiredDem;

    /** Weight */
    private Long wgt;

    /** Vessel name */
    private String vslNm;

    /** Voyage */
    private String voyNo;

    /** Operator Code */
    private String opeCode;

    /** Cang Chuyen Tai */
    private String loadingPort;

    /** Cang Dich */
    private String dischargePort;

    /** Phuong Tien */
    private String transportType;

    /** Noi Ha Vo */
    private String emptyDepot;

    /** VGM Check */
    private Long vgmChk;

    /** VGM */
    private String vgm;

    /** VGM Person Info */
    private String vgmPersonInfo;

    /** Custom Declare Number */
    private String customDeclareNo;

    /** Custom Status (N,C,H,R) */
    private String customStatus;

    /** Payment Status (Y,N,W,E) */
    private String paymentStatus;

    /** Process Status(Y,N,E) */
    private String processStatus;

    /** T.T DO Goc */
    private String doStatus;

    /** Ngay Nhan DO Goc */
    private Date doReceivedTime;

    /** Xac Thuc (Y,N) */
    private String userVerifyStatus;

    /** Boc Chi Dinh (Y,N) */
    private String preorderPickup;

    /** null */
    private Integer movingContAmount;

    /** null */
    private String transportIds;

    /** Status */
    private Integer status;

    /** Bay*/
    private Integer bay;
    
    /** Roww*/
    private Integer roww;
    
    /** Tier*/
    private Integer tier;
    
    public Integer getBay() {
		return bay;
	}

	public void setBay(Integer bay) {
		this.bay = bay;
	}

	public Integer getRow() {
		return roww;
	}

	public void setRow(Integer roww) {
		this.roww = roww;
	}

	public Integer getTier() {
		return tier;
	}

	public void setTier(Integer tier) {
		this.tier = tier;
	}

	public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setShipmentId(Long shipmentId) 
    {
        this.shipmentId = shipmentId;
    }

    public Long getShipmentId() 
    {
        return shipmentId;
    }
    public void setLogisticGroupId(Long logisticGroupId) 
    {
        this.logisticGroupId = logisticGroupId;
    }

    public Long getLogisticGroupId() 
    {
        return logisticGroupId;
    }
    public void setRegisterNo(String registerNo) 
    {
        this.registerNo = registerNo;
    }

    public String getRegisterNo() 
    {
        return registerNo;
    }
    public void setContainerNo(String containerNo) 
    {
        this.containerNo = containerNo;
    }

    public String getContainerNo() 
    {
        return containerNo;
    }
    public void setContainerStatus(String containerStatus) 
    {
        this.containerStatus = containerStatus;
    }

    public String getContainerStatus() 
    {
        return containerStatus;
    }
    public void setSztp(String sztp) 
    {
        this.sztp = sztp;
    }

    public String getSztp() 
    {
        return sztp;
    }
    public void setFe(String fe) 
    {
        this.fe = fe;
    }

    public String getFe() 
    {
        return fe;
    }
    public void setBookingNo(String bookingNo) 
    {
        this.bookingNo = bookingNo;
    }

    public String getBookingNo() 
    {
        return bookingNo;
    }
    public void setBlNo(String blNo) 
    {
        this.blNo = blNo;
    }

    public String getBlNo() 
    {
        return blNo;
    }
    public void setSealNo(String sealNo) 
    {
        this.sealNo = sealNo;
    }

    public String getSealNo() 
    {
        return sealNo;
    }
    public void setConsignee(String consignee) 
    {
        this.consignee = consignee;
    }

    public String getConsignee() 
    {
        return consignee;
    }
    public void setExpiredDem(Date expiredDem) 
    {
        this.expiredDem = expiredDem;
    }

    public Date getExpiredDem() 
    {
        return expiredDem;
    }
    public void setWgt(Long wgt) 
    {
        this.wgt = wgt;
    }

    public Long getWgt() 
    {
        return wgt;
    }
    public void setVslNm(String vslNm) 
    {
        this.vslNm = vslNm;
    }

    public String getVslNm() 
    {
        return vslNm;
    }
    public void setVoyNo(String voyNo) 
    {
        this.voyNo = voyNo;
    }

    public String getVoyNo() 
    {
        return voyNo;
    }
    public void setOpeCode(String opeCode) 
    {
        this.opeCode = opeCode;
    }

    public String getOpeCode() 
    {
        return opeCode;
    }
    public void setLoadingPort(String loadingPort) 
    {
        this.loadingPort = loadingPort;
    }

    public String getLoadingPort() 
    {
        return loadingPort;
    }
    public void setDischargePort(String dischargePort) 
    {
        this.dischargePort = dischargePort;
    }

    public String getDischargePort() 
    {
        return dischargePort;
    }
    public void setTransportType(String transportType) 
    {
        this.transportType = transportType;
    }

    public String getTransportType() 
    {
        return transportType;
    }
    public void setEmptyDepot(String emptyDepot) 
    {
        this.emptyDepot = emptyDepot;
    }

    public String getEmptyDepot() 
    {
        return emptyDepot;
    }
    public void setVgmChk(Long vgmChk) 
    {
        this.vgmChk = vgmChk;
    }

    public Long getVgmChk() 
    {
        return vgmChk;
    }
    public void setVgm(String vgm) 
    {
        this.vgm = vgm;
    }

    public String getVgm() 
    {
        return vgm;
    }
    public void setVgmPersonInfo(String vgmPersonInfo) 
    {
        this.vgmPersonInfo = vgmPersonInfo;
    }

    public String getVgmPersonInfo() 
    {
        return vgmPersonInfo;
    }
    public void setCustomDeclareNo(String customDeclareNo) 
    {
        this.customDeclareNo = customDeclareNo;
    }

    public String getCustomDeclareNo() 
    {
        return customDeclareNo;
    }
    public void setCustomStatus(String customStatus) 
    {
        this.customStatus = customStatus;
    }

    public String getCustomStatus() 
    {
        return customStatus;
    }
    public void setPaymentStatus(String paymentStatus) 
    {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatus() 
    {
        return paymentStatus;
    }
    public void setProcessStatus(String processStatus) 
    {
        this.processStatus = processStatus;
    }

    public String getProcessStatus() 
    {
        return processStatus;
    }
    public void setDoStatus(String doStatus) 
    {
        this.doStatus = doStatus;
    }

    public String getDoStatus() 
    {
        return doStatus;
    }
    public void setDoReceivedTime(Date doReceivedTime) 
    {
        this.doReceivedTime = doReceivedTime;
    }

    public Date getDoReceivedTime() 
    {
        return doReceivedTime;
    }
    public void setUserVerifyStatus(String userVerifyStatus) 
    {
        this.userVerifyStatus = userVerifyStatus;
    }

    public String getUserVerifyStatus() 
    {
        return userVerifyStatus;
    }
    public void setPreorderPickup(String preorderPickup) 
    {
        this.preorderPickup = preorderPickup;
    }

    public String getPreorderPickup() 
    {
        return preorderPickup;
    }
    public void setMovingContAmount(Integer movingContAmount) 
    {
        this.movingContAmount = movingContAmount;
    }

    public Integer getMovingContAmount() 
    {
        return movingContAmount;
    }
    public void setTransportIds(String transportIds) 
    {
        this.transportIds = transportIds;
    }

    public String getTransportIds() 
    {
        return transportIds;
    }
    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("shipmentId", getShipmentId())
            .append("logisticGroupId", getLogisticGroupId())
            .append("registerNo", getRegisterNo())
            .append("containerNo", getContainerNo())
            .append("containerStatus", getContainerStatus())
            .append("sztp", getSztp())
            .append("fe", getFe())
            .append("bookingNo", getBookingNo())
            .append("blNo", getBlNo())
            .append("sealNo", getSealNo())
            .append("consignee", getConsignee())
            .append("expiredDem", getExpiredDem())
            .append("wgt", getWgt())
            .append("vslNm", getVslNm())
            .append("voyNo", getVoyNo())
            .append("opeCode", getOpeCode())
            .append("loadingPort", getLoadingPort())
            .append("dischargePort", getDischargePort())
            .append("transportType", getTransportType())
            .append("emptyDepot", getEmptyDepot())
            .append("vgmChk", getVgmChk())
            .append("vgm", getVgm())
            .append("vgmPersonInfo", getVgmPersonInfo())
            .append("customDeclareNo", getCustomDeclareNo())
            .append("customStatus", getCustomStatus())
            .append("paymentStatus", getPaymentStatus())
            .append("processStatus", getProcessStatus())
            .append("doStatus", getDoStatus())
            .append("doReceivedTime", getDoReceivedTime())
            .append("userVerifyStatus", getUserVerifyStatus())
            .append("preorderPickup", getPreorderPickup())
            .append("movingContAmount", getMovingContAmount())
            .append("transportIds", getTransportIds())
            .append("status", getStatus())
            .append("bay", getBay())
            .append("roww", getRow())
            .append("tier", getTier())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
