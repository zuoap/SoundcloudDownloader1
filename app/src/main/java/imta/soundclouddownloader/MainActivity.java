package imta.soundclouddownloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import imta.soundclouddownloader.core.SoundcloudDownloader;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String qrCode = data.getStringExtra("result");
//                Toast toast = Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_LONG);
//                toast.setMargin(50, 50);
//                toast.show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void scanQrCodeBtn(View view) {
        startActivityForResult(new Intent(MainActivity.this, QrCodeScannerActivity.class), 1);
    }

    public void inputUrlBtn(View view) {
        EditText url = findViewById(R.id.url_text);
        SoundcloudDownloader.DOWNLOAD_DIR = ;
        SoundcloudDownloader.download(SoundcloudDownloader.getInfo(url.getText().toString()));
    }
}