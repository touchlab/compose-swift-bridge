# compose-expect-swift

Compose Swift Interop is a code generator that helps bringing UIKit and SwiftUI views to Compose KMP
by generating factory interface that can be implemented on Swift and expose to KMP also by leveraging
Compose state changes to the SwiftUI and UIKit.

## How to use it

Annotate a `@Composable expect fun` with `@ExpectSwiftView`

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

On the iOS target (iosMain), we need to request the Factory interface in the Compose UIViewController function
and provide it in the Local Composition.

iosMain:
```kotlin
fun MainViewController(
    generatedViewFactory: NativeViewFactory
): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(
        LocalNativeViewFactory provides generatedViewFactory,
    ) {
        AppScreen()
    }
}
```

On iOS project, we need to implement the `NativeViewFactory` and pass it to `MainViewController`.
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

We update MainViewController call passing the `SwiftUINativeViewFactory()`

```swift
struct MainView : UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        MainViewController(generatedViewFactory: SwiftUINativeViewFactory())
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}
```

### Annotation configurations

```
annotation class ExpectSwiftView(
    val factoryName: String = "NativeView",
    val type: ViewType = ViewType.SwiftUI,
    val keepStateCrossNavigation: Boolean = false
)
```

The code generation works by generating the actual function in the iOS target, this function
also uses a generate Factory interface that contains all factory functions to each of the annotated
Composable function with `@ExpectSwiftView`.

`factoryName`: Defines the name of the generated factories interfaces. By default the name of the factory
is called `NativeView`, when the following functions, interfaces are generated with this name, for example:
NativeView**Factory**(Used on iOS for the implementing the factory),
**Local**NativeView**Factory**(The local composition that you have to provide in order to the expect function to work).

This is mostly important when you want to fragment in multiple factories with their own responsibility or when you are
using Multi module setup and you should use a per Module Factory Name.

`keepStateCrossNavigation`: Used when your native view has it own state
and it should be kept when navigating back, for example, if you are replacing
a hole screen with SwiftUI and there is scrolling, if this is set to false,
when you navigating away from the screen and back, the scroll state will be
lose and the component will be recreated, by setting to true, the generator
will wrap your View inside a ViewModel that will survive the composition
and when going back, when reuse the same ViewController storage in the ViewModel. Notice: This should be
avoid for small components that can easily be recreated without cost, because
the ViewModel will survive even if you remove the Component from the Composition
in the same screen, in this case, having a untended memory leak until the Screen
that have started the Native view be disposed/removed from the stack.
Note: This uses Androidx ViewModel KMP under the hood, check with your Navigation library if does support it 
on iOS target.

## Setup

This is a KSP generator that uses SKIE Bundling Swift 0.8.4 feature, is required to have both setup in
your project.

```kotlin
plugins {
    alias(libs.plugins.skie) // TODO: Change here
    alias(libs.plugins.ksp) // TODO: Change here
}
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("co.touchlab.compose:compose-swift-interop:0.1.0-ALPHA")
        }
    }
}

dependencies {
    val composeSwiftInteropKsp = "co.touchlab.compose:compose-swift-interop-ksp:0.1.0-ALPHA"
    "kspCommonMainMetadata"(composeSwiftInteropKsp)
    "kspAndroid"(composeSwiftInteropKsp)

    "kspIosSimulatorArm64"(composeSwiftInteropKsp)
    "kspIosArm64"(composeSwiftInteropKsp)
    "kspIosX64"(composeSwiftInteropKsp)

    skieSubPlugin("co.touchlab.compose:compose-swift-interop-skie:0.1.0-ALPHA")
}

tasks.withType<KspTaskNative>().configureEach {
    options.add(SubpluginOption("apoption", "swiftInterop.targetName=$target"))
}

// support for generating ksp code in commonCode
// see https://github.com/google/ksp/issues/567
tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

### Multi Module setup

Sample can be found [here](sample/multimodule).

Considerations:
1. Each of your Modules should have a different Factory Name configured in the annotations.
2. The modules should be exported to iOS (aka ``framework { export(project("your_module")) }``)

The modules containing Compose UI and the expect composable with @ExpectSwiftView
```kotlin
plugins {
    alias(libs.plugins.ksp) // TODO: Change here
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("co.touchlab.compose:compose-swift-interop:0.1.0-ALPHA")
        }
    }
}

dependencies {
    val composeSwiftInteropKsp = "co.touchlab.compose:compose-swift-interop-ksp:0.1.0-ALPHA"
    "kspCommonMainMetadata"(composeSwiftInteropKsp)
    "kspAndroid"(composeSwiftInteropKsp)

    "kspIosSimulatorArm64"(composeSwiftInteropKsp)
    "kspIosArm64"(composeSwiftInteropKsp)
    "kspIosX64"(composeSwiftInteropKsp)
}

tasks.withType<KspTaskNative>().configureEach {
    options.add(SubpluginOption("apoption", "swiftInterop.targetName=$target"))
}

// support for generating ksp code in commonCode
// see https://github.com/google/ksp/issues/567
tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

The umbrella module (that module that packs all modules to generate the iOS Framework)
```kotlin
plugins {
    alias(libs.plugins.skie) // TODO: Change here
}

dependencies {
    skieSubPlugin("co.touchlab.compose:compose-swift-interop-skie:0.1.0-ALPHA")
}

// configuring the export like this
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            ...

            export(project(":the-module-that-contains-compose"))
        }
    }
}
```
