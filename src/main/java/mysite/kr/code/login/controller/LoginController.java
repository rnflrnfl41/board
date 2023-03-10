package mysite.kr.code.login.controller;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;
import mysite.kr.code.common.security.ZRsaSecurity;
import mysite.kr.code.login.service.LoginService;
import mysite.kr.code.login.vo.LoginData;

/*
 * 클라이언트에서 어떤형태로 데이터를 넘기냐에 따라 파라메터 형태가 바뀜
 * @ModelAttribute는 클라이언트에서 폼데이터로 넘어올때 사용
 * @RequestBody는 클라이언트에서 JSON타입으로 데이터가 넘어올때
 * @RequestParam은 클라이언트에서 데이터가 AJAX타입으로 넘어올때
 *  
 */


@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

	private final LoginService service;
	
	@GetMapping("/form.do")
	public ModelAndView loginView() throws Exception{
		ModelAndView view = new ModelAndView();
		view.setViewName("login/loginForm");
		
		return view;
	}
	
	@GetMapping("/error/info.do")
	public ModelAndView errorView() throws Exception{
		ModelAndView view = new ModelAndView();
		view.setViewName("login/error_info");
		return view;
	}
	
	/*
	 * 세션에 담겨있는 개인키를 
	 */
	@PostMapping("/proc.do")
	public ModelAndView loginProc(@ModelAttribute LoginData.LoginRequest loginRequest, HttpServletRequest req) throws Exception{
		ModelAndView view = new ModelAndView();
		ZRsaSecurity security = new ZRsaSecurity();
		PrivateKey privateKey = null;
		
		int result = 0;
		if(req.getSession().getAttribute("_rsaPrivateKey_") != null) {
			privateKey = (PrivateKey)req.getSession().getAttribute("_rsaPrivateKey_");
			
			//사용자 ID 복호화
			String userId = security.decryptRSA(privateKey, loginRequest.getSecuredUserId());
			//사영자 비밀번호 복호화
			String userPasswd = security.decryptRSA(privateKey, loginRequest.getSecuredUserPasswd());
			
			Map<String, Object> param = new HashMap<>();
			param.put("userId", userId);
			param.put("userPasswd", userPasswd);
			
			result = service.loginProcess(param, req.getSession());
			
			if(result >0) {
				view.setViewName("redirect:/main/main.do");
			}else {
				view.setViewName("login/error");
				if(result== -1) {
					view.addObject("msg","비밀번호가 옳바르지 않습니다.");
				}else if(result == -2) {
					view.addObject("msg","아이디가 옳바르지 않습니다.");
				}else if(result == 0) {
					view.addObject("msg","시스템에 문제가 발생했습니다.");
				}
			}
			
		}else {
			view.setViewName("login/error");
			view.addObject("msg", "비정상접근을 통한 로그인시도 입니다.");
		}
			
		
		return view;
	}
	
	//개인키를 Session에 등록 및 공개키를 resultMap에다가 담아서 클라이언트(loginForm.jsp rsaFrm)에 넘겨줌
	@GetMapping("/get/rsa-key.do")
	@ResponseBody
	public Map<String, Object> getRSAKey(HttpServletRequest request) throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			ZRsaSecurity security = new ZRsaSecurity();
			PrivateKey privateKey = security.getPrivateKey();
			
			HttpSession session = request.getSession();
			
			if(session.getAttribute("_rsaPrivateKey_")!=null) {
				session.removeAttribute("_rsaPrivateKey_");
			}
			
			session.setAttribute("_rsaPrivateKey_", privateKey);
			
			String publicKeyModule = security.getRsaPublicKeyModulus();
			String publicKeyExponent = security.getRsaPublicKeyExponent();
			
			resultMap.put("publicKeyModule", publicKeyModule);
			resultMap.put("publicKeyExponent", publicKeyExponent);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}
	
	@GetMapping("/out.do")
	public ModelAndView logout(HttpServletRequest req) throws Exception{
		ModelAndView view = new ModelAndView();
		
		HttpSession session = req.getSession();
		
		if(session.getAttribute("userInfo") != null) {
			session.removeAttribute("userInfo");
		}
		
		view.setViewName("redirect:/main/main.do");
		return view;
	}
	
	@GetMapping("/join.do")
	public ModelAndView joinView() throws Exception{
		ModelAndView view = new ModelAndView();
		view.setViewName("login/joinForm");
		
		return view;
	}
	
	@PostMapping("/join.do")
	@ResponseBody
	// ajax통신으로 데이터를 주고받을때 파라메터를 @RequestParam을 줬다면
	//json으로 통신 할때는 @RequestBody로 파라메터를 준다.
	public Map<String, Object> insertUserInfo(@RequestBody LoginData.LoginUserInfo userInfo)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		int result = service.insertUserInfo(userInfo);
		if(result > 0) {
			resultMap.put("resultCode", 200);
		}else {
			resultMap.put("resultCode", 500);
		}
		return resultMap;
	}
	
	@GetMapping("/check/id.do")
	@ResponseBody
	public Map<String, Object> checkUserId(@RequestParam(name="userId") String userId) throws Exception{
		Map <String, Object> resultMap = new HashMap<>();
		int result = service.userIdCheck(userId);
		
		if(result == 0) {
			resultMap.put("resultCode", 200);
		}else {
			resultMap.put("resultCode", 300);
		}

		return resultMap;
	}
	
}
