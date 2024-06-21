package dev.srsouza.compose.swift.interop

import androidx.lifecycle.ViewModel
import platform.UIKit.UIViewController

class NativeViewHolderViewModel<VIEW_TYPE : Any, DELEGATE : Any>(
    val factory: () -> Pair<VIEW_TYPE, DELEGATE>
) : ViewModel() {
    private val keep by lazy { factory() }

    val view by lazy { keep.first }
    val delegate by lazy { keep.second }
}