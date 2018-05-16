package com.reeching.uoter.ui.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.IAgentWebSettings;
import com.reeching.uoter.BaseApplication;
import com.reeching.uoter.R;
import com.reeching.uoter.ui.fragment.ContactListFragment;
import com.reeching.uoter.utils.AndroidInterfaceForJS;
import com.reeching.uoter.utils.LogUtils;
import com.reeching.uoter.view.XRadioGroup;

public class Main3Activity extends FragmentActivity implements View.OnClickListener, XRadioGroup.OnCheckedChangeListener{

    private Button colorButton;
    private Button colorButton1;
    private AgentWeb mAgentWeb;
    private XRadioGroup radioGroup;
    private Fragment fragment;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_main3);
        colorButton = (Button)findViewById(R.id.color);
        colorButton1 = (Button)findViewById(R.id.color1);
        colorButton.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        radioGroup = (XRadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        initWebView();
    }

    private void initWebView() {
         mLinearLayout = (LinearLayout) this.findViewById(R.id.line1);
        mAgentWeb = AgentWeb
                .with(this)//传入Activity or Fragment
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .createAgentWeb()//
                .ready()
                .go("file:///android_asset/index.html");
        //注入对象
        IAgentWebSettings agentWebSettings = mAgentWeb.getAgentWebSettings();
        agentWebSettings.getWebSettings().setDomStorageEnabled(true);
        agentWebSettings.getWebSettings().setAllowFileAccess(true); //允许file 协议 ， 加载本地文件
        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterfaceForJS(mAgentWeb, this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.color://调用JS中的有参数方法
//                String imei = PhoneUtil.getInstance().getPhoneImei(Main3Activity.this);
//                String mode = PhoneUtil.getInstance().getPhoneModel();
                mAgentWeb.getJsAccessEntrace().quickCallJs("account");
                LogUtils.i("执行js方法");
                break;
            case R.id.color1://调用JS中的有参数方法
                mAgentWeb.getJsAccessEntrace().quickCallJs("team");
//                ActivityUtil.openAct(Main3Activity.this,WeiXinActivity.class);
                break;

        }
    }

    @Override
    public void onCheckedChanged(XRadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_home_bill: {
                mLinearLayout.setVisibility(View.VISIBLE);
//                fragment = MessageFragment.newInstance();//首页
                mAgentWeb.getJsAccessEntrace().quickCallJs("account");
            }
            break;
            case R.id.rb_home_team: {
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("team");
            }
            break;
            case R.id.rb_home_mag: {
                mLinearLayout.setVisibility(View.GONE);
                fragment = new ContactListFragment();//消息
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            }
            break;
            case R.id.rb_home_home: {
//                fragment = MessageFragment.newInstance();//我的
                mLinearLayout.setVisibility(View.VISIBLE);
                mAgentWeb.getJsAccessEntrace().quickCallJs("mine");
            }
            break;
        }
//        if (fragment != null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
//        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
