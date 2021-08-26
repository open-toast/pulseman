# Changelog

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
