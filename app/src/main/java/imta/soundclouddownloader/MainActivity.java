package imta.soundclouddownloader;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import imta.soundclouddownloader.core.SoundcloudDownloader;

public class MainActivity extends AppCompatActivity {

//    private static final String MY_PREFS_NAME = "DownloadDir";
    private static final String DIR_PROPERTY = "dir";
    private static final int QR_CODE_REQUEST = 1;
    private static final int CHANGE_DIR_REQUEST = 2;
    public static DownloadManager mgr;
    private static final int PERM_STORAGE_REQ = 33;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
//        editor.putString(DIR_PROPERTY, Environment.DIRECTORY_DOWNLOADS);
//        editor.apply();

        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        registerReceiver(new BroadcastReceiver() {
                             @Override
                             public void onReceive(Context context, Intent intent) {
                                 Toast toast = Toast.makeText(getApplicationContext(), "Download complete", Toast.LENGTH_LONG);
                                 toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, -10);
                                 toast.show();
                             }
                         },
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//        registerReceiver(new BroadcastReceiver() {
//                             @Override
//                             public void onReceive(Context context, Intent intent) {
//
//                             }
//                         },
//                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == QR_CODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

//                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//                String downloadDirectory = prefs.getString(DIR_PROPERTY, null);

                String qrCode = data.getStringExtra("result");

//                Toast toast = Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, -10);
//                toast.show();

                download(qrCode);
            }
        } /*else if (requestCode == CHANGE_DIR_REQUEST) {
            if (resultCode == RESULT_OK) {

                String directory = data.getData().getPath();

                if (directory != null) {
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(DIR_PROPERTY, directory);
                    editor.apply();

                    Toast toast = Toast.makeText(getApplicationContext(), "Download directory was changed", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, -10);
                    toast.show();
                }
            }
        }*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.settings_download_path:
//
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                startActivityForResult(intent, CHANGE_DIR_REQUEST);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    public void scanQrCodeBtn(View view) {
        startActivityForResult(new Intent(MainActivity.this, QrCodeScannerActivity.class), QR_CODE_REQUEST);
    }

    public void downloadBtn(View view) {
        EditText url = findViewById(R.id.url_text);
        String inputUrl = url.getText().toString();

        download(inputUrl);
    }

    private void download(String inputUrl) {
//        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//        String downloadDirectory = prefs.getString(DIR_PROPERTY, null);
//
//        SoundcloudDownloader.DOWNLOAD_DIR = downloadDirectory;

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            // this will request for permission when user has not granted permission for the app
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_STORAGE_REQ);
//            return;
//        }

        try {
            SoundcloudDownloader.download(SoundcloudDownloader.getInfo(inputUrl));
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Wrong input url!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, -10);
            toast.show();
        }
    }

}
