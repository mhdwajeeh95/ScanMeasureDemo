package com.example.scanmeasuredemo1.measure;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Toast;

import com.example.scanmeasuredemo1.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/5/2018.
 */

public class MeasureInputDialog extends Dialog implements View.OnClickListener {

    @BindView(R.id.measure_input)
    AppCompatEditText measureInput;
    @BindView(R.id.ok_btn)
    View okBtn;
    MeasureInputInterface measureInputInterface;


    public MeasureInputDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public MeasureInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected MeasureInputDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        setContentView(R.layout.measure_input_dialog);
        setCancelable(false);
        ButterKnife.bind(this);
        okBtn.setOnClickListener(this);

    }

    public void setMeasureInputInterface(MeasureInputInterface measureInputInterface) {
        this.measureInputInterface = measureInputInterface;
    }

    @Override
    public void onClick(View v) {
        if (measureInput.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "fill the measure", Toast.LENGTH_SHORT).show();
            return;
        }

        measureInputInterface.onMeasureInput(Double.parseDouble(measureInput.getText().toString()));
        dismiss();
    }

    interface MeasureInputInterface {
        void onMeasureInput(double measure);
    }
}
