# JSplice
----

JSplice is a java class hotpatcher CLI powered by javassist
  1. Acquire jsplice patch file
  2. Apply patch with a single command
  3. Magic

# Features
  - Add, replace, or modify methods from existing classes
  - Add interface implementation to existing class

#### You can also:
  - Import additional classes to use in the patched code
  - Wrap a method to intercept, modify, or act on an rval

# Why?
Modifying a class usually involves recompiling the entire project, which ends up being troublesome when the source code is not available - and based on the build system even *when* the source is available.
JSplice recompiles and exports the modified classes, which allows you to repack the class into your original container (be it `jar`, `war`, raw classes, or something entirely different)

# Getting Started
### The JSplice patch format
The patches are written in JSON. An example patch might look like this:
```json
[{
  "target": "com.example.app.TargetClass",
  "methods": [{
    "name": "myTargetMethod",
    "type": 0,
    "body": [
      "System.out.println(\"myTargetMethod call blocked\");"
    ]
  }]
}]
```
The above patch will replace^1^ the method `myTargetMethod` in `com.example.app.TargetClass`, thus the original method `myTargetMethod` will never be called.

^1^ `type 0` is method replacement

----
Alternatively, you can append to existing methods instead of replacing it entirely:
```json
[{
  "target": "com.example.app.TargetClass",
  "methods": [{
    "name": "myTargetMethod",
    "type": 1,
    "body": [
      "System.out.println(\"original result was: \"+jsplice_result);",
      "return true;"
    ]
  }]
}]
```
That one would append^1^ `body` to `myTargetMethod` of `com.example.app.TargetClass` by renaming the original method and inserting a new method calling the original under the original name. The return value of the original method will be available in `jsplice_result`, which always has the type of the original rval. Thus our patched method ends up printing the original rval^(boolean)^ and `return`ing `true` to the caller.

^1^ `type 1` is method wrapping^(appending)^

### Commandline options
`-import`: Classpath to import, can be specified multiple times, should contain your patch targets and patch dependencies
`-patch`: Path to the JSplice patch json
`-outdir`: Output path for the patched classes

### Example Applying a Patch
```sh
$ mkdir out
$ java -jar jsplice.jar -import PatchTarget.jar -patch mypatch.json -outdir out
```
Then, all that's left to do is repacking the modified classes into your patch target.

# Patch Syntax Documentation

TODO, lol

### Tech
JSplice uses a number of open source projects to work properly:
* JCommander ^com.beust.jcommander^ - Command line parsing
* Javassist ^org.javassist.javassist^ - Hotpatching
* GSON ^com.google.code.gson^ - JSON parsing of jsplice patch files
* commons-io ^org.apache.commons.commons-io^ - IO utility functions

### Compilation

JSplice requires a Java JDK (1.8+) and Apache Maven

Compilation and Packaging:
```sh
$ mvn package
```

The built artifact ends up at `target/jsplice-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Development
Want to contribute? Great!

You can submit patches as GitHub Pull Requests

### Todos
 - Write proper tests
 - Add ability to create new classes for simplicity of complex patches
 - Add automatic repacking of patched target (CLI toggle, default off)

License
----
GNU Lesser General Public License v3.0
