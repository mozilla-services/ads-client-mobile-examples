# Building New Applications

this doc outlines how to build a new Android or iOS app that an ingest from a local build of Application Services.

## Building a new Android application

To build a new Android application that reads from the local maven repository, you can follow the steps below

Android apps should be setup up so that repositories include **`mavenLocal()`**, **Google**, **Maven Central**, and **`https://maven.mozilla.org/maven2`**. See: `./example_apps/android/settings.gradle.kts`.

Then we can add whatever dependencies we need to `biuld.gradle.kts`. e.g.

```kts
    implementation("org.mozilla.appservices:ads-client:143.0a1")
    implementation("org.mozilla.appservices:httpconfig:143.0a1")
    ...
```

We additionally need:

```kts
    implementation("org.mozilla.components:concept-fetch:143.0b1")
    implementation("org.mozilla.components:lib-fetch-okhttp:143.0b1")
```

In order to create an `OkHTTPClient` to initialize the Rust backend with. Without this, we cannot make HTTP requests through `viaduct`.

If these steps are followed, you should be able to just import our ad-client rust component like any other library from Maven!

## Building a new iOS application

Building a new iOS app that ingests these components locally is hopefully pretty straight forward as we just copy the files we need generated from A-S.

The steps are:
1. Use XCode to generate a new project
2. Add the `glean` package dependency found at `https://github.com/mozilla/glean-swift`
3. Go to the **Build Phases** settings for the app, and include `libc++.tbd` and `libc++abi.tbd` In **Link Binary With Libraries**.
4. Copy the generated `.xcframework` and any needed swift wrappers to the working directory.
5. Build and run your application!
