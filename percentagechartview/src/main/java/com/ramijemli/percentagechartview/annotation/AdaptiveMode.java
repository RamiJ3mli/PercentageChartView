package com.ramijemli.percentagechartview.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.DARKER_MODE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.INVALID_MODE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.LIGHTER_MODE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;

@Retention(RetentionPolicy.SOURCE)
@IntDef({DARKER_MODE, LIGHTER_MODE})
public @interface AdaptiveMode {
}
