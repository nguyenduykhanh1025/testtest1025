package vn.com.irtech.eport.carrier.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.com.irtech.eport.carrier.domain.CarrierAccount;
import vn.com.irtech.eport.carrier.service.ICarrierAccountService;
import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.core.controller.BaseController;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.page.TableDataInfo;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.common.utils.poi.ExcelUtil;
import vn.com.irtech.eport.framework.config.MailConfig;
import vn.com.irtech.eport.framework.shiro.service.SysPasswordService;
import vn.com.irtech.eport.framework.util.ShiroUtils;

/**
 * Carrier AccountController
 * 
 * @author irtech
 * @date 2020-04-04
 */
@Controller
@RequestMapping("/carrier/account")
public class CarrierAccountController extends BaseController
{
    private String prefix = "carrier/account";

    @Autowired
    private ICarrierAccountService carrierAccountService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private MailConfig mailService;

    @RequiresPermissions("carrier:account:view")
    @GetMapping()
    public String account()
    {
        return prefix + "/account";
    }

    /**
     * Get Carrier Account List
     */
    @RequiresPermissions("carrier:account:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(CarrierAccount carrierAccount)
    {
        startPage();
        List<CarrierAccount> list = carrierAccountService.selectCarrierAccountList(carrierAccount);
        return getDataTable(list);
    }

    /**
     * Export Carrier Account List
     */
    @RequiresPermissions("carrier:account:export")
    @Log(title = "Carrier Account", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(CarrierAccount carrierAccount)
    {
        List<CarrierAccount> list = carrierAccountService.selectCarrierAccountList(carrierAccount);
        ExcelUtil<CarrierAccount> util = new ExcelUtil<CarrierAccount>(CarrierAccount.class);
        return util.exportExcel(list, "account");
    }

    /**
     * Add Carrier Account
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * Add or Update Carrier Account
     */
    @RequiresPermissions("carrier:account:add")
    @Log(title = "Carrier Account", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CarrierAccount carrierAccount)
    {
        if (carrierAccountService.checkEmailUnique(carrierAccount.getEmail()).equals("1")) {
            return error("Email already exist");
        }
        carrierAccount.setSalt(ShiroUtils.randomSalt());
        carrierAccount.setPassword(passwordService.encryptPassword(carrierAccount.getEmail()
        , carrierAccount.getPassword(), carrierAccount.getSalt()));
        Map<String, Object> variables = new HashMap<>();
				variables.put("username", "username");
        variables.put("password", "password");
        try {
        mailService.prepareAndSend("Title", "tronghieu8531@gmail.com", variables);
          
        } catch (Exception e) {
        }
        return toAjax(carrierAccountService.insertCarrierAccount(carrierAccount));
    }

    /**
     * Update Carrier Account
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        CarrierAccount carrierAccount = carrierAccountService.selectCarrierAccountById(id);
        mmap.put("carrierAccount", carrierAccount);
        return prefix + "/edit";
    }

    /**
     * Update Save Carrier Account
     */
    @RequiresPermissions("carrier:account:edit")
    @Log(title = "Carrier Account", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(CarrierAccount carrierAccount)
    {
        return toAjax(carrierAccountService.updateCarrierAccount(carrierAccount));
    }

    /**
     * Delete Carrier Account
     */
    @RequiresPermissions("carrier:account:remove")
    @Log(title = "Carrier Account", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(carrierAccountService.deleteCarrierAccountByIds(ids));
    }
}
