package com.reeching.uoter.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.just.agentweb.AgentWeb;
import com.reeching.uoter.BaseApplication;
import com.reeching.uoter.DemoHelper;
import com.reeching.uoter.R;
import com.reeching.uoter.db.DemoDBManager;
import com.reeching.uoter.ui.MainActivity;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;
import static com.reeching.uoter.ui.CallActivity.TAG;

/**
 * Created by 绍轩 on 2018/3/30.
 * 前端交互
 */

public class AndroidInterfaceForJS {

    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Activity activity;
    private boolean progressShow;
    private boolean autoLogin = false;

    public AndroidInterfaceForJS(AgentWeb agent, Activity activity) {
        this.agent = agent;
        this.activity = activity;
    }


    //登录
    @JavascriptInterface
    public void callAndroid(final String name,final String paw,final String token) {

        PreferenceManager.getInstance().setCurrentUserToken(token);
        PreferenceManager.getInstance().setCurrentUserName(name);
        PreferenceManager.getInstance().setCurrentUserPsw(paw);
        LogUtils.d(token);
        LogUtils.d(name);
        LogUtils.d(paw);
        login(name,paw);

//        deliver.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("Info", "main Thread:" + Thread.currentThread());
////                Toast.makeText(activity, name+paw, Toast.LENGTH_LONG).show();
//            }
//        });
//        Map<String,Object> params=new HashMap<>();
//        params.put("name",name);
//        params.put("paw",paw);
//        ActivityUtil.openAct(activity, LoginActivity.class,params);
//        Log.i("Info", "Thread:" + Thread.currentThread());
    }


    public void login(String name,String paw) {
        if (!EaseCommonUtils.isNetWorkConnected(activity)) {
            Toast.makeText(activity, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
//        String currentUsername = name;
//        String currentPassword = paw;
        LogUtils.i("账户="+name+"密码="+paw);

		String currentUsername = "www";
		String currentPassword = "123456";
        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(activity, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(activity, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        progressShow = true;
        final ProgressDialog pd = new ProgressDialog(activity);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "EMClient.getInstance().onCancel");

                progressShow = false;
            }
        });
        pd.setMessage(activity.getString(R.string.Is_landing));
        pd.show();

        // After logout，the DemoDB may still be accessed due to async callback, so the DemoDB will be re-opened again.
        // close it before login to make sure DemoDB not overlap
        DemoDBManager.getInstance().closeDB();

        // reset current user name before login
        DemoHelper.getInstance().setCurrentUserName(currentUsername);

        final long start = System.currentTimeMillis();
        // call login method
        Log.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");


                // ** manually load all local groups and conversation
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();

                // update current user's display name for APNs
                boolean updatenick = EMClient.getInstance().pushManager().updatePushNickname(
                        BaseApplication.currentUserNick.trim());
                if (!updatenick) {
                    Log.e("LoginActivity", "更新信息失败");
                }

                if (!activity.isFinishing() && pd.isShowing()) {
                    pd.dismiss();
                }
                //通知h5环信登录成功
                agent.getJsAccessEntrace().quickCallJs("AndroidLogin");
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress");
            }

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login: onError: " + code);
                if (!progressShow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        Toast.makeText(activity, activity.getString(R.string.Login_failed) + message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    @JavascriptInterface
    public void addressBookAndroid() {
        //通讯录
        ActivityUtil.openAct(activity, MainActivity.class);
    }
    @JavascriptInterface
    public void logoutAndroid() {
        //退出登录
        LogUtils.d("退出登录");
        DemoHelper.getInstance().logout(true,null);

    }



}
