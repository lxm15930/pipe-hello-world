package com.mind.product.eca.system.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import org.springframework.beans.factory.annotation.Autowired;

public class AuthRealm extends AuthorizingRealm{
  @Autowired//1.0  
  ILoginService loginService;
  
  
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken utoken = (UsernamePasswordToken) token;//获取用户输入的token
    String username = utoken.getUsername();
    String cust_code = utoken.getHost();
    User user = loginService.findUserByCode(username,cust_code);
    ByteSource salt = ByteSource.Util.bytes(username);
    return new SimpleAuthenticationInfo(user, user.getPass_code(),salt,this.getClass().getName());//放入shiro.调用CredentialsMatcher检验密码
  }
  @PostConstruct
  public void initCredentialsMatcher() {
      //该句作用是重写shiro的密码验证，让shiro用我自己的验证
      setCredentialsMatcher(new CredentialsMatcher());

  }
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
    User user = (User) principal.fromRealm(this.getClass().getName()).iterator().next();//获取session中的用户
    List<String> permissions=null;
    Map<String, String> armenu_id = null;
    if(user.getPermissions()==null||user.getPermissions().size()==0) {
      permissions=new ArrayList<>();
      armenu_id = new HashMap<String, String>();
      List<Func> funcs = loginService.findFuncByUserId(user.getRow_id());
      if(funcs.size()>0) {
        for(Func func:funcs) {
          permissions.add(func.getAr_code());
          armenu_id.put(func.getFunc_id()+"", func.getAr_code());
        }
      }
      user.setPermissions(permissions);
      user.getUserToken().setArmenu_id(armenu_id);
    }else {
      permissions=user.getPermissions();
    }
    SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
    info.addStringPermissions(permissions);//将权限放入shiro中.
    return info;
  }

}
