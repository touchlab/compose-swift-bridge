package dev.srsouza.compose.swift.interop

import androidx.lifecycle.ViewModel
import platform.UIKit.UIViewController

class NativeViewHolderViewModel<DELEGATE : Any>(
    val factory: () -> Pair<UIViewController, DELEGATE>
) : ViewModel() {
    private val keep by lazy { factory() }

    val viewController by lazy { keep.first }
    val delegate by lazy { keep.second }
}