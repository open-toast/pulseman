# Changelog

## [1.4.0] - 2023-11-08

- Add auth support for querying available pulsar topics.
- Update project to support new required mac signing tool.
- Update build pipeline to replace deprecated circle ci mac images.
- Alert the user when they have conflicting common protos loaded.
- Support ProtoKT common protos by default and allow them to work side by side with standard google common protos.

## [1.3.0] - 2022-12-09

NOTE: The project file format has been updated in this release. Any old format projects will be upgraded to the new
format on opening but this new format will not work on older versions of pulseman.

- Fix multi-line log messages getting cut off.
- Add base64 support for sending text over pulsar, text will be converted to and from base64.
- Update all library dependencies to their latest versions.
- Add UI dividers between tabs.
- Replace send and receive protobuf class selection with a single column.
- Add a Convert tab for protobuf, this will convert HEX/base64 byte array logs to a selected pulsar class.

## [1.2.0] - 2022-05-27

NOTE: The project file format has been updated in this release. Any old format projects will be upgraded to the new
format on opening but this new format will not work on older versions of pulseman.

- Allow user feedback to be copied.
- Fix code generation imports for protokt inner classes, replace '$' with '.'.
- Use native file dialogs.
- Add support for sending plain text messages with pulsar.
- Add support for googles new kotlin protobuf dsl.

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
