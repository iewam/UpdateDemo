package com.spark.update;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载管理者
 */
public class UpdateManager {

    private static UpdateManager updateManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    static {
        updateManager = new UpdateManager();
    }

    private UpdateManager() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public static UpdateManager getInstance() {
        return updateManager;
    }

    protected void startDownloads (String downloadUrl, String localPath, UpdateDownloadListener downloadListener) {
        if (request != null) {
            return;
        }

        checkLocalFilePath(localPath);
        request = new UpdateDownloadRequest(downloadUrl, localPath, downloadListener);
        threadPoolExecutor.submit(request);
    }

    /**
     * 检查本地路径是否存在
     * @param localPath
     */
    private void checkLocalFilePath(String localPath) {
        File dir = new File(localPath.substring(0, localPath.lastIndexOf("/") + 1));
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(localPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
