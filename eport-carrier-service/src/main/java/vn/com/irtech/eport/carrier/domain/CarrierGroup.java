package vn.com.irtech.eport.carrier.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import vn.com.irtech.eport.common.annotation.Excel;
import vn.com.irtech.eport.common.core.domain.BaseEntity;

/**
 * Carrier Group Object carrier_group
 * 
 * @author irtech
 * @date 2020-04-06
 */
public class CarrierGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** Group Code */
    @Excel(name = "Group Code")
    private String groupCode;

    /** Group Name */
    @Excel(name = "Group Name")
    private String groupName;

    /** Operate Codes */
    @Excel(name = "Operate Codes")
    private String operateCode;

    /** Main Email */
    @Excel(name = "Main Email")
    private String mainEmail;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setGroupCode(String groupCode) 
    {
        this.groupCode = groupCode;
    }

    public String getGroupCode() 
    {
        return groupCode;
    }
    public void setGroupName(String groupName) 
    {
        this.groupName = groupName;
    }

    public String getGroupName() 
    {
        return groupName;
    }
    public void setOperateCode(String operateCode) 
    {
        this.operateCode = operateCode;
    }

    public String getOperateCode() 
    {
        return operateCode;
    }
    public void setMainEmail(String mainEmail) 
    {
        this.mainEmail = mainEmail;
    }

    public String getMainEmail() 
    {
        return mainEmail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("groupCode", getGroupCode())
            .append("groupName", getGroupName())
            .append("operateCode", getOperateCode())
            .append("mainEmail", getMainEmail())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}