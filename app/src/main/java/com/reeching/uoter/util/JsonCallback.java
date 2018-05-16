package com.reeching.uoter.util;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lzy.okgo.callback.AbsCallback;

import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by 绍轩 on 2018/1/27.
 */

public abstract class JsonCallback<T> extends AbsCallback<T>{
    private Type type;
    private Class<T> clazz;

    public JsonCallback() {
    }
    public JsonCallback(Type type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }
    @Override
    public T convertResponse(Response response) {
        //详细自定义的原理和文档，看这里： https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback
        ResponseBody body=response.body();
        if (body==null) return null;
        T data=null;
        Gson gson=new Gson();
        JsonReader jsonReader=new JsonReader(body.charStream());
        if (type!=null)data=gson.fromJson(jsonReader,type);
        if (clazz!=null)data=gson.fromJson(jsonReader,clazz);
        return data;
    }


}
