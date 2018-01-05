(require 'leiningen.core.eval)

;; per-os jvm-opts code cribbed from Overtone
(def JVM-OPTS
  {:common   []
   :macosx   ["-XstartOnFirstThread" "-Djava.awt.headless=true"]
   :linux    []
   :windows  []})

(defn jvm-opts
  "Return a complete vector of jvm-opts for the current os."
  [] (let [os (leiningen.core.eval/get-os)]
       (vec (set (concat (get JVM-OPTS :common)
                         (get JVM-OPTS os))))))

(def LWJGL_NS "org.lwjgl")

;; Edit this to change the version.
(def LWJGL_VERSION "3.1.1")

;; Edit this to add/remove packages.
(def LWJGL_MODULES ["lwjgl"
                    "lwjgl-assimp"
                    "lwjgl-bgfx"
                    "lwjgl-egl"
                    "lwjgl-glfw"
                    "lwjgl-jawt"
                    "lwjgl-jemalloc"
                    "lwjgl-lmdb"
                    "lwjgl-nanovg"
                    "lwjgl-nfd"
                    "lwjgl-nuklear"
                    "lwjgl-openal"
                    "lwjgl-opencl"
                    "lwjgl-opengl"
                    "lwjgl-opengles"
                    ;; The LWJGL downloads page disables this by default, so we
                    ;; do the same here.
                    ;; "lwjgl-ovr"
                    "lwjgl-par"
                    "lwjgl-sse"
                    "lwjgl-stb"
                    "lwjgl-tinyfd"
                    "lwjgl-vulkan"
                    "lwjgl-xxhash"])

;; It's safe to just include all native dependencies, but you might save some
;; space if you know you don't need some platform.
(def LWJGL_PLATFORMS ["linux" "macos" "windows"])

;; These packages don't have any associated native ones.
(def no-natives? #{"lwjgl-egl" "lwjgl-jawt" "lwjgl-opencl" "lwjgl-vulkan"})

(defn lwjgl-deps-with-natives []
  (apply concat
         (for [m LWJGL_MODULES]
           (let [prefix [(symbol LWJGL_NS m) LWJGL_VERSION]]
             (into [prefix]
                   (if (no-natives? m)
                     []
                     (for [p LWJGL_PLATFORMS]
                       (into prefix [:classifier (str "natives-" p)
                                     :native-prefix ""]))))))))

(def all-dependencies
  (into
   ;; Add your non-LWJGL dependencies here
   '[[org.clojure/clojure "1.8.0"]
     [cider/cider-nrepl "0.11.0"]]
   (lwjgl-deps-with-natives)))

(defproject hello_lwjgl "0.3.1"
  :description "Simple LWJGL3 clojure test."
  :url "https://github.com/rogerallen/hello_lwjgl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies ~all-dependencies
  :min-lein-version "2.1.0"
  :jvm-opts ^:replace ~(jvm-opts)
  :main hello-lwjgl.core)
