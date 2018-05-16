package com.reeching.uoter.ui; /**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMClientListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMMultiDeviceListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;
import com.just.agentweb.AbsAgentWebUIController;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.IAgentWebSettings;
import com.reeching.uoter.BaseApplication;
import com.reeching.uoter.Constant;
import com.reeching.uoter.DemoHelper;
import com.reeching.uoter.R;
import com.reeching.uoter.db.InviteMessgeDao;
import com.reeching.uoter.db.UserDao;
import com.reeching.uoter.runtimepermissions.PermissionsManager;
import com.reeching.uoter.runtimepermissions.PermissionsResultAction;
import com.reeching.uoter.ui.activity.MessageFragment;
import com.reeching.uoter.utils.AndroidInterfaceForJS;
import com.reeching.uoter.utils.LogUtils;
import com.reeching.uoter.view.XRadioGroup;

import java.util.List;

@SuppressLint("NewApi")
public class MainActivity extends BaseActivity implements XRadioGroup.OnCheckedChangeListener {

    protected static final String TAG = "MainActivity";
    // textview for unread message count
    private static TextView unreadLabel;
    // textview for unread event message
//	private TextView unreadAddressLable;
    private XRadioGroup radioGroup;
    private Button[] mTabs;
    //	private ContactListFragment contactListFragment;
//	ConversationListFragment conversationListFragment;
//	private Fragment[] fragments;
    private int index;
    // user logged into another device
    //用户登录到另一个设备
    public boolean isConflict = false;
    // user account was removed
    //用户帐户被删除
    private boolean isCurrentAccountRemoved = false;
    private LinearLayout mLinearLayout;
    private AgentWeb mAgentWeb;
    private int count;
    private MessageFragment fragment;


    /**
     * check if current user account was remove
     * 检查当前用户帐户是否被删除
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getInstance().addActivity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    //some device doesn't has activity to handle this intent
                    //so add try catch
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }

        //make sure activity will not in background if user is logged into another device or removed
        //如果用户被登录到另一个设备或删除，请确保活动不会在后台运行
        if (getIntent() != null &&
                (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) ||
                        getIntent().getBooleanExtra(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD, false) ||
                        getIntent().getBooleanExtra(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE, false))) {
            DemoHelper.getInstance().logout(false, null);
            //退出登录
            mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidClear");
            finish();
//			startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (getIntent() != null && getIntent().getBooleanExtra("isConflict", false)) {
            mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidClear");
            finish();
//			startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        setContentView(R.layout.em_activity_main);
        // runtime permission for android 6.0, just require all permissions here for simple
        requestPermissions();
        fragment = new MessageFragment();
        initView();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        showExceptionDialogFromIntent(getIntent());

        inviteMessgeDao = new InviteMessgeDao(this);
        UserDao userDao = new UserDao(this);

//		conversationListFragment = new ConversationListFragment();
//		contactListFragment = new ContactListFragment();
        SettingsFragment settingFragment = new SettingsFragment();


        EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
        EMClient.getInstance().addClientListener(clientListener);
        EMClient.getInstance().addMultiDeviceListener(new MyMultiDeviceListener());
        //debug purpose only
        registerInternalDebugReceiver();
    }

    EMClientListener clientListener = new EMClientListener() {
        @Override
        public void onMigrate2x(boolean success) {
            Toast.makeText(MainActivity.this, "onUpgradeFrom 2.x to 3.x " + (success ? "success" : "fail"), Toast.LENGTH_LONG).show();
            if (success) {
                refreshUIWithMessage();
            }
        }
    };

    @TargetApi(23)

    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * init views
     */
    private void initView() {
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
        RelativeLayout rlUoter = (RelativeLayout) findViewById(R.id.rl_uoter);
        rlUoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidiNdex");
            }
        });
//		unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);
        radioGroup = (XRadioGroup) findViewById(R.id.main_bottom);
        radioGroup.setOnCheckedChangeListener(this);
        mLinearLayout = (LinearLayout) this.findViewById(R.id.line1);
        mAgentWeb = AgentWeb
                .with(this)//传入Activity or Fragment
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .setWebViewClient(mWebViewClient)
                .createAgentWeb()
                .ready()
                .go("");
//                .go("file:///android_asset/index.html");
        //注入对象
        IAgentWebSettings agentWebSettings = mAgentWeb.getAgentWebSettings();
        agentWebSettings.getWebSettings().setDomStorageEnabled(true);
//        agentWebSettings.getWebSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        agentWebSettings.getWebSettings().setUseWideViewPort(true);

        agentWebSettings.getWebSettings().setLoadWithOverviewMode(true);
        agentWebSettings.getWebSettings().setAllowFileAccess(true); //允许file 协议 ， 加载本地文件
        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterfaceForJS(mAgentWeb, this));

    }

    protected WebViewClient mWebViewClient = new WebViewClient() {
        //
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            Log.i(TAG, "view:" + new Gson().toJson(view.getHitTestResult()));
            Log.i(TAG, "mWebViewClient shouldOverrideUrlLoading:" + url);
            Log.i(TAG, "mWebViewClient shouldOverride:" + view.getUrl());
            //intent:// scheme的处理 如果返回false ， 则交给 DefaultWebClient 处理 ， 默认会打开该Activity  ， 如果Activity不存在则跳到应用市场上去.  true 表示拦截
            //例如优酷视频播放 ，intent://play?...package=com.youku.phone;end;
            //优酷想唤起自己应用播放该视频 ， 下面拦截地址返回 true  则会在应用内 H5 播放 ，禁止优酷唤起播放该视频， 如果返回 false ， DefaultWebClient  会根据intent 协议处理 该地址 ， 首先匹配该应用存不存在 ，如果存在 ， 唤起该应用播放 ， 如果不存在 ， 则跳到应用市场下载该应用 .
//            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
//                return true;
//            }

			/*else if (isAlipay(view, mUrl))   /    /1.2.5开始不用调用该方法了 ，只要引入支付宝sdk即可 ， DefaultWebClient 默认会处理相应url调起支付宝
                return true;*/
            return false;
        }

        //加载完成之后调用
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            radioGroup.setVisibility(View.VISIBLE);
            LogUtils.i(url);
            LogUtils.i(view.getUrl());
//            String url1 = view.getUrl();
//            if (url1.indexOf("#/") != -1 && view != null) {
//                //截取之后的字符
//                String endUrl = url1.substring(url1.indexOf("#/"));
//                LogUtils.i(endUrl);
//                if ("#/".equals(endUrl)||"#/mine".equals(endUrl) || "#/index".equals(endUrl) || "#/team".equals(endUrl) || "#/account".equals(endUrl)) {
//                    radioGroup.setVisibility(View.VISIBLE);
//                } else {
//                    radioGroup.setVisibility(View.GONE);
//                }
//            }
        }

        @Override

        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        /*错误页回调该方法 ， 如果重写了该方法， 上面传入了布局将不会显示 ， 交由开发者实现，注意参数对齐。*/
        public void onMainFrameError(AbsAgentWebUIController agentWebUIController, WebView view, int errorCode, String description, String failingUrl) {
            Log.i(TAG, "AgentWebFragment onMainFrameError");
            agentWebUIController.onMainFrameError(view, errorCode, description, failingUrl);

        }

    };
    EMMessageListener messageListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            // notify new message
            for (EMMessage message : messages) {
                DemoHelper.getInstance().getNotifier().onNewMsg(message);
            }
            refreshUIWithMessage();
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            refreshUIWithMessage();
        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {
        }

        @Override
        public void onMessageDelivered(List<EMMessage> message) {
        }

        @Override
        public void onMessageRecalled(List<EMMessage> messages) {
            refreshUIWithMessage();
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
        }
    };

    //接受到消息时更新布局
    private void refreshUIWithMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                // refresh unread count
                updateUnreadLabel();
                //刷新UI
                fragment.refresh();
            }
        });
    }

    @Override
    public void back(View view) {
        super.back(view);
    }


    @Override
    public void onCheckedChanged(XRadioGroup group, @IdRes int checkedId) {

        switch (checkedId) {
            case R.id.rb_home_bill: {
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidaCcount");//首页
            }
            break;
            case R.id.rb_home_team: {
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidtEam");//团队
            }
            break;
            case R.id.rb_home_mag: {
                mLinearLayout.setVisibility(View.GONE);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if (fragment != null) {
                    fragmentTransaction.hide(fragment);
                }
                if (!fragment.isAdded()) {
                    fragmentTransaction.add(R.id.container, fragment);
                } else {
                    fragmentTransaction.show(fragment);
                }
                fragmentTransaction.commit();
            }
            break;
            case R.id.rb_home_home: {
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidmIne");
            }
            break;
        }
    }

    public class MyContactListener implements EMContactListener {
        @Override
        public void onContactAdded(String username) {
        }

        @Override
        public void onContactDeleted(final String username) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (ChatActivity.activityInstance != null && ChatActivity.activityInstance.toChatUsername != null &&
                            username.equals(ChatActivity.activityInstance.toChatUsername)) {
                        String st10 = getResources().getString(R.string.have_you_removed);
                        Toast.makeText(MainActivity.this, ChatActivity.activityInstance.getToChatUsername() + st10, Toast.LENGTH_LONG)
                                .show();
                        ChatActivity.activityInstance.finish();
                    }
                }
            });
            updateUnreadAddressLable();
        }

        @Override
        public void onContactInvited(String username, String reason) {
        }

        @Override
        public void onFriendRequestAccepted(String username) {
        }

        @Override
        public void onFriendRequestDeclined(String username) {
        }
    }

    public class MyMultiDeviceListener implements EMMultiDeviceListener {

        @Override
        public void onContactEvent(int event, String target, String ext) {

        }

        @Override
        public void onGroupEvent(int event, String target, final List<String> username) {
            switch (event) {
                case EMMultiDeviceListener.GROUP_LEAVE:
                    ChatActivity.activityInstance.finish();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();

        if (exceptionBuilder != null) {
            exceptionBuilder.create().dismiss();
            exceptionBuilder = null;
            isExceptionDialogShow = false;
        }

        try {
            unregisterReceiver(internalDebugReceiver);
        } catch (Exception e) {
        }

    }

    /**
     * 更新会话未读计数
     */
    public void updateUnreadLabel() {
        runOnUiThread(new Runnable() {
            public void run() {
                count = getUnreadMsgCountTotal();
                if (count > 0) {
                    unreadLabel.setText(String.valueOf(count));
                    unreadLabel.setVisibility(View.VISIBLE);
                } else {
                    unreadLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * 更新系统通知未读计数
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int c = getUnreadAddressCountTotal();
                if (c > 0) {
                    unreadLabel.setText(String.valueOf(count + c));
                    unreadLabel.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * get unread event notification count, including application, accepted, etc
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        unreadAddressCountTotal = inviteMessgeDao.getUnreadMessagesCount();
        return unreadAddressCountTotal;
    }

    /**
     * get unread message count
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        return EMClient.getInstance().chatManager().getUnreadMsgsCount();
    }

    private static InviteMessgeDao inviteMessgeDao;

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            public void run() {
                if (DemoHelper.getInstance().isLoggedIn()) {
                    // 自动登录模式，确保所有的群组和对话在进入主屏幕之前被loaed
                    EMClient.getInstance().chatManager().loadAllConversations();
                    EMClient.getInstance().groupManager().loadAllGroups();

                } else {
                    mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidClear");
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAgentWeb.getWebLifeCycle().onResume();//恢复
        if (!isConflict && !isCurrentAccountRemoved) {
            updateUnreadLabel();
            updateUnreadAddressLable();
        }

        // unregister this event listener when this activity enters the
        // background
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.pushActivity(this);
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        EMClient.getInstance().removeClientListener(clientListener);
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.popActivity(this);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private android.app.AlertDialog.Builder exceptionBuilder;
    private boolean isExceptionDialogShow = false;
    private BroadcastReceiver internalDebugReceiver;


    private LocalBroadcastManager broadcastManager;

    private int getExceptionMessageId(String exceptionType) {
        if (exceptionType.equals(Constant.ACCOUNT_CONFLICT)) {
            return R.string.connect_conflict;
        } else if (exceptionType.equals(Constant.ACCOUNT_REMOVED)) {
            return R.string.em_user_remove;
        } else if (exceptionType.equals(Constant.ACCOUNT_FORBIDDEN)) {
            return R.string.user_forbidden;
        }
        return R.string.Network_error;
    }

    /**
     * show the dialog when user met some exception: such as login on another device, user removed or user forbidden
     * 当用户遇到一些异常时，显示对话框：例如在另一个设备上的登录，用户删除或禁用用户
     */
    private void showExceptionDialog(String exceptionType) {
        isExceptionDialogShow = true;
        DemoHelper.getInstance().logout(false, null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!MainActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (exceptionBuilder == null)
                    exceptionBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                exceptionBuilder.setTitle(st);
                exceptionBuilder.setMessage(getExceptionMessageId(exceptionType));
                exceptionBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        exceptionBuilder = null;
                        isExceptionDialogShow = false;
                        //通知h5页面退出登录
                        mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidClear");
                        //完全退出
                        BaseApplication.getInstance().exit(MainActivity.this);
//						Intent  intent= new Intent(MainActivity.this, LoginActivity.class);
//						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent);
                    }
                });
                exceptionBuilder.setCancelable(false);
                exceptionBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }
        }
    }

    private void showExceptionDialogFromIntent(Intent intent) {
        EMLog.e(TAG, "showExceptionDialogFromIntent");
        if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_CONFLICT, false)) {
            showExceptionDialog(Constant.ACCOUNT_CONFLICT);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_REMOVED, false)) {
            showExceptionDialog(Constant.ACCOUNT_REMOVED);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_FORBIDDEN, false)) {
            showExceptionDialog(Constant.ACCOUNT_FORBIDDEN);
        } else if (intent.getBooleanExtra(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD, false) ||
                intent.getBooleanExtra(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE, false)) {
            mAgentWeb.getJsAccessEntrace().quickCallJs("aNdroidClear");
            this.finish();
//            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showExceptionDialogFromIntent(intent);
    }

    /**
     * debug purpose only, you can ignore this
     * 只有调试目的，您可以忽略它
     */
    private void registerInternalDebugReceiver() {
        internalDebugReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                DemoHelper.getInstance().logout(false, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                finish();
//                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                    }
                });
            }
        };
        IntentFilter filter = new IntentFilter(getPackageName() + ".em_internal_debug");
        registerReceiver(internalDebugReceiver, filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
}
