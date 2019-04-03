package com.ramijemli.percentagechartview.annotation;

import android.graphics.Typeface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@Retention(RetentionPolicy.SOURCE)
@IntDef({Typeface.NORMAL, Typeface.ITALIC, Typeface.BOLD, Typeface.BOLD_ITALIC})
public @interface TextStyle {
}
