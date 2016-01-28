# hello_lwjgl

How to get LWJGL working with Leiningen & Clojure.

I've noticed that I get several hits per week on this project.  So,
I've decided to update this and write a "How To" for you to create
your own LWJGL clojar natives release.

*Now with LWJGL3 goodness!*

The following setup echoes how I made the library work for my
[shadertone](http://github.com/overtone/shadertone) project.  I
haven't updated that to LWJGL3, yet, though.

I have verified this works for LWJGL 3.0.0b1 build 64 on Mac.  I'll
update when I test on Win/Linux, soon.

## Your Own LWJGL Lib Setup

The official LWJGL package is not setup properly for Clojure.  It
mixes the 32 and 64-bit libraries and the current Leiningen workaround
for this doesn't work.  What follows are the instructions on creating
your own clojars package of LWJGL.  IMO, it is easier to make your own
package than to work around the issues that the official package
presents.

I did this on a Mac.  Linux will be similar.  You'll have to
translate this for Windows yourself.

### download official jar file

goto https://www.lwjgl.org/download
and get the lwjgl.zip file you want.  Instructions below assume
LWJGL 3.0.0b1 build 64 (see build.txt file).

### unzip the release zip file

    unzip lwjgl.zip

### make sandbox to use for packing up the new jar you will create

    mkdir sandbox
    cd sandbox

### make native dirs as clojure expects

    mkdir -p native/macosx/x86_64
    mkdir -p native/linux/x86
    mkdir -p native/linux/x86_64
    mkdir -p native/windows/x86
    mkdir -p native/windows/x86_64

### copy natives from official spots to the right spots for clojure

    cp ../native/glfw.dll             native/windows/x86_64
    cp ../native/glfw32.dll           native/windows/x86
    cp ../native/jemalloc.dll         native/windows/x86_64
    cp ../native/jemalloc32.dll       native/windows/x86
    cp ../native/libglfw.dylib        native/macosx/x86_64
    cp ../native/libglfw.so           native/linux/x86_64
    cp ../native/libglfw32.so         native/linux/x86
    cp ../native/libjemalloc.dylib    native/macosx/x86_64
    cp ../native/libjemalloc.so       native/linux/x86_64
    cp ../native/libjemalloc32.so     native/linux/x86
    cp ../native/liblwjgl.dylib       native/macosx/x86_64
    cp ../native/liblwjgl.so          native/linux/x86_64
    cp ../native/liblwjgl32.so        native/linux/x86
    cp ../native/libopenal.dylib      native/macosx/x86_64
    cp ../native/libopenal.so         native/linux/x86_64
    cp ../native/libopenal32.so       native/linux/x86
    cp ../native/lwjgl.dll            native/windows/x86_64
    cp ../native/lwjgl32.dll          native/windows/x86
    cp ../native/OpenAL.dll           native/windows/x86_64
    cp ../native/OpenAL32.dll         native/windows/x86

### copy licenses

    cp -r ../doc .

### extract the jar file

    jar xvf ../jar/lwjgl.jar

### make your jar for clojure's use

    jar -cMf lwjgl-3.0.0b1.jar doc org native

    Note, I've skipped including the src.

### edit pom.xml

Change this as-needed for your own purposes.

    <?xml version="1.0" encoding="UTF-8"?>
    <project>
      <modelVersion>4.0.0</modelVersion>
      <groupId>hello_lwjgl</groupId>
      <artifactId>lwjgl</artifactId>
      <version>3.0.0b1</version>
      <name>lwjgl</name>
      <description>packaging of LWJGL for Clojure</description>
      <url>http://github.com/roger_allen/hello_lwjgl</url>
    </project>

### local test install

    mvn install:install-file -Dfile=lwjgl-3.0.0b1.jar -DpomFile=pom.xml

### try this out in hello_lwjgl

edit project.clj to use `[hello_lwjgl/lwjgl "3.0.0b1"]`

    lein clean
    lein -o deps
    lein -o run alpha

### upload this new lwjgl library to clojars for use by others

Add authentication info to settings.xml (typically in your ~/.m2 directory):

  <settings>
   <servers>
    <server>
     <id>clojars</id>
     <username>username</username>
     <password>password</password>
    </server>
   </servers>
  </settings>

Then you can deploy with

  mvn deploy:deploy-file -Dfile=lwjgl-3.0.0b1.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo


### check to make sure it all works from clojars

    > rm -rf ~/.m2/repository/hello_lwjgl/lwjgl
    > lein deps
    Retrieving hello_lwjgl/lwjgl/3.0.0b1/lwjgl-3.0.0b1.pom from clojars
    Retrieving hello_lwjgl/lwjgl/3.0.0b1/lwjgl-3.0.0b1.jar from clojars
    > lein run alpha

That should do it.  Now you have LWJGL from Clojure.  Enjoy!  If you
have feedback on this, please file an issue and let me know.

p.s. One thing I could improve here is adding the LWJGL license properly.
If you can suggest a proper way to package that, file an issue.

## Application Setup

If you want to create your own application using Clojure & LWJGL, here's what I did:

```bash
> lein new app hello_lwjgl
> cd hello_lwjgl
```

See project.clj and the source code for more info.  Hopefully it is easy to follow.

## Usage

All of these are very basic examples.

### alpha
A spinning triangle that uses OpenGL 1.1
### beta
A spinning triangle that uses OpenGL 3.2
### gamma
A fullscreen spinning triangle that you can move with a keyboard

To run these examples, just add the name to the lein run commandline.
E.g. to run the 'alpha' test:

```bash
> lein run alpha
```

### Running from the commandline

First create the 'uberjar'

```bash
> lein uberjar
...
Created .../hello_lwjgl/target/hello_lwjgl-0.2.0-SNAPSHOT-standalone.jar

Then you can run it with just a little extra help on the commandline.  Here are instructions for Mac.  Extrapolate for Windows/Linux.

```bash
> java -Djava.library.path=native/macosx/x86_64 -jar hello_lwjgl-0.2.0-SNAPSHOT-standalone.jar
```

## Notes

* found this helpful example: https://github.com/honeytree/clojure-lwjgl
* found this discussion: https://groups.google.com/forum/#!msg/leiningen/MAFbNqDYT78/Ub2scaa4RCoJ
* See this issue for some background. https://github.com/technomancy/leiningen/issues/898

## License

Copyright © 2013-2016 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.
