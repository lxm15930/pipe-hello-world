package com.mind.product.eca.system.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;

import com.mind.platform.system.base.CMReturn;
import com.mind.platform.system.base.Page;
import com.mind.platform.system.base.UserToken;
import com.mind.platform.system.utils.DateUtils;
import com.mind.product.eca.MTSWeb;
import com.mind.product.eca.cm.cert.ca.IKey3Verify;
import com.mind.product.eca.system.common.aop.IsaVerify;
import com.mind.product.eca.system.common.aop.OperLog;
import com.mind.product.eca.system.index.api.IIndexService;
import com.mind.product.eca.system.index.entity.Func;
import com.mind.product.eca.system.link.api.IUserLinkService;
import com.mind.product.eca.system.log.api.ISysLogService;
import com.mind.product.eca.system.log.entity.SysLog;
import com.mind.product.eca.system.online.api.IOnlineService;
import com.mind.product.eca.system.online.entity.Online;
import com.mind.product.eca.system.paramconf.api.IParamConfService;
import com.mind.product.eca.system.synclogin.api.ISyncLoginMsgService;
import com.mind.product.eca.system.syncusekey.api.ISyncKeyUserService;
import com.mind.product.eca.system.user.api.ISysUserService;
import com.mind.product.eca.system.user.entity.Apply;
import com.mind.product.eca.system.user.entity.SysUser;

@Controller
public class LoginController {
  
  @Autowired//1.0
  IIndexService indexService;
  @Autowired//1.0
  ISysLogService logService;
  
  @Autowired//1.0
  IOnlineService onlineService;
  @Autowired//1.0
  ILoginService loginService;
  @Autowired//1.0
  ISyncLoginMsgService syncLoginMsgService;	
  @Autowired//1.0
  IUserLinkService userLinkService; 
  //引入参数查询服务
  @Autowired//1.0
  IParamConfService iParamConfService;
  @Autowired//1.0
  IKey3Verify key3Verify; 
  @Autowired//1.0
  ISysUserService sysuserservice;
  @Autowired//1.0
  ISyncKeyUserService syncKeyUser;
  
  private final static Logger logger = LoggerFactory.getLogger(LoginController.class);
  @Autowired
  LocaleResolver localeResolver;
  
/*  @Autowired//1.0
  IRedisUtil redisUtil;*/
  
  @RequestMapping(value = "")
  public String loginRoot(HttpServletRequest aRequest,HttpSession session,Model aModel,HttpServletResponse response) throws UnknownHostException {
      //退出登录后,返回登录页
      Subject subject = SecurityUtils.getSubject();
      User oUser = (User)aRequest.getSession().getAttribute("user");
      UserToken oUserToken = oUser.getUserToken();
      SysLog oLog = new SysLog();
      oLog.setLog_type("1");
      oLog.setLog_body("系统注销");
      oLog.setPoint_code("logout:mng");
      oLog.setFunc_id(-1);
      subject.logout();
      CookieUtil.removeCookie(response,  "MTSTOKEN");
      logService.add(oLog, oUserToken);
      Online oOnline = new Online();
      oOnline.setLogout_time(new Timestamp(new Date().getTime()));
      oOnline.setUser_id(oUserToken.getUserId());
      oOnline.setUser_name(oUserToken.getUserName());
      onlineService.updateLogout(oOnline, oUserToken);
      aModel.addAttribute("result", "");
      return "eca/system/login/login";
  }
  
  @RequestMapping(value = "login")
  public String login(HttpSession aSession,Model aModel,HttpServletResponse aResponse,String originFlag){
	Subject subject = SecurityUtils.getSubject();
//    if(aSession!=null) {
//      System.out.println(aSession.getId());
//    }
    if(subject.isAuthenticated()) {
      subject.logout();
      CookieUtil.removeCookie(aResponse,  "MTSTOKEN");
    }
    aModel.addAttribute("result", "");
    return "eca/system/login/login";
  }
  
  @RequestMapping(value = "loginUser")
  public String loadin(@ModelAttribute(value = "aUser") @Valid User aUser,BindingResult bindingResult,Model aModel,HttpSession aSession,HttpServletRequest aRequest, HttpServletResponse aResponse){
    User oUser = (User) aSession.getAttribute("user");
    if (oUser != null) {
      return "redirect:logOut";
    }
    if(StringUtils.isNotEmpty(aUser.getPass_code())) {
    	try {
    	    String privateKey="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALJNGQZngpUFT363tIi+uHfDZpdzdUmiUiePg1sa9Wr1aMWovFuzFEAaZn0sNyWRqX3qNkNTDoQgTM2IiI6Kt4zkKKlA8u0KjFkvtXPzoVOjzUS9JOXsvojIrve3ZPXXdCIlo5/lSVVhqd5LuuWpI78sqJ6b7GOpny0k/3f0cWyDAgMBAAECgYEAnXYS9KHzI1HGS51G1UDBPhsVfgjb+sRcE03dblbXh7bk6TJD7iOvbZEjE8Y5iXcjHOz7MOenuS2hRh4eouuEfm1upGd2D+JufCuQ9Cj1sArtxwZnZjvDditLtY4agOymNR4TpQ5vq+/FE5k9D5/Po/g9Uo/xlSsC3ts6RYzL52kCQQD6HOQf535gY9ZZ8cVckcGvo+a+6Vcl5ep6ng39vC306yYxVb3rjyiCd244U/8z+3ERyHeV9wqSVR1VRB6rD2VdAkEAtn99ZMLpryjCslIplw6cPCLiC89WaVm45exJDMQbZw/G6i9gIOMFif9v3dNYAWf/8G8Xs0H3OcNqoc0Qh5IbXwJBAPCKmqdIZxViz0L/r7UghStbsU6IcYUEQuccXQ5bTcOP59JoyNfkfaxEGl/YAMfImezlZIV46tTjQOvBMNGJZA0CQC+56Z4XRyuymjAuQogpnvwvFzSFdZC5kRw4DeaTxqLOQnPuDdKr7D/pmGTHp4U+oHVNaEJN5wypKVLFISSDNjsCQQDk9wV9VYKRUW7JHpJVPW1Zyb7pcP3jEOrAeK4OQXysjCCLDA4b//wJUlx6edPOPXeeFGA6o7xfalCo1aOOzwyv";
    		String outputStr = IsaVerify.decrypt(aUser.getPass_code(),privateKey);
    		aUser.setPass_code(outputStr);
    	} catch (Exception e) {
    		aUser.setPass_code("");
    		logger.error("", e);
    	}	
    } else {
    	aUser.setPass_code("");
	}
    
    CMReturn result = new CMReturn(1);
    logger.info("system.login.logininfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
    UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(aUser.getUser_code(), aUser.getPass_code(), aUser.getCust_code());
    Subject subject = SecurityUtils.getSubject();
    User user = new User();
    String[] errorMsg = new String[] { "cust_code", "user_code", "pass_code" };
    aModel.addAttribute("errorMsg", errorMsg);
    User userInfo = new User();
    String sfail_num = iParamConfService.getParamValueInCache("login_fail_num");
    int ifail_num = Integer.parseInt(sfail_num);

    // ****** add by baixf 20200206 反洗钱客户校验 begin *********/
    // 查询是否是反洗钱高风险客户
    if (aUser.getCust_code() != null && !("").equals(aUser.getCust_code())) {
      int infoMessage = loginService.checkFxqCuster(aUser.getCust_code());
      if (infoMessage == 1) { // 返回结果是1代表该客户是反洗钱高风险客户或其他异常
        aModel.addAttribute("result", "公司账户异常，根据人行有关规定，请到柜面办理");
        logger.info("system.login.checkFxqCuster(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
        return "eca/system/login/login";
      }
    }
    // ****** add by baixf 20200206 反洗钱客户校验 end *********/

    try {
      if (bindingResult.hasErrors()) {
        subject.logout();
        return "eca/system/login/login";
      } else {
        if (subject.isAuthenticated()) {
        } else {
          MTSWeb.redirectFlag = iParamConfService.getParamValueInCache("redirect_flag");
          MTSWeb.redirectUrl = iParamConfService.getParamValueInCache("redirect_url");
          userInfo = loginService.findUserByCode(aUser.getUser_code(), aUser.getCust_code());
          if (userInfo.getRow_id() <= 0) {
            aModel.addAttribute("result", "用户信息填写不正确");// [2]-用户被锁定
            logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
          if (result.getValue() == 1) {
            // 1 查询登录失败次数
            // 如果登录次数大于ifail_num(参数配置)
            if (userInfo.getLogin_fail_num() >= ifail_num) {
              if (userInfo.getNext_login_time().getTime() > userInfo.getNow_time().getTime()) {
                aModel.addAttribute("result", "用户被锁定请稍后再试");// [2]-用户被锁定
                logger.info("system.login.userLock(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                return "eca/system/login/login";
              } else {
                // 设置用户登录失败次数为零
                userInfo.setLogin_fail_num(0);
                // loginService.update(userInfo);
              }
            }

            if (result.getValue() == 1) {
              // 密码检查 -1-异常;1-检查通过;3-密码不对
              try {
                subject.login(usernamePasswordToken); // 完成登录
                user = (User) subject.getPrincipal();
                user.setUrl("logOut?rd=" + new Date().getTime());
                aSession.setAttribute("user", user);
                if (checkCert(aRequest)) {// 是否需要证书校验开关
                  if (checkUserCertFlag(aRequest, aUser.getCust_code(), aUser.getUser_code())) {// 是否绑定了证书
                    boolean veriFlag = true;
                    // 证书合法性校验
                    logger.info("system.login.checkKeyLegalStart(cust_code:[{}];user_code:[{}];key_code:[{}])", aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                    if (veriFlag) {
                      boolean legalFlag = loginService.checkKeyLegal(aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                      if (!legalFlag) {
                        logger.info("system.login.checkKeyLegal(cust_code:[{}];user_code:[{}];key_code:[{}])", aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                        aModel.addAttribute("result", "证书不合法!");
                        subject.logout();
                        return "eca/system/login/login";
                      }
                    }

                    if ("2".equals(iParamConfService.getParamValueInCache("certVerify"))) {
                      logger.info("system.login.CerVerifyStart(cust_code:[{}];user_code:[{}];sys_id:[{}])", aUser.getCust_code(), aUser.getUser_code(), userInfo.getSys_id());
                      CMReturn keyRtn = syncKeyUser.CerVerify(aRequest.getParameter("SignCertCN"), userInfo.getSys_id(), aUser.getUserToken());
                      String useFlag = (String) keyRtn.getReturnPara("ukey_status", "");
                      logger.info("system.login:", useFlag);
                      if (!"00".equals(useFlag)) { // 01-已废止、00-未废止
                        veriFlag = false;
                        logger.info("system.login.CerVerify(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                        subject.logout();
                        return "eca/system/login/login";
                      }
                    }
                    if (veriFlag) {
                      boolean aFlag = key3Verify.keyVerify(aRequest, aUser.getCust_code(), aUser.getUser_code());
                      // boolean aFlag = verifySign.verifyFormSign( keyMap, aUser.getCust_code(),
                      // aUser.getUser_code());
                      if (!aFlag) {
                        aModel.addAttribute("result", "证书验证失败");
                        logger.info("system.login.checkCert(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                        subject.logout();
                        return "eca/system/login/login";
                      }
                      if (checkUseCA(aRequest)) {
                        aSession.setAttribute("isUseCa", "2");// 付款验证标记
                      }
                    }
                  }
                }
              } catch (Exception e) {
                // 0.0密码不对
                // 1.1查询连续输入时间间隔多久失败次数被重置(秒)
                String stime_interval = iParamConfService.getParamValueInCache("time_interval");
                int itime_interval = Integer.parseInt(stime_interval);
                if (userInfo.getLast_login_time() != null && userInfo.getNow_time().getTime() - userInfo.getLast_login_time().getTime() < 1000 * itime_interval) {
                  if (userInfo.getLogin_fail_num() >= ifail_num - 1) {
                    // 1.1.1失败次数加一
                    userInfo.setLogin_fail_num(userInfo.getLogin_fail_num() + 1);
                    // 1.1.2查询失败次数到达上限后用户被锁定时间
                    String slock_time = iParamConfService.getParamValueInCache("lock_time");
                    int ilock_time = Integer.parseInt(slock_time);
                    // 1.1.3设置下次登录时间
                    userInfo.setNext_login_time(DateUtils.addTime(userInfo.getNow_time(), 1000 * ilock_time));
                    // 更新数据库登录失败次数和下次登录时间
                    CMReturn oRtn = loginService.update(userInfo);
                    if (oRtn.getValue() < 0) {
                      result = oRtn;
                    } else {
                      result.setValueCode(2, "用户信息输入错误" + userInfo.getLogin_fail_num() + "次，用户锁定，请稍后再试");
                      logger.info("system.login.userLocked(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                    }
                  } else {
                    // 1.2.1失败次数小于四次
                    // 1.2.2失败次数加一
                    userInfo.setLogin_fail_num(userInfo.getLogin_fail_num() + 1);
                    // 1.2.3设置下次登录时间为当前时间
                    userInfo.setNext_login_time(userInfo.getNow_time());
                    // 1.2.4更新数据库登录失败次数和下次登录时间
                    CMReturn oRtn = loginService.update(userInfo);
                    if (oRtn.getValue() < 0) {
                      result = oRtn;
                    } else {
                      result.setValueCode(3, "用户信息输入错误" + userInfo.getLogin_fail_num() + "次，请输入正确的用户信息");
                      logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                    }
                  }
                } else {
                  // 2.1 如果用户输入密码间隔大于设置间隔时间则重置登录次数和下次登录时间
                  // 2.1.1失败次数设置为1
                  userInfo.setLogin_fail_num(1);
                  // 2.1.2设置下次登录时间为当前时间
                  userInfo.setNext_login_time(userInfo.getNow_time());
                  // 1.2.4更新数据库登录失败次数和下次登录时间
                  CMReturn oRtn = loginService.update(userInfo);
                  if (oRtn.getValue() < 0) {
                    result = oRtn;
                  } else {
                    result.setValueCode(3, "用户信息输入错误,请输入正确的用户信息");
                    logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());

                  }
                }
                aModel.addAttribute("result", result.getMsgCode());
                return "eca/system/login/login";
              }
              // 0.1密码正确
              // 失败次数设置为0
              userInfo.setLogin_fail_num(0);
              // 设置下次登录时间为当前时间
              userInfo.setNext_login_time(userInfo.getNow_time());
              // 更新数据库登录失败次数和下次登录时间
              CMReturn oRtn = loginService.update(userInfo);
              if (oRtn.getValue() < 0) {
                result = oRtn;
              }
            }
          }
        }

        if (result.getValue() == 1) {
          user = (User) aSession.getAttribute("user");
          // 验证证书是否激活
          String status = loginService.findStatusBySysId(user.getSys_id());
          if (status.equals("A")) {
            aModel.addAttribute("result", "证书未激活");
            logger.info("system.login.certActive(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
        }

        if (result.getValue() == 1) {
          // 企业是否激活
          if (user.getOper_key_id() != -1) {
            String active_flag = loginService.findActiveFlagByOperId(user.getOper_key_id(), user.getSys_id());
            if (active_flag.equals("0")) {
              aModel.addAttribute("result", "企业未激活");
              logger.info("system.login.certActive(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
              return "eca/system/login/login";
            }
          }
        }
        String oLang = user.getLang_type();
        if ("zh_cn".equalsIgnoreCase(oLang)) {
          localeResolver.setLocale(aRequest, aResponse, Locale.CHINA);
        } else if ("en_us".equalsIgnoreCase(oLang)) {
          localeResolver.setLocale(aRequest, aResponse, Locale.ENGLISH);
        } else {
          localeResolver.setLocale(aRequest, aResponse, Locale.CHINA);
        }
        if (user.getUserToken().getApp_id() <= 0) {
          Page<Apply> oRtn = indexService.getApply(user.getUserToken());
          if (oRtn.getDatas().size() > 0) {
            user.getUserToken().setApp_id(oRtn.getDatas().get(0).getRow_id());
            indexService.updateApply(oRtn.getDatas().get(0).getRow_id(), user.getUserToken());
          } else {
            aModel.addAttribute("result", "用户信息配置有误");
            logger.info("system.login.userConfig(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
        }
        Func oFunc = new Func();
        if ("0".equals(user.getFirstlogin_flag())) {// &&(user.getPass_nextmodi_time()==null||user.getNow_time().getTime()<user.getPass_nextmodi_time().getTime())//三个月需要修改密码
          oFunc = indexService.getFunc(user.getUserToken());
        } else {
          oFunc.setSystem_name(user.getSystem_name());
        }
        aModel.addAttribute("funcs", oFunc);
        SysLog oLog = new SysLog();
        oLog.setLog_type("1");
        oLog.setLog_body("系统登录");
        oLog.setPoint_code("login:mng");
        oLog.setFunc_id(-1);
        oLog.setOper_time(new java.sql.Timestamp(userInfo.getNow_time().getTime()));
        OperLog.getMe().addLog(oLog);
        final UserToken oUserToken = user.getUserToken();
        subject.getSession().setTimeout(1000l * 60 * 60);
        Apply aBean = indexService.getApplyBean(user.getUserToken().getApp_id());
        // 如果成功设置登录次数为0 下次登录时间为当前登录时间
        userInfo.setLogin_fail_num(0);
        userInfo.setNext_login_time(user.getNow_time());
        loginService.update(userInfo);
        final ISyncLoginMsgService msgService=syncLoginMsgService;
        if (!("").equals(aBean.getApp_path())) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              SysUser sysUser = sysuserservice.getUser(oUserToken.getUserId());
              try {
                msgService.getaddRe(sysUser, oUserToken);
              } catch (SQLException e) {
                logger.error("",e);
              }
            }
          }).start();
        }
        String switchUsersParam = iParamConfService.getParamValueInCache("switchUsersParam");
        aModel.addAttribute("switchUsersParam", switchUsersParam);
        // 替换LAST_login_id为最新生成id并记录在会话中
        long last_login_id = loginService.updateLastLoginId(user);
        if (last_login_id < 0) {
          aModel.addAttribute("result", "更新用户登录信息异常");
          logger.info("system.login.userConfig(user_code:[{}])", aUser.getUser_code());
          return "eca/system/login/login";
        } else {
          // 设置最后登陆记录id
          aSession.setAttribute("LastLoginId", last_login_id);
          aModel.addAttribute("LastLoginId", last_login_id);
        }
        final SsoData sData = new SsoData();
        sData.setServer_session_id("");
        sData.setUser_code(aUser.getUser_code());
        sData.setTenant_code(aUser.getCust_code());
        String tokencode = UUID.randomUUID().toString();
        sData.setToken_code(tokencode);
//        OperLog.getMe().addsso(sData);
        new Thread(new Runnable() {
          @Override
          public void run() {
            loginService.add(sData, oUserToken);
          }
        }).start();
        try {
          CookieUtil.addCookie(aResponse, "MTSTOKEN", tokencode, -1);
        } catch (Exception e) {
          logger.info("SSO信息保存异常,SSO暂不支持");
          logger.error(e.getMessage(), e);
        }
        user.setOnline_flag("1");
        int updateUserOnlineFlag = loginService.updateUserOnlineFlag(user);
        if (-1==updateUserOnlineFlag) { // 01-已废止、00-未废止
            logger.info("system.login.updateUserOnlineFlag(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            subject.logout();
            return "eca/system/login/login";
          }
        return "eca/system/index/" + aBean.getApp_path();
      }
    } catch (Exception e) {
      return "eca/system/login/login";
    }
  }
  
  
  @RequestMapping(value = "loginUser1")
  public String loadin1(@ModelAttribute(value = "aUser") @Valid User aUser,BindingResult bindingResult,String safeFlag,Model aModel,HttpSession aSession,HttpServletRequest aRequest, HttpServletResponse aResponse){
    User oUser = (User) aSession.getAttribute("user");
    if (oUser != null) {
      return "redirect:logOut";
    }
    CMReturn result = new CMReturn(1);
    logger.info("system.login.logininfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
    UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(aUser.getUser_code(), aUser.getPass_code(), aUser.getCust_code());
    Subject subject = SecurityUtils.getSubject();
    User user = new User();
    String[] errorMsg = new String[] { "cust_code", "user_code", "pass_code" };
    aModel.addAttribute("errorMsg", errorMsg);
    User userInfo = new User();
    String sfail_num = iParamConfService.getParamValueInCache("login_fail_num");
    int ifail_num = Integer.parseInt(sfail_num);

    // ****** add by baixf 20200206 反洗钱客户校验 begin *********/
    // 查询是否是反洗钱高风险客户
    if (aUser.getCust_code() != null && !("").equals(aUser.getCust_code())) {
      int infoMessage = loginService.checkFxqCuster(aUser.getCust_code());
      if (infoMessage == 1) { // 返回结果是1代表该客户是反洗钱高风险客户或其他异常
        aModel.addAttribute("result", "公司账户异常，根据人行有关规定，请到柜面办理");
        logger.info("system.login.checkFxqCuster(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
        return "eca/system/login/login";
      }
    }
    // ****** add by baixf 20200206 反洗钱客户校验 end *********/

    try {
      if (bindingResult.hasErrors()) {
        subject.logout();
        return "eca/system/login/login";
      } else {
        if (subject.isAuthenticated()) {
        } else {
          MTSWeb.redirectFlag = iParamConfService.getParamValueInCache("redirect_flag");
          MTSWeb.redirectUrl = iParamConfService.getParamValueInCache("redirect_url");
          userInfo = loginService.findUserByCode(aUser.getUser_code(), aUser.getCust_code());
          if (userInfo.getRow_id() <= 0) {
            aModel.addAttribute("result", "用户信息填写不正确");// [2]-用户被锁定
            logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
          if (result.getValue() == 1) {
            // 1 查询登录失败次数
            // 如果登录次数大于ifail_num(参数配置)
            if (userInfo.getLogin_fail_num() >= ifail_num) {
              if (userInfo.getNext_login_time().getTime() > userInfo.getNow_time().getTime()) {
                aModel.addAttribute("result", "用户被锁定请稍后再试");// [2]-用户被锁定
                logger.info("system.login.userLock(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                return "eca/system/login/login";
              } else {
                // 设置用户登录失败次数为零
                userInfo.setLogin_fail_num(0);
                // loginService.update(userInfo);
              }
            }

            if (result.getValue() == 1) {
              // 密码检查 -1-异常;1-检查通过;3-密码不对
              try {
                subject.login(usernamePasswordToken); // 完成登录
                user = (User) subject.getPrincipal();
                user.setUrl("logOut?rd=" + new Date().getTime());
                aSession.setAttribute("user", user);
                if (checkCert(aRequest)) {// 是否需要证书校验开关
                  if (checkUserCertFlag(aRequest, aUser.getCust_code(), aUser.getUser_code())) {// 是否绑定了证书
                    boolean veriFlag = true;
                    // 证书合法性校验
                    logger.info("system.login.checkKeyLegalStart(cust_code:[{}];user_code:[{}];key_code:[{}])", aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                    if (veriFlag) {
                      boolean legalFlag = loginService.checkKeyLegal(aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                      if (!legalFlag) {
                        logger.info("system.login.checkKeyLegal(cust_code:[{}];user_code:[{}];key_code:[{}])", aUser.getCust_code(), aUser.getUser_code(), aRequest.getParameter("SignCertCN"));
                        aModel.addAttribute("result", "证书不合法!");
                        subject.logout();
                        return "eca/system/login/login";
                      }
                    }

                    if ("2".equals(iParamConfService.getParamValueInCache("certVerify"))) {
                      logger.info("system.login.CerVerifyStart(cust_code:[{}];user_code:[{}];sys_id:[{}])", aUser.getCust_code(), aUser.getUser_code(), userInfo.getSys_id());
                      CMReturn keyRtn = syncKeyUser.CerVerify(aRequest.getParameter("SignCertCN"), userInfo.getSys_id(), aUser.getUserToken());
                      String useFlag = (String) keyRtn.getReturnPara("ukey_status", "");
                      logger.info("system.login:", useFlag);
                      if (!"00".equals(useFlag)) { // 01-已废止、00-未废止
                        veriFlag = false;
                        logger.info("system.login.CerVerify(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                        subject.logout();
                        return "eca/system/login/login";
                      }
                    }
                    if (veriFlag) {
                      boolean aFlag = key3Verify.keyVerify(aRequest, aUser.getCust_code(), aUser.getUser_code());
                      // boolean aFlag = verifySign.verifyFormSign( keyMap, aUser.getCust_code(),
                      // aUser.getUser_code());
                      if (!aFlag) {
                        aModel.addAttribute("result", "证书验证失败");
                        logger.info("system.login.checkCert(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                        subject.logout();
                        return "eca/system/login/login";
                      }
                      if (checkUseCA(aRequest)) {
                        aSession.setAttribute("isUseCa", "2");// 付款验证标记
                      }
                    }
                  }
                }
              } catch (Exception e) {
                // 0.0密码不对
                // 1.1查询连续输入时间间隔多久失败次数被重置(秒)
                String stime_interval = iParamConfService.getParamValueInCache("time_interval");
                int itime_interval = Integer.parseInt(stime_interval);
                if (userInfo.getLast_login_time() != null && userInfo.getNow_time().getTime() - userInfo.getLast_login_time().getTime() < 1000 * itime_interval) {
                  if (userInfo.getLogin_fail_num() >= ifail_num - 1) {
                    // 1.1.1失败次数加一
                    userInfo.setLogin_fail_num(userInfo.getLogin_fail_num() + 1);
                    // 1.1.2查询失败次数到达上限后用户被锁定时间
                    String slock_time = iParamConfService.getParamValueInCache("lock_time");
                    int ilock_time = Integer.parseInt(slock_time);
                    // 1.1.3设置下次登录时间
                    userInfo.setNext_login_time(DateUtils.addTime(userInfo.getNow_time(), 1000 * ilock_time));
                    // 更新数据库登录失败次数和下次登录时间
                    CMReturn oRtn = loginService.update(userInfo);
                    if (oRtn.getValue() < 0) {
                      result = oRtn;
                    } else {
                      result.setValueCode(2, "用户信息输入错误" + userInfo.getLogin_fail_num() + "次，用户锁定，请稍后再试");
                      logger.info("system.login.userLocked(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                    }
                  } else {
                    // 1.2.1失败次数小于四次
                    // 1.2.2失败次数加一
                    userInfo.setLogin_fail_num(userInfo.getLogin_fail_num() + 1);
                    // 1.2.3设置下次登录时间为当前时间
                    userInfo.setNext_login_time(userInfo.getNow_time());
                    // 1.2.4更新数据库登录失败次数和下次登录时间
                    CMReturn oRtn = loginService.update(userInfo);
                    if (oRtn.getValue() < 0) {
                      result = oRtn;
                    } else {
                      result.setValueCode(3, "用户信息输入错误" + userInfo.getLogin_fail_num() + "次，请输入正确的用户信息");
                      logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
                    }
                  }
                } else {
                  // 2.1 如果用户输入密码间隔大于设置间隔时间则重置登录次数和下次登录时间
                  // 2.1.1失败次数设置为1
                  userInfo.setLogin_fail_num(1);
                  // 2.1.2设置下次登录时间为当前时间
                  userInfo.setNext_login_time(userInfo.getNow_time());
                  // 1.2.4更新数据库登录失败次数和下次登录时间
                  CMReturn oRtn = loginService.update(userInfo);
                  if (oRtn.getValue() < 0) {
                    result = oRtn;
                  } else {
                    result.setValueCode(3, "用户信息输入错误,请输入正确的用户信息");
                    logger.info("system.login.checkUserInfo(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());

                  }
                }
                aModel.addAttribute("result", result.getMsgCode());
                return "eca/system/login/login";
              }
              // 0.1密码正确
              // 失败次数设置为0
              userInfo.setLogin_fail_num(0);
              // 设置下次登录时间为当前时间
              userInfo.setNext_login_time(userInfo.getNow_time());
              // 更新数据库登录失败次数和下次登录时间
              CMReturn oRtn = loginService.update(userInfo);
              if (oRtn.getValue() < 0) {
                result = oRtn;
              }
            }
          }
        }

        if (result.getValue() == 1) {
          user = (User) aSession.getAttribute("user");
          // 验证证书是否激活
          String status = loginService.findStatusBySysId(user.getSys_id());
          if (status.equals("A")) {
            aModel.addAttribute("result", "证书未激活");
            logger.info("system.login.certActive(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
        }

        if (result.getValue() == 1) {
          // 企业是否激活
          if (user.getOper_key_id() != -1) {
            String active_flag = loginService.findActiveFlagByOperId(user.getOper_key_id(), user.getSys_id());
            if (active_flag.equals("0")) {
              aModel.addAttribute("result", "企业未激活");
              logger.info("system.login.certActive(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
              return "eca/system/login/login";
            }
          }
        }
        String oLang = user.getLang_type();
        if ("zh_cn".equalsIgnoreCase(oLang)) {
          localeResolver.setLocale(aRequest, aResponse, Locale.CHINA);
        } else if ("en_us".equalsIgnoreCase(oLang)) {
          localeResolver.setLocale(aRequest, aResponse, Locale.ENGLISH);
        } else {
          localeResolver.setLocale(aRequest, aResponse, Locale.CHINA);
        }
        if (user.getUserToken().getApp_id() <= 0) {
          Page<Apply> oRtn = indexService.getApply(user.getUserToken());
          if (oRtn.getDatas().size() > 0) {
            user.getUserToken().setApp_id(oRtn.getDatas().get(0).getRow_id());
            indexService.updateApply(oRtn.getDatas().get(0).getRow_id(), user.getUserToken());
          } else {
            aModel.addAttribute("result", "用户信息配置有误");
            logger.info("system.login.userConfig(cust_code:[{}];user_code:[{}])", aUser.getCust_code(), aUser.getUser_code());
            return "eca/system/login/login";
          }
        }
        Func oFunc = new Func();
        if ("0".equals(user.getFirstlogin_flag())) {// &&(user.getPass_nextmodi_time()==null||user.getNow_time().getTime()<user.getPass_nextmodi_time().getTime())//三个月需要修改密码
          oFunc = indexService.getFunc(user.getUserToken());
        } else {
          oFunc.setSystem_name(user.getSystem_name());
        }
        aModel.addAttribute("funcs", oFunc);
        SysLog oLog = new SysLog();
        oLog.setLog_type("1");
        oLog.setLog_body("系统登录");
        oLog.setOper_time(new java.sql.Timestamp(userInfo.getNow_time().getTime()));
        OperLog.getMe().addLog(oLog);
        final UserToken oUserToken = user.getUserToken();
        subject.getSession().setTimeout(1000l * 60 * 60);
        Apply aBean = indexService.getApplyBean(user.getUserToken().getApp_id());
        // 如果成功设置登录次数为0 下次登录时间为当前登录时间
        userInfo.setLogin_fail_num(0);
        userInfo.setNext_login_time(user.getNow_time());
        loginService.update(userInfo);
        final ISyncLoginMsgService msgService=syncLoginMsgService;
        if (!("").equals(aBean.getApp_path())) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              SysUser sysUser = sysuserservice.getUser(oUserToken.getUserId());
              try {
                msgService.getaddRe(sysUser, oUserToken);
              } catch (SQLException e) {
                logger.error("",e);
              }
            }
          }).start();
        }
        String switchUsersParam = iParamConfService.getParamValueInCache("switchUsersParam");
        aModel.addAttribute("switchUsersParam", switchUsersParam);
        // 替换LAST_login_id为最新生成id并记录在会话中
        long last_login_id = loginService.updateLastLoginId(user);
        if (last_login_id < 0) {
          aModel.addAttribute("result", "更新用户登录信息异常");
          logger.info("system.login.userConfig(user_code:[{}])", aUser.getUser_code());
          return "eca/system/login/login";
        } else {
          // 设置最后登陆记录id
          aSession.setAttribute("LastLoginId", last_login_id);
          aModel.addAttribute("LastLoginId", last_login_id);
        }
        final SsoData sData = new SsoData();
        sData.setServer_session_id("");
        sData.setUser_code(aUser.getUser_code());
        sData.setTenant_code(aUser.getCust_code());
        String tokencode = UUID.randomUUID().toString();
        sData.setToken_code(tokencode);
//        OperLog.getMe().addsso(sData);
        new Thread(new Runnable() {
          @Override
          public void run() {
            loginService.add(sData, oUserToken);
          }
        }).start();
        try {
          CookieUtil.addCookie(aResponse, "MTSTOKEN", tokencode, -1);
        } catch (Exception e) {
          logger.info("SSO信息保存异常,SSO暂不支持");
          logger.error(e.getMessage(), e);
        }
        return "eca/system/index/" + aBean.getApp_path();
      }
    } catch (Exception e) {
      return "eca/system/login/login";
    }
  }
  
  @RequestMapping("logOut")
  public String logOut(HttpServletRequest aRequest,HttpSession session,HttpServletResponse httpServletResponse) throws UnknownHostException {
    Subject subject = SecurityUtils.getSubject();
    User oUser = (User)aRequest.getSession().getAttribute("user");
    UserToken oUserToken = oUser.getUserToken();
    SysLog oLog = new SysLog();
    oLog.setLog_type("1");
    oLog.setLog_body("系统注销");
    oLog.setPoint_code("logout:mng");
    oLog.setFunc_id(-1);
    logger.info("system.logout(cust_code:[{}];user_code:[{}])", oUser.getCust_code(), oUser.getUser_code());
    subject.logout();
    oUser.setOnline_flag("0");
    loginService.updateUserOnlineFlag(oUser);
    logService.add(oLog, oUserToken);
    Online oOnline = new Online();
    oOnline.setLogout_time(new Timestamp(new Date().getTime()));
    oOnline.setUser_id(oUserToken.getUserId());
    oOnline.setUser_name(oUserToken.getUserName());
    CookieUtil.removeCookie(httpServletResponse,  "MTSTOKEN");
    onlineService.updateLogout(oOnline, oUserToken);
    return "eca/system/login/login";
  }
  @RequestMapping("switchLogOut")
  @ResponseBody
  public String switchLogOut(HttpServletRequest aRequest,HttpSession session,HttpServletResponse httpServletResponse) throws UnknownHostException {
    Subject subject = SecurityUtils.getSubject();
    User oUser = (User)aRequest.getSession().getAttribute("user");
    UserToken oUserToken = oUser.getUserToken();
    SysLog oLog = new SysLog();
    oLog.setLog_type("1");
    oLog.setLog_body("系统注销");
    oLog.setPoint_code("logout:mng");
    oLog.setFunc_id(-1);
    CookieUtil.removeCookie(httpServletResponse,  "MTSTOKEN");
    subject.logout();
    oUser.setOnline_flag("0");
    loginService.updateUserOnlineFlag(oUser);
    logService.add(oLog, oUserToken);
    Online oOnline = new Online();
    oOnline.setLogout_time(new Timestamp(new Date().getTime()));
    oOnline.setUser_id(oUserToken.getUserId());
    oOnline.setUser_name(oUserToken.getUserName());
    onlineService.updateLogout(oOnline, oUserToken);
    return "切换用户失败请重新登录";
  }
  
  @RequestMapping("queryCustCode")
  @ResponseBody
  public String queryCustCode(HttpServletRequest aRequest,String dn) {
   /* User oUser = (User)aRequest.getSession().getAttribute("user");
    aUserToken = oUser.getUserToken();*/
    String cust_code = loginService.queryCustCode(dn);
    return cust_code;
  }
  
  @RequestMapping("checkUserCertFlag")
  @ResponseBody
  public boolean checkUserCertFlag(HttpServletRequest aRequest,String custCode,String userCode) {
   /* User oUser = (User)aRequest.getSession().getAttribute("user");
    aUserToken = oUser.getUserToken();*/
    boolean keyFlag = loginService.checkUserCertFlag(custCode,userCode);
    return keyFlag;
  }
  @RequestMapping("checkCert")
  @ResponseBody
  public boolean checkCert(HttpServletRequest aRequest) {
    boolean keyFlag = loginService.checkCert();
    return keyFlag;
  }
  
  @RequestMapping("checkUseCA")
  @ResponseBody
  public boolean checkUseCA(HttpServletRequest aRequest) {
    boolean keyFlag = loginService.checkUseCA();
    return keyFlag;
  }
  @RequestMapping("loginDownload")
  public String loginDownload(HttpServletResponse response,HttpServletRequest aRequest, HttpSession aSession,  Model aModel, UserToken aUserToken) throws SQLException {
	  String url = loginService.getUrlDownLoad().trim();
      File file = new File(url);
	  String fileName = url.substring(url.lastIndexOf("/")+1);
      try {
    	if(file.exists()) {
    		fileName = new String(fileName.getBytes("UTF-8"),"ISO8859-1");
    		response.reset();
    		response.setContentType("application/octet-stream");// 告诉浏览器输出内容为流
    		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    		FileInputStream inputStream = new FileInputStream(url);
    		OutputStream outputStream = response.getOutputStream();
            byte[] bytes = new byte[4096];
            int length;
            while ((length = inputStream.read(bytes)) > 0){
                outputStream.write(bytes, 0, length);
            }
            inputStream.close();
            outputStream.flush();
    		return null;
    	}else{
    		logger.info("system.login.loginDownload.null([{}])",url);
    		aModel.addAttribute("result", "安全控件不存在！");
    		return "eca/system/login/login";
    	}
	} catch (Exception e) {
		logger.error("",e);
		logger.error("loginDownload([{exception:}])",e);
		return "eca/system/login/login";
	}
  } 
  //获取session
  @RequestMapping("getNewSession")
  @ResponseBody
  public long getNewSession(HttpServletRequest request,HttpSession aSession) {
    long attribute = (long) aSession.getAttribute("LastLoginId");
    new Thread(new Runnable() {
      @Override
      public void run() {
        OperLog.getMe().log();
      }
    }).start();
    return attribute;
  }
}
