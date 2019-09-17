

package com.dyw.testhotfix;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadHttpsConnection implements com.liulishuo.okdownload.core.connection.DownloadConnection, com.liulishuo.okdownload.core.connection.DownloadConnection.Connected {
    @NonNull
    final OkHttpClient client;
    @NonNull
    private final Request.Builder requestBuilder;

    private Request request;
    private Response response;

    /**
     * 构造方法
     *
     * @param client         OkHttpClient对象
     * @param requestBuilder OkHttp 的请求Builder
     */
    public DownloadHttpsConnection(@NonNull OkHttpClient client,
                                   @NonNull Request.Builder requestBuilder) {
        this.client = client;
        this.requestBuilder = requestBuilder;
    }

    /**
     * 构造方法
     *
     * @param client OkHttpClient对象
     * @param url    下载的url
     */
    public DownloadHttpsConnection(@NonNull OkHttpClient client, @NonNull String url) {
        this(client, new Request.Builder().url(url));
    }

    /**
     * 添加请求头
     *
     * @param name  请求头名
     * @param value 请求头值
     */
    @Override
    public void addHeader(String name, String value) {
        if (TextUtils.equals(value, "bytes=0-0")) {
            value = "bytes=0-";
        }
        this.requestBuilder.addHeader(name, value);
    }

    /***
     * 发起请求
     * @return {@link Connected}
     * @throws IOException
     */
    @Override
    public Connected execute() throws IOException {
        request = requestBuilder.build();
        response = client.newCall(request).execute();
        return this;
    }

    /**
     * 释放请求和相应资源
     */
    @Override
    public void release() {
        request = null;
        if (response != null) {
            response.close();
        }
        response = null;
    }

    /**
     * 获取请求头的所有内容
     *
     * @return 包含请求头所有内容的map
     */
    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (request != null) {
            return request.headers().toMultimap();
        } else {
            return requestBuilder.build().headers().toMultimap();
        }
    }

    /**
     * 获取请求头的指定的键值
     *
     * @param key 请求头名
     * @return 对应请求头的值
     */
    @Override
    public String getRequestProperty(String key) {
        if (request != null) {
            return request.header(key);
        } else {
            return requestBuilder.build().header(key);
        }
    }

    /**
     * 获取相应状态码
     *
     * @return http 状态码
     * @throws IOException
     */
    @Override
    public int getResponseCode() throws IOException {
        if (response == null) {
            throw new IOException("Please invoke execute first!");
        }
        return response.code();
    }

    /**
     * 获取网络请求响应体的输入流
     *
     * @return
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (response == null) {
            throw new IOException("Please invoke execute first!");
        }
        final ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("no body found on response!");
        }
        return body.byteStream();
    }

    /**
     * 设置请求方法
     *
     * @param method 请求方法
     * @return
     * @throws ProtocolException
     */
    @Override
    public boolean setRequestMethod(@NonNull String method) throws ProtocolException {
        this.requestBuilder.method(method, null);
        return true;
    }

    /**
     * 获取响应头信息
     *
     * @return map集合 可空
     */
    @Override
    @Nullable
    public Map<String, List<String>> getResponseHeaderFields() {
        return response == null ? null : response.headers().toMultimap();
    }

    /**
     * 获取指定响应头的值
     *
     * @param name 响应头名称
     * @return 对应响应头的值
     */
    @Override
    @Nullable
    public String getResponseHeaderField(String name) {
        return response == null ? null : response.header(name);
    }

    /**
     * 获取重定向地址
     *
     * @return 重定向的url
     */
    @Override
    public String getRedirectLocation() {
        return request.url().toString();
    }

}
