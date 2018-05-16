package com.reeching.uoter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 绍轩 on 2018/3/22.
 */

public class BaseApplication extends Application{

    private List<Activity> list = new ArrayList<Activity>();
    public static Context applicationContext;
    private static BaseApplication instance;
    public static String currentUserNick = "";
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        OkGo.getInstance().init(this);
        DemoHelper.getInstance().init(applicationContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }
    public static BaseApplication getInstance() {
        return instance;
    }

    public void addActivity(Activity activity) {
        list.add(activity);
    }

    public void exit(Context context) {
        for (Activity activity : list) {
            activity.finish();
        }
        System.exit(0);
    }
}
