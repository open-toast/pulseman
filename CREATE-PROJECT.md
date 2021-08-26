# Creating a project

## General settings

**Topic**: The topic you want to sent messages to or monitor.  
e.g `persistent://tenant/namespace/topic`  
**Service URL**:  The URL of the Pulsar service that the client should connect to  
e.g `pulsar+ssl://pulsar.eng.com:6651`

## Adding jars to a project

Three types of Jars can be added in the **Message**, **Auth** or **Other** jars tabs.  
These are described below. If a jar is added to any tab it is available to the whole project.

## Set up auth

If your pulsar set up utilizes authentication you can import your own Auth classes if they implement these apache pulsar
interfaces.  
[Authentication](https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/Authentication.html)  
[EncodedAuthenticationParameterSupport](https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/EncodedAuthenticationParameterSupport.html)

You will then need to provide your auth settings as a string, these will be passed to the **configure** method of the
**EncodedAuthenticationParameterSupport** interface at runtime.

### Steps

1. Import your jar in the **Auth Jars** tab.
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

1. Press the **Jars** button and go to the **Message** tab.
2. Add any jars containing your message classes.
3. In the **Class** tab you can now select any class to send a message with or deserialize with.  
   You can define multiple deserialization classes if you want to try to decode a message into multiple formats.

## Send a message to a topic

1. Import your message jar
2. Select your message class to send in the **Class** Tab.
3. In the **Send** tab select **Generate** to give a template for the message you want to send.
4. This is kotlin scripting code, the last value returned is what will be serialized and sent.  
   Create the message you want to send and hit **Compile**.
5. Hit the **Send** button, and you're done, hitting **Send** multiple times will send the same message unless you
   recompile.

## Monitor a topic.

1. Import the message jar you wish to deserialize messages with.
2. In the **Class** tab select 1 or more classes to decode messages with in the **Receive** column.
3. In the **Receive** tab every message on the topic will be decoded with the classes selected.

Note: Each message decoded will show the properties of the message also.
