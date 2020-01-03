package com.example.signmein;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//This activity will assure that the neccessary permissions are requested, perform initial setup,
//then launch the app client.
public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);
        requestPermissions();
    }


    private final static int REQUEST_FINE_LOCATION_PERMISSION = 0;

    private boolean CHECK_PERMISSIONS_ON_RESUME = false;

    protected void onResume() {
        super.onResume();
        if (CHECK_PERMISSIONS_ON_RESUME) {
            CHECK_PERMISSIONS_ON_RESUME = false;
            requestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted.
                    startApp(500);
                }
                else {
                    boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton(android.R.string.ok, null);

                    if (showRationale) {
                        //Permission is denied. Since location is required, ask again.

                        builder.setTitle("Sorry, but we need permission. ");
                        builder.setMessage("Android will not permit us to access bluetooth and wifi hardware without location permission. ");

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
                            }
                        });
                    }
                    else {
                        //Permission has been denied, with "don't ask again" selected.

                        builder.setTitle("Location permission is required. ");
                        builder.setMessage("SignMeIn needs location permission in order to work. Since you have blocked additional permission requests, we will open settings so that you can grant us the permission manually. ");

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                //Open our application page in settings.
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                CHECK_PERMISSIONS_ON_RESUME = true;
                                startActivity(intent);
                            }
                        });
                    }
                    builder.show();
                }
                return;
            }
        }
    }

    private void startApp(int delayMillis) {
        //We will show the splash screen for a short period of time, then launch the app.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent openMainApp = new Intent(getBaseContext(), MainActivity.class);
                openMainApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getBaseContext().startActivity(openMainApp);
            }
        }, delayMillis);
    }

    private void startApp() {
        startApp(1500); //Default delay of 1500 milliseconds
    }

    protected void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //TODO: We should request all permissions in case they are reclassified from normal to dangerous.

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("SignMeIn Needs Location Access");
                builder.setMessage("Bluetooth and Wifi hardware is used by SignMeIn. Since location can be approximated using such hardware, android requires us to ask for permission to access your location. ");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
                    }
                });

                builder.show();
            }
            else {
                startApp();
            }
        }
        else {
            //Below Android Marshmellow, permissions must be granted for our app to install.
            startApp();
        }
    }

}
