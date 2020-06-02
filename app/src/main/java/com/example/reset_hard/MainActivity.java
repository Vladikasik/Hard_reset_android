package com.example.reset_hard;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.things.device.DeviceManager;

public class MainActivity extends AppCompatActivity {
    DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;

    public static class WipeDataReceiver extends DeviceAdminReceiver {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final int currentAPIVersion = Build.VERSION.SDK_INT;

        if (currentAPIVersion >= android.os.Build.VERSION_CODES.FROYO) {
            //2.2+
            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            mDeviceAdmin = new ComponentName(this, WipeDataReceiver.class);
        }

        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceManager deviceManager = DeviceManager.getInstance(); // Also erase shared external storage
                boolean wipeExternal = true; deviceManager.factoryReset(true);
                OnReset(currentAPIVersion);
            }
        });
    }

    private void OnReset(int currentAPIVersion){
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.FROYO) {
            // 2.2+
            if (!mDPM.isAdminActive(mDeviceAdmin)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Process will remove user installed applications, settings, wallpaper and sound settings. Are you sure you want to wipe device?");
                startActivityForResult(intent, 1234);
            } else {
                // device administrator, can do security operations
                mDPM.wipeData(0);
            }

        } else {
            // 2.1
            try {
                Context foreignContext = this.createPackageContext("com.android.settings", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
                Class<?> yourClass = foreignContext.getClassLoader().loadClass("com.android.settings.MasterClear");
                Intent i = new Intent(foreignContext, yourClass);
                this.startActivityForResult(i, 1234);
            } catch (ClassNotFoundException | PackageManager.NameNotFoundException e) {

            }

        }


    }
}
