package vn.com.irtech.eport.logistic.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.com.irtech.eport.common.core.text.Convert;
import vn.com.irtech.eport.logistic.domain.PickupHistory;
import vn.com.irtech.eport.logistic.form.Pickup;
import vn.com.irtech.eport.logistic.form.PickupHistoryDetail;
import vn.com.irtech.eport.logistic.form.PickupHistoryForm;
import vn.com.irtech.eport.logistic.mapper.PickupHistoryMapper;
import vn.com.irtech.eport.logistic.service.IPickupHistoryService;

/**
 * Pickup historyService Business Processing
 * 
 * @author baohv
 * @date 2020-06-27
 */
@Service
public class PickupHistoryServiceImpl implements IPickupHistoryService 
{
    @Autowired
    private PickupHistoryMapper pickupHistoryMapper;

    /**
     * Get Pickup history
     * 
     * @param id Pickup historyID
     * @return Pickup history
     */
    @Override
    public PickupHistory selectPickupHistoryById(Long id)
    {
        return pickupHistoryMapper.selectPickupHistoryById(id);
    }

    /**
     * Get list Pickup history without yard position
     * 
     * @return Pickup history List
     */
    @Override
    public List<PickupHistory> selectPickupHistoryWithoutYardPostion(Map<String, String> searchParams){
    	return pickupHistoryMapper.selectPickupHistoryWithoutYardPostion(searchParams);
    }
    
    /**
     * Get list Pickup history has yard position
     * 
     * @return Pickup history List
     */
    @Override
    public List<PickupHistory> selectPickupHistoryHasYardPostion(Map<String, String> searchParams){
    	return pickupHistoryMapper.selectPickupHistoryHasYardPostion(searchParams);
    }
    
    /**
     * Add Pickup history
     * 
     * @param pickupHistory Pickup history
     * @return result
     */
    @Override
    public int insertPickupHistory(PickupHistory pickupHistory)
    {
        return pickupHistoryMapper.insertPickupHistory(pickupHistory);
    }

    /**
     * Update Pickup history
     * 
     * @param pickupHistory Pickup history
     * @return result
     */
    @Override
    public int updatePickupHistory(PickupHistory pickupHistory)
    {
        return pickupHistoryMapper.updatePickupHistory(pickupHistory);
    }

    /**
     * Delete Pickup history By ID
     * 
     * @param ids Entity ID
     * @return result
     */
    @Override
    public int deletePickupHistoryByIds(String ids)
    {
        return pickupHistoryMapper.deletePickupHistoryByIds(Convert.toStrArray(ids));
    }

    /**
     * Delete Pickup history
     * 
     * @param id Pickup historyID
     * @return result
     */
    @Override
    public int deletePickupHistoryById(Long id)
    {
        return pickupHistoryMapper.deletePickupHistoryById(id);
    }

    @Override
    public List<PickupHistory> selectPickupHistoryListForReport(PickupHistory pickupHistory) {
        return pickupHistoryMapper.selectPickupHistoryListForReport(pickupHistory);
    }

    @Override
    public List<PickupHistory> selectPickupHistoryListForHistory(PickupHistory pickupHistory) {
        return pickupHistoryMapper.selectPickupHistoryListForHistory(pickupHistory);
    }

    @Override
    public List<PickupHistory> selectPickupHistoryListForOmSupport(PickupHistory pickupHistory) {
        return pickupHistoryMapper.selectPickupHistoryListForOmSupport(pickupHistory);
    }
    
    @Override
    public List<PickupHistoryForm> selectPickupHistoryForDriver(@Param("userId") Long userId) {
        return pickupHistoryMapper.selectPickupHistoryForDriver(userId);
    }

    @Override
    public List<Pickup> selectPickupListByDriverId(Long driverId) {
        return pickupHistoryMapper.selectPickupListByDriverId(driverId);
    }

    @Override
    public PickupHistoryDetail selectPickupHistoryDetailById(@Param("driverId") Long driverId, @Param("pickupId") Long pickupId) {
        return pickupHistoryMapper.selectPickupHistoryDetailById(driverId, pickupId);
    }

    /**
     * Check pickup history exists
     * 
     * @param shipmentId
     * @param containerNo
     * @return  int
     */
    @Override
    public int checkPickupHistoryExists(Long shipmentId, String containerNo) {
        return pickupHistoryMapper.checkPickupHistoryExists(shipmentId, containerNo);
    }

    /**
     * Check possible pickup
     * 
     * @param driverId
     * @return Boolean
     */
    @Override
    public Boolean checkPossiblePickup(Long driverId, Integer serviceType) {
        List<Pickup> pickups = pickupHistoryMapper.selectPickupListByDriverId(driverId);
        // Empty can pick
        if (pickups.isEmpty()) {
            return true;
        }

        // Exceed max number 4, can't pick
        if (pickups.size() > 4) {
            return false;
        }

        // Check condition to pickup
        int countCont20 = 0;
        for (Pickup pickup: pickups) {
            if (pickup.getServiceType() == serviceType) {
                if ("20G0".equals(pickup.getSztp())) {
                    countCont20++;
                } else {
                    countCont20 += 2;
                }
            }
            
            // Is gate in but not confirm finish yet, can't pick
            if (pickup.getStatus() == 2) {
                return false;
            }
        }

        if (countCont20 > 1) {
            return false;
        }
        return true;
    }
}
