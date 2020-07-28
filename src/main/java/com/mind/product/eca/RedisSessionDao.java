/*package com.mind.product.eca.system.login;

import java.io.Serializable;
import java.util.Collection;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mind.product.eca.common.redis.IRedisUtil;
@Service
public class RedisSessionDao extends AbstractSessionDAO{
  
  private long expireTime = 120000;
  
  @Autowired//1.0
  IRedisUtil redisUtil;
  
  public RedisSessionDao() {
    super();
  }

  public RedisSessionDao(long expireTime, IRedisUtil redisUtil) {
    super();
    this.expireTime = expireTime;
    this.redisUtil = redisUtil;
  }

  @Override // 更新session
  public void update(Session session) throws UnknownSessionException {
    System.out.println("===============update================");
    if (session == null || session.getId() == null) {
      return;
    }
    session.setTimeout(expireTime);
    redisUtil.set( session.getId(), session, expireTime);
  }
 
  @Override // 删除session
  public void delete(Session session) {
    System.out.println("===============delete================");
    if (null == session) {
      return;
    }
    redisUtil.del(session.getId());
  }
 
  @Override// 获取活跃的session，可以用来统计在线人数，如果要实现这个功能，可以在将session加入redis时指定一个session前缀，统计的时候则使用keys("session-prefix*")的方式来模糊查找redis中所有的session集合
  public Collection<Session> getActiveSessions() {
    System.out.println("==============getActiveSessions=================");
    return null;
  }
 
  @Override// 加入session
  protected Serializable doCreate(Session session) {
    System.out.println("===============doCreate================");
    Serializable sessionId = this.generateSessionId(session);
    this.assignSessionId(session, sessionId);
    redisUtil.set(session.getId(), session, expireTime);
    return sessionId;
  }
 
  @Override// 读取session
  protected Session doReadSession(Serializable sessionId) {
    System.out.println("==============doReadSession=================");
    if (sessionId == null) {
      return null;
    }
    return (Session) redisUtil.get(sessionId);
  }
  
  public long getExpireTime() {
    return expireTime;
  }
 
  public void setExpireTime(long expireTime) {
    this.expireTime = expireTime;
  }

  public IRedisUtil getRedisUtil() {
    return redisUtil;
  }

  public void setRedisUtil(IRedisUtil redisUtil) {
    this.redisUtil = redisUtil;
  }
  
}
*/