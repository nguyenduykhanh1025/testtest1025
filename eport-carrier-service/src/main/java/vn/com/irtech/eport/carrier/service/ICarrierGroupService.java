package vn.com.irtech.eport.carrier.service;

import java.util.List;
import vn.com.irtech.eport.carrier.domain.CarrierGroup;

/**
 * Carrier GroupService Interface
 * 
 * @author irtech
 * @date 2020-04-06
 */
public interface ICarrierGroupService 
{
    /**
     * Get Carrier Group
     * 
     * @param id Carrier GroupID
     * @return Carrier Group
     */
    public CarrierGroup selectCarrierGroupById(Long id);

    /**
     * Get Carrier Group List
     * 
     * @param carrierGroup Carrier Group
     * @return Carrier Group List
     */
    public List<CarrierGroup> selectCarrierGroupList(CarrierGroup carrierGroup);

    /**
     * Add Carrier Group
     * 
     * @param carrierGroup Carrier Group
     * @return result
     */
    public int insertCarrierGroup(CarrierGroup carrierGroup);

    /**
     * Update Carrier Group
     * 
     * @param carrierGroup Carrier Group
     * @return result
     */
    public int updateCarrierGroup(CarrierGroup carrierGroup);

    /**
     * Batch Delete Carrier Group
     * 
     * @param ids Entity Ids
     * @return result
     */
    public int deleteCarrierGroupByIds(String ids);

    /**
     * Delete Carrier Group
     * 
     * @param id Carrier GroupID
     * @return result
     */
    public int deleteCarrierGroupById(Long id);
}