package vn.com.irtech.eport.carrier.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.carrier.domain.CarrierGroup;
import vn.com.irtech.eport.carrier.service.ICarrierGroupService;
import vn.com.irtech.eport.common.core.controller.BaseController;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.utils.poi.ExcelUtil;
import vn.com.irtech.eport.framework.util.ShiroUtils;
import vn.com.irtech.eport.common.core.page.TableDataInfo;

/**
 * Carrier GroupController
 * 
 * @author irtech
 * @date 2020-04-06
 */
@Controller
@RequestMapping("/carrier/group")
public class CarrierGroupController extends BaseController
{
    private String prefix = "carrier/group";

    @Autowired
    private ICarrierGroupService carrierGroupService;

    @RequiresPermissions("carrier:group:view")
    @GetMapping()
    public String account()
    {
        return prefix + "/group";
    }

    /**
     * Get Carrier Group List
     */
    @RequiresPermissions("carrier:group:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(CarrierGroup carrierGroup)
    {
        startPage();
        List<CarrierGroup> list = carrierGroupService.selectCarrierGroupList(carrierGroup);
        return getDataTable(list);
    }

    /**
     * Export Carrier Group List
     */
    @RequiresPermissions("carrier:group:export")
    @Log(title = "Carrier Group", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(CarrierGroup carrierGroup)
    {
        List<CarrierGroup> list = carrierGroupService.selectCarrierGroupList(carrierGroup);
        ExcelUtil<CarrierGroup> util = new ExcelUtil<CarrierGroup>(CarrierGroup.class);
        return util.exportExcel(list, "account");
    }

    /**
     * Add Carrier Group
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * Add or Update Carrier Group
     */
    @RequiresPermissions("carrier:group:add")
    @Log(title = "Carrier Group", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CarrierGroup carrierGroup)
    {
        carrierGroup.setCreateBy(ShiroUtils.getSysUser().getUserName());
        return toAjax(carrierGroupService.insertCarrierGroup(carrierGroup));
    }

    /**
     * Update Carrier Group
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        CarrierGroup carrierGroup = carrierGroupService.selectCarrierGroupById(id);
        mmap.put("carrierGroup", carrierGroup);
        return prefix + "/edit";
    }

    /**
     * Update Save Carrier Group
     */
    @RequiresPermissions("carrier:group:edit")
    @Log(title = "Carrier Group", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(CarrierGroup carrierGroup)
    {
    	carrierGroup.setCreateBy(ShiroUtils.getSysUser().getUserName());
        return toAjax(carrierGroupService.updateCarrierGroup(carrierGroup));
    }

    /**
     * Delete Carrier Group
     */
    @RequiresPermissions("carrier:group:remove")
    @Log(title = "Carrier Group", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(carrierGroupService.deleteCarrierGroupByIds(ids));
    }

    /**
     * Search Carrier Group Name
     */
    @RequestMapping("/searchGroupNameByKeyword")
    @ResponseBody
    public List<JSONObject> searchGroupNameByKeyword(String keyword) {
        CarrierGroup carrierGroup = new CarrierGroup();
        carrierGroup.setGroupName(keyword);
        List<CarrierGroup> carrierGroups = carrierGroupService.selectCarrierGroupListByName(carrierGroup);
        List<JSONObject> result = new ArrayList<>();
        int limit = 0; 
		for (CarrierGroup i : carrierGroups) {
			JSONObject json = new JSONObject();
			json.put("id", i.getId());
			json.put("text", i.getGroupName());
            result.add(json);
            limit++;
            if (limit == 5) {
                break;
            }
		}
        return result;
    }

    @RequestMapping("/getGroupNameById")
    @ResponseBody
    public String getGroupNameById(long id) {
        CarrierGroup carrierGroup = carrierGroupService.selectCarrierGroupById(id);
        return carrierGroup.getGroupName();
    }
}
