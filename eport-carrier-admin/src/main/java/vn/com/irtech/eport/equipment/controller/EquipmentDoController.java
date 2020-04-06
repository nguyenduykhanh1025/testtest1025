package vn.com.irtech.eport.equipment.controller;

import java.util.List;
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
import vn.com.irtech.eport.equipment.domain.EquipmentDo;
import vn.com.irtech.eport.equipment.service.IEquipmentDoService;
import vn.com.irtech.eport.common.core.controller.BaseController;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.utils.poi.ExcelUtil;
import vn.com.irtech.eport.common.core.page.TableDataInfo;

/**
 * Exchange Delivery OrderController
 * 
 * @author irtech
 * @date 2020-04-04
 */
@Controller
@RequestMapping("/equipment/do")
public class EquipmentDoController extends BaseController
{
    private String prefix = "equipment/do";

    @Autowired
    private IEquipmentDoService equipmentDoService;

    @RequiresPermissions("equipment:do:view")
    @GetMapping()
    public String view()
    {
        return prefix + "/list";
    }

    /**
     * Get Exchange Delivery Order List
     */
    @RequiresPermissions("equipment:do:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(EquipmentDo equipmentDo)
    {
        startPage();
        List<EquipmentDo> list = equipmentDoService.selectEquipmentDoList(equipmentDo);
        return getDataTable(list);
    }

    /**
     * Export Exchange Delivery Order List
     */
    @RequiresPermissions("equipment:do:export")
    @Log(title = "Exchange Delivery Order", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(EquipmentDo equipmentDo)
    {
        List<EquipmentDo> list = equipmentDoService.selectEquipmentDoList(equipmentDo);
        ExcelUtil<EquipmentDo> util = new ExcelUtil<EquipmentDo>(EquipmentDo.class);
        return util.exportExcel(list, "do");
    }

    /**
     * Add Exchange Delivery Order
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * Add or Update Exchange Delivery Order
     */
    @RequiresPermissions("equipment:do:add")
    @Log(title = "Exchange Delivery Order", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(EquipmentDo equipmentDo)
    {
        return toAjax(equipmentDoService.insertEquipmentDo(equipmentDo));
    }

    /**
     * Update Exchange Delivery Order
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        EquipmentDo equipmentDo = equipmentDoService.selectEquipmentDoById(id);
        mmap.put("equipmentDo", equipmentDo);
        return prefix + "/edit";
    }

    /**
     * Update Save Exchange Delivery Order
     */
    @RequiresPermissions("equipment:do:edit")
    @Log(title = "Exchange Delivery Order", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(EquipmentDo equipmentDo)
    {
        return toAjax(equipmentDoService.updateEquipmentDo(equipmentDo));
    }

    /**
     * Delete Exchange Delivery Order
     */
    @RequiresPermissions("equipment:do:remove")
    @Log(title = "Exchange Delivery Order", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(equipmentDoService.deleteEquipmentDoByIds(ids));
    }
}