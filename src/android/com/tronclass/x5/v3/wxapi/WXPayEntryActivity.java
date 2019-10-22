package com.tronclass.x5.v3.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tronclass.cordova.Config;
import com.tronclass.x5.base.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{

	private static final String TAG = "Weixin";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	api = WXAPIFactory.createWXAPI(this, Config.getAppId());
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			Intent intent;
			try {
				intent = new Intent(this, WXPayEntryActivity.class.getClassLoader().loadClass(Constants.PACKNAME+ "."+ Constants.ACTIVITYCLASSNAME));
				Bundle bundle=new Bundle();
				bundle.putInt("weixinPayRespCode",  resp.errCode);
				bundle.putString("intentType", "com.tronclass.cordova.plugin.WeChatPayment");
			    intent.putExtras(bundle);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    Log.i(TAG, "startActivity");
			    startActivity(intent);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
