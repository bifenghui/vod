package com.dcampus.vod.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.Global;
import com.dcampus.common.service.BaseService;
import com.dcampus.vod.util.JsonUtil;

/**
 * weblib的登录验证和资源下载
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class WeblibService extends BaseService{
	
	/**weblib中的接口地址*/
	String weblibUrl = Global.getWeblibUrl();
	String loginActionUrl = Global.getWeblibLoginUrl();
	String downloadActionUrl = Global.getWeblibDownloadUrl();
	String selectidActionUrl = Global.getWeblibSelectUrl();	
	
	/**建立weblib访问*/
	CloseableHttpClient weblibClient = HttpClients.createDefault();
	Header cookie = null;
	
	/**
	 * 登录weblib,执行查询用户action
	 * 
	 */
	public void loginWeblib(){
		String username = Global.getWelbibUsername();
		String passwd = Global.getWelbibPassword();
		
		HttpPost loginPost = new HttpPost(weblibUrl+loginActionUrl);
		List<NameValuePair> loginParams = new ArrayList<NameValuePair>();
		loginParams.add(new BasicNameValuePair("account", username));
		loginParams.add(new BasicNameValuePair("password", passwd));
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(loginParams,"UTF-8");
			loginPost.setEntity(uefEntity);
			CloseableHttpResponse loginResponse = weblibClient.execute(loginPost);
//			cookie = loginResponse.getFirstHeader("Set-Cookie");
//			System.out.println("用户: "+ username +"  cookie信息: "+ cookie.toString());
			try {
				HttpEntity responseEntiry = loginResponse.getEntity();
				//Cookie cookieStore = weblibClient.
				if(loginResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
					if(responseEntiry!=null){
						BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntiry.getContent()));
						
						//解析返回的Json数据
						String line = null;
						JSONObject loginObject;
						JSONObject memObject;
						
						while((line=rd.readLine())!=null){
							if(!line.isEmpty()){
								loginObject = new JSONObject(line);
								String loginValue = loginObject.getString("members");
								String loginValueOb = loginValue.substring(loginValue.indexOf("[")+1,loginValue.lastIndexOf("]"));
								
								memObject = new JSONObject(loginValueOb);
								int memId = memObject.getInt("id");
								
								if(selectMember(memId)){
									System.out.println("有此用户");
								}
							}
						}
					}
				}else{
					System.out.println("=.=出问题了！");
				}
			} finally {
				loginResponse.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检测用户是否存在，不能删除，否则会造成未成功登录
	 * @param id
	 * @return
	 */
	public boolean selectMember(int id){
		HttpPost mempost = new HttpPost(weblibUrl+selectidActionUrl+"?memberId="+id);
		CloseableHttpResponse memResponse = null;
		boolean flag = false;
		try {
			memResponse = weblibClient.execute(mempost);
			if(memResponse.getEntity()!=null){
				BufferedReader rd = new BufferedReader(new InputStreamReader(memResponse.getEntity().getContent()));
				String line = null;
				JSONObject memberOb;
				while((line=rd.readLine())!=null){
					if(!line.isEmpty()){
						try {
							memberOb = new JSONObject(line);
							int memId = memberOb.getInt("id");
							if(memId==id){
								flag = true;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}finally{
			try {
				memResponse.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
//	String loginActionUrl = Global.getWeblibLoginUrl();
//	String downloadActionUrl = Global.getWeblibDownloadUrl();
//	Header cookie = null;
//	
//	
//	/**建立weblib访问*/
//	CloseableHttpClient weblibClient = HttpClients.createDefault();
//	
//	   
//	/**
//	 * description:登录weblib,执行查询用户action
//	 * @param 在Global.xml中配置的用户名和密码登录weblib
//	 * @return
//	 */
//	public void loginWeblib(){
//		String username = Global.getWelbibUsername();
//		String passwd = Global.getWelbibPassword();
//		
//		HttpPost loginPost = new HttpPost(weblibUrl+loginActionUrl);
//		List<NameValuePair> loginParams = new ArrayList<NameValuePair>();
//		loginParams.add(new BasicNameValuePair("account", username));
//		loginParams.add(new BasicNameValuePair("password", passwd));
//		UrlEncodedFormEntity uefEntity;
//		try {
//			uefEntity = new UrlEncodedFormEntity(loginParams,"UTF-8");
//			loginPost.setEntity(uefEntity);
//			loginPost.addHeader("Connection", "Keep-Alive");
//			CloseableHttpResponse loginResponse = weblibClient.execute(loginPost);
//			cookie = loginResponse.getFirstHeader("Set-Cookie");
//			try {
//				HttpEntity responseEntiry = loginResponse.getEntity();
//				//Cookie cookieStore = weblibClient.
//				System.out.println("用户: "+ username +"  cookie信息: "+ cookie.toString());
//				System.out.println(loginResponse.getStatusLine().getStatusCode());
//				System.out.println(loginResponse.getEntity().getContent().toString());	
//				if(loginResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
//					if(responseEntiry!=null){
//						BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntiry.getContent()));
//						
//						//解析返回的Json数据
//						String line = null;
//						JSONObject loginObject;
//						JSONObject memObject;
//						
//						while((line=rd.readLine())!=null){
//							if(!line.isEmpty()){
//								loginObject = new JSONObject(line);
//								String loginValue = loginObject.getString("members");
//								System.out.println(loginValue);
//								String loginValueOb = loginValue.substring(loginValue.indexOf("[")+1,loginValue.lastIndexOf("]"));
//								
//								memObject = new JSONObject(loginValueOb);
//								int memId = memObject.getInt("id");
//
//							}
//						}
//					}
//				}else{
//					System.out.println("=.=出问题了！");
//				}
//			} finally {
//				loginResponse.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
		
	
	/**
	 * description:向weblib请求下载资源
	 * @param filewid 资源在weblib中对应的id
	 * @return filename 下载成功返回资源名字,下载失败返回null
	 */
	public String downloadFromWeblib(Long filewid){
		boolean over = false;
		String filename = null;
		CloseableHttpResponse downResponse = null;
		/** 资源存放路径*/
		String downUrl = Global.getFileRootPath();
		try{
			HttpGet downGet = new HttpGet(weblibUrl + downloadActionUrl +"?id=" + filewid);
			downGet.addHeader("Connection", "Keep-Alive");
			downResponse = weblibClient.execute(downGet);
			String temp = downResponse.getFirstHeader("Content-Disposition").getValue();
			String[] args = temp.split("\"");
			filename = args[1];	
			System.out.println(filename);
			
			
			StatusLine statusLine = downResponse.getStatusLine();
			System.out.println(statusLine.getStatusCode());
			if(statusLine.getStatusCode() == 200){
				File file = new File(downUrl+filename);
				FileOutputStream outputStream = new FileOutputStream(file);
				InputStream inputStream = downResponse.getEntity().getContent();
				byte length[] = new byte[1024];
				int len=0;
				while((len = inputStream.read(length))!=-1){
					outputStream.write(length, 0, len);
				}
				outputStream.flush();
				outputStream.close();
			}	
			over = true;
		}catch(Exception e){
			e.printStackTrace();
			over = false;
		}finally{
			try {
				downResponse.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(over = true){
			return filename;
		}else{
			return null;
		}
	}
	
}
