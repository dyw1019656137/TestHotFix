package com.dyw.testhotfix;

import android.app.Application;

import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.connection.DownloadConnection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //支持https下载
        DownloadConnection.Factory factory = new DownloadConnection.Factory() {
            @Override
            public DownloadConnection create(String url) throws IOException {

                okhttp3.OkHttpClient.Builder okhttpClient = (new OkHttpClient()).newBuilder()
                        .connectTimeout(20L, TimeUnit.SECONDS)
                        .readTimeout(30L, TimeUnit.SECONDS)
                        .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                        .hostnameVerifier(SSLSocketClient.getHostnameVerifier());
                return new DownloadHttpsConnection(okhttpClient.build(), url);
            }
        };
        OkDownload.Builder builder = new OkDownload.Builder(this).connectionFactory(factory);
        OkDownload.setSingletonInstance(builder.build());
    }
}
