package vn.com.irtech.eport.logistic.service.impl;

import java.util.List;
import vn.com.irtech.eport.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.irtech.eport.logistic.mapper.DriverAccountMapper;
import vn.com.irtech.eport.logistic.domain.DriverAccount;
import vn.com.irtech.eport.logistic.service.IDriverAccountService;
import vn.com.irtech.eport.common.core.text.Convert;

/**
 * Driver login infoService Business Processing
 * 
 * @author ruoyi
 * @date 2020-05-19
 */
@Service
public class DriverAccountServiceImpl implements IDriverAccountService 
{
    @Override
	public int checkPhoneUnique(String phoneNumber) {
        int count = driverAccountMapper.checkPhoneUnique(phoneNumber);
        if (count > 0)
        {
            return count;
        }
        return 0;
	}

	@Autowired
    private DriverAccountMapper driverAccountMapper;

    /**
     * Get Driver login info
     * 
     * @param id Driver login infoID
     * @return Driver login info
     */
    @Override
    public DriverAccount selectDriverAccountById(Long id)
    {
        return driverAccountMapper.selectDriverAccountById(id);
    }

    /**
     * Get Driver login info List
     * 
     * @param driverAccount Driver login info
     * @return Driver login info
     */
    @Override
    public List<DriverAccount> selectDriverAccountList(DriverAccount driverAccount)
    {
        return driverAccountMapper.selectDriverAccountList(driverAccount);
    }

    /**
     * Add Driver login info
     * 
     * @param driverAccount Driver login info
     * @return result
     */
    @Override
    public int insertDriverAccount(DriverAccount driverAccount)
    {
        driverAccount.setCreateTime(DateUtils.getNowDate());
        return driverAccountMapper.insertDriverAccount(driverAccount);
    }

    /**
     * Update Driver login info
     * 
     * @param driverAccount Driver login info
     * @return result
     */
    @Override
    public int updateDriverAccount(DriverAccount driverAccount)
    {
        driverAccount.setUpdateTime(DateUtils.getNowDate());
        return driverAccountMapper.updateDriverAccount(driverAccount);
    }

    /**
     * Delete Driver login info By ID
     * 
     * @param ids Entity ID
     * @return result
     */
    @Override
    public int deleteDriverAccountByIds(String ids)
    {
        return driverAccountMapper.deleteDriverAccountByIds(Convert.toStrArray(ids));
    }

    /**
     * Delete Driver login info
     * 
     * @param id Driver login infoID
     * @return result
     */
    @Override
    public int deleteDriverAccountById(Long id)
    {
        return driverAccountMapper.deleteDriverAccountById(id);
    }
}