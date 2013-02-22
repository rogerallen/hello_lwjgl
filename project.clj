(defproject hello_lwjgl "0.1.0-SNAPSHOT"
  :description "Simple LWJGL clojure test."
  :url "https://github.com/rogerallen/hello_lwjgl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.lwjgl.lwjgl/lwjgl "2.8.5"]
                 [org.lwjgl.lwjgl/lwjgl_util "2.8.5"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.8.5" :classifier "natives-osx"]]
  :main hello-lwjgl.core)
