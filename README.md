# Overview

This repository contains Java (JNI) bindings to the Twilio Video C++ SDK. The bindings are auto-generated using
[SWIG](http://www.swig.org/Doc3.0/SWIGDocumentation.html).

# Building

This is a standard Maven project. However, to properly download the dependencies and build the native part of it you
need to define at least one property -- `build.platform`. The supported values are:

* `darwin`
* `linux`

In addtion, there is an optional property -- `build.debug`, which can be used to force a debug build of the native (JNI)
library. This can be accomplished by setting it to `1`. If the propery is omitted or set to any other value, the build
will produce non-debug version of the native library.

# Testing

Unit tests require live Twilio credentials to run. These credentials can be passed in using the following Maven
properties:

* `ACCOUND_SID`
* `API_KEY`
* `API_KEY_SECRET`

If one or more properties are omitted the unit tests will not run.

# Examples

1. Build `video-java` package for Linux with the debug version of native library (unit tests are skipped):

```
mvn -Dbuild.platform=linux -Dbuild.debug=1 clean package
```

2. Build `video-java` package for MacOSX with non-debug verion of native library and run unit tests with the specified
credentials:
```
mvn -Dbuild.platform=darwin -DACCOUNT_SID=AC1234567890abcdef1234567890abcdef -DAPI_KEY=SK1234567890abcdef1234567890abcdef -DAPI_KEY_SECRET=AbCdEfGhIjKlMnOpQrStUvWxYz123456 clean package
```

