package vn.com.irtech.eport.carrier.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import vn.com.irtech.eport.carrier.domain.Edo;

/**
 * Exchange Delivery OrderService Interface
 * 
 * @author ruoyi
 * @date 2020-06-26
 */
public interface IEdoService 
{
    /**
     * Get Exchange Delivery Order
     * 
     * @param id Exchange Delivery OrderID
     * @return Exchange Delivery Order
     */
    public Edo selectEdoById(Long id);

    /**
     * Get Exchange Delivery Order List
     * 
     * @param edo Exchange Delivery Order
     * @return Exchange Delivery Order List
     */
    public List<Edo> selectEdoList(Edo edo);

    /**
     * Add Exchange Delivery Order
     * 
     * @param edo Exchange Delivery Order
     * @return result
     */
    public int insertEdo(Edo edo);

    /**
     * Update Exchange Delivery Order
     * 
     * @param edo Exchange Delivery Order
     * @return result
     */
    public int updateEdo(Edo edo);

    /**
     * Batch Delete Exchange Delivery Order
     * 
     * @param ids Entity Ids
     * @return result
     */
    public int deleteEdoByIds(String ids);

    /**
     * Delete Exchange Delivery Order
     * 
     * @param id Exchange Delivery OrderID
     * @return result
     */
    public int deleteEdoById(Long id);

    public Edo checkContainerAvailable(@Param("container") String cont,@Param("billNo") String billNo);
    
    public String getOpeCodeByBlNo(String blNo);

    
    public  List<Edo> readEdi(String[] text);

    public Long getCountContainerAmountByBlNo(String blNo);
}