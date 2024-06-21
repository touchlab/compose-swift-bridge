# compose-expect-swift

Compose Swift Interop is a code generator that helps bringing UIKit and SwiftUI views to Compose KMP
by generating factory interface that can be implemented on Swift and expose to KMP also by leveraging
Compose state changes to the SwiftUI and UIKit.

## How to use it

Annotate a `expect fun` with `@ExpectSwiftView`

commonMain:
```kotlin
@ExpectSwiftView
@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    title: String,
    coordinate: MapCoordinates,
)

@ExpectSwiftView(type = ViewType.UIView)
@Composable
expect fun MapViewExampleUiKit(
    modifier: Modifier = Modifier,
    title: String,
    coordinate: MapCoordinates,
)
```

On the iOS target, we need to request the Factory interface in the Compose UIViewController function
and provide it in the Local Composition.

iosMain:
```kotlin
fun MainViewController(
    generatedViewFactory: ComposeNativeViewFactory
): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(
        LocalNativeViewFactory provides generatedViewFactory,
    ) {
        AppScreen()
    }
}
```

On iOS, we need to implement the `NativeViewFactory` and pass it to `MainViewController`.
We receive a ObservableObject that contains all State parameters from the defined expect function,
when Compose recompose with a new state, only that parameter will be updated on SwiftUI.

```swift
class SwiftUINativeViewFactory : NativeViewFactory {
    // SwiftUI type example (default annotation configuration)
    func createMapView(observable: MapViewObservable) -> AnyView {
        let view = NativeMapViewBinding(observable: observable)
        return AnyView(view)
    }
    
    // UIKit View type example
    // In this example, the Custom UIView implements MapViewExampleUiKitDelegate
    // to consume state updates, so it conforms to MapViewExampleUiKitDelegate.
    func createMapViewExampleUiKit(title: String, coordinate: MapCoordinates) -> (view: UIView, delegate: MapViewExampleUiKitDelegate) {
        let view = NativeMapUIKitView(title: title, coordinate: coordinate)
        return (view: view, delegate: view)
    }
```

We update MainViewController call passing the `iOSNativeViewFactory(SwiftUINativeViewFactory())`

```swift
struct MainView : UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        MainViewController(generatedViewFactory: iOSNativeViewFactory(SwiftUINativeViewFactory()))
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}
```

## Setup

This is a KSP generator that uses SKIE Bundling Swift 0.8.0 feature, is required to have both setup in
your project.

```kotlin
plugins {
    alias(libs.plugins.skie) // TODO: Change here
    alias(libs.plugins.ksp) // TODO: Change here
}
```

```kotlin
dependencies {
    "kspCommonMainMetadata"(projects.composeSwiftInteropGenerator) // TODO: Change here
    "kspAndroid"(projects.composeSwiftInteropGenerator) // TODO: Change here

    "kspIosSimulatorArm64"(projects.composeSwiftInteropGenerator) // TODO: Change here
    "kspIosArm64"(projects.composeSwiftInteropGenerator) // TODO: Change here
    "kspIosX64"(projects.composeSwiftInteropGenerator) // TODO: Change here
}

tasks.withType<KspTaskNative> {
    val skieCompilationAbsolutePath = layout.buildDirectory.file("skie/compilation/").get().asFile.absolutePath
    outputs.dir(skieCompilationAbsolutePath) // forces KSP task cache to sync with SKIE output folder

    options.add(SubpluginOption("apoption", "swiftInterop.targetName=${this.target}"))
    options.add(
        SubpluginOption(
            "apoption",
            "swiftInterop.swiftOutputPath=${skieCompilationAbsolutePath}"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
```
