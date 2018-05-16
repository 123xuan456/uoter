package com.reeching.uoter.ui.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.reeching.uoter.Constant;
import com.reeching.uoter.R;
import com.reeching.uoter.ui.GroupsActivity;
import com.reeching.uoter.ui.MainActivity;
import com.reeching.uoter.ui.SettingsFragment;
import com.reeching.uoter.ui.fragment.ContactListFragment;
import com.reeching.uoter.ui.fragment.ConversationListFragment;

/**
 * 消息页面
 */
public class MessageFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{


    private Fragment[] fragments;
    private Fragment fragment;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private ConversationListFragment conversationListFragment;
    private ContactListFragment contactListFragment;
    private int currentTabIndex;
    public MessageFragment() {
    }
    public static Fragment newInstance() {
        MessageFragment f = new MessageFragment();
        return f;
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_CONTACT_CHANAGED);
        intentFilter.addAction(Constant.ACTION_GROUP_CHANAGED);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((MainActivity) getActivity()).updateUnreadLabel();
                ((MainActivity) getActivity()).updateUnreadAddressLable();
                if (currentTabIndex == 0) {
                    // refresh conversation list
                    if (conversationListFragment != null) {
                        conversationListFragment.refresh();
                    }
                } else if (currentTabIndex == 1) {
                    if(contactListFragment != null) {
                        contactListFragment.refresh();
                    }
                }
                String action = intent.getAction();
                if(action.equals(Constant.ACTION_GROUP_CHANAGED)){
                    if (EaseCommonUtils.getTopActivity(getActivity()).equals(GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mag, container, false);
        RadioGroup radioGroup= (RadioGroup) view.findViewById(R.id.mag_radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        conversationListFragment = new ConversationListFragment();
        contactListFragment = new ContactListFragment();
        SettingsFragment settingFragment = new SettingsFragment();
        fragments = new Fragment[] { conversationListFragment, contactListFragment, settingFragment};
        switchFragment(conversationListFragment);
        //注册广播接收器接收来自DemoHelper的组的变化
        registerBroadcastReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    // 切换Fragment方法
    private void switchFragment(Fragment fragment) {
        FragmentManager manager = getChildFragmentManager();
        manager.beginTransaction().replace(R.id.mag_fragment, fragment).commit();
    }
    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId){
            case R.id.mag_radiobutton:
                fragment = fragments[0];
                currentTabIndex = 0;
                break;
            case R.id.mag_radiobutton1:
                fragment = fragments[1];
                currentTabIndex = 1;
                break;
        }
        switchFragment(fragment);
    }
    public void refresh() {
        conversationListFragment.refresh();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }
    private void unregisterBroadcastReceiver(){
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
