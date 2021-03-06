package vn.com.irtech.eport.edo.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.com.irtech.eport.common.core.controller.BaseController;
import vn.com.irtech.eport.common.core.page.PageAble;
import vn.com.irtech.eport.common.core.page.TableDataInfo;
import vn.com.irtech.eport.equipment.domain.EquipmentDo;
import vn.com.irtech.eport.equipment.service.IEquipmentDoService;
import vn.com.irtech.eport.carrier.domain.EquipmentDoAuditLog;
import vn.com.irtech.eport.carrier.service.IEquipmentDoAuditLogService;

@Controller
@RequestMapping("/do/manage")
public class DoManageController extends BaseController {

    final String PREFIX = "do/manage";

    @Autowired
    private IEquipmentDoService equipmentDoService;


    @Autowired
    private IEquipmentDoAuditLogService equipmentDoAuditLogService;


    @GetMapping("/index")
    public String EquipmentDo() {
        return PREFIX + "/do";
    }

    @PostMapping("/billNo")
    @ResponseBody
    public TableDataInfo billNo(@RequestBody PageAble < EquipmentDo > param) {
        startPage(param.getPageNum(), param.getPageSize(), param.getOrderBy());
        EquipmentDo equipmentDo = param.getData();
        if (equipmentDo == null) {
            equipmentDo = new EquipmentDo();
        }
        List < EquipmentDo > dataList = equipmentDoService.selectEdoListByBillNo(equipmentDo);
        return getDataTable(dataList);
    }

    @PostMapping("/edo")
    @ResponseBody
    public TableDataInfo edo(@RequestBody PageAble < EquipmentDo > param) {
        startPage(param.getPageNum(), param.getPageSize(), param.getOrderBy());
        EquipmentDo equipmentDo = param.getData();
        if (equipmentDo == null) {
            equipmentDo = new EquipmentDo();
        }
        List < EquipmentDo > dataList = equipmentDoService.selectEquipmentDoList(equipmentDo);
        return getDataTable(dataList);
    }

    @GetMapping("/history/{id}")
    public String getHistory(@PathVariable("id") Long id, ModelMap map) {
        map.put("edoId", id);
        return PREFIX + "/history";
    }


    @GetMapping("/auditLog/{equipmenDdoId}")
    @ResponseBody
    public TableDataInfo edoAuditLog(@PathVariable("equipmenDdoId") Long equipmentDoId, EquipmentDoAuditLog equipmentDoAuditLog) {
        equipmentDoAuditLog.setDoId(equipmentDoId);
        List < EquipmentDoAuditLog > edoAuditLogsList = equipmentDoAuditLogService.selectEquipmentDoAuditLogList(equipmentDoAuditLog);
        return getDataTable(edoAuditLogsList);
    }

    @PostMapping("/getOprCode")
  @ResponseBody
  public List<EquipmentDo> lisOprCode()
  {
        EquipmentDo equipmentDo = new EquipmentDo();
      return equipmentDoService.selectOprCode(equipmentDo);
  }


  @PostMapping("/getVoyNo/{carrierCode}/{vessel}")
	@ResponseBody
	public List<EquipmentDo> listVoyNos(@PathVariable("carrierCode") String carrierCode,@PathVariable("vessel") String vessel) {
        EquipmentDo equipmentDo = new EquipmentDo();
        equipmentDo.setVessel(vessel);
        equipmentDo.setCarrierCode(carrierCode);
		return equipmentDoService.selectVoyNos(equipmentDo);
	}

	@PostMapping("/getVessel/{carrierCode}")
	@ResponseBody
        public List<EquipmentDo> listVessels(@PathVariable("carrierCode") String carrierCode) {
        EquipmentDo equipmentDo = new EquipmentDo();
        equipmentDo.setCarrierCode(carrierCode);
        return equipmentDoService.selectVessels(equipmentDo);
	}


}