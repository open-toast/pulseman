# Creating a project

## General settings test

**Topic**: The topic you want to sent messages to or monitor.  
e.g `persistent://tenant/namespace/topic`  
**Service URL**:  The URL of the Pulsar service that the client should connect to  
e.g `pulsar+ssl://pulsar.eng.com:6651`

## Adding jars to a project

The **Jars** button in the top pane allows two types of Jars to be added to the project in the **Auth** or **Other**
tab.  
If the **Protobuf** serialization option is selected you can add the messaging jars in the **Jars** tab on the bottom
pane.  
These are described below. If a jar is added to any tab it is available to the whole project.

## Set up auth

If your pulsar set up utilizes authentication you can import your own Auth classes if they implement these apache pulsar
interfaces.  
[Authentication](https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/Authentication.html)  
[EncodedAuthenticationParameterSupport](https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/EncodedAuthenticationParameterSupport.html)

You will then need to provide your auth settings as a string, these will be passed to the **configure** method of the
**EncodedAuthenticationParameterSupport** interface at runtime.

### Steps

1. Import your jar in the **Auth** tab.
2. In the **Auth Settings** tab select the authentication class you want to use, if it doesn't show up it doesn't
   implement the correct interfaces.  
   If you want to disable auth unselect any classes here.
3. Still in the **Auth Settings** tab define your auth settings in the Auth parameters section.  
   This will be a user defined format, for the auth class you import.

## Define properties

You can also define an optional key/value map of user-defined properties sent in each message.

### Steps

1. Go to the properties tab.
2. Define a json map of key value pairs.

``` 
{
    "propertyKey": "propertyValue"
}
```

## Add a generic dependency jar

If you just want to add any dependency for use in serialization/deserialization/auth you can add it to the
**Dependency Jars** tab

## Searching for topics

If you select the magnifying glass icon and point it at your pulsar set up

e.g `http://localhost:8079`

You can pull down all the topics configured and select which one to use in the project, this only works for
unauthenticated pulsar setups currently.

## Import your messaging jars

Currently only
[protokt](https://github.com/open-toast/protokt/blob/main/protokt-runtime/src/main/kotlin/com/toasttab/protokt/rt/KtMessage.kt)
and
[GeneratedMessageV3](https://www.javadoc.io/static/com.google.protobuf/protobuf-java/3.5.1/com/google/protobuf/GeneratedMessageV3.html)
protobuf messaging formats are supported. You can import any jars that have classes implementing these interfaces.

More messaging formats will be added in the future.

### Steps

1. Select the **Jars** tab in any **Protobuf** test tab.
2. Add any jars containing your message classes.
3. In the **Class** tab you can now select any class to send a message with or deserialize with.  
   You can define multiple deserialization classes if you want to try to decode a message into multiple formats.

## Send a message to a topic

### Protobuf

1. Import your message jar
2. Select your message class to send in the **Class** Tab.
3. In the **Send** tab select **Generate** to give a template for the message you want to send.
4. This is kotlin scripting code, the last value returned is what will be serialized and sent.  
   Create the message you want to send and hit **Compile**.
5. Hit the **Send** button, and you're done, hitting **Send** multiple times will send the same message unless you
   recompile.

### Text

1. Define the text format in the **Serialization** tab.  
   Note: Selecting Base64 will convert your text to base64, the other selections only encode to a specific character
   set.
2. Enter the text you want to send in the **Send** tab and press the **Send** button.

## Monitor a topic.

Each message decoded will show the pulsar properties of the message also.

### Protobuf

1. Import the message jar you wish to deserialize messages with.
2. In the **Class** tab select a class to decode messages with.
3. In the **Receive** tab every message on the topic will be decoded with the class selected.

### Text

1. Define the text format in the **Serialization** tab.  
   Note: Selecting Base64 will convert your text from base64, the other selections only decode a specific character set.
2. In the **Receive** tab every message on the topic will be decoded with the serialization method selected in step one.

## Convert logs

### Protobuf

Currently only protobuf log conversion is supported. Both base64 and HEX byte array representation can be converted.

1. In the **Class** tab select a class to decode messages with.
2. In the **Convert** tab select the format you wish to convert from.
3. Paste the text you wish to convert in the **Convert** tab and select **Convert**
