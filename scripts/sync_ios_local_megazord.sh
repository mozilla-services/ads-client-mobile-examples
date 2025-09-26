#!/usr/bin/env bash
set -e

while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
  --app-services)
    APPLICATION_SERVICES_PATH="$2"
    shift
    shift
    ;;
  --mobile-examples)
    ADS_CLIENT_MOBILE_EXAMPLES_PATH="$2"
    shift
    shift
    ;;
  *)
    shift
    ;;
  esac
done

if [[ -z "$APPLICATION_SERVICES_PATH" || -z "$ADS_CLIENT_MOBILE_EXAMPLES_PATH" ]]; then
  echo "Usage: $0 --app-services <path-to-application-services> -mobile-examples <path-to-ads-client-mobile-examples>"
  exit 1
fi

IOS_APP_DIR="$ADS_CLIENT_MOBILE_EXAMPLES_PATH/example_apps/ios/AdsClientExampleIOS"

echo "Building iOS artifacts"
(cd "$APPLICATION_SERVICES_PATH" && ./automation/build_ios_artifacts.sh)

echo "Copying MozillaRustComponents.xcframework into the iOS Mobile Examples repo"
XCFRAMEWORK_PATH="$APPLICATION_SERVICES_PATH/megazords/ios-rust/MozillaRustComponents.xcframework"
DEST_XCFRAMEWORK_DIR="$IOS_APP_DIR/MozillaRustComponents.xcframework"
rm -rf "$DEST_XCFRAMEWORK_DIR"
cp -R "$XCFRAMEWORK_PATH" "$DEST_XCFRAMEWORK_DIR"

echo "Moving Swift bindings to Firefox iOS"
mkdir -p "$IOS_APP_DIR/MozillaRustComponentsWrapper/Generated"
cp -R "$APPLICATION_SERVICES_PATH/megazords/ios-rust/Sources/MozillaRustComponentsWrapper/Generated/." "$IOS_APP_DIR/MozillaRustComponentsWrapper/Generated/"

echo "Opening the example iOS app in Xcode"
open "$IOS_APP_DIR/AdsClientExampleIOS.xcodeproj"

echo "Sync complete! You are now good to build."
