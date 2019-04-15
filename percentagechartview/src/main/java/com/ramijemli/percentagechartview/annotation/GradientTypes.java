/*
 * Copyright 2018 Rami Jemli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ramijemli.percentagechartview.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_LINEAR;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_RADIAL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_SWEEP;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.INVALID_GRADIENT;

@Retention(RetentionPolicy.SOURCE)
@IntDef({INVALID_GRADIENT, GRADIENT_LINEAR, GRADIENT_RADIAL, GRADIENT_SWEEP})
public @interface GradientTypes {
}
