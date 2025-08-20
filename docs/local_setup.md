# Ads Client – Local Dev Setup

This doc will outline how to:

* Build the `ads-client` Rust UniFFI component (`components/ads-client`) from mozilla/application-services
* Publish the component and the megazord to a local maven instance
* Consume the components into a barebones example Android app for testing

### Prerequisites

* macOS or Linux with Android toolchain
* Java 17+ (match your AGP)
* Android Studio (recommended) or Gradle CLI
* Android SDK + NDK
* Rust + Cargo
* CMake + ninja (commonly required by NDK builds)
* Kotlin toolchain aligned with App Services artifacts (currently **Kotlin 2.2.10** in our setup)
* Access to **maven.mozilla.org** (for Android Components)
* A working build process for application-services. See the [docs](https://github.com/mozilla/application-services/blob/main/docs/building.md) for more info.

### Other Repos

* **application-services/** – Rust sources + Gradle build that produces Android artifacts
  * `components/ads-client/` (your Rust component)



## Local Setup with Android Studio

1. Follow [this guide](https://github.com/mozilla/application-services/blob/main/docs/building.md#building-for-fenix) in A-S for *Building for Fenix* ignoring the first step where you clone firefox-android (not needed!).
2. Make sure your env variables are set correctly and verify that the A-S script `./lib/verify-android-environment.sh` is green. (Follow it's recommendations otherwise)

3. Adjust Android Studio SDK settings
If you are using a macOS, go to:

```
Android Studio -> Settings -> Language and Frameworks -> Android SDK
```

From here select the `SDK Tools` section and enable the following:

* NDK (Side by side) pinned to the version listed in [this doc](Build & Publish from Application Services)
* Android SDK Command-line Tools (latest)

Things like Android SDK Build-Tools and CMake should already be selected by default.

Once this is all complete, you should be able to follow the rest of this guide to publish a local A-S artifact and ingest it in the Android app!

## Build & Publish from Application Services

In order to test local changes to the application-services `ads-client` component, we can publish to a local Maven that the app can consume from.

From the root of **application-services**:

```shell
./gradlew publishToMavenLocal
```

Errors here indicate that you may still have additional setup needed. Check the A-S build guide for info.

If things succeed, artifacts should appear under:

```shell
~/.m2/repository/org/mozilla/appservices/
  full-megazord/<VER>/
  ads-client/<VER>/
  init-rust-components/<VER>/
```

You can verify which components have been installed with:

```shell
./gradlew projects
```

We only need `ads-client` and `httpconfig`.

### Common issues

Many I'm sure, but currently the sample size is 1... so this will be updated as more people go through this process.

## Running the example apps

### Android

To run our Android app, simply build the project through Android Studios UI or do:

```shell
./gradlew clean :app:installDebug
```

You can then start the app using the built-in Android Studio emulators. Assuming there are no issues in the build, you should see a simple UI which allows us to fetch two billboard ads from MARS prod. Once fetched, we show the ad as well as display buttons that fire a click, impression, or report callback.

### iOS 

Under Construction

## Building a new application

To build a new application that reads from the local maven repository, you can follow the steps below

### Android

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

### iOS

**Under Construction**

## Local Component Development and Testing

A primary reason for building this example app is to allow for us to rapidly test changes to the Rust component in a "real" android app. For development, I prefer the following flow:

* Make changes to the Rust component in a local version A-S
* Build and publish those changes from A-S with:
```shell
./gradlew :ads-client:publishToMavenLocal
```

(`./gradlew publishToMavenLocal` will publish the full-megazord)
* Rebuild the Android app using your favorite method e.g.

```shell
./gradlew clean :app:installDebug
```

* Re-run the emulator and your changes should appear!
