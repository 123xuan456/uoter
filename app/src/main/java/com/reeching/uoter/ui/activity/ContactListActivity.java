package com.reeching.uoter.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.NetUtils;
import com.reeching.uoter.DemoHelper;
import com.reeching.uoter.R;
import com.reeching.uoter.ui.BaseActivity;

import java.util.Hashtable;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 通讯录页面
 */
public class ContactListActivity extends BaseActivity {

    @Bind(R.id.title_bar)
    EaseTitleBar titleBar;
    @Bind(R.id.query)
    EditText query;
    @Bind(R.id.search_clear)
    ImageButton searchClear;
    @Bind(R.id.contact_list)
    EaseContactList contactList;
    @Bind(R.id.content_container)
    FrameLayout contentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        ButterKnife.bind(this);


        titleBar.setRightImageResource(R.drawable.em_add);
        titleBar.setRightLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), AddContactActivity.class));
                NetUtils.hasDataConnection(ContactListActivity.this);
            }
        });
        //设置联系人数据
        Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
        if (m instanceof Hashtable<?, ?>) {
            m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>) m).clone();
        }
    }
}
