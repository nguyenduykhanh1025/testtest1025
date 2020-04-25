package vn.com.irtech.eport.carrier.domain;

import vn.com.irtech.eport.common.annotation.Excel;

/**
 * Container Infomation Object container_info
 * 
 * @author Admin
 * @date 2020-04-16
 */
public class ContainerInfo extends ContainerInfoBase {
	private static final long serialVersionUID = 1L;
	
	/** remark */
	@Excel(name = "Remark")
	private String remark;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
