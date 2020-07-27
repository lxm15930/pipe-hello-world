package com.mind.product.eca;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mind.platform.system.base.SysManager;
import com.mind.platform.system.op.ex.DBCall;
import com.mind.platform.system.op.ex.IDBCall;
import com.mind.platform.system.op.ex.IDBSession;
import com.mind.platform.system.op.ex.impl.DBValueSetting4Bean;
import com.mind.platform.system.op.ex.sql.DBSql;
import com.mind.product.eca.system.paramconf.entity.Params;


@Component
@Order(value = 1)
@PropertySource("classpath:application.properties")
public class MTSRunner implements ApplicationRunner {
  private final static Logger logger = LoggerFactory.getLogger(MTSRunner.class);
  @Autowired
  private DataSource dataSource;
  @Value("${mts.verNum}")
  String svnRevision="";
  @Value("${committedDate}")
  String committedDate="";
  @Value("${buildTime}")
  String buildTime="";
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    SysManager.initAll("",dataSource);
    SysManager.start();
    //获取版本号
    String versionNumber = getParam("version_number_parameter").getValue_code();
    logger.info("mts.data.verNum:[{}]",versionNumber);
    logger.info("mts.svn.verNum:[{}]",svnRevision);
    logger.info("mts.svn.CommitedDate:[{}]",committedDate);
    logger.info("mts.buildTime:[{}]",buildTime);
  }
  public Params getParam(String param_code)  {
      Params oRtn;
      try {
       oRtn = new DBCall<Params>("system").call(new IDBCall<Params>() {
         @Override
         public Params call(IDBSession aDBSession) throws SQLException {
           return getBean(aDBSession, param_code);
         }
       });
      } catch (Exception e) {
       logger.error("",e);
       return null;
      }
      return oRtn;
   }

    public Params getBean(IDBSession aDBSession, String param_code) throws SQLException {
      Params oRtn=new Params();
      DBSql oSql=new DBSql();
      oSql.append(" select value_code,value_txt from p1pf_param  ");
      oSql.append("  where param_code=?").addPara(param_code);
      new DBValueSetting4Bean(oRtn).load(aDBSession, oSql, oSql.getParas());
      return oRtn;
    }
}
