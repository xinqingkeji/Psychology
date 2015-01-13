package com.dcy.psychology.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.dcy.psychology.MyApplication;
import com.dcy.psychology.R;
import com.dcy.psychology.gsonbean.CommentBean;
import com.dcy.psychology.gsonbean.CommentDetailBean;
import com.dcy.psychology.gsonbean.LoginBean;
import com.dcy.psychology.model.UserInfoModel;
import com.dcy.psychology.view.QuestionView;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.activity.LoginActivity;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.easemob.util.HanziToPinyin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.Toast;

public class Utils {
	public static String loadRawString(Context context , int resId) {
		StringBuffer stringBuffer = new StringBuffer();
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = context.getResources().openRawResource(resId);
			String temp = null;
			br = new BufferedReader(new InputStreamReader(is, "GBK"));
			while ((temp = br.readLine()) != null) {
				stringBuffer.append(temp);
				stringBuffer.append("\n");
			}
		} catch (NotFoundException e) {
			Toast.makeText(context, R.string.not_found_exception,
					Toast.LENGTH_SHORT).show();
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(context, R.string.unsupported_encoding,
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(context, R.string.io_exception, Toast.LENGTH_SHORT)
					.show();
		} finally {
			try {
				if (is != null)
					is.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuffer.toString();
	}
	
	private static SoapObject getResultFromRequest(SoapObject request) {
		//���ɵ���WebService��SOAP����
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
		envelope.bodyOut = request;
		envelope.dotNet = true;
		HttpTransportSE transportSE = new HttpTransportSE(Constants.UserWSDL, Constants.TimeOut);
		try {
			//1.1�汾��Ҫʹ�õ�һ������SoapAction(��http://114.215.179.130/Login),1.2����ҪSoapAction
			transportSE.call("", envelope);
		} catch (HttpResponseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		/*SoapObject result = null;
		try {
			result = (SoapObject) envelope.getResponse();
		} catch (SoapFault e) {
			e.printStackTrace();
		}*/
		//SoapFault error = (SoapFault)envelope.bodyIn;
		//Log.i("mylog", error.toString());
		
		SoapObject result = null;
		try {
			result = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			if(envelope.bodyIn instanceof SoapFault){
				Log.i("mylog" , envelope.bodyIn.toString());
			}else {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static LoginBean getLoginWeb(String name,String pwd){
		//�˴�����û�С�/������Ȼ�ᵼ�¡�û����Ч�Ĳ�������<1.2>������1.0�����޷����ݲ���
		SoapObject request = new SoapObject(Constants.SpaceName,Constants.LoginMethod);
		//���õ��ò���,1.2�汾ǰ��������Ҫ�������һ��
		request.addProperty("loginName", name);
		request.addProperty("loginPwd", pwd);
		SoapObject result = getResultFromRequest(request);
		if(result == null){
			return new LoginBean();
		}
		return MyApplication.mGson.fromJson(result.getPropertyAsString(0), LoginBean.class);
	}
	
	public static void getRegisterResult(UserInfoModel user){
		SoapObject request = new SoapObject(Constants.SpaceName,Constants.RegisterUserMethod);
		request.addProperty("userLoginName", user.getUserLoginName());
		request.addProperty("userPwd", user.getUserPwd());
		request.addProperty("userName", user.getUserName());
		request.addProperty("userSex", user.getUserSex());
		request.addProperty("userAge", user.getUserAge());
		request.addProperty("userPhone", user.getUserPhone());
		request.addProperty("userEmail", user.getUserEmail());
		request.addProperty("pwdQuestion", user.getPwdQuestion());
		request.addProperty("pwdAnswer", user.getPwdAnswer());
		SoapObject result = getResultFromRequest(request);
		if(result == null){
			//return new LoginBean();
		}
	}
	
	public static String publishComment(String loginName , String comment){
		SoapObject request = new SoapObject(Constants.SpaceName,Constants.PublishComment);
		request.addProperty("userLoginName", loginName);
		request.addProperty("fileName", "");
		request.addProperty("heartWeiBo", comment);
		request.addProperty("img", null);
		SoapObject result = getResultFromRequest(request);
		if(result == null){
			return "";
		}
		try {
			return (new JSONObject(result.getPropertyAsString(0))).getString("PublishState");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<CommentBean> getCommentList(int pageIndex){
		SoapObject request = new SoapObject(Constants.SpaceName,Constants.GetCommentList);
		request.addProperty("pageIndex", pageIndex);
		request.addProperty("pageSize", Constants.PageCount);
		SoapObject result = getResultFromRequest(request);
		if(result == null){
			return new ArrayList<CommentBean>();
		}
		return MyApplication.mGson.fromJson(result.getPropertyAsString(0), new TypeToken<ArrayList<CommentBean>>(){}.getType());
	}
	
	public static boolean commentItem(int id, String content){
		SoapObject request = new SoapObject(Constants.SpaceName, Constants.CommentItem);
		request.addProperty("heartWeiBoID", id);
		request.addProperty("reviewUserLoginName", MyApplication.myUserName);
		request.addProperty("reviewContent", content);
		SoapObject result = getResultFromRequest(request);
		if(result == null)
			return false;
		return Boolean.valueOf(result.getProperty(0).toString());
	}
	
	public static ArrayList<CommentDetailBean> getCommentDetail(int id){
		SoapObject request = new SoapObject(Constants.SpaceName, Constants.GetCommentDetail);
		request.addProperty("heartWeiBoID", id);
		SoapObject result = getResultFromRequest(request);
		if(result == null)
			return new ArrayList<CommentDetailBean>();
		return MyApplication.mGson.fromJson(result.getPropertyAsString(0), new TypeToken<ArrayList<CommentDetailBean>>(){}.getType());
	}
	
	public static String getArticleList(){
		SoapObject request = new SoapObject(Constants.SpaceName,Constants.GetArticleMethod);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
		envelope.bodyOut = request;
		envelope.dotNet = true;
		HttpTransportSE transportSE = new HttpTransportSE(Constants.ArticleWSDL, Constants.TimeOut);
		try {
			transportSE.call("", envelope);
		} catch (HttpResponseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		SoapObject result = (SoapObject) envelope.bodyIn;
		return "";
	}
	
	public static class MainTabAdapter extends PagerAdapter{
		private FragmentManager fm;
		private FragmentTransaction ft;
		private List<Fragment> dataList;
		
		public MainTabAdapter(FragmentManager fm , List<Fragment> dataList) {
			this.fm = fm;
			ft = fm.beginTransaction();
			this.dataList = dataList;
		}
		
		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return ((Fragment)object).getView() == view;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.i("mylog", "++++++++++++");
			if(ft == null)
				ft = fm.beginTransaction();
			Fragment item = dataList.get(position);
			ft.show(item);
			return item;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.i("mylog", "-------------");
			if(ft == null)
				ft = fm.beginTransaction();
			ft.hide((Fragment)object);
		}
		
		@Override
		public void finishUpdate(ViewGroup container) {
			Log.i("mylog", "uppppppppppppp");
			if(ft != null){
				ft.commit();
				ft = null;
			}
		}
	}
	
	public static class ViewAdapter extends PagerAdapter {
		private ArrayList<View> viewList;

		public ViewAdapter(ArrayList<View> viewList) {
			this.viewList = viewList;
		}

		@Override
		public int getCount() {
			return viewList.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(viewList.get(position));
			return viewList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(viewList.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
	
	public static String getSDPath(){
		boolean isExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if(isExist){
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}
	
	public static boolean validatePhoneNumber(String phoneNumber) {
		Pattern pattern = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");
		Matcher m = pattern.matcher(phoneNumber);
		return m.matches();
	}
	
	/**
	 * ��ȡ���µ�ʱ��
	 * 
	 * @param dateStr
	 * @return
	 * @throws Exception
	 */
	public static String getDateString(Calendar date) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (calendar.get(Calendar.YEAR) - date.get(Calendar.YEAR) > 0) {
			return sdf.format(date.getTime());
		} else if (calendar.get(Calendar.MONTH) - date.get(Calendar.MONTH) > 0) {
			return sdf.format(date.getTime());
		} else if (calendar.get(Calendar.DAY_OF_MONTH) - date.get(Calendar.DAY_OF_MONTH) > 6) {
			return sdf.format(date.getTime());
		} else if ((calendar.get(Calendar.DAY_OF_MONTH) - date.get(Calendar.DAY_OF_MONTH) > 0) && 
				(calendar.get(Calendar.DAY_OF_MONTH) - date.get(Calendar.DAY_OF_MONTH) < 6)) {
			int i = calendar.get(Calendar.HOUR_OF_DAY) - date.get(Calendar.HOUR_OF_DAY);
			return i + "��ǰ";
		} else if (calendar.get(Calendar.HOUR_OF_DAY) - date.get(Calendar.HOUR_OF_DAY) > 0) {
			int i = calendar.get(Calendar.HOUR_OF_DAY) - date.get(Calendar.HOUR_OF_DAY);
			return i + "Сʱǰ";
		} else if (calendar.get(Calendar.MINUTE) - date.get(Calendar.MINUTE) > 0) {
			int i = calendar.get(Calendar.MINUTE) - date.get(Calendar.MINUTE);
			return i + "����ǰ";
		} else if (calendar.get(Calendar.SECOND) - date.get(Calendar.SECOND) > 0) {
			int i = calendar.get(Calendar.SECOND) - date.get(Calendar.SECOND);
			return i + "��ǰ";
		} else if (calendar.get(Calendar.SECOND) - date.get(Calendar.SECOND) == 0) {
			return "�ո�";
		} else {
			return sdf.format(date);
		}
	}
	
	public static void getFriends(Context context){
		try {
			List<String> usernames = EMChatManager.getInstance().getContactUserNames();
			Map<String, User> userlist = new HashMap<String, User>();
			for (String username : usernames) {
				User user = new User();
				user.setUsername(username);
				setUserHearder(username, user);
				userlist.put(username, user);
			}
			User newFriends = new User();
			newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
			newFriends.setNick("������֪ͨ");
			newFriends.setHeader("");
			userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
			User groupUser = new User();
			groupUser.setUsername(Constant.GROUP_USERNAME);
			groupUser.setNick("Ⱥ��");
			groupUser.setHeader("");
			userlist.put(Constant.GROUP_USERNAME, groupUser);

			MyApplication.getInstance().setContactList(userlist);
			UserDao dao = new UserDao(context);
			List<User> users = new ArrayList<User>(userlist.values());
			dao.saveContactList(users);

			EMGroupManager.getInstance().getGroupsFromServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setUserHearder(String username, User user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick();
		} else {
			headerName = user.getUsername();
		}
		if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
			user.setHeader("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			user.setHeader("#");
		} else {
			user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}
	
	/*static HostnameVerifier hv = new HostnameVerifier() {  
		@Override
		public boolean verify(String urlHostName, SSLSession session) {  
	        System.out.println("Warning: URL Host: " + urlHostName + " vs. "  
	                           + session.getPeerHost());  
	        return true;  
	    }
	
	};  
	  
	private static void trustAllHttpsCertificates() throws Exception {  
	    javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
	    javax.net.ssl.TrustManager tm = new miTM();  
	    trustAllCerts[0] = tm;  
	    javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
	            .getInstance("SSL");  
	    sc.init(null, trustAllCerts, null);  
	    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
	            .getSocketFactory());  
	}  
	
	static class miTM implements javax.net.ssl.TrustManager,  
	        javax.net.ssl.X509TrustManager {  
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	        return null;  
	    }  
	
	    public boolean isServerTrusted(  
	            java.security.cert.X509Certificate[] certs) {  
	        return true;  
	    }  
	
	    public boolean isClientTrusted(  
	            java.security.cert.X509Certificate[] certs) {  
	        return true;  
	    }  
	
	    public void checkServerTrusted(  
	            java.security.cert.X509Certificate[] certs, String authType)  
	            throws java.security.cert.CertificateException {  
	        return;  
	    }  
	
	    public void checkClientTrusted(  
	            java.security.cert.X509Certificate[] certs, String authType)  
	            throws java.security.cert.CertificateException {  
	        return;  
	    }  
	}  
	
		try {
			trustAllHttpsCertificates();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	*/
}