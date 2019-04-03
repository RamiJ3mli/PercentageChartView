package com.ramijemli.percentagechartview;

import android.content.Context;

public interface IPercentageChartView {

    Context getViewContext();

    void requestInvalidate();

    void onProgressUpdated(float progress);

}
