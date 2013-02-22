# hello_lwjgl

Just a basic attempt to get LWJGL working with Leiningen & Clojure.

## Setup

Here's what I did

```bash
> lein new app hello_lwjgl
> cd hello_lwjgl
```

See project.clj for my additions.  Note that fixing Leiningen is happening here:
https://github.com/technomancy/leiningen/issues/898

Keeping this list around for future potential issues...

```bash
> lein deps
Retrieving org/lwjgl/lwjgl/lwjgl/2.8.5/lwjgl-2.8.5.pom from central
Retrieving org/lwjgl/lwjgl/parent/2.8.5/parent-2.8.5.pom from central
Retrieving org/lwjgl/lwjgl/lwjgl-platform/2.8.5/lwjgl-platform-2.8.5.pom from central
Retrieving net/java/jinput/jinput/2.0.5/jinput-2.0.5.pom from central
Retrieving net/java/jutils/jutils/1.0.0/jutils-1.0.0.pom from central
Retrieving net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5.pom from central
Retrieving org/lwjgl/lwjgl/lwjgl/2.8.5/lwjgl-2.8.5.jar from central
Retrieving net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar from central
Retrieving org/lwjgl/lwjgl/lwjgl-platform/2.8.5/lwjgl-platform-2.8.5-natives-osx.jar from central
Retrieving org/lwjgl/lwjgl/lwjgl-platform/2.8.5/lwjgl-platform-2.8.5-natives-windows.jar from central
Retrieving org/lwjgl/lwjgl/lwjgl-platform/2.8.5/lwjgl-platform-2.8.5-natives-linux.jar from central
Retrieving net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar from central
Retrieving net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-linux.jar from central
Retrieving net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-windows.jar from central
Retrieving net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-osx.jar from central
```

To link in natives, until Leiningen gets better...do this for your platform.

```bash
> mkdir -p target/native/macosx/x86_64
> cd target/native/macosx/x86_64
> jar xf ~/.m2/repository/org/lwjgl/lwjgl/lwjgl-platform/2.8.5/lwjgl-platform-2.8.5-natives-osx.jar
```

## Usage

To see a spinning triangle
```bash
> lein run alpha
```

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
