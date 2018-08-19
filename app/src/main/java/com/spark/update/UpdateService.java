package com.spark.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.spark.skupdateutils.R;

import java.io.File;

/**
 * 下载更新服务
 */

public class UpdateService extends Service {

    private String apkUrl;
    private String filePath;
    private NotificationManager notificationManager;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("update", "updateService onCreate()");
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        filePath = Environment.getExternalStorageDirectory() + "/update/newVersion.apk";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("update", "updateService onStartCommand()");
        if (intent == null) {
            notifyUser(getString(R.string.update_service_start_failure), 0);
            stopSelf();
        }

        apkUrl = intent.getStringExtra("apkUrl");
        notifyUser(getString(R.string.update_service_start), 0);
        startDownload();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateManager.getInstance().startDownloads(apkUrl, filePath, new UpdateDownloadListener() {
            @Override
            public void onStarted() {
                Log.e("update", "download start");
            }

            @Override
            public void onProgressChanged(int progress, String downloadUrl) {
                notifyUser(getString(R.string.update_progress_changed), progress);
            }

            @Override
            public void onFinished(float completeSize, String downloadUrl) {
                Log.e("update", "download finished");
                notifyUser(getString(R.string.update_finished), 100);
                stopSelf();
            }

            @Override
            public void onFailure() {
                Log.e("update", "download fail");
                notifyUser(getString(R.string.update_failure), 0);
                stopSelf();
            }
        });
    }

    /**
     * 更新 notification
     * @param result
     * @param progress
     */
    private void notifyUser(String result, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name));
        if (progress > 0 && progress <=100) {
            builder.setProgress(100, progress, false);
            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            builder.setTicker(result);
            builder.setContentIntent(progress >= 100 ? getContentIntent() : PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
            notification = builder.build();
            notificationManager.notify(0, notification);
        }
//        else {
//            builder.setProgress(0, 0, false);
//        }

    }

    /**
     * apk安装程序
     * @return
     */
    private PendingIntent getContentIntent() {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        startActivity(intent);
        return pendingIntent;
    }

































    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
