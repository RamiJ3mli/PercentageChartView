package com.ramijemli.percentagechartview.callback;

public interface AdaptiveColorProvider {

    default int provideProgressColor(float progress) {
        return -1;
    }

    default int provideBackgroundColor(float progress) {
        return -1;
    }

    default int provideTextColor(float progress) {
        return -1;
    }

    default int provideBackgroundBarColor(float progress) {
        return -1;
    }

}
