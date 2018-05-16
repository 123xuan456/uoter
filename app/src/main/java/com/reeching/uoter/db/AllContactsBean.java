package com.reeching.uoter.db;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 绍轩 on 2018/4/3.
 */

public class AllContactsBean implements Serializable{

    /**
     * result : 0
     * msg : 成功！
     * list : [{"nickName":"111111","name":"111111","restName":"8b6552ed8189403a9f0ffdbde9f55117"},{"nickName":"zhangda","name":"zhangda","restName":"70e92ded09ea457ab1b1634b51daa7ac"},{"nickName":"wanger","name":"wanger","restName":"5cd03b53bdb841399c32900441ad75f8"},{"nickName":"zhanghaoran","name":"zhanghaoran","restName":"e241caefd9d44a66aa2a46291d37e820"}]
     */

    private int result;
    private String msg;
    private List<ListBean> list;

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

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * nickName : 111111
         * name : 111111
         * restName : 8b6552ed8189403a9f0ffdbde9f55117
         */

        private String nickName;
        private String name;
        private String restName;

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRestName() {
            return restName;
        }

        public void setRestName(String restName) {
            this.restName = restName;
        }
    }
}
