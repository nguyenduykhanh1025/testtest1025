package vn.com.irtech.eport.logistic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.com.irtech.eport.common.core.page.TableDataInfo;

@Controller
@RequestMapping("/logistic/container/status")
public class LogisticContainerStatusController extends LogisticBaseController {
	
	private final String PREFIX = "logistic/container/status";
	
	@GetMapping()
	public String reportContainer() {
		return PREFIX + "/index";
	}
	
	@GetMapping("/list")
	@ResponseBody
	public TableDataInfo listShipment() {
		startPage();
		return getDataTable(null);
	}
}
