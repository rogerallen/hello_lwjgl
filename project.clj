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

(defproject hello_lwjgl "0.3.1"
  :description "Simple LWJGL3 clojure test."
  :url "https://github.com/rogerallen/hello_lwjgl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [hello_lwjgl/lwjgl   "3.0.0"]
                 [cider/cider-nrepl "0.11.0"]]
  :min-lein-version "2.1.0"
  :jvm-opts ^:replace ~(jvm-opts)
  :main hello-lwjgl.core)
