# Compose Swift Bridge

Compose Swift Bridge is an experimental tool that helps you define expect composable functions to be
implemented later on the iOS Project using Swift. The tool works by generating Kotlin and Swift code
based on the Expect Composable functions annotated with `@ExpectSwiftView` and generate a View factory interface that will be implemented
by hand in the iOS Project. Also, the tool generates an ObservableObject for each parameter in the Expect Composable function
allowing to easily keep the state parameter updated in the SwiftUI View without any hassle. This is all possible by using [SKIE](https://skie.touchlab.co/) Sub Plugin API and KSP.

See docs at [Compose Swift Bridge](https://touchlab.co/composeswiftbridge)

## License

TODO