package co.touchlab.compose.expect.swift

/**
 * Annotation that marks a Expected Composable function for generating
 * the iOS Actual implementation and that calls a the generate View
 * Factory that will be implemented and provided to Compose on consumer
 * iOS app.
 *
 * @param factoryName defines the name of the generated Factory interface,
 * this allows the separation of the components in different factory interface
 * being easy to maintain and being able to have native components by features
 * for example.
 * @param type defines what is the expected view type that should be provided
 * from the iOS consumer app, there are 3 view types, SwiftUI that is represented
 * by SwiftUI AnyView, UIViewController and UIKit UIView.
 * @param keepStateCrossNavigation used when your native view has it own state
 * and it should be kept when navigating back, for example, if you are replacing
 * a hole screen with SwiftUI and there is scrolling, if this is set to false,
 * when you navigating away from the screen and back, the scroll state will be
 * lose and the component will be recreated, by setting to true, the generator
 * will wrap your View inside a ViewModel that will survive the composition
 * and when going back, it will reuse the factory view instead. Notice should be
 * avoid for small components that can easily be recreated without cost because
 * the ViewModel will survive even if you remove the Component from the Composition
 * in the same screen, in this case, having a untended memory leak until the Screen
 * that have started the Native view be disposed/removed from the stack.
 * Note: This uses Androidx ViewModel under the hood, check with your Navigation
 * library if does support it.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ExpectSwiftView(
    val factoryName: String = "NativeView",
    val type: ViewType = ViewType.SwiftUI,
    val keepStateCrossNavigation: Boolean = false,
)

enum class ViewType {
    SwiftUI,
    UIViewController,
    UIView,
}

@Target(AnnotationTarget.FUNCTION)
annotation class ExpectSwiftViewCustomInteropComposable(
    val composableFqn: String
)