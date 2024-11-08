# Compose Swift Bridge

Compose Swift Bridge is an experimental tool that helps you define expect composable functions to be
implemented later on the iOS Project using Swift. The tool works by generating Kotlin and Swift code
based on the Expect Composable functions annotated with `@ExpectSwiftView` and generate a View factory interface that will be implemented
by hand in the iOS Project. Also, the tool generates an ObservableObject for each parameter in the Expect Composable function
allowing to easily keep the state parameter updated in the SwiftUI View without any hassle. This is all possible by using [SKIE](https://skie.touchlab.co/) Sub Plugin API and KSP.

See docs at [Compose Swift Bridge](https://touchlab.co/composeswiftbridge)

### Running The Sample

The sample project showcases the Compose Swift Bridge by defining an expect/actual `MapView`. The
sample displays a [SwiftUI MapKit Map](https://developer.apple.com/documentation/mapkit/mapkit_for_swiftui) on iOS, 
and a [Google Maps Composable](https://github.com/googlemaps/android-maps-compose) on Android.

In order to run the sample you'll need to correctly configure Google Maps on Android (iOS does not need an API key). 
For that you will need a [Google Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

Then you'll need to create a `secrets.properties` file in the root and add that API key to your `secrets.properties` like so:

```
MAPS_API_KEY=YOUR_API_KEY
```

> If you don't need to view the actual map, then you can leave the API key blank like so: `MAPS_API_KEY=`


## License

```
Copyright 2024 Touchlab, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```