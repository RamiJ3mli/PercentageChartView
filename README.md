# Percentage Chart View  
A customizable Android percentage chart that displays the progress of any single given task or information.  

## Setup
Dependency should be declared in your app module level  `build.gradle`  file:

```
dependencies {  
    implementation 'com.ramijemli.percentagechartview:percentagechartview:0.1.0'  
}
```

## Attributes  
  
|Name|Format|Description|  
|---|:---:|---|  
| `pcv_mode` | `enum` | Set percentage chart appearance to **`"ring"`** or **`"pie"`**  
| `pcv_orientation` | `enum` | Set progress bar's direction to **`"clockwise"`** or **`"counter_clockwise"`**  
| `pcv_startAngle` | `integer` | Set progress bar's start angle to **[0..360]**  
| `pcv_backgroundColor` | `color` | Change progress background color  
| `pcv_backgroundWidth` | `dimension` | set progress background bar width **if `pcv_mode="ring"`**  
| `pcv_percentageColor` | `color` | Change progress foreground color  
| `pcv_percentageWidth` | `dimension` | set progress foreground bar width **if `pcv_mode="ring"`** 
| `pcv_percentageStyle` | `enum` | Change progress foreground bar style to **`"round"`** or **`"square"`**  
| `pcv_textColor` | `color` | Change text color  
| `pcv_textSize` | `dimension` | Set text size in SP  
| `pcv_animDuration` | `integer` | Set progress update's animation duration  
| `pcv_animInterpolator` | `enum` | Set progress update's animation interpolator to **`"linear"`** (default), **`"accelerate"`**, **`"decelerate"`**, **`"accelerate_decelerate"`**, **`"anticipate"`**, **`"overshoot"`**, **`"anticipate_overshoot"`**, **`"bounce"`**, **`"fast_out_linear_in"`**, **`"fast_out_slow_in"`**, **`"linear_out_slow_in"`**.  
  
## To do
 - [x] ~~Initial release~~
 - [ ] Add progress based adaptive color feature
 - [ ] Add filled background support for ring mode
 - [ ] Setup text typeface support
 - [ ] Add a new mode/appearance
 - [ ] Add dynamic text suffix support
 - [ ] Add text formatter support
 - [ ] Add gradient colors support
 - [ ] Setup component's lifecycle awareness
 - [ ] Add segmented progress support for ring mode


## Feedback  
  
All bugs, feature requests, pull requests, feedback, etc. are welcome. Please, feel free to [create an issue](https://github.com/RamiJ3mli/PercentageChartView/issues).
  
  ## Contributors  
  
<table>  
<tr>  
<td>  
<a href="https://github.com/RamiJ3mli"><img src="https://avatars2.githubusercontent.com/u/22471667?s=460&v=4" title="Rami Jemli" width="80" height="80"></a><br /><sub><center><b>Rami Jemli</b></center></sub>  
</td>  
</tr>  
</table>  

## License  
  
 Copyright 2019 Rami Jemli
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at  
 http://www.apache.org/licenses/LICENSE-2.0  
 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.<br/>
