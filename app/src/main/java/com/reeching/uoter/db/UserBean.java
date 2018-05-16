package com.reeching.uoter.db;

/**
 * Created by 绍轩 on 2018/4/13.
 *  获取本地服务器用户信息
 */

public class UserBean {
    /**
     * result : 0
     * msg : success
     * restHeadPic : /userfiles/f5dcce54522a4577b67d9273d7fc7e91/images/photo/2017/11/456123headpic.jpg
     * restUserName : test
     * restNickName : test
     */

    private int result;
    private String msg;
    private String restHeadPic;
    private String restUserName;
    private String restNickName;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRestHeadPic() {
        return restHeadPic;
    }

    public void setRestHeadPic(String restHeadPic) {
        this.restHeadPic = restHeadPic;
    }

    public String getRestUserName() {
        return restUserName;
    }

    public void setRestUserName(String restUserName) {
        this.restUserName = restUserName;
    }

    public String getRestNickName() {
        return restNickName;
    }

    public void setRestNickName(String restNickName) {
        this.restNickName = restNickName;
    }
}
