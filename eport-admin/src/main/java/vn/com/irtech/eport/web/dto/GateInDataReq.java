package vn.com.irtech.eport.web.dto;

import java.io.Serializable;

public class GateInDataReq  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String truckNo;
	
	private String chassisNo;
	
	private String gatePass;
	
	private Integer loadableWgt;
	
	private Integer weight;
	
	private Boolean sendOption;
	
	private Boolean receiveOption;
	
	private String containerSend1;
	
	private String containerSend2;
	
	private String yardPosition1;
	
	private String yardPosition2;
	
	private String containerReceive1;
	
	private String containerReceive2;
	
	private String refFlg1;
	
	private String refFlg2;
	
	private String refNo1;
	
	private String refNo2;

	public String getTruckNo() {
		return truckNo;
	}

	public void setTruckNo(String truckNo) {
		this.truckNo = truckNo;
	}

	public String getChassisNo() {
		return chassisNo;
	}

	public void setChassisNo(String chassisNo) {
		this.chassisNo = chassisNo;
	}

	public String getGatePass() {
		return gatePass;
	}

	public void setGatePass(String gatePass) {
		this.gatePass = gatePass;
	}

	public Integer getLoadableWgt() {
		return loadableWgt;
	}

	public void setLoadableWgt(Integer loadableWgt) {
		this.loadableWgt = loadableWgt;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Boolean getSendOption() {
		return sendOption;
	}

	public void setSendOption(Boolean sendOption) {
		this.sendOption = sendOption;
	}

	public Boolean getReceiveOption() {
		return receiveOption;
	}

	public void setReceiveOption(Boolean receiveOption) {
		this.receiveOption = receiveOption;
	}

	public String getContainerSend1() {
		return containerSend1;
	}

	public void setContainerSend1(String containerSend1) {
		this.containerSend1 = containerSend1;
	}

	public String getContainerSend2() {
		return containerSend2;
	}

	public void setContainerSend2(String containerSend2) {
		this.containerSend2 = containerSend2;
	}

	public String getYardPosition1() {
		return yardPosition1;
	}

	public void setYardPosition1(String yardPosition1) {
		this.yardPosition1 = yardPosition1;
	}

	public String getYardPosition2() {
		return yardPosition2;
	}

	public void setYardPosition2(String yardPosition2) {
		this.yardPosition2 = yardPosition2;
	}

	public String getContainerReceive1() {
		return containerReceive1;
	}

	public void setContainerReceive1(String containerReceive1) {
		this.containerReceive1 = containerReceive1;
	}

	public String getContainerReceive2() {
		return containerReceive2;
	}

	public void setContainerReceive2(String containerReceive2) {
		this.containerReceive2 = containerReceive2;
	}

	public String getRefFlg1() {
		return refFlg1;
	}

	public void setRefFlg1(String refFlg1) {
		this.refFlg1 = refFlg1;
	}

	public String getRefFlg2() {
		return refFlg2;
	}

	public void setRefFlg2(String refFlg2) {
		this.refFlg2 = refFlg2;
	}

	public String getRefNo1() {
		return refNo1;
	}

	public void setRefNo1(String refNo1) {
		this.refNo1 = refNo1;
	}

	public String getRefNo2() {
		return refNo2;
	}

	public void setRefNo2(String refNo2) {
		this.refNo2 = refNo2;
	}
	
}
