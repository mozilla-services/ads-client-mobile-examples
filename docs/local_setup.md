# Ads Client – Local Dev Setup

This doc will outline how to:

* Build the `ads-client` Rust UniFFI component (`components/ads-client`) from mozilla/application-services
* Publish the component and the megazord to a local maven instance
* Consume the components into a barebones example Android or iOS app for testing and debugging.


### Other Repos

* **application-services/** – Rust sources + Gradle build that produces Android artifacts
  * `components/ads-client/`

## Android

### Local Setup with Android Studio

1. Checkout the latest `main` branch from Application-Services
2. Follow the instructions in Application-Services to setup your local env to build the components. (See: `/docs/building.md`)
3. Build the android components with `./build-all.sh android` in A-S
4. Download or verify you have Java17 installed. `java --version`
  * You can use the openJDK via homebrew. `brew install openjdk@17`
  * You do **not** need to make this a system default or symlink.

5. Add `JAVA_HOME` to your env vars
e.g.
```shell
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```
4. Download Android studio and add `ANDROID_SDK_ROOT` and `ANDROID_HOME` to tyour env vars.
e.g.
```shell
export ANDROID_SDK_ROOT=~/Library/Android/sdk
export ANDROID_HOME=~/Library/Android/sdk
```

6. Adjust Android Studio SDK settings to include the NDK and CLI

```
Android Studio -> Settings -> Language and Frameworks -> Android SDK
```

From here select the `SDK Tools` section and enable the following:

* NDK (Side by side) pinned to the version listed in [this doc](Build & Publish from Application Services)
* Android SDK Command-line Tools (latest)
* CMake `3.31.6`

Once this is all complete, you should be able to follow the rest of this guide to publish a local A-S artifact and ingest it in the Android app!

### Build & Publish from Application Services to local Maven

In order to test local changes to the application-services `ads-client` component, we can publish to a local Maven instance that the app can consume from.

### Setup

First make sure you have python 3.9 installed. Later or earlier version of python will not work.

```shell
pyenv install 3.9
pyenv local 3.9.22
```

Install Ninja:
```shell
brew install ninja
```

Install wget
```shell
wget https://bootstrap.pypa.io/ez_setup.py -O - | python3 -
git clone https://chromium.googlesource.com/external/gyp.git ~/tools/gyp
cd ~/tools/gyp
pip install .
```

Set paths:
```shell
export PATH="~/tools/gyp:$PATH"
```

You also will likely need `six`

```shell
pip install six
```

From the root of **application-services**:

```shell
./libs/verify-desktop-environment.sh
```


Build the Android artifacts with:
```shell
./build-all.sh android
```


Now we can try building to local Maven!
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

### Running the example Android app

To run our Android app, simply build the project through Android Studios UI or do:

```shell
./gradlew clean :app:installDebug
```

You can then start the app using the built-in Android Studio emulators. Assuming there are no issues in the build, you should see a simple UI which allows us to fetch two billboard ads from MARS prod. Once fetched, we show the ad as well as display buttons that fire a click, impression, or report callback.

## iOS

iOS should be much simpler, because we just directly copy the generated objects to the iOS repo instead of trying to link to them.

### Building iOS Artifacts

We do not commit the .xcframework binary directly to git, so we have to generate it locally. To do this we have a script:

```sh
./scripts/sync_ios_local_megazord.sh --app-services <path-to-application-services> -mobile-examples <path-to-ads-client-mobile-examples>
```

This will build the necessary iOS artifacts and generated Swift code in Application-Services and copy the needed files to our iOS app.

### Running the Example App locally

Assuming the build system in A-S is setup correctly, you should be good to build and run the iOS after the script above complete! It is easiest just to launch it directly from XCode.

## Local Component Development and Testing

### Android

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


### iOS

Once changes are made to the Rust component, you can just re-run the sync script and re-build.
