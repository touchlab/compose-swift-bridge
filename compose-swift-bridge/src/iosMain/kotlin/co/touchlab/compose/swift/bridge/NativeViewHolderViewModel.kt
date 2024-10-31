package co.touchlab.compose.swift.bridge

import androidx.lifecycle.ViewModel

class NativeViewHolderViewModel<VIEW_TYPE : Any, DELEGATE : Any>(
    val factory: () -> Pair<VIEW_TYPE, DELEGATE>
) : ViewModel() {
    private val keep by lazy { factory() }

    val view by lazy { keep.first }
    val delegate by lazy { keep.second }
}