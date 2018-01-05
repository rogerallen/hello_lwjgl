# hello_lwjgl

How to get LWJGL3 working with Leiningen & Clojure.

*Now with automatic downloads!*

In [this pull
request](https://github.com/rogerallen/hello_lwjgl/pull/8)
[euccastro](https://github.com/euccastro) provides an elegant way to
download the native libraries easily.  While I have some concern that
this might break 32-bit compatibility, it would seem that there is
less interest in maintaining that pathway.  So, I can delete the old,
complicated instructions that were here and now you can just look at
the top of project.clj and configure LWJGL3 to download & use the
libraries & version you want in a very simple manner.

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

For Mac OS X, you should add the Emacs cider-nrepl package into your ~/.lein/profiles.clj file in order to get a REPL working.  I'm not sure about other external tools, so for now cider-nrepl is the way to get a REPL on the Mac.  To start this, add `cider` after the name of the example.  E.g. `lein alpha cider` will create a cider-nrepl server and report this on startup.  Something like this:

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
Created .../hello_lwjgl/target/hello_lwjgl-0.4.0-standalone.jar
```

Then you can run it with a commandline.

```bash
> java -jar target/hello_lwjgl-0.2.0-SNAPSHOT-standalone.jar
```

## Notes

* found this helpful example: https://github.com/honeytree/clojure-lwjgl
* found this discussion: https://groups.google.com/forum/#!msg/leiningen/MAFbNqDYT78/Ub2scaa4RCoJ
* See this issue for some background. https://github.com/technomancy/leiningen/issues/898
* If you get an error similar to this, update to Java 8: java.lang.UnsupportedClassVersionError: org/lwjgl/opengl/GL : Unsupported major.minor version 52.0


## License

Copyright © 2013-2018 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.
