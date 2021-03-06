#### NanoStarbox: Net

*[Extended Library Class Path](./)*

This directory houses functionality which may be required by an
implementation that is not featured in the core library. The core library
will load functionality from any of these archives, preferentially,
when found in the jvm-user-classpath, which requires a local-file-source.

These libraries can be used via their official repository access URLs,
however any URL classloading should cache the downloaded data to an
implementation defined local path for both host and client, operational
efficiency.

The [NSR7A Specification](https://github.com/hypersoft/NanoStarbox/releases/tag/NSR7A) introduced [nano-jna-4.5.2-extended.jar](https://github.com/hypersoft/NanoStarbox/blob/NSR7/net/nano-jna-4.5.2-extended.jar) which houses
all jna libraries that are not included with the core, but are defined by
the core.

*NSR7 jna support was not officially enabled until NSR8*.

The [NSR8 Specification](https://github.com/hypersoft/NanoStarbox/releases/tag/NSR8) introduced single machine architecture archives,
which includes all specifications not included with the core. Each machine dependent
archive is listed of the form: 'nano-jna-OS_NAME-OS_ARCH-4.5.2.jar', except in the case
of "darwin" OS which is located at 'nano-jna-darwin-4.5.2.jar'.

The [NSR8 Specification](https://github.com/hypersoft/NanoStarbox/releases/tag/NSR8) 
also introduced [byte-buddy](https://github.com/hypersoft/NanoStarbox/blob/NSR8/net/byte-buddy) 1.9.13, a 
[code generation utility](https://bytebuddy.net/#/) for creating and 
modifying Java classes during the runtime of a Java application,
without the help of a compiler. This enables a scripting implementation
to construct classes on the fly. Android support is available. This
addition seeks to overcome the limitations and shortcomings of the
Mozilla Rhino Class Compiler Suite, while providing that extensibility
to the NanoStarbox Core user, providing a simple upgrade path from
Web Commander, to JVM/Web Commander. The inclusion of this library is a
mere courtesy provided due to the size and complexity of the project.
No source level modifications have been made to this snapshot.

It should be duly noted that the byte-buddy library combined with the
macro scripting and command shell capabilities of the core, provide a
wide range of code opportunities, which make this NanoSDK a formidable
library for any systems engineer. The compact size of the SDK ensures
a short learning curve with a toolset that won't change its stripes
on the developer.

\[(~4.89MB - ~859KB): Net Transfer Load]