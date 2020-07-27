package com.mind.product.eca;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ServiceReqestTmpl  {

  
  @Test
  public void balanceSendPostDataByJson() throws ClientProtocolException, IOException {
      String url = "http://192.168.50.103:8080/ECAService/bankService";
   /*   Map<String, String> map = new HashMap<String, String>();
      map.put("name", "wyj");
      map.put("city", "南京");*/
      JSONObject json= (JSONObject) JSONObject.parse("{\r\n" + 
          "  head:{\r\n" + 
          "    cid:\"\",\r\n" + 
          "    pkg_sn:\"20190723111502000192\",\r\n" + 
          "    security:\"\"\r\n" +  
          "  },\r\n" + 
          "  req:{\r\n" + 
          "    bank_code:\"310\",\r\n" + 
          "    trade_type:\"queryBalance\",\r\n" + 
          "    account_code:\"952A9997220008092\",\r\n" + 
          "    account_name:\"123\",\r\n" + 
          "    currency_code:\"\",\r\n" + 
          "    district:\"\",\r\n" + 
          "    reserved1:\"\",\r\n" + 
          "    reserved2:\"\",\r\n" + 
          "    reserved3:\"\",\r\n" + 
          "    reserved4:\"\",\r\n" + 
          "    reserved5:\"\"\r\n" + 
          "  }\r\n" + 
          "}");
      
      String body = sendPostDataByJson(url, json.toJSONString(), "utf-8");
      System.out.println("响应结果：" + body);
  }
  @Test
  public void deatilSendPostDataByJson() throws ClientProtocolException, IOException {
    String url = "http://192.168.50.103:8080/ECAService/bankService";
    /*   Map<String, String> map = new HashMap<String, String>();
      map.put("name", "wyj");
      map.put("city", "南京");*/
    JSONObject json= (JSONObject) JSONObject.parse("{\r\n" + 
        "  head:{\r\n" + 
        "    cid:\"\",\r\n" + 
        "    pkg_sn:\"20190621164849000345\",\r\n" + 
        "    security:\"\"\r\n" + 
        "  },\r\n" + 
        "  req:{\r\n" + 
        "    bank_code:\"310\",\r\n" + 
        "    trade_type:\"queryDetail\",\r\n" + 
        "    account_code:\"952A9997220008092\",\r\n" + 
        "    account_name:\"\",\r\n" + 
        "    currency_code:\"\",\r\n" + 
        "    begin_date:\"20190620\",\r\n" + 
        "    end_date:\"20190620\",\r\n" + 
        "    next_mark:\"1\",\r\n" + 
        "    district:\"1\",\r\n" + 
        "    reserved1:\"\",\r\n" + 
        "    reserved2:\"\",\r\n" + 
        "    reserved3:\"\",\r\n" + 
        "    reserved4:\"\"\r\n" + 
        "  }\r\n" + 
        "}");
    String body = sendPostDataByJson(url, json.toJSONString(), "utf-8");
    System.out.println("响应结果：" + body);
  }
  @Test
  public void QueryTransSendPostDataByJson() throws ClientProtocolException, IOException {
    String url = "http://192.168.50.103:8080/ECAService/bankService";
    /*   Map<String, String> map = new HashMap<String, String>();
      map.put("name", "wyj");
      map.put("city", "南京");*/
    JSONObject json= (JSONObject) JSONObject.parse("{\r\n" + 
        "  head:{\r\n" + 
        "    cid:\"\",\r\n" + 
        "    pkg_sn:\"20190621164849000250\",\r\n" + 
        "    security:\"\"\r\n" + 
        "  },\r\n" + 
        "  req:{\r\n" + 
        "    bank_code:\"310\",\r\n" + 
        "    trade_type:\"queryTrans\",\r\n" + 
        "    trade_date:\"20190617\",\r\n" + 
        "    account_code:\"952A9997220008092\",\r\n" + 
        "    serial:\"\",\r\n" + 
        "    public:\"\"\r\n" + 
        "  }\r\n" + 
        "}");
    String body = sendPostDataByJson(url, json.toJSONString(), "utf-8");
    System.out.println("响应结果：" + body);
  }
  @Test
  public void transSendPostDataByJson() throws ClientProtocolException, IOException {
    String url = "http://192.168.50.103:8080/ECAService/bankService";
    /*   Map<String, String> map = new HashMap<String, String>();
      map.put("name", "wyj");
      map.put("city", "南京");*/
    JSONObject json= (JSONObject) JSONObject.parse("    {\r\n" + 
        "      head:{\r\n" + 
        "        cid:\"\",\r\n" + 
        "        pkg_sn:\"20190621195951000366\",\r\n" + 
        "        security:\"\"\r\n" + 
        "      },\r\n" + 
        "      req:{\r\n" + 
        "        bank_code:\"310\",\r\n" + 
        "        trade_type:\"trans\",\r\n" + 
        "        pay_account:\r\n" + 
        "            {\r\n" + 
        "              account_name:\"浦发2489675304\",\r\n" + 
        "              account_code:\"952A9997220008092\",\r\n" + 
        "              hbank_code:\"\",\r\n" + 
        "              hbank_name:\"\",\r\n" + 
        "              branch_code:\"\",\r\n" + 
        "              currency_code:\"\",\r\n" + 
        "              district:\"\",\r\n" + 
        "              reserved1:\"\",\r\n" + 
        "              reserved2:\"\",\r\n" + 
        "              reserved3:\"\",\r\n" + 
        "              reserved4:\"\"\r\n" + 
        "            },\r\n" + 
        "       rec_account:\r\n" + 
        "            {\r\n" + 
        "              account_name:\"浦发2878487587\",\r\n" + 
        "              account_code:\"952A9997220008349\",\r\n" + 
        "              account_type:\"0\",\r\n" + 
        "              hbank_code:\"\",\r\n" + 
        "              hbank_name:\"上海浦东发展银行江阴支行\",\r\n" + 
        "              branch_code:\"310302201005\",\r\n" + 
        "              currency_code:\"\",\r\n" + 
        "              district:\"江苏省无锡市\",\r\n" + 
        "              reserved1:\"\",\r\n" + 
        "              reserved2:\"\",\r\n" + 
        "              reserved3:\"\",\r\n" + 
        "              reserved4:\"\"\r\n" + 
        "            },  \r\n" + 
        "        pl_amount:\"1.00\",\r\n" + 
        "        trans_date:\"\",\r\n" + 
        "        usage:\"\",\r\n" + 
        "        serial:\"H20190621003\",\r\n" + 
        "        channel_type:\"0\",            \r\n" + 
        "        trade_speed:\"\",\r\n" + 
        "        public:\"\",\r\n" + 
        "        digest:\"\",\r\n" + 
        "        remark:\"\",\r\n" + 
        "        postscript:\"测试\",\r\n" + 
        "        reserved1:\"\",\r\n" + 
        "        reserved2:\"\",\r\n" + 
        "        reserved3:\"\",\r\n" + 
        "        reserved4:\"\"      \r\n" + 
        "      }\r\n" + 
        "    }");
    String body = sendPostDataByJson(url, json.toJSONString(), "utf-8");
    System.out.println("响应结果：" + body);
  }
  
  /**
   * post请求传输json数据
   * 
   * @param url
   * @param json
   * @param encoding
   * @return
   * @throws ClientProtocolException
   * @throws IOException
   */
  public static String sendPostDataByJson(String url, String json, String encoding) throws ClientProtocolException, IOException {
      String result = "";

      // 创建httpclient对象
      CloseableHttpClient httpClient = HttpClients.createDefault();

      // 创建post方式请求对象
      HttpPost httpPost = new HttpPost(url);

      // 设置参数到请求对象中
      StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
      stringEntity.setContentEncoding("utf-8");
      httpPost.setEntity(stringEntity);

      // 执行请求操作，并拿到结果（同步阻塞）
      CloseableHttpResponse response = httpClient.execute(httpPost);

      // 获取结果实体
      // 判断网络连接状态码是否正常(0--200都数正常)
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          result = EntityUtils.toString(response.getEntity(), "utf-8");
      }
      // 释放链接
      response.close();

      return result;
  }


}