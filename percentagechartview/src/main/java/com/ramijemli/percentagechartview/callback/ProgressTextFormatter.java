package com.ramijemli.percentagechartview.callback;

import androidx.annotation.NonNull;

public interface ProgressTextFormatter {

    @NonNull
    String provideFormattedText(float progress);

}
