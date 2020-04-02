package com.example.scanmeasuredemo1.scan;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.scanmeasuredemo1.R;
import com.example.scanmeasuredemo1.vision.processor.FootRealOnPaperProcessor;

import org.opencv.android.OpenCVLoader;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.toString();
    private int REQUEST_CAMERA_PERMISSION = 0, EXIT_TIME = 2000;
    private long latestBackPressTime;

    private ScanPresenter mPresenter;
    @BindView(R.id.scan_preview)
    ScanPreview mScanPreview;
    @BindView(R.id.shut_btn)
    AppCompatImageView shutBtn;

    // debug
    public static AppCompatImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        // initialize UI elements
        initUI();
        // initialize Scan Presenter
        mPresenter = new ScanPresenter(this);
        prepare();
    }

    private void initUI() {
        ButterKnife.bind(this);

        // debug
        imageView = findViewById(R.id.image_view);

    }

    private void prepare() {
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "loading opencv error, exit");
            finish();
        }
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this
                    , new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        shutBtn.setOnClickListener(v -> mPresenter.shut());

        latestBackPressTime = System.currentTimeMillis();
    }

    public ScanPreview getScanPreview() {
        return mScanPreview;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.exit();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - latestBackPressTime > EXIT_TIME) {
            Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
        latestBackPressTime = System.currentTimeMillis();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && (grantResults[Arrays.asList(permissions).indexOf(android.Manifest.permission.CAMERA)] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            mPresenter.initCamera();
            mPresenter.updateCamera();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        if (((RadioButton) view).isChecked())
            mPresenter.radioButtonChecked(view);

    }
}
