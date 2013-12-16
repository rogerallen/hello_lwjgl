# hello_lwjgl

How to get LWJGL working with Leiningen & Clojure.

I've noticed (via the Bitdeli badge below) that I get several hits per
week on this project.  Since 2.9.1 was recently released (December 2,
2013), I've decided to update this and write a "How To" for you to
create your own LWJGL clojar natives release.

The following setup echoes how I made the library work for my [shadertone](http://github.com/overtone/shadertone) project.

I have verified this works for 2.9.0 on Mac/Win/Linux.  I'll update
when I test 2.9.1 on Win/Linux, soon.

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

goto
http://sourceforge.net/projects/java-game-lib/files/Official%20Releases/
and get the lwjgl-#.#.#.zip file you want.  Instructions below assume
2.9.1.

### check md5 & unzip the release zip file

    md5 lwjgl-2.9.1.zip
    unzip lwjgl-2.9.1.zip

### make sandbox to use for packing up the new jar you will create

    mkdir sandbox
    cd sandbox

### make native dirs as clojure expects

    mkdir -p native/macosx/x86
    mkdir -p native/macosx/x86_64
    mkdir -p native/linux/x86
    mkdir -p native/linux/x86_64
    mkdir -p native/windows/x86
    mkdir -p native/windows/x86_64

### copy natives from official spots to the right spots for clojure

    cp ../lwjgl-2.9.1/native/linux/libjinput-linux.so     native/linux/x86
    cp ../lwjgl-2.9.1/native/linux/libjinput-linux64.so   native/linux/x86_64
    cp ../lwjgl-2.9.1/native/linux/liblwjgl.so            native/linux/x86
    cp ../lwjgl-2.9.1/native/linux/liblwjgl64.so          native/linux/x86_64
    cp ../lwjgl-2.9.1/native/linux/libopenal.so           native/linux/x86
    cp ../lwjgl-2.9.1/native/linux/libopenal64.so         native/linux/x86_64
    cp ../lwjgl-2.9.1/native/macosx/libjinput-osx.jnilib  native/macosx/x86_64
    cp ../lwjgl-2.9.1/native/macosx/liblwjgl.jnilib       native/macosx/x86_64
    cp ../lwjgl-2.9.1/native/macosx/openal.dylib          native/macosx/x86_64
    cp ../lwjgl-2.9.1/native/windows/OpenAL32.dll         native/windows/x86
    cp ../lwjgl-2.9.1/native/windows/OpenAL64.dll         native/windows/x86_64
    cp ../lwjgl-2.9.1/native/windows/jinput-dx8.dll       native/windows/x86
    cp ../lwjgl-2.9.1/native/windows/jinput-dx8_64.dll    native/windows/x86_64
    cp ../lwjgl-2.9.1/native/windows/jinput-raw.dll       native/windows/x86
    cp ../lwjgl-2.9.1/native/windows/jinput-raw_64.dll    native/windows/x86_64
    cp ../lwjgl-2.9.1/native/windows/lwjgl.dll            native/windows/x86
    cp ../lwjgl-2.9.1/native/windows/lwjgl64.dll          native/windows/x86_64

### grab the rest of the jars

See http://www.lwjgl.org/wiki/index.php?title=General_FAQ#What_are_all_the_Jars_include_in_the_LWJGL_download_bundle.3F

After reading this, I decided to only include lwjgl and lwjgl_util in this
bundle.  Your needs may be different.

    cp ../lwjgl-2.9.1/jar/lwjgl.jar .
    cp ../lwjgl-2.9.1/jar/lwjgl_util.jar .
    jar xvf lwjgl.jar
    jar xvf lwjgl_util.jar

### make jar

    jar -cMf lwjgl-2.9.1.jar org native

### edit pom.xml

Change this as-needed for your own purposes.

    <?xml version="1.0" encoding="UTF-8"?>
    <project>
      <modelVersion>4.0.0</modelVersion>
      <groupId>hello_lwjgl</groupId>
      <artifactId>lwjgl</artifactId>
      <version>2.9.1</version>
      <name>lwjgl</name>
      <description>packaging of LWJGL for Clojure</description>
      <url>http://github.com/roger_allen/hello_lwjgl</url>
    </project>

### local test install

    mvn install:install-file -Dfile=lwjgl-2.9.1.jar -DpomFile=pom.xml

### try this out in hello_lwjgl

edit project.clj to use `[hello_lwjgl/lwjgl "2.9.1"]`

    lein clean
    lein -o deps
    lein -o run alpha

### upload this new lwjgl library to clojars for use by others

    scp lwjgl-2.9.1.jar pom.xml clojars@clojars.org:

### check to make sure it all works from clojars

    > rm -rf ~/.m2/repository/hello_lwjgl/lwjgl
    > lein deps
    Retrieving hello_lwjgl/lwjgl/2.9.1/lwjgl-2.9.1.pom from clojars
    Retrieving hello_lwjgl/lwjgl/2.9.1/lwjgl-2.9.1.jar from clojars
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

Copyright © 2013 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/rogerallen/hello_lwjgl/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
