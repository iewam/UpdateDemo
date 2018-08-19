package com.spark.update;

/**
 * 下载监听回调接口
 */
public interface UpdateDownloadListener {

    public void onStarted();
    public void onProgressChanged(int progress, String downloadUrl);
    public void onFinished(float completeSize, String downloadUrl);
    public void onFailure();

}
