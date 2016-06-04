# hello_lwjgl

How to get LWJGL working with Leiningen & Clojure.

I've noticed that I get several hits per week on this project.  So,
I've decided to update this and write a "How To" for you to create
your own LWJGL clojar natives release.

*Now with LWJGL3 goodness!*

The following setup echoes how I made the library work for my
[shadertone](http://github.com/overtone/shadertone) project.  I
haven't updated that to LWJGL3, yet, though.

I have verified this works for LWJGL 3.0.0 build 90 on Mac.
[will test on Windows/Linux soon, but should work]

## Your Own LWJGL Lib Setup

The official LWJGL package is not setup properly for Clojure.  It
mixes the 32 and 64-bit libraries and the current Leiningen workaround
for this doesn't work.  What follows are the instructions on creating
your own clojars package of LWJGL.  IMO, it is easier to make your own
package than to work around the issues that the official package
presents.

You don't have to create your own LWJGL library.  You can just use
mine if you'd like.  But, this is how I created it for myself.  I did
this on a Mac.  Linux will be similar.  You'll have to translate this
for Windows yourself.

### download official jar file

Goto https://www.lwjgl.org/download and get the lwjgl.zip file you
want.  Instructions below assume LWJGL 3.0.0 build 90 (see build.txt
file) which is the file that was downloaded via the "Release" button
in June, 2016.

### unzip the release zip file

    unzip lwjgl.zip

### make sandbox to use for packing up the new jar you will create

    mkdir sandbox
    cd sandbox

### make native dirs as clojure expects

(As of 3.0.0 there are no longer 32-bit binaries provided for linux.)

    mkdir -p native/macosx/x86_64
    mkdir -p native/linux/x86_64
    mkdir -p native/windows/x86
    mkdir -p native/windows/x86_64

### copy natives from official spots to the right spots for clojure

    cp ../native/OpenAL.dll           native/windows/x86_64
    cp ../native/OpenAL32.dll         native/windows/x86
    cp ../native/glfw.dll             native/windows/x86_64
    cp ../native/glfw32.dll           native/windows/x86
    cp ../native/jemalloc.dll         native/windows/x86_64
    cp ../native/jemalloc32.dll       native/windows/x86
    cp ../native/libglfw.dylib        native/macosx/x86_64
    cp ../native/libglfw.so           native/linux/x86_64
    cp ../native/libjemalloc.dylib    native/macosx/x86_64
    cp ../native/libjemalloc.so       native/linux/x86_64
    cp ../native/liblwjgl.dylib       native/macosx/x86_64
    cp ../native/liblwjgl.so          native/linux/x86_64
    cp ../native/libopenal.dylib      native/macosx/x86_64
    cp ../native/libopenal.so         native/linux/x86_64
    cp ../native/lwjgl.dll            native/windows/x86_64
    cp ../native/lwjgl32.dll          native/windows/x86

### copy licenses

    cp -r ../doc .

### extract the jar file

    jar xvf ../jar/lwjgl.jar

### make your jar for clojure's use

    jar -cMf lwjgl-3.0.0.jar META-INF doc org native

    Note, I've skipped including the src.

### edit pom.xml

Change this as-needed for your own purposes.

    <?xml version="1.0" encoding="UTF-8"?>
    <project>
      <modelVersion>4.0.0</modelVersion>
      <groupId>hello_lwjgl</groupId>
      <artifactId>lwjgl</artifactId>
      <version>3.0.0</version>
      <name>lwjgl</name>
      <description>Packaging of LWJGL3 for Clojure</description>
      <url>http://github.com/roger_allen/hello_lwjgl</url>
    </project>

### local test install

    mvn install:install-file -Dfile=lwjgl-3.0.0.jar -DpomFile=pom.xml

### try this out in hello_lwjgl

edit project.clj to use `[hello_lwjgl/lwjgl "3.0.0"]`

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

Then you can deploy (from the sandbox dir) with

    mvn deploy:deploy-file -Dfile=lwjgl-3.0.0.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo

### check to make sure it all works from clojars (in the hello_lwjgl dir)

    > rm -rf ~/.m2/repository/hello_lwjgl/lwjgl
    > lein deps
    Retrieving hello_lwjgl/lwjgl/3.0.0/lwjgl-3.0.0.pom from clojars
    Retrieving hello_lwjgl/lwjgl/3.0.0/lwjgl-3.0.0.jar from clojars
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
A fullscreen spinning triangle that uses OpenGL 1.1 and you can move
with a keyboard.  The triangle also rotates to follow the mouse.

To run these examples, just add the name to the lein run commandline.
E.g. to run the 'alpha' test:

```bash
> lein run alpha
```

### REPL Usage

Because of interations between the GLFW and AWT window system and Mac OS X, using the REPL on Mac OS X is a bit different than on PC/Linux.  On PC/Linux, I think you can just use `lein repl` as you would normally.  But, on Mac, we need to start the nREPL server ourselves in a separate thread.  See Issue #6 for details and thanks to @antoinevg for his efforts in figuring this out.

For Mac OS X, we have added the Emacs cider-nrepl package in order to get a REPL working.  I'm not sure about other external tools, so for now cider-nrepl is the way to get a REPL on the Mac.  To start this, add `cider` after the name of the example.  E.g. `lein alpha cider` will create a cider-nrepl server and report this on startup.  Something like this:

```
> lein run alpha cider
Hello, Lightweight Java Game Library! V 3.0.0b SNAPSHOT
Run example Alpha
Starting Cider Nrepl Server Port 7888
OpenGL version: 2.1 NVIDIA-10.4.2 310.41.35f01
```

In emacs, use `M-x cider-connect` and use port 7888 to connect.  A repl pane shoudl open up.  Now, you can adjust the code live, for example, adjust the angle of the triangle.

```
user> (in-ns 'hello-lwjgl.alpha)
#namespace[hello-lwjgl.alpha]
hello-lwjgl.alpha> (swap! globals assoc :angle 0.0)
```

Please note that you may need to adjust the cider-nrepl package version to match your local install.  It changes often.

### Running from the commandline

First create the 'uberjar'

```bash
> lein uberjar
...
Created .../hello_lwjgl/target/hello_lwjgl-0.2.0-SNAPSHOT-standalone.jar
```

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
