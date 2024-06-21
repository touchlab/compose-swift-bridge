package dev.srsouza.compose.swift.interop

annotation class ExpectSwiftView(
    val factoryName: String = "NativeView",
    val type: ViewType = ViewType.SwiftUI,
)

enum class ViewType {
    SwiftUI,
    UIViewController,
    UIView,
}