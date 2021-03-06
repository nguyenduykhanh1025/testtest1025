package vn.com.irtech.eport.logistic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.core.domain.AjaxResult;
import vn.com.irtech.eport.common.core.page.TableDataInfo;
import vn.com.irtech.eport.common.enums.BusinessType;
import vn.com.irtech.eport.common.enums.OperatorType;
import vn.com.irtech.eport.common.utils.poi.ExcelUtil;
import vn.com.irtech.eport.framework.shiro.service.SysPasswordService;
import vn.com.irtech.eport.framework.util.ShiroUtils;
import vn.com.irtech.eport.logistic.domain.DriverAccount;
import vn.com.irtech.eport.logistic.domain.DriverTruck;
import vn.com.irtech.eport.logistic.domain.LogisticAccount;
import vn.com.irtech.eport.logistic.domain.LogisticGroup;
import vn.com.irtech.eport.logistic.domain.LogisticTruck;
import vn.com.irtech.eport.logistic.service.IDriverAccountService;
import vn.com.irtech.eport.logistic.service.IDriverTruckService;
import vn.com.irtech.eport.logistic.service.ILogisticGroupService;
import vn.com.irtech.eport.logistic.service.ILogisticTruckService;

/**
 * Driver login infoController
 * 
 * @author irtech
 * @date 2020-05-19
 */
@Controller
@RequiresPermissions("logistic:transport")
@RequestMapping("/logistic/transport")
public class DriverAccountController extends LogisticBaseController {
	private String prefix = "logistic/transport";

	@Autowired
	private IDriverAccountService driverAccountService;

	@Autowired
	private ILogisticGroupService logisticGroupService;

	@Autowired
	private SysPasswordService passwordService;

	@Autowired
	private IDriverTruckService driverTruckService;

	@Autowired
	private ILogisticTruckService logisticTruckService;

	@GetMapping()
	public String account() {
		return prefix + "/index";
	}

	@GetMapping("/direction")
	public String directionOfTruck() {
		return prefix + "/assignTruck";
	}

	/**
	 * Get Driver login info List
	 */
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(DriverAccount driverAccount, String groupName) {
		startPage();
		LogisticAccount currentUser = getUser();
		driverAccount.setDelFlag(false);
		LogisticGroup logisticGroup = new LogisticGroup();
		logisticGroup.setGroupName(groupName.toLowerCase());
		driverAccount.setLogisticGroupId(currentUser.getGroupId());
		driverAccount.setLogisticGroup(logisticGroup);
		driverAccount.setFullName(driverAccount.getFullName().toLowerCase());
//        driverAccount.setPlateNumber(driverAccount.getPlateNumber().toLowerCase());
		List<DriverAccount> list = driverAccountService.selectDriverAccountList(driverAccount);
		return getDataTable(list);
	}

	/**
	 * Export Driver login info List
	 */
	@RequiresPermissions("system:account:export")
	@Log(title = "Export DS Tài Xế", businessType = BusinessType.EXPORT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/export")
	@ResponseBody
	public AjaxResult export(DriverAccount driverAccount) {
		List<DriverAccount> list = driverAccountService.selectDriverAccountList(driverAccount);
		ExcelUtil<DriverAccount> util = new ExcelUtil<DriverAccount>(DriverAccount.class);
		return util.exportExcel(list, "account");
	}

	/**
	 * Add Driver login info
	 */
	@GetMapping("/add")
	public String add() {
		return prefix + "/add";
	}

	/**
	 * Add or Update Driver login info
	 */
	@Log(title = "Tạo Tài Xế", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(DriverAccount driverAccount) {
		driverAccount.setLogisticGroupId(getUser().getGroupId());
		if (driverAccount.getPassword().length() < 6) {
			return error("Mật khẩu không được ít hơn 6 ký tự!");
		}
		if (driverAccountService.checkPhoneUnique(driverAccount.getMobileNumber()) > 0) {
			return error("PhoneNumber này đã tồn tại!");
		}
		if (!Pattern.matches(PHONE_PATTERN, driverAccount.getMobileNumber())) {
			return error("PhoneNumber này phải từ 10 đến 11 số!");
		}
		driverAccount.setSalt(ShiroUtils.randomSalt());
		driverAccount.setPassword(passwordService.encryptPassword(driverAccount.getMobileNumber(),
				driverAccount.getPassword(), driverAccount.getSalt()));
		driverAccount.setCreateBy(getUser().getFullName());
		return toAjax(driverAccountService.insertDriverAccount(driverAccount));
	}

	/**
	 * Update Driver login info
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		DriverAccount driverAccount = driverAccountService.selectDriverAccountById(id);
		mmap.put("driverAccount", driverAccount);
		return prefix + "/edit";
	}

	/**
	 * Update Save Driver login info
	 */
	@Log(title = "Cập Nhật Tài Xế", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(DriverAccount driverAccount) {
		if (driverAccountService.checkPhoneUnique(driverAccount.getMobileNumber()) > 1) {
			return error("PhoneNumber này đã tồn tại!");
		}
		if (!Pattern.matches(PHONE_PATTERN, driverAccount.getMobileNumber())) {
			return error("PhoneNumber này phải từ 10 đến 11 số!");
		}
		return toAjax(driverAccountService.updateDriverAccount(driverAccount));
	}

	/**
	 * Delete Driver login info
	 */
	@Log(title = "Xóa Tài Xế", businessType = BusinessType.DELETE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/remove")
	@ResponseBody
	public AjaxResult remove(Long id) {
		return toAjax(driverAccountService.deleteDriverAccountById(id));
	}

	@Log(title = "Reset password", businessType = BusinessType.UPDATE)
	@GetMapping("/resetPwd/{id}")
	public String resetPwd(@PathVariable("id") Long id, ModelMap mmap) {
		mmap.put("driverAccount", driverAccountService.selectDriverAccountById(id));
		return prefix + "/resetPwd";
	}

	@Log(title = "Reset Mật Khẩu Tài Xế", businessType = BusinessType.UPDATE, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/resetPwd")
	@ResponseBody
	public AjaxResult resetPwdSave(DriverAccount driverAccount) {
		driverAccount.setUpdateBy(getUser().getFullName());
		driverAccount.setSalt(ShiroUtils.randomSalt());
		driverAccount.setPassword(passwordService.encryptPassword(driverAccount.getMobileNumber(),
				driverAccount.getPassword(), driverAccount.getSalt()));
		if (driverAccountService.updateDriverAccount(driverAccount) == 1)
			return success();
		return error();
	}

	/**
	 * Search Carrier Group Name
	 */
	@RequestMapping("/searchGroupNameByKeyword")
	@ResponseBody
	public List<JSONObject> searchGroupNameByKeyword(String keyword, Long groupId) {
		LogisticGroup logisticGroup = new LogisticGroup();
		logisticGroup.setGroupName(keyword.toLowerCase());
		List<LogisticGroup> logisticGroups = logisticGroupService.selectLogisticGroupListByName(logisticGroup);
		List<JSONObject> result = new ArrayList<>();
		for (LogisticGroup i : logisticGroups) {
			if (i.getId() != groupId) {
				JSONObject json = new JSONObject();
				json.put("id", i.getId());
				json.put("text", i.getGroupName());
				result.add(json);
			}
		}
		return result;
	}

	@RequestMapping("/getGroupNameById")
	@ResponseBody
	public String getGroupNameById(long id) {
		LogisticGroup logisticGroup = logisticGroupService.selectLogisticGroupById(id);
		return logisticGroup.getGroupName();
	}

	@GetMapping("/listDriverAccount")
	@ResponseBody
	public List<DriverAccount> listDriverAccount(DriverAccount driverAccount) {
		driverAccount.setLogisticGroupId(getUser().getGroupId());
		driverAccount.setDelFlag(false);
		return driverAccountService.selectDriverAccountList(driverAccount);
	}

	@Log(title = "Thêm Thuê Ngoài", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/saveExternalDriverAccount")
	@ResponseBody
	public List<DriverAccount> saveExternalDriverAccount(@RequestBody List<DriverAccount> driverAccounts) {
		if (driverAccounts.size() > 0) {
			LogisticAccount user = getUser();
			for (DriverAccount driverAccount : driverAccounts) {
				driverAccount.setDelFlag(false);
				driverAccount.setCreateBy(user.getFullName());
				driverAccount.setStatus("1");
				// driverAccount.setExternalRentStatus("1");
				driverAccount.setLogisticGroupId(user.getGroupId());
				driverAccount.setSalt(ShiroUtils.randomSalt());
				driverAccount.setPassword(passwordService.encryptPassword(driverAccount.getMobileNumber(),
						driverAccount.getPassword(), driverAccount.getSalt()));
				driverAccountService.insertDriverAccount(driverAccount);
			}
			DriverAccount driverAccount = new DriverAccount();
			// driverAccount.setExternalRentStatus("1");
			driverAccount.setLogisticGroupId(user.getGroupId());
			return driverAccountService.selectDriverAccountList(driverAccount);
		}
		return null;
	}

	/**
	 * UpdateLogisticTruck
	 */
//    @GetMapping("/driverTruck/{id}")
//    public String editDriverTruck(@PathVariable("id") Long id, ModelMap mmap)
//    {
//        DriverTruck driverTruck = new DriverTruck();
//        driverTruck.setDriverId(id);
//        List<DriverTruck> tractorList = driverTruckService.selectTractorByDriverId(id);
//        List<DriverTruck> trailerList = driverTruckService.selectTrailerByDriverId(id);
//        mmap.put("tractorList", tractorList);
//        mmap.put("trailerList", trailerList);
//        mmap.put("driverId", id);
//        return prefix + "/driverTruck";
//    }

//    @Log(title = "Thêm Xe Cho Tài Xế", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
//    @PostMapping("/truckAssign")
//    @ResponseBody
//    @Transactional
//    public AjaxResult addDriverTruck(@RequestParam(value = "truckIds[]", required = false)  String[] truckIds, Long driverId){
//        driverTruckService.deleteDriverTruckById(driverId);
//        if(truckIds != null){
//            for (String i : truckIds) {
//                DriverTruck driverTruck = new DriverTruck();
//                driverTruck.setDriverId(driverId);
//                driverTruck.setTruckId(Long.parseLong(i));
//                driverTruckService.insertDriverTruck(driverTruck);
//            }
//        }
//        return success();
//    } 
	@GetMapping("edit/driver/{id}")
	public String editDriver(@PathVariable("id") Long id, ModelMap mmap) {
		DriverAccount driverAccount = driverAccountService.selectDriverAccountById(id);
		mmap.put("driverAccount", driverAccount);
		return prefix + "/driverTruck";
	}

	/**
	 * Load table truck assigned follow driver
	 */
	@RequestMapping("/driver/truck/list")
	@ResponseBody
	public List<LogisticTruck> getDriverTruckList(DriverTruck driverTruck) {
		List<LogisticTruck> logisticTrucks = new ArrayList<LogisticTruck>();
		DriverAccount driverAccount = driverAccountService.selectDriverAccountById(driverTruck.getDriverId());
		// check driver of current logisticGroup
		if (driverAccount.getLogisticGroupId().equals(getUser().getGroupId())) {
			// get ds xe theo driverId (table mapping)
			List<DriverTruck> tractors = driverTruckService.selectTractorByDriverId(driverTruck.getDriverId());
			List<DriverTruck> trailers = driverTruckService.selectTrailerByDriverId(driverTruck.getDriverId());
			if (tractors.size() != 0) {
				for (DriverTruck tracktor : tractors) {
					// logisticTrucks.add(logisticTruckService.selectLogisticTruckById(i.getTruckId()));
					LogisticTruck logisticTruck = tracktor.getLogisticTruck();
					logisticTruck.setId(tracktor.getTruckId());
					logisticTrucks.add(logisticTruck);
				}
			}
			if (trailers.size() != 0) {
				for (DriverTruck trailer : trailers) {
					// logisticTrucks.add(logisticTruckService.selectLogisticTruckById(i.getTruckId()));
					LogisticTruck logisticTruck = trailer.getLogisticTruck();
					logisticTruck.setId(trailer.getTruckId());
					logisticTrucks.add(trailer.getLogisticTruck());
				}
			}
		}
		return logisticTrucks;
	}

	/**
	 * Load table truck not assigned follow driver
	 */
	@GetMapping("/trucks/not-picked")
	@ResponseBody
	public List<LogisticTruck> getTrucks(@RequestParam(value = "truckIds[]", required = false) Long[] truckIds) {
		LogisticTruck logisticTruck = new LogisticTruck();
		logisticTruck.setLogisticGroupId(getUser().getGroupId());
		List<LogisticTruck> trucks = logisticTruckService.selectLogisticTruckList(logisticTruck);
		if (truckIds != null && trucks.size() > 0) {
			for (Long i : truckIds) {
				for (int j = 0; j < trucks.size(); j++) {
					if (trucks.get(j).getId().equals(i)) {
						trucks.remove(j);
					}
				}
			}
		}
		return trucks;
	}

	/**
	 * Save assign truck for driver
	 * 
	 * @param truckIds
	 * @param driverId
	 * @return
	 */
	@Log(title = "Thêm Xe Cho Tài Xế", businessType = BusinessType.INSERT, operatorType = OperatorType.LOGISTIC)
	@PostMapping("/truck/assign/save")
	@ResponseBody
	@Transactional
	public AjaxResult saveAssignTruck(@RequestParam(value = "truckIds[]", required = false) Long[] truckIds,
			Long driverId) {
		// check this driver is of current logisticGoup
		DriverAccount driverAccount = driverAccountService.selectDriverAccountById(driverId);
		if (!driverAccount.getLogisticGroupId().equals(getUser().getGroupId())) {
			return error();
		}
		if (truckIds != null) {
			// truckIds is of current logisticGroup
			LogisticTruck logisticTruck = new LogisticTruck();
			logisticTruck.setLogisticGroupId(getUser().getGroupId());
			List<LogisticTruck> logisticTrucks = logisticTruckService.selectLogisticTruckList(logisticTruck);
			if (logisticTrucks.size() > 0) {
				int count = 0;
				for (Long i : truckIds) {
					for (int j = 0; j < logisticTrucks.size(); j++) {
						if (logisticTrucks.get(j).getId().equals(i)) {
							count++;
						}
					}
				}
				if (count != truckIds.length) {
					// TH: a truckId isn't of current logisticGroup
					return error();
				}
			} else {
				// TH:current logisticGroup hasn't truck
				return error();
			}
			driverTruckService.deleteDriverTruckById(driverId);
			for (Long i : truckIds) {
				DriverTruck driverTruck = new DriverTruck();
				driverTruck.setDriverId(driverId);
				driverTruck.setTruckId(i);
				driverTruckService.insertDriverTruck(driverTruck);
			}
		} else {
			// TH: delete tat ca xe cua tai xe
			driverTruckService.deleteDriverTruckById(driverId);
		}
		return success();
	}
}
