package vn.com.irtech.api.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import vn.com.irtech.api.entity.ProcessOrderEntity;
import vn.com.irtech.api.entity.ShipmentDetailEntity;
import vn.com.irtech.api.entity.ShipmentEntity;
@Mapper
public interface ShipmentDetailDao extends BaseMapper<ShipmentDetailEntity> {
    public List<ShipmentDetailEntity> selectShipmentDetailsByBLNo(String blNo);
    
    public ShipmentDetailEntity selectShipmentDetailByContNo(@Param("blNo") String blNo, @Param("containerNo") String containerNo);
    
    public List<ShipmentDetailEntity> selectCoordinateOfContainers(String blNo);
    
	public List<String> selectVesselCodeList();
	
	public List<String> selectPODList(ShipmentDetailEntity shipmentDetailEntity);
	
	public List<String> selectConsigneeList();
	
	public List<String> selectVoyageNoListByVesselCode(String vesselCode);
	
	public List<String> selectOpeCodeList();
	
	public ShipmentEntity getGroupNameByTaxCode(String taxCode);
	
	public List<String> checkContReservedByContainerNos(String[] containerNos);
	
	public Integer getCountContByBlNo(String blNo);
	
	public ShipmentEntity getOpeCodeCatosByBlNo(String blNo);
	
	public Boolean checkCustomStatus(@Param("containerNo") String containerNo, @Param("voyNo") String voyNo);
	
	public List<String> selectVesselCodeBerthPlan(String opeCode);
	
	public String getYearByVslCodeAndVoyNo(@Param("vesselCode") String vesselCode, @Param("voyNo") String voyNo);
	
	public List<String> selectOpeCodeListInBerthPlan();
	
	public List<ShipmentDetailEntity> selectVesselVoyageBerthPlan(String opeCode);
	
	public Integer checkBookingNoForSendFReceiveE(@Param("bookingNo") String bookingNo, @Param("fe") String fe);
	
	public ShipmentDetailEntity getInforSendFReceiveE(ShipmentDetailEntity shipmentDetailEntity);
	
	public List<Date> getIndexContMasterForSSRByContainerNo(String containerNo);
	
	public List<ShipmentDetailEntity> getIndexBooking(ShipmentDetailEntity shipmentDetailEntity);
	
	public ShipmentDetailEntity getLocationForReceiveF(@Param("blNo") String blNo, @Param("containerNo") String containerNo);
	
	public String checkContainerStatus(ShipmentDetailEntity shipmentDetailEntity);
	
	public List<String> getBlockList(String keyword);
	
	public List<String> getAreaList(String keyword);
	
	public ShipmentDetailEntity checkContReserved(ShipmentDetailEntity shipmentDetailEntity);
	
	public Integer checkTheNumberOfContainersNotOrderedForReceiveContEmpty(@Param("bookingNo") String bookingNo, @Param("sztp") String sztp);
	
	public Integer checkTheNumberOfContainersNotOrderedForSendContFull(@Param("bookingNo") String bookingNo, @Param("sztp") String sztp);
	
	/**
	 * Select Consignee And TaxCode List
	 * 
	 * @param shipmentEntity
	 * @return	List<ShipmentEntity>
	 */
	public List<ShipmentEntity> selectConsigneeTaxCode(ShipmentEntity shipmentEntity);
	
	/**
	 * Get sztp by container no from table TB_MASTER
	 * 
	 * @param containerNo
	 * @return String
	 */
	public String getSztpByContainerNoMaster(String containerNo);
	
	/**
	 * Get sztp by container no from table TB_INVENTORY
	 * 
	 * @param containerNo
	 * @return
	 */
	public String getSztpByContainerNoInventory(String containerNo);
	
	/**
	 * Get tax code by snm group name
	 * 
	 * @param consignee
	 * @return String
	 */
	public String getTaxCodeBySnmGroupName(String consignee);
}