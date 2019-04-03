package com.ramijemli.percentagechartview.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_CLOCKWISE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ORIENTATION_CLOCKWISE, ORIENTATION_COUNTERCLOCKWISE})
public @interface ProgressOrientation {
}
