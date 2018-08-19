package com.spark.update;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 实现Runnable接口的下载线程类,创建下载任务
 */
public class UpdateDownloadRequest implements Runnable {

    private String downloadUrl;
    private String loaclFilePath;
    private UpdateDownloadListener downloadListener;
    private boolean isDownloading = false;
    private int currentLength;

    private DownloadResponseHandler downloadResponseHandler;

    public UpdateDownloadRequest(String downloadUrl, String loaclFilePath, UpdateDownloadListener downloadListener) {
        this.downloadUrl = downloadUrl;
        this.loaclFilePath = loaclFilePath;
        this.downloadListener = downloadListener;
        isDownloading = true;
        downloadResponseHandler = new DownloadResponseHandler();
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeRequest() throws IOException {
        if (!Thread.currentThread().isInterrupted()) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();
                currentLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    downloadResponseHandler.sendResponseMessage(connection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 下载过程中的异常
     */
    public enum FailureCode{
        UnknownHost, Socket, SocketTimeout, connectionTimeout,IO, HttpResponse,
        Json, Interrupted

    }

    /**
     * 下载响应通信类
     */
    public class DownloadResponseHandler {
        protected static final int SUCCESS_MESSAGE = 0x0001;
        protected static final int FAILURE_MESSAGE = 0x0002;
        protected static final int START_MESSAGE = 0x0003;
        protected static final int FINISH_MESSAGE = 0x0004;
        protected static final int NETWORK_OFF = 0x0005;
        protected static final int PROGRESS_CHANGED = 0x0006;

        private float completeSize = 0;
        private int progress = 0;

        private Handler handler;

        public DownloadResponseHandler() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };
        }

        private void handleSelfMessage(Message msg) {
            Object[] response;
            switch (msg.what) {
                case FAILURE_MESSAGE:
                    response = (Object[])msg.obj;
                    handleOnFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[])msg.obj;
                    handleProgressChangedMessage(((Integer)response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    handleOnFinishMessage();
                    break;
            }
        }

        private void sendProgressChangedMessage(int progress) {
            sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));
        }

        private void sendFinishMessage() {
            sendMessage(obtainMessage(FINISH_MESSAGE, null));
        }

        private void sendFailureMessage(FailureCode failureCode) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));
        }



        private void handleProgressChangedMessage(int progress) {
            downloadListener.onProgressChanged(progress, downloadUrl);
        }

        private void handleOnFinishMessage() {
            downloadListener.onFinished(completeSize, downloadUrl);
        }

        private void handleOnFailureMessage(FailureCode failureCode) {
            downloadListener.onFailure();
        }


        private void sendMessage(Message msg) {
            if (handler != null) {
                handler.sendMessage(msg);
            } else {
                handleSelfMessage(msg);
            }
        }

        private Message obtainMessage(int responseMessage, Object response) {
            Message msg = null;
            if (handler != null) {
                msg = handler.obtainMessage(responseMessage, response);
            } else {
                msg = Message.obtain();
                msg.what = responseMessage;
                msg.obj = response;
            }
            return msg;
        }

        private void sendResponseMessage(InputStream is) {
            RandomAccessFile randomAccessFile = null;
            completeSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1;
                int limit = 0;
                randomAccessFile = new RandomAccessFile(loaclFilePath, "rwd");
                while ((length = is.read(buffer)) != -1) {
                    if (isDownloading) {
                        randomAccessFile.write(buffer, 0, length);
                        completeSize += length;
                        if (completeSize < currentLength) {
                            Log.e("update", "completeSize" + completeSize);
                            Log.e("update", "currentSize" + currentLength);
                            progress = (int) (100 * Float.parseFloat(getTwoPointFloatStr(completeSize/currentLength)));
                            Log.e("update", "progress" + progress);
                            if (limit % 30 == 0 && progress <= 100) { // 每循环30次更新一次notification
                                sendProgressChangedMessage(progress);
                            }
                            limit++;
                        }
                    }
                }
                //下载完成
                sendFinishMessage();
            } catch (IOException e) {
                sendFailureMessage(FailureCode.IO);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }

                } catch (IOException e) {
                    sendFailureMessage(FailureCode.IO);
                }

            }


        }

        private String getTwoPointFloatStr(float value){
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(value);

        }
    }





















}
