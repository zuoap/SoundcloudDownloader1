package imta.soundclouddownloader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import imta.soundclouddownloader.core.SoundcloudDownloader;
import imta.soundclouddownloader.core.TrackInfo;

public class MainActivity extends AppCompatActivity {

    private static final int QR_CODE_REQUEST = 1;
    public static DownloadManager mgr;
    private static final int TOAST_GRAVITY = 20;

    public static final String LINK_REGEX = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
    public static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        registerReceiver(new BroadcastReceiver() {
                             @Override
                             public void onReceive(Context context, Intent intent) {
                                 Toast toast = Toast.makeText(getApplicationContext(), "Download complete", Toast.LENGTH_LONG);
                                 toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, TOAST_GRAVITY);
                                 toast.show();
                             }
                         },
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();

        //make sure it's an action and type we can handle
        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            String stringExtra = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            Matcher m = LINK_PATTERN.matcher(stringExtra);
            if (m.find()) {
                String link = stringExtra.substring(m.start(), m.end());
                System.out.println(link);
                download(link);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Text does not contain link!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, TOAST_GRAVITY);
                toast.show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == QR_CODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                String qrCode = data.getStringExtra("result");

                download(qrCode);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void scanQrCodeBtn(View view) {
        startActivityForResult(new Intent(MainActivity.this, QrCodeScannerActivity.class), QR_CODE_REQUEST);
    }

    public void downloadBtn(View view) {
        EditText url = findViewById(R.id.url_text);
        String inputUrl = url.getText().toString();

        download(inputUrl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 7: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    download(inputUrl);
                }
            }
            break;
        }
    }

    private String inputUrl;

    private void download(String inputUrl) {
        this.inputUrl = inputUrl;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // this will request for permission when user has not granted permission for the app
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
            return;
        }

        try {
            final List<TrackInfo> info = SoundcloudDownloader.getInfo(inputUrl);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Download " + (info.size() > 1 ? "these songs?" : "this song?"));
            StringBuilder sb = new StringBuilder();
            if (info.size() > 1) {
                int i = 1;
                for (TrackInfo trackInfo : info)
                    sb.append(i++).append(") ").append(trackInfo.toString()).append("\n\n");
            } else {
                sb.append(info.get(0).toString());
            }
            builder.setMessage(sb.toString());
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        SoundcloudDownloader.download(info);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(), "Wrong input url!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, TOAST_GRAVITY);
                        toast.show();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Wrong input url!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, TOAST_GRAVITY);
            toast.show();
        }
    }

}
