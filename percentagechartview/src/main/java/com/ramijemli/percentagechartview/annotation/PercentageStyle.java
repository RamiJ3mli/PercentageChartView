package com.ramijemli.percentagechartview.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.CAP_ROUND;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.CAP_SQUARE;


@Retention(RetentionPolicy.SOURCE)
@IntDef({CAP_ROUND, CAP_SQUARE})
public @interface PercentageStyle {
}
