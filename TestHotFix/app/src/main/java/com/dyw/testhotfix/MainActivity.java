package com.dyw.testhotfix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.patch.annotaion.Modify;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button patch, btnResult, btnOpen;

    private TextView result,process;

    private DownloadTask task;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patch = (Button) findViewById(R.id.patch);
        btnResult = (Button) findViewById(R.id.btn_result);
        btnOpen = (Button) findViewById(R.id.btn_open);
        result = (TextView) findViewById(R.id.result);
        process = (TextView) findViewById(R.id.process);


        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open();
            }
        });

        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download("https://android-test-1253993905.cos.ap-chengdu.myqcloud.com/robust/patch.jar", "patch.jar");
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult();
            }
        });
    }

    private void runRobust() {
        new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new RobustCallBackSample(MainActivity.this,process)).start();
    }

    @Modify
    public void setResult() {
        result.setText("打补丁咯");
    }

    public void setProcess(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                process.setText(info);
            }
        });
    }


    public void download(String url, String fileName) {

        String path = Environment.getExternalStorageDirectory().getPath()+ File.separator+"robust"+File.separator + "patch"+File.separator;

        Log.d(TAG, "download: path"+path);

        task = new DownloadTask.Builder(url, path, fileName)
                .setMinIntervalMillisCallbackProcess(150)
                .setPassIfAlreadyCompleted(false)
                .setConnectionCount(1).build();

        task.enqueue(new DownloadListener4WithSpeed() {

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
                Log.d(TAG, "infoReady: " + info.getTotalLength());

                setProcess("infoReady: " + info.getTotalLength());
            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {

            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
                Log.d(TAG, "progress: " + currentOffset);
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {

            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
                if (cause == EndCause.COMPLETED) {
                    if (task.getFile() == null || !task.getFile().exists()) {
                        //下载失败
                        Log.d(TAG, "taskEnd: 下载失败");
                    } else {
                        //下载成功
                        Log.d(TAG, "taskEnd: 下载成功：" + task.getFile().getAbsolutePath());
                        setProcess("下载成功，准备加载补丁 ");
                        runRobust();
                    }

                } else if (cause == EndCause.CANCELED) {
                    //下载停止
                    Log.d(TAG, "taskEnd: 下载停止" + realCause.toString());
                } else {
                    //下载停止
                    Log.d(TAG, "taskEnd: 下载停止" + realCause.toString());
                }
            }

            @Override
            public void taskStart(@NonNull DownloadTask task) {

            }

            @Override
            public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {

            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {

            }
        });
    }

    /**
     * 获取应用根文件夹
     *
     * @return
     */
    public String getStoragePath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + getPackageName(this.getApplicationContext()) + "/";
    }

    /**
     * 获取包名
     *
     * @return
     */
    public static String getPackageName(Context con) {
        return getPackageInfo(con).packageName;
    }

    /**
     * 获取包信息
     *
     * @param con
     * @return
     */
    public static PackageInfo getPackageInfo(Context con) {
        PackageManager packageManager = con.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(con.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo;
    }

    private void open() {
        try {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("", "");
            intent.setComponent(cn);
            Uri uri = Uri.parse("");
            intent.setData(uri);
            startActivity(intent);

        } catch (Exception e) {
            Log.d(TAG, "open: 应用未安装，请先安装");
        }
    }
}
