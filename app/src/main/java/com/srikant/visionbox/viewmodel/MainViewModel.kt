package com.srikant.visionbox.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.srikant.visionbox.model.RecognizedObjects
import com.srikant.visionbox.util.BaseViewModel

class MainViewModel(application: Application): BaseViewModel(application) {
    val recognizedObjectsListesiLiveData = MutableLiveData<List<RecognizedObjects>>()
}
