package vn.com.irtech.eport.logistic.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import vn.com.irtech.eport.logistic.domain.LogisticGroup;

/**
 * Logistic GroupService Interface
 * 
 * @author admin
 * @date 2020-05-07
 */
public interface ILogisticGroupService 
{
    /**
     * Get Logistic Group
     * 
     * @param id Logistic GroupID
     * @return Logistic Group
     */
    public LogisticGroup selectLogisticGroupById(Long id);

    /**
     * Get Logistic Group List
     * 
     * @param logisticGroup Logistic Group
     * @return Logistic Group List
     */
    public List<LogisticGroup> selectLogisticGroupList(LogisticGroup logisticGroup);

    /**
     * Add Logistic Group
     * 
     * @param logisticGroup Logistic Group
     * @return result
     */
    public int insertLogisticGroup(LogisticGroup logisticGroup);

    /**
     * Update Logistic Group
     * 
     * @param logisticGroup Logistic Group
     * @return result
     */
    public int updateLogisticGroup(LogisticGroup logisticGroup);

    /**
     * Batch Delete Logistic Group
     * 
     * @param ids Entity Ids
     * @return result
     */
    public int deleteLogisticGroupByIds(String ids);
    public int updateDelFlagLogisticGroupByIds(String ids);

    /**
     * Delete Logistic Group
     * 
     * @param id Logistic GroupID
     * @return result
     */
    public int deleteLogisticGroupById(Long id);
    
    public List<LogisticGroup> selectLogisticGroupListByName(LogisticGroup logisticGroup);
    
    /**
     * Check delegate permission
     * 
     * @param consigneeTaxCode
     * @param logisticTaxCode
     * @return	int
     */
    public int checkDelegatePermission(@Param("consigneeTaxCode") String consigneeTaxCode, @Param("logisticTaxCode") String logisticTaxCode, @Param("delegateType") String delegateType);
}
