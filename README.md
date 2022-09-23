# hello_lwjgl

## How to get LWJGL3 working with Leiningen & Clojure.
A series of examples of LWJGL in Clojure.
Supports live code reloading in the REPL.
Works with Mac ARM, Linux, (Windows?, Mac x86?)



## Standard Usage
To run these examples, add the name of the example to the 'lein run' command in your console, (as the first arg)

- **alpha** - a spinning triangle that uses OpenGL 1.1
  * `> lein run alpha`
- **beta** - a spinning triangle that uses OpenGL 3.2
  * `> lein run beta`
- **gamma** - A fullscreen spinning triangle that uses OpenGL 1.1, that can be moved with a keyboard. The triangle also rotates to follow the mouse.
  * `> lein run gamma`


## REPL Usage
This is where the real power lies. You have the ability to live code reload using the REPL, and thus dynamically change the render, while your app is running. It's pretty neat :).

If you want to take advantage of live reloading, your LWJGL app must run in a thread separate from the thread your REPL is running in, because the main loop blocks.

Use an atom to communicate between threads by using reset! on the draw function (or associated logic) within the REPL thread, and dereference the draw function in your main opengl loop.[^1]
See `alpha-live-reload/main` for an example.

[^1]: https://stackoverflow.com/questions/38961679/how-to-inject-code-from-one-thread-to-another-in-clojure-for-live-opengl-editin


### OSX Specifics
For OSX: LWJGL must be called from the main thread, so start this project with
```bash
> lein run alpha-live-reload cider           
Hello, Lightweight Java Game Library! V 3.3.1 build 7
Running on macosx
-- Main Thread: lwjgl
-- Child Thread: nrepl
Run example Alpha with live reloading
Starting Cider Nrepl Server Port 7888
OpenGL version: 2.1 Metal - 76.3
``` 
and connect to the nREPL in cider with `M-x cider-connect` on port 7888.

This optional 2nd argument calls LWJGL in the main thread, and initiates an nREPL server in a child thread, which you can then connect to from CIDER. [^2]
This is needed due to interactions between the GLFW and AWT window system and Mac OS X. [^3] [^4]


Note: on other platforms (linux) you can run the nREPL in the main thread with `M-x cider-jack-in`, and call GLFW functions in a child thread (or in the same thread as the nREPL, but doing so blocks, and you lose live reload functionality).

[^2]: Please note that you may need to adjust the cider-nrepl package version to match your local install.  It changes often.
[^3]: See Issue #6 for details and thanks to @antoinevg for his efforts in figuring this out.
[^4]: Also, it is no longer needed to add the Emacs cider-nrepl package into your ~/.lein/profiles.clj file. 





## Application Setup

If you want to create your own application using Clojure & LWJGL, create an app with lein the standard way

```bash
> lein new app [your-name-here]
> cd [your-name-here]
```
And copy over any relevant logic from this library. See project.clj and the source code for more info,
it is imperative to do so for a complete understanding the behavior of this program.




### Running from the commandline

First create the 'uberjar'

```bash
> lein uberjar
...
Created .../hello_lwjgl/target/hello_lwjgl-0.4.0-standalone.jar
```

Then you can run it with a commandline.

```bash
> java -jar target/hello_lwjgl-0.4.0-SNAPSHOT-standalone.jar
```

## Notes


*Now with automatic downloads!*

In [this pull request](https://github.com/rogerallen/hello_lwjgl/pull/8), [euccastro](https://github.com/euccastro) provides an elegant way to download the native libraries easily.
While I have some concern that this might break 32-bit compatibility, it would seem that there is less interest in maintaining that pathway.
So, I can delete the old, complicated instructions that were here and now you can just look at the top of project.clj and configure LWJGL3 to download & use the libraries & version you want in a very simple manner.

--------------------------------------------------


* found this helpful example: https://github.com/honeytree/clojure-lwjgl
* found this discussion: https://groups.google.com/forum/#!msg/leiningen/MAFbNqDYT78/Ub2scaa4RCoJ
* See this issue for some background. https://github.com/technomancy/leiningen/issues/898
* If you get an error similar to this, update to Java 8: java.lang.UnsupportedClassVersionError: org/lwjgl/opengl/GL : Unsupported major.minor version 52.0


## License

Copyright © 2013-2022 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.


