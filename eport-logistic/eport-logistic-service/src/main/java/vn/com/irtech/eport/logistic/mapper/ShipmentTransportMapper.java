package vn.com.irtech.eport.logistic.mapper;

import java.util.List;
import vn.com.irtech.eport.logistic.domain.ShipmentTransport;

/**
 * Thong tin dieu xeMapper Interface
 * 
 * @author irtech
 * @date 2020-05-26
 */
public interface ShipmentTransportMapper 
{
    /**
     * Get Thong tin dieu xe
     * 
     * @param id Thong tin dieu xeID
     * @return Thong tin dieu xe
     */
    public ShipmentTransport selectShipmentTransportById(Long id);

    /**
     * Get Thong tin dieu xe List
     * 
     * @param shipmentTransport Thong tin dieu xe
     * @return Thong tin dieu xe List
     */
    public List<ShipmentTransport> selectShipmentTransportList(ShipmentTransport shipmentTransport);

    /**
     * Add Thong tin dieu xe
     * 
     * @param shipmentTransport Thong tin dieu xe
     * @return Result
     */
    public int insertShipmentTransport(ShipmentTransport shipmentTransport);

    /**
     * Update Thong tin dieu xe
     * 
     * @param shipmentTransport Thong tin dieu xe
     * @return Result
     */
    public int updateShipmentTransport(ShipmentTransport shipmentTransport);

    /**
     * Delete Thong tin dieu xe
     * 
     * @param id Thong tin dieu xeID
     * @return result
     */
    public int deleteShipmentTransportById(Long id);

    /**
     * Batch Delete Thong tin dieu xe
     * 
     * @param ids IDs
     * @return result
     */
    public int deleteShipmentTransportByIds(String[] ids);
}
