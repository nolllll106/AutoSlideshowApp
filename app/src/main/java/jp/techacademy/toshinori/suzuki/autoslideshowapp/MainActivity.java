package jp.techacademy.toshinori.suzuki.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Button mStartButton;
    Button mNextButton;
    Button mBackButton;
    Cursor cursor;

    Timer mTimer;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartButton = (Button)findViewById(R.id.button_start);
        mStartButton.setOnClickListener(this);
        mNextButton = (Button)findViewById(R.id.button_next);
        mNextButton.setOnClickListener(this);
        mBackButton = (Button)findViewById(R.id.button_back);
        mBackButton.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("ANDROID", "許可されている");
                ContentResolver resolver = getContentResolver();
                cursor = resolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            } else {

                Log.d("ANDROID", "許可されていない");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                mNextButton.setEnabled(false);
                mBackButton.setEnabled(false);
                mStartButton.setEnabled(false);
            }
        } else {
            getContentsInfo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(cursor != null)
            cursor.close();
    }

    @Override
    public void onClick(View v) {
        int fieldIndex;
        if(v.getId() == R.id.button_next) {

            if(cursor.moveToNext()) {
                setImageView();
            } else {
                cursor.moveToFirst();
                setImageView();
            }
        } else if(v.getId() == R.id.button_back) {
            if(cursor.moveToPrevious()) {
                setImageView();
            } else {
                cursor.moveToLast();
                setImageView();
            }
        } else if(v.getId() == R.id.button_start) {
            if(mStartButton.getText().equals("再生")) {
                mNextButton.setEnabled(false);
                mBackButton.setEnabled(false);
                mStartButton.setText("停止");

                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        if(mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (cursor.moveToNext()) {
                                        setImageView();
                                    } else {
                                        cursor.moveToFirst();
                                        setImageView();
                                    }
                                }
                            });
                        }
                    }
                }, 2000, 2000);
            } else {
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mStartButton.setText("再生");
                mTimer.cancel();
                mTimer = null;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された");
                } else {
                    Log.d("ANDROID", "許可されなかった");
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);
        }
    }

    private void setImageView() {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}
