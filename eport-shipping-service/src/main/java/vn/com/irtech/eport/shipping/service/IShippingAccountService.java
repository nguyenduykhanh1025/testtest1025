package vn.com.irtech.eport.shipping.service;

import java.util.List;
import vn.com.irtech.eport.shipping.domain.ShippingAccount;

/**
 * Shipping line accountService Interface
 * 
 * @author Irtech
 * @date 2020-04-03
 */
public interface IShippingAccountService 
{
    /**
     * Get Shipping line account
     * 
     * @param id Shipping line accountID
     * @return Shipping line account
     */
    public ShippingAccount selectShippingAccountById(Long id);

    /**
     * Get Shipping line account List
     * 
     * @param shippingAccount Shipping line account
     * @return Shipping line account List
     */
    public List<ShippingAccount> selectShippingAccountList(ShippingAccount shippingAccount);

    /**
     * Add Shipping line account
     * 
     * @param shippingAccount Shipping line account
     * @return result
     */
    public int insertShippingAccount(ShippingAccount shippingAccount);

    /**
     * Update Shipping line account
     * 
     * @param shippingAccount Shipping line account
     * @return result
     */
    public int updateShippingAccount(ShippingAccount shippingAccount);

    /**
     * Batch Delete Shipping line account
     * 
     * @param ids Entity Ids
     * @return result
     */
    public int deleteShippingAccountByIds(String ids);

    /**
     * Delete Shipping line account
     * 
     * @param id Shipping line accountID
     * @return result
     */
    public int deleteShippingAccountById(Long id);
}
