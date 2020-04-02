package com.example.scanmeasuredemo1.measure;

import android.view.View;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/3/2018.
 */

public class MeasurePresenter implements View.OnClickListener {

    private static final String TAG = MeasurePresenter.class.toString();
    private MeasureActivity mActivity;
    private MeasurePreview measurePreview;
    private View okBtn;

    public MeasurePresenter(MeasureActivity mActivity) {
        this.mActivity = mActivity;
        init();
    }

    private void init() {
        measurePreview = mActivity.getMeasurePreview();
        measurePreview.setCurrentContours(Source.currentContours);
        measurePreview.nextAdjustBoundary();
        measurePreview.setImageBitmap(Source.currentBitmap);

        okBtn = mActivity.getOkBtn();
        okBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (measurePreview.isAdjustingBoundaries()) {
            measurePreview.nextAdjustBoundary();
        }

        if (measurePreview.isReferenceDrawn()) {
            MeasureInputDialog dialog = new MeasureInputDialog(mActivity);
            dialog.setMeasureInputInterface(d -> {
                measurePreview.onReferenceMeasure(d);
            });
            dialog.show();
        }

    }
}
