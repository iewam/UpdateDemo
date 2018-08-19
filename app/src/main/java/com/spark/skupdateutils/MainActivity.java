package com.spark.skupdateutils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.spark.update.UpdateService;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkPermission();

    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            Log.e("update", "checkPermission: 已经授权！");
        }
    }


    public void showCommonDialog(View v) {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setTitle("版本更新");
        commonDialog.setMessage("发现新版本");
        commonDialog.setRightBtnText("立即更新");
        commonDialog.setRightButtonClickListener(new CommonDialog.RightButtonClickListener() {
            @Override
            public void onRightButtonClick() {
                Intent intent = new Intent(MainActivity.this, UpdateService.class);
                intent.putExtra("apkUrl", "http://203.156.235.194:1307/hi-app-apk/mixiaohua.apk");
                startService(intent);
            }
        });
        commonDialog.show();
    }

}
