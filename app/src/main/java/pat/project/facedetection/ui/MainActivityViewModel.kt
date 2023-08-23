package pat.project.facedetection.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import pat.project.facedetection.face.FaceAnalyzer

@OptIn(FlowPreview::class)
class MainActivityViewModel : ViewModel() {
    private val _value = MutableStateFlow(0f)
    val value = _value.asStateFlow()
    init {
        viewModelScope.launch {
            FaceAnalyzer.faceFlow
                .sample(1500)
                .collect{ newValue ->
                    _value.emit(
                        newValue
                    )
                }

        }
    }

}