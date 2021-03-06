package vn.com.irtech.eport.carrier.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import vn.com.irtech.eport.common.annotation.Excel;
import vn.com.irtech.eport.common.core.domain.BaseEntity;

/**
 * Container Infomation Object container_info
 * 
 * @author Admin
 * @date 2020-04-16
 */
public class ContainerInfoBase extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long cntrId;
   
    /** Class Mode */
    @Excel(name = "CLASS MODE")
    private String classMode;

    /** Container Number */
    @Excel(name = "CONTAINER NO")
    private String cntrNo;

    /** Size/type */
    @Excel(name = "SZTP")
    private String sztp2;

    /** F/E */
    @Excel(name = "F/E")
    private String fe;

    /** Operator */
    @Excel(name = "OPR")
    private String ptnrCode;

    /** Cargo Type */
    @Excel(name = "CARGO TYPE")
    private String cargoType;

    
    /** Vessel Name */
    @Excel(name = "VESSEL NAME")
    private String vesselName;

     /** Vessel Code */
    @Excel(name = "VESSEL CODE")
    private String vesselCode;


    
    
    /** yard location - Block */
    private String block;

    /** yard location - Bay */
    private String bay;

    /** yard location - Row */
    private String roww;

    /** yard location - Tier */
    private String tier;
  
    private Long wgt;

   

    /** Shipper/consignee */
    private String consignee;

    /** Booking Number */
    @Excel(name = "BOOKING NO")
    private String bookingNo;

    /** BL number */
    @Excel(name = "BL NO")
    private String blNo;

     /** pol */
     @Excel(name = "POL")
     private String pol;

      /** Pod */
    @Excel(name = "POD")
    private String pod;
    /** Seal Number */
    @Excel(name = "CARRIER SEAL")
    private String sealNo1;

    @Excel(name = "EXPORT SEAL")
    private String sealNo3;
    /** Gate mode for terminal in */
    private String gateMode;

    /** Trans Type In */
    @Excel(name = "TRANS TYPE IN")
    private String transTypeIn;
   
    /** Gate In Date */
    @Excel(name = "IN DATE")
    private String inDate;

    /** Trans Type Out */
    @Excel(name = "TRANS TYPE OUT")
    private String transTypeOut;
   
    /** Gate Out Date */
    @Excel(name = "OUT DATE")
    private String outDate;

     /** Container State  (S: Stacking, D:Delivered) */
     @Excel(name = "STATUS")
     private String cntrState;

    private String vgm;
    @Excel(name = "YARD POSITION")
    private String yardPosition;


   


     
    public void setTransTypeIn(String transTypeIn) 
    {
        this.transTypeIn = transTypeIn;
    }
  
    public String getTransTypeIn() 
    {
        return transTypeIn;
    }
    public String getYardPosition()
    {
        return yardPosition;
    }
    
    public void setTransTypeOut(String transTypeOut) 
    {
        this.transTypeOut = transTypeOut;
    }

    public String getTransTypeOut() 
    {
        return transTypeOut;
    }
    public void setYardPosition(String yardPosition) {
        if(yardPosition != null)
        {
            yardPosition = yardPosition.replace("---", "");
        }
    	this.yardPosition = yardPosition;
    }

    /** SEARCH */
    private String toDate;

    private String fromDate;

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return this.toDate;
    }

    public String getFromDate() {
        return this.fromDate;
    }

    public void setCntrId(Long cntrId) 
    {
        this.cntrId = cntrId;
    }

    public Long getCntrId() 
    {
        return cntrId;
    }
    public void setCntrNo(String cntrNo) 
    {
        this.cntrNo = cntrNo;
    }

    public String getCntrNo() 
    {
        return cntrNo;
    }
    public void setSztp2(String sztp2) 
    {
        this.sztp2 = sztp2;
    }

    public String getSztp2() 
    {
        return sztp2;
    }
    public void setFe(String fe) 
    {
        this.fe = fe;
    }

    public String getFe() 
    {
        return fe;
    }
    public void setPtnrCode(String ptnrCode) 
    {
        this.ptnrCode = ptnrCode;
    }

    public String getPtnrCode() 
    {
        return ptnrCode;
    }
    public void setBlock(String block) 
    {
        this.block = block;
    }

    public String getBlock() 
    {
        return block;
    }
    public void setBay(String bay) 
    {
        this.bay = bay;
    }

    public String getBay() 
    {
        return bay;
    }
    public void setRoww(String roww) 
    {
        this.roww = roww;
    }

    public String getRoww() 
    {
        return roww;
    }
    public void setTier(String tier) 
    {
        this.tier = tier;
    }

    public String getTier() 
    {
        return tier;
    }
    public void setWgt(Long wgt) 
    {
        this.wgt = wgt;
    }

    public Long getWgt() 
    {
        return wgt;
    }
    
    public void setConsignee(String consignee) 
    {
        this.consignee = consignee;
    }

    public String getConsignee() 
    {
        return consignee;
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
    public void setSealNo1(String sealNo1) 
    {
        this.sealNo1 = sealNo1;
    }

    public String getSealNo1() 
    {
        return sealNo1;
    }
    public void setGateMode(String gateMode) 
    {
        this.gateMode = gateMode;
    }

    public void setSealNo3(String sealNo3) 
    {
        this.sealNo3 = sealNo3;
    }

    public String getSealNo3() 
    {
        return sealNo3;
    }
    public String getGateMode() 
    {
        return gateMode;
    }

    public void setInDate(String inDate) 
    {
        this.inDate = inDate;
    }

    public String getInDate() 
    {
        return inDate;
    }
    public void setOutDate(String outDate) 
    {
        this.outDate = outDate;
    }

    public String getOutDate() 
    {
        return outDate;
    }
    public void setVgm(String vgm) 
    {
        this.vgm = vgm;
    }

    public String getVgm() 
    {
        return vgm;
    }
    public void setCntrState(String cntrState) 
    {
        this.cntrState = cntrState;
    }

    public String getCntrState() 
    {
        return cntrState;
    }

 
    public void setClassMode(String classMode) 
    {
        this.classMode = classMode;
    }

    public String getClassMode() 
    {
        return classMode;
    }

    public void setCargoType(String cargoType) 
    {
        this.cargoType = cargoType;
    }

    public String getCargoType() 
    {
        return cargoType;
    }

    public void setVesselCode(String vesselCode) 
    {
        this.vesselCode = vesselCode;
    }

    public String getVesselCode() 
    {
        return vesselCode;
    }

    public void setVesselName(String vesselName) 
    {
        this.vesselName = vesselName;
    }

    public String getVesselName() 
    {
        return vesselName;
    }

    public void setPol(String pol) 
    {
        this.pol = pol;
    }

    public String getPol() 
    {
        return pol;
    }

    public void setPod(String pod) 
    {
        this.pod = pod;
    }

    public String getPod() 
    {
        return pod;
    }
    
	@Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("cntrId", getCntrId())
            .append("cntrNo", getCntrNo())
            .append("sztp2", getSztp2())
            .append("fe", getFe())
            .append("ptnrCode", getPtnrCode())
            .append("block", getBlock())
            .append("bay", getBay())
            .append("roww", getRoww())
            .append("tier", getTier())
            .append("wgt", getWgt())
            .append("consignee", getConsignee())
            .append("bookingNo", getBookingNo())
            .append("blNo", getBlNo())
            .append("sealNo1", getSealNo1())
            .append("gateMode", getGateMode())
            .append("inDate", getInDate())
            .append("outDate", getOutDate())
            .append("vgm", getVgm())
            .append("cntrState", getCntrState())
            .append("remark", getRemark())
            .append("classMode", getClassMode())
            .append("cargoType", getCargoType())
            .append("vesselCode", getVesselCode())
            .append("vesselName", getVesselName())
            .append("pol", getPol())
            .append("pod", getPod())
            .toString();
    }
}
