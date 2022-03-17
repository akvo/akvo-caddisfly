package org.akvo.caddisfly.sensor.cbt

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class CbtViewModel(application: Application) : AndroidViewModel(application) {
    var result1: String = "00000"
    var result2: String = "00000"
    var index1: Int = 0
    var index2: Int = 0
    var sampleQuantity: String = "100"
    var resultCount: Int = 0
}