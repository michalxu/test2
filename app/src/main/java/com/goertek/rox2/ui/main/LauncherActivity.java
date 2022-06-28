package com.goertek.rox2.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.common.utils.Utils;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.utils.ProgressBar;

public class LauncherActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        progressBar = findViewById(R.id.launcher_progress_bar);
        Animation animation = AnimationUtils.loadAnimation(LauncherActivity.this,R.anim.progress_bar_rotate_animation);
        animation.setRepeatCount(Animation.INFINITE);
        progressBar.startAnimation(animation);
        Handler handler =  new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                  switch (Utils.getDevice().connectionState){
                        case ConnectState.STATE_CONNECTED:
                        case ConnectState.STATE_DATA_READY:
                            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case ConnectState.STATE_DISCONNECTED:

                            Intent intent1 = new Intent(LauncherActivity.this,UnConnectedActivity.class);
                            startActivity(intent1);
                            finish();
                            break;
                        default:

                            Intent intent2 = new Intent(LauncherActivity.this, MainActivity.class);
                            startActivity(intent2);
                            finish();
                            break;

                    }
            }
        };
        handler.sendEmptyMessageDelayed(0,1000);

    }
}