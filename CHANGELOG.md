# Changelog

## [1.1.0] - 2022-02-07

- Upgrade to 1.0.0 release of Jetpack Compose for Desktop, general refactor was required.
  - Update AppManager to using Window.
  - Use JFrame for swing components. 
- Make all the views as stateless as possible using state hoisting. 
- Use the correct home directory on Mac so the app has permissions and a signed version of the app can be used. 
- Moved companion objects to the bottom of all classes.
- Fix issue where a successful send message was logged even on fail.
- Alphabetise imports and dependencies.
- Upgrade all import versions to latest.
- Refactor usage of Reflections library with latest version, the old api was deprecated.
- Slightly refactor Jackson serialisation usage after version update.
- Remove manually imported and cleaned up pulsar-client jar as it no longer blocks signing of the app.
- import sl4jNoop to prevent warnings.(Currently no logging framework is used but its been imported by a dependency).
- Move all translatable and reusable strings into AppStrings.kt, moving them to a resource file to follow up in future.
- Covert all string where possible into consts.
- Put repeated definition of the RSyntax theme to one location in AppResources.kt.
- Change multiple onChange type functions to a generic onStateChange function.

### Features

- Add support for mac signed apps

## [1.0.0] - 2021-08-26

- Initial release

### Features

- Send/generate messages to send to a topic using Kotlin Scripting
- Monitor/receive messages from a topic
- Query all topics(non-authenticated connections only at the moment)
- Import jars for serialization and deserialization of messages
- Import custom authentication jars
- Store and share test collections

Current formats supported for serialization and deserialization of messages.

- [protokt](https://github.com/open-toast/protokt/blob/main/protokt-runtime/src/main/kotlin/com/toasttab/protokt/rt/KtMessage.kt) (
  protobuf format)
- [GeneratedMessageV3](https://www.javadoc.io/static/com.google.protobuf/protobuf-java/3.5.1/com/google/protobuf/GeneratedMessageV3.html) (
  protobuf format)

Currently, this has only been tested and targeted to work on macOS.
