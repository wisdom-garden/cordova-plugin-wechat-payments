package com.tronclass.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tronclass.cordova.Config;


/**
 * This class echoes a string called from JavaScript.
 */
public class WeChatPayment extends CordovaPlugin {

    public static final String TAG = "Weixin";

	public static final String ERROR_WX_NOT_INSTALLED = "未安装微信";

	protected IWXAPI api;
    protected static CallbackContext currentCallbackContext;
	private String appId;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
   		// save the current callback context
   		currentCallbackContext = callbackContext;
   		// check if installed
   		if (api != null && !api.isWXAppInstalled()) {
    		callbackContext.error(ERROR_WX_NOT_INSTALLED);
    		return true;
    	}

    	if (action.equals("initApp")) {
			return initApp(args);
		} else if(action.equals("sendPayReq")) {
    		return sendPayReq(args);
   		}

   		return false;
   	}

	protected boolean initApp(JSONArray args){
		Log.i(TAG, "init app id");
		try {
			String appId = args.getJSONObject(0).getString("appId");
			Config.init(appId);
			this.appId = appId;
			getWXAPI();
			currentCallbackContext.success();
		} catch (JSONException e) {
			e.printStackTrace();
			currentCallbackContext.error("参数错误");
			return false;
		}
		return true;
	}

    protected boolean sendPayReq(JSONArray args) {
   		Log.i(TAG, "pay begin");
   		try {
   			JSONObject prepayWeXinObj = args.getJSONObject(0);
   			String appId = prepayWeXinObj.getString("appid");
   			String prepayId = prepayWeXinObj.getString("prepayid");
   			String partnerId = prepayWeXinObj.getString("partnerid");
   			String nonceStr = prepayWeXinObj.getString("noncestr");
   			String timeStamp = prepayWeXinObj.getString("timestamp");
   			String packageValue = prepayWeXinObj.getString("package");
   			String sign = prepayWeXinObj.getString("sign");

   			final PayReq req = new PayReq();
			req.appId = appId;
			req.partnerId = partnerId;
			req.prepayId = prepayId;
			req.packageValue = packageValue;
			req.nonceStr = nonceStr;
			req.timeStamp = timeStamp;
			req.sign = sign;

			api.registerApp(appId);
			cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					Boolean sended = api.sendReq(req);
					if(!sended){
						currentCallbackContext.error("发送支付请求失败");
					}
				}
			});
    	} catch (JSONException e) {
    		e.printStackTrace();
    		currentCallbackContext.error("参数错误");
    		return false;
   		}
   		return true;
   	}


    protected void getWXAPI() {
    	if (api == null) {
    		api = WXAPIFactory.createWXAPI(webView.getContext(), appId, true);
   			api.registerApp(appId);
   		}
   	}


    @Override
   	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
   		super.initialize(cordova, webView);
   		this.onWeixinResp(cordova.getActivity().getIntent());
   	}

    private void onWeixinResp(Intent intent) {
    	Bundle extras =  intent.getExtras();
       	if(extras!=null){
       		String intentType = extras.getString("intentType");
      		if("com.tronclass.cordova.plugin.WeChatPayment".equals(intentType)){
        		if(currentCallbackContext != null){
        			currentCallbackContext.success(extras.getInt("weixinPayRespCode"));
   				}
   			}
       	}
  	}

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       	Log.i(TAG, "onNewIntent");
   		this.onWeixinResp(intent);
   	}
}
