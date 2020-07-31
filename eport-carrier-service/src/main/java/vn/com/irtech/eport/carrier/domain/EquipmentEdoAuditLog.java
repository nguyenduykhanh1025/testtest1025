package vn.com.irtech.eport.carrier.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import vn.com.irtech.eport.common.annotation.Excel;
import vn.com.irtech.eport.common.core.domain.BaseEntity;

/**
 * eDO Audit Trail Log Object equipment_edo_audit_log
 * 
 * @author ruoyi
 * @date 2020-07-31
 */
public class EquipmentEdoAuditLog extends BaseEntity
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

    /** EDO ID */
    @Excel(name = "EDO ID")
    private Long edoId;

    /** Sequence Number 1->n */
    @Excel(name = "Sequence Number 1->n")
    private Long seqNo;

    /** Data Field Name */
    @Excel(name = "Data Field Name")
    private String fieldName;

    /** Old Value */
    @Excel(name = "Old Value")
    private String oldValue;

    /** New Value */
    @Excel(name = "New Value")
    private String newValue;

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
    public void setEdoId(Long edoId) 
    {
        this.edoId = edoId;
    }

    public Long getEdoId() 
    {
        return edoId;
    }
    public void setSeqNo(Long seqNo) 
    {
        this.seqNo = seqNo;
    }

    public Long getSeqNo() 
    {
        return seqNo;
    }
    public void setFieldName(String fieldName) 
    {
        this.fieldName = fieldName;
    }

    public String getFieldName() 
    {
        return fieldName;
    }
    public void setOldValue(String oldValue) 
    {
        this.oldValue = oldValue;
    }

    public String getOldValue() 
    {
        return oldValue;
    }
    public void setNewValue(String newValue) 
    {
        this.newValue = newValue;
    }

    public String getNewValue() 
    {
        return newValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("carrierId", getCarrierId())
            .append("carrierCode", getCarrierCode())
            .append("edoId", getEdoId())
            .append("seqNo", getSeqNo())
            .append("fieldName", getFieldName())
            .append("oldValue", getOldValue())
            .append("newValue", getNewValue())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
