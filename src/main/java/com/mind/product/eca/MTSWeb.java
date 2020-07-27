package com.mind.product.eca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

//@ServletComponentScan("com.mind.product.eca.system.common.xss")
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
@EnableTransactionManagement
public class MTSWeb extends SpringBootServletInitializer{
  public static String redirectFlag="";//1-启用;0-不启用
  public static String redirectUrl="";
  
  private final static Logger logger = LoggerFactory.getLogger(MTSWeb.class);
  public static void main(String[] args) {
    SpringApplication.run(MTSWeb.class, args);
  }
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      // 注意这里要指向原先用main方法执行的Application启动类
      return builder.sources(MTSWeb.class);
  }
  @Bean
  InternalResourceViewResolver internalResourceViewResolver(){
    logger.info("-----------------setRedirectHttp10Compatible");
    InternalResourceViewResolver resolver= new InternalResourceViewResolver();
    resolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);
    resolver.setPrefix("classpath:/templates");
    resolver.setSuffix(".html");
    resolver.setRedirectHttp10Compatible(false);
   return resolver;
  }
}
