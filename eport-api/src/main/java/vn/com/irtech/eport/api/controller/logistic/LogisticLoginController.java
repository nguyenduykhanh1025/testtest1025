package vn.com.irtech.eport.api.controller.logistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import vn.com.irtech.eport.api.domain.EportUserType;
import vn.com.irtech.eport.api.form.LoginReq;
import vn.com.irtech.eport.api.security.service.LoginService;
import vn.com.irtech.eport.common.core.controller.BaseController;
import vn.com.irtech.eport.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/logistic")
public class LogisticLoginController extends BaseController {

	@Autowired
	private LoginService loginService;

	@PostMapping("/login")
	@ResponseBody
	public AjaxResult login(@RequestBody LoginReq loginForm) {
		AjaxResult ajaxResult = AjaxResult.success();
		ajaxResult.put("token", loginService.login(loginForm, EportUserType.LOGISTIC));
		return ajaxResult;
	}
}
