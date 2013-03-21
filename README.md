# hello_lwjgl

Just a basic attempt to get LWJGL working with Leiningen & Clojure.

## Setup

If you want to create something similar with Clojure & LWJGL, here's what I did

```bash
> lein new app hello_lwjgl
> cd hello_lwjgl
```

See project.clj for my additions.  Requires Leiningen 2.1.0 or later.
See this issue for the background.  https://github.com/technomancy/leiningen/issues/898

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

* Try out leiningen on windows to confirm it works.
* Figure out `lein uberjar` and how to create a native mac/win/linux app.

## Notes

* found this helpful example: https://github.com/honeytree/clojure-lwjgl
* found this discussion: https://groups.google.com/forum/#!msg/leiningen/MAFbNqDYT78/Ub2scaa4RCoJ

## License

Copyright © 2013 Roger Allen.

Distributed under the Eclipse Public License, the same as Clojure.
