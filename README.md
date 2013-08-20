# hello_lwjgl

Just a basic attempt to get LWJGL working with Leiningen & Clojure.

UPDATE: I have found that this project setup does not work well when you intend the project to be a library.  It forces the dependent project to add lwjgl dependencies in a similar manner.

It also can have troubles on Linux 64-bit, forcing you to rename the 64-bit linux .so file.

For my [shadertone](http://github.com/overtone/shadertone) project, I just finally created a clojars native library with the expected native directory hierarchy for my own purposes and that works fine and wasn't that much work.  I wish the lwjgl folks would just do this, too.

All that said, this setup should work okay for getting your project to basically work.  Just know that, at some point, you may need to attend to some of these details.

## Setup

If you want to create something similar with Clojure & LWJGL, here's what I did

```bash
> lein new app hello_lwjgl
> cd hello_lwjgl
```

See project.clj for my additions.  Requires Leiningen 2.1.0 or later.
See this issue for the background.  https://github.com/technomancy/leiningen/issues/898
I've verified this works for me on Mac, Linux and Windows.

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

## To Do

* Figure out `lein uberjar` and how to create a native mac/win/linux app.

## Notes

* found this helpful example: https://github.com/honeytree/clojure-lwjgl
* found this discussion: https://groups.google.com/forum/#!msg/leiningen/MAFbNqDYT78/Ub2scaa4RCoJ

## License

Copyright © 2013 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.
