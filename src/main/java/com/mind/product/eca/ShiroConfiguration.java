package com.mind.product.eca.system.login;

import java.util.LinkedHashMap;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class ShiroConfiguration {
    @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager manager) {
      ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
      bean.setSecurityManager(manager);
      //配置登录的url和登录成功的url
      bean.setLoginUrl("/login");
      bean.setSuccessUrl("/main");
      //配置访问权限
      LinkedHashMap<String, String> filterChainDefinitionMap=new LinkedHashMap<>();
	  filterChainDefinitionMap.put("/version.json", "anon");
      filterChainDefinitionMap.put("/adminlte/**", "anon");
      filterChainDefinitionMap.put("/bootstrap-plugins/**", "anon");
      filterChainDefinitionMap.put("/colorpicker/**", "anon");
      filterChainDefinitionMap.put("/common/**", "anon");
      filterChainDefinitionMap.put("/cookie/**", "anon");
      filterChainDefinitionMap.put("/js/**", "anon");
      filterChainDefinitionMap.put("/css/**", "anon");
      filterChainDefinitionMap.put("/data/**", "anon");
      filterChainDefinitionMap.put("/cron-quartz/**", "anon");
      filterChainDefinitionMap.put("/cropper/**", "anon");
      filterChainDefinitionMap.put("/fastclick/**", "anon");
      filterChainDefinitionMap.put("/fonts/**", "anon");
      filterChainDefinitionMap.put("/bootstrap/**", "anon");
      filterChainDefinitionMap.put("/bootstrap-multiselect/**", "anon");
      filterChainDefinitionMap.put("/icheck/**", "anon");
      filterChainDefinitionMap.put("/iconfont/**", "anon");
      filterChainDefinitionMap.put("/images/**", "anon");
      filterChainDefinitionMap.put("/jqGrid/**", "anon");
      filterChainDefinitionMap.put("/jquery/**", "anon");
      filterChainDefinitionMap.put("/jquery-asDatepicker/**", "anon");
      filterChainDefinitionMap.put("/jquery-plugins/**", "anon");
      filterChainDefinitionMap.put("/jquery-toastr/**", "anon");
      filterChainDefinitionMap.put("/jquery-validation/**", "anon");
      filterChainDefinitionMap.put("/jquery-ztree/**", "anon");
      filterChainDefinitionMap.put("/pdf/**", "anon");
      filterChainDefinitionMap.put("/cert/**", "anon");
      filterChainDefinitionMap.put("/laydate/**", "anon");
      filterChainDefinitionMap.put("/layer/**", "anon");
      filterChainDefinitionMap.put("/layer-v3.1.1/**", "anon");
      filterChainDefinitionMap.put("/modules/**", "anon");
      filterChainDefinitionMap.put("/mpc/**", "anon");
      filterChainDefinitionMap.put("/my97/**", "anon");
      filterChainDefinitionMap.put("/select2/**", "anon");
      filterChainDefinitionMap.put("/toastr-master/**", "anon");
      filterChainDefinitionMap.put("/topjui/**", "anon");
      filterChainDefinitionMap.put("/upbw/**", "anon");
      filterChainDefinitionMap.put("/wdScrollTab/**", "anon");
      filterChainDefinitionMap.put("/webuploader/**", "anon");
      filterChainDefinitionMap.put("/index", "anon");
      filterChainDefinitionMap.put("/checkCert", "anon");
      filterChainDefinitionMap.put("/queryCustCode", "anon");
      filterChainDefinitionMap.put("/checkUserCertFlag", "anon");
      filterChainDefinitionMap.put("/loginUser", "anon");
      filterChainDefinitionMap.put("/loginUser1", "anon");
      filterChainDefinitionMap.put("/login1", "anon");
      filterChainDefinitionMap.put("/loginDownload", "anon");
      filterChainDefinitionMap.put("/*", "authc");
      filterChainDefinitionMap.put("/**", "authc");
      filterChainDefinitionMap.put("/*.*", "authc");
      bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
      return bean;
    }
    //配置核心安全事务管理器
    @Bean(name="securityManager")
    public SecurityManager securityManager(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher matcher) {
      DefaultWebSecurityManager manager=new DefaultWebSecurityManager();
      manager.setRealm(authRealm(matcher));
      //manager.setSessionManager(webSessionManager());
      return manager;
    }
    //配置自定义的权限登录器
    @Bean(name="authRealm")
    public AuthRealm authRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
      AuthRealm authRealm=new AuthRealm();
      authRealm.setCredentialsMatcher(matcher);
      return authRealm;
    }
    //配置自定义的密码比较器
    @Bean(name="credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
      return new CredentialsMatcher();
    }
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
      return new LifecycleBeanPostProcessor();
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
      DefaultAdvisorAutoProxyCreator creator=new DefaultAdvisorAutoProxyCreator();
      creator.setProxyTargetClass(true);
      return creator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager manager) {
      AuthorizationAttributeSourceAdvisor advisor=new AuthorizationAttributeSourceAdvisor();
      advisor.setSecurityManager(manager);
      return advisor;
    }
    @Bean(name = "authRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public AuthRealm authRealm(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher matcher){
      AuthRealm realm = new AuthRealm();
      realm.setAuthorizationCachingEnabled(false);
      realm.setCredentialsMatcher(matcher);
      return realm;
    }

    @Bean(name = "hashedCredentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
      HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
      hashedCredentialsMatcher.setHashAlgorithmName("MD5");
      hashedCredentialsMatcher.setHashIterations(1024);
      hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
      return hashedCredentialsMatcher;
    }
    
    /*@Bean(name="webSessionManager")
    public DefaultWebSessionManager webSessionManager(){
      DefaultWebSessionManager manager = new DefaultWebSessionManager();
      //manager.setCacheManager(cacheManager);// 加入缓存管理器
      RedisSessionDao sessionDao = new RedisSessionDao();
      manager.setSessionDAO(sessionDao);// 设置SessionDao
      manager.setDeleteInvalidSessions(true);// 删除过期的session
      manager.setGlobalSessionTimeout(sessionDao.getExpireTime());// 设置全局session超时时间
      manager.setSessionValidationSchedulerEnabled(true);// 是否定时检查session
      return manager;
    }*/
    
}
