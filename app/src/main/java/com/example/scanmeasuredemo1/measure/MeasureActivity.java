package com.example.scanmeasuredemo1.measure;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.scanmeasuredemo1.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeasureActivity extends AppCompatActivity {

    private static final String TAG = MeasureActivity.class.toString();

    private MeasurePresenter mPresenter;
    @BindView(R.id.measure_preview)
    MeasurePreview measurePreview;
    @BindView(R.id.ok_btn)
    View okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        // initialize UI elements
        initUI();
        // initialize Scan Presenter
        mPresenter = new MeasurePresenter(this);
    }

    private void initUI() {
        ButterKnife.bind(this);
    }

    public MeasurePreview getMeasurePreview() {
        return measurePreview;
    }

    public View getOkBtn() {
        return okBtn;
    }
}
