package com.ramijemli.percentagechartview;

import android.content.Context;

public interface IPercentageChartView {

    Context getViewContext();

    void invalidate();

    void requestLayout();

    void onProgressUpdated(float progress);

    boolean isInEditMode();

}
