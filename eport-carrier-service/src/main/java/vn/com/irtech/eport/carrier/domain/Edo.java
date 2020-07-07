package vn.com.irtech.eport.carrier.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import vn.com.irtech.eport.common.annotation.Excel;
import vn.com.irtech.eport.common.core.domain.BaseEntity;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Exchange Delivery Order Object edo
 * 
 * @author ruoyi
 * @date 2020-06-26
 */
public class Edo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** ID Nhan Vien Hang Tau */
    @Excel(name = "ID Nhan Vien Hang Tau")
    private Long carrierId;

    /** Ma Hang Tau */
    @Excel(name = "Ma Hang Tau")
    private String carrierCode;

    /** So Lenh (Optional) */
    @Excel(name = "So Lenh (Optional)")
    private String orderNumber;

    /** So B/L */
    @Excel(name = "So B/L")
    private String billOfLading;

    /** Don Vi Khai Thac (Optional) */
    @Excel(name = "Don Vi Khai Thac (Optional)")
    private String businessUnit;

    /** Chu Hang */
    @Excel(name = "Chu Hang")
    private String consignee;

    /** So Cont */
    @Excel(name = "So Cont")
    private String containerNumber;

    /** Han Lenh */
    @Excel(name = "Han Lenh", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expiredDem;

    /** Noi Ha Rong */
    @Excel(name = "Noi Ha Rong")
    private String emptyContainerDepot;

    /** So Ngay Mien Luu Vo Cont */
    @Excel(name = "So Ngay Mien Luu Vo Cont")
    private Integer detFreeTime;

    /** Ma Bao Mat (optional) */
    @Excel(name = "Ma Bao Mat (optional)")
    private String secureCode;

    /** Ngay Release */
    @Excel(name = "Ngay Release", width = 30, dateFormat = "yyyy-MM-dd")
    private Date releaseDate;

    /** Tau */
    @Excel(name = "Tau")
    private String vessel;

    /** Chuyen */
    @Excel(name = "Chuyen")
    private String voyNo;

    /** eDO Status (new, đã khai báo, đã làm lệnh, gatein, gateout) */
    @Excel(name = "eDO Status (new, đã khai báo, đã làm lệnh, gatein, gateout)")
    private String status;

    /** Release Status (0:tren bai cang, 1: released) */
    @Excel(name = "Release Status (0:tren bai cang, 1: released)")
    private String releaseStatus;

    /** Nguon Tao: web, edi, api */
    @Excel(name = "Nguon Tao: web, edi, api")
    private String createSource;

    /** Delete Flag */
    @Excel(name = "Delete Flag")
    private int delFlg;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date fromDate;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date toDate;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCarrierId(Long carrierId) 
    {
        this.carrierId = carrierId;
    }

    public Long getCarrierId() 
    {
        return carrierId;
    }
    public void setCarrierCode(String carrierCode) 
    {
        this.carrierCode = carrierCode;
    }

    public String getCarrierCode() 
    {
        return carrierCode;
    }
    public void setOrderNumber(String orderNumber) 
    {
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() 
    {
        return orderNumber;
    }
    public void setBillOfLading(String billOfLading) 
    {
        this.billOfLading = billOfLading;
    }

    public String getBillOfLading() 
    {
        return billOfLading;
    }
    public void setBusinessUnit(String businessUnit) 
    {
        this.businessUnit = businessUnit;
    }

    public String getBusinessUnit() 
    {
        return businessUnit;
    }
    public void setConsignee(String consignee) 
    {
        this.consignee = consignee;
    }

    public String getConsignee() 
    {
        return consignee;
    }
    public void setContainerNumber(String containerNumber) 
    {
        this.containerNumber = containerNumber;
    }

    public String getContainerNumber() 
    {
        return containerNumber;
    }
    public void setExpiredDem(Date expiredDem) 
    {
        this.expiredDem = expiredDem;
    }

    public Date getExpiredDem() 
    {
        return expiredDem;
    }
    public void setEmptyContainerDepot(String emptyContainerDepot) 
    {
        this.emptyContainerDepot = emptyContainerDepot;
    }

    public String getEmptyContainerDepot() 
    {
        return emptyContainerDepot;
    }
    public void setDetFreeTime(Integer detFreeTime) 
    {
        this.detFreeTime = detFreeTime;
    }

    public Integer getDetFreeTime() 
    {
        return detFreeTime;
    }
    public void setSecureCode(String secureCode) 
    {
        this.secureCode = secureCode;
    }

    public String getSecureCode() 
    {
        return secureCode;
    }
    public void setReleaseDate(Date releaseDate) 
    {
        this.releaseDate = releaseDate;
    }

    public Date getReleaseDate() 
    {
        return releaseDate;
    }
    public void setVessel(String vessel) 
    {
        this.vessel = vessel;
    }

    public String getVessel() 
    {
        return vessel;
    }
    public void setVoyNo(String voyNo) 
    {
        this.voyNo = voyNo;
    }

    public String getVoyNo() 
    {
        return voyNo;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setReleaseStatus(String releaseStatus) 
    {
        this.releaseStatus = releaseStatus;
    }

    public String getReleaseStatus() 
    {
        return releaseStatus;
    }
    public void setCreateSource(String createSource) 
    {
        this.createSource = createSource;
    }

    public String getCreateSource() 
    {
        return createSource;
    }
    public void setDelFlg(int delFlg) 
    {
        this.delFlg = delFlg;
    }

    public int getDelFlg() 
    {
        return delFlg;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getToDate() {
        return toDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("carrierId", getCarrierId())
            .append("carrierCode", getCarrierCode())
            .append("orderNumber", getOrderNumber())
            .append("billOfLading", getBillOfLading())
            .append("businessUnit", getBusinessUnit())
            .append("consignee", getConsignee())
            .append("containerNumber", getContainerNumber())
            .append("expiredDem", getExpiredDem())
            .append("emptyContainerDepot", getEmptyContainerDepot())
            .append("detFreeTime", getDetFreeTime())
            .append("secureCode", getSecureCode())
            .append("releaseDate", getReleaseDate())
            .append("vessel", getVessel())
            .append("voyNo", getVoyNo())
            .append("status", getStatus())
            .append("releaseStatus", getReleaseStatus())
            .append("createSource", getCreateSource())
            .append("remark", getRemark())
            .append("delFlg", getDelFlg())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("fromDate", getFromDate())
            .append("toDate", getToDate())
            .toString();
    }
}
