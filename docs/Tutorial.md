# Tutorial

## 1. Configure the project with the plugin

There are type types of setup:
1. The single module approach, your hole app is in the same module
2. Multi module approach, your screens and widgets are spread between multiples modules
   and you have Umbrella Module that exports the Framework to iOS

### Single module approach

The tool requires [SKIE](https://skie.touchlab.co/) and KSP be configure in your project `build.gradle.kts` for example:

```kotlin
plugins {
    id("co.touchlab.skie") version "0.9.3"
    id("com.google.devtools.ksp") version "YOUR KSP VERSION HERE"
    ...
}
```

We need to add the Compose Swift Expect dependency to the common main to allow us access the Annotation.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("co.touchlab.compose:compose-swift-interop:0.1.0-ALPHA")
        }
    }
}
```

We need to configure the Compose Swift Expect KSP Plugin and SKIE SubPlugin. You should add the KSP dependency
to all targets that your project support, for example wasm, js, jvm, android, etc. The main reason is that the
tool will generate `expect interface`'s that depending on the target, it will generate the actual implementations
automatically.

```kotlin
dependencies {
    val composeSwiftInteropKsp = "co.touchlab.compose:compose-swift-interop-ksp:0.1.0-ALPHA"
    "kspCommonMainMetadata"(composeSwiftInteropKsp) // Common Main generation required
   
    // iOS targets
    "kspIosSimulatorArm64"(composeSwiftInteropKsp)
    "kspIosArm64"(composeSwiftInteropKsp)
    "kspIosX64"(composeSwiftInteropKsp)
      
    // All targets your module support, here, is Android only as a example
    "kspAndroid"(composeSwiftInteropKsp)

    // add the SKIE SubPlugin that will generate the Swift code
    skieSubPlugin("co.touchlab.compose:compose-swift-interop-skie:0.1.0-ALPHA")
}
```

We need to Configure KSP to be able to identify the target that is running for the tool and support Common code generation.

```kotlin
// Adds the required targetName for the KSP plugin
tasks.withType<KspTaskNative>().configureEach {
    options.add(SubpluginOption("apoption", "compose-swift-interop.targetName=$target"))
}

// support for generating ksp code in commonCode
// see https://github.com/google/ksp/issues/567
tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

We are all setup!

### Multi Module setup

Multi Module setup works in a different way, your project has multiple modules with Compose code,
and you want to be able to annotated this Composable with `@ExpectSwiftView`, for that we have to keep
some considerations on mind.


Considerations:
1. Each of your Modules should have a different Factory Name (The name that the generator use for creating the Interface that will be implemented in Swift)
   configured, so you have two options:
   1. Making sure that each Expect Composable has being configured with a Custom Factory name by configuring at the annotation `@ExpectSwiftView`.
   2. Or less error prune, using KSP property `ksp { arg("compose-swift-interop.defaultFactoryName", "MyModuleNameView") }`. (See sample bellow)
2. The modules should be exported to iOS (aka ``framework { export(project("your_module")) }``)

Let's dive in.

The modules containing Compose UI and the expect composable with @ExpectSwiftView should have KSP configured
similar to the umbrella module setup.

```kotlin
plugins {
    id("com.google.devtools.ksp") version "YOUR KSP VERSION HERE"
    ...
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // adds the dependency for accessing annotations
            implementation("co.touchlab.compose:compose-swift-interop:0.1.0-ALPHA")
        }
    }
}

dependencies {
   val composeSwiftInteropKsp = "co.touchlab.compose:compose-swift-interop-ksp:0.1.0-ALPHA"
   "kspCommonMainMetadata"(composeSwiftInteropKsp) // Common Main generation required

   // iOS targets
   "kspIosSimulatorArm64"(composeSwiftInteropKsp)
   "kspIosArm64"(composeSwiftInteropKsp)
   "kspIosX64"(composeSwiftInteropKsp)

   // All targets your module support, here, is Android only as a example
   "kspAndroid"(composeSwiftInteropKsp)

   // add the SKIE SubPlugin that will generate the Swift code
   skieSubPlugin("co.touchlab.compose:compose-swift-interop-skie:0.1.0-ALPHA")
}

// Adds the required targetName for the KSP plugin
tasks.withType<KspTaskNative>().configureEach {
   options.add(SubpluginOption("apoption", "compose-swift-interop.targetName=$target"))
}

ksp {
    // Configure the module with a custom default factory name, the default is called "NativeView"
    arg("compose-swift-interop.defaultFactoryName", "MyModuleNameView")
}

// support for generating ksp code in commonCode
// see https://github.com/google/ksp/issues/567
tasks.withType<KotlinCompile<*>>().configureEach {
   if (name != "kspCommonMainKotlinMetadata") {
      dependsOn("kspCommonMainKotlinMetadata")
   }
}
```

## 2. Using the tool

So you have setup the project properly, now the next step is starting using the tool. For the sake of the tutorial
lets imagine two scenarios.

1. First you have a Compose Component, for example a Map, that on Android you have Google Maps but
   for iOS the UIKit API is not good enough to be using at `iosMain`, so you have choose to implement this in Swift.
2. Second you have a Screen that for performance reasons or native feeling your Managers want it to be implemented
   in the Native view system of iOS, so in this case, you will write in SwiftUI.

### 2.1: Applying the annotation

For the first scenario, here a example:

You have a MapView component, that receives a Title and a Coordinate of where the Map Pin will be located, on Android
you use Google Maps as you would, on iOS you don't need to implemented this actual function, you just need to annotated with
`@ExpectSwiftView`, the tool will take care of creating the actual implementation using KSP.

```kotlin
@ExpectSwiftView
@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    title: String,
    coordinate: MapCoordinates,
)
```

### 2.2: Providing the Factory Interface that will come from iOS

Now you have to build the project for iOS, for example: `gradlew :linkDebugFrameworkIosArm64`.
This will make both KSP and SKIE Sub Plugin and generate all required files to you to continue
the first setup.

After successfully running the build, this Kotlin types will be generated: `Local{FactoryName}Factory` a LocalComposition
and `{FactoryName}Factory` the interface for factoring the IOS Views (SwiftUI, UIKit). The `FactoryName` placeholder is based
on the configuration that you can declare at the `@ExpectSwiftView` or if you have configure a using KSP property `compose-swift-interop.defaultFactoryName`,
by default, if you have not customize it, it will be always `NativeView`, so you should expect to be generated with the following names:
`LocalNativeViewFactory` and `NativeViewFactory`.

Now we need to update your Compose IOS EntryPoint (The function that returns a UIViewController and initialize Compose with `ComposeUIViewController`)
with the `{FactoryName}Factory` parameter, and provide what we will receive from the iOS Project at the LocalComposition `Local{FactoryName}Factory`.

```kotlin
// For the sake of the example, we are using the Default Factory Name: NativeView
fun MainViewController(
    nativeViewFactory: NativeViewFactory
): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(
        LocalNativeViewFactory provides nativeViewFactory,
    ) {
        AppScreen() // Your UI Content
    }
}
```

### 2.3: The SwiftUI Native Component

Lets start by the sample Map View using SwiftUI close to what the Composable that we define previously,
we need a `title` and `coordinate`, so here is a example Map View implementation following this.

```swift
import SwiftUI
import ComposeApp
import MapKit

struct NativeMapView : View {
    let title: String
    let coordinate: MapCoordinates
    
    @State private var position: MapCameraPosition

    init(title: String, coordinate: MapCoordinates) {
        self.title = title
        self.coordinate = coordinate
        _position = State(initialValue: .region(
            MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: coordinate.lat, longitude: coordinate.lng),
                span: MKCoordinateSpan(latitudeDelta: 0.008, longitudeDelta: 0.008)
            )
        ))
    }
    
    var body: some View {
        MapReader { reader in
            Map(position: $position) {
                Annotation(
                    title,
                    coordinate: CLLocationCoordinate2D(
                        latitude: coordinate.lat,
                        longitude: coordinate.lng
                    )
                ) {
                    VStack {
                      Group {
                        Image(systemName: "mappin.circle.fill")
                          .resizable()
                          .frame(width: 30.0, height: 30.0)
                        Circle()
                          .frame(width: 8.0, height: 8.0)
                      }
                      .foregroundColor(.red)
                    }
                }
            }
            .disabled(true)
        }
        .onChange(of: coordinate) { newCoordinate in
            position = .region(
                MKCoordinateRegion(
                    center: CLLocationCoordinate2D(latitude: newCoordinate.lat, longitude: newCoordinate.lng),
                    span: MKCoordinateSpan(latitudeDelta: 0.008, longitudeDelta: 0.008)
                )
            )
        }
    }
}
```

Now we can make things interesting, Compose Expect Swift also generates a ObservableObject for all
parameters at the Composable function, this way, the SwiftUI View can always keep updated with the
composable states. The Observable name generate follows the name of the Composable: `{ComposableName}Observable`,
in the case of the tutorial, will be `MapViewObservable`.

With this ObservableObject we can directly update the View component to receive it or we can create a
binding/observable component that receives the ObservableObject as parameter and renders the Native View
as show bellow.

```swift
struct NativeMapViewBinding : View {
    @ObservedObject var observable: MapViewObservable
    
    var body: some View {
        NativeMapView(title: observable.title, coordinate: observable.coordinate)
    }
}
```

### 2.4: Implementing the Factory protocol

As we have saw previously, the code generator will generate a `{FactoryName}Factory` interface/protocol that we
need to implement on iOS and provide our SwiftUI Views for each Composable function annotated with `@ExpectSwiftView`.
In the case of the tutorial, the protocol we need to implement on iOS is `NativeViewFactory` because we are using the
default factory name as mentioned previously.

```swift
class SwiftUINativeViewFactory : NativeViewFactory {
    func createMapView(observable: ComposeApp.MapViewObservable) -> AnyView {
        return AnyView(NativeMapViewBinding(observable: observable))
    }
}
```

### 2.5: Update iOS to provide to Compose

The last part is to actual provide this implementation that we have built on iOS to your
Compose Entrypoint function on the iOS side. This example bellow the app is a SwiftUI app,
so we have a `UIViewControllerRepresentable`, but may different on your setup, the main change here
that we want to do is update the `MainViewController` call to provide the `SwiftUINativeViewFactory`
that we have created.

```swift
struct MainView : UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        MainViewController(nativeViewFactory: SwiftUINativeViewFactory())
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}
```

Now, just run the iOS app and see in practice. 👏👏