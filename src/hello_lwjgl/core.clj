(ns hello-lwjgl.core
  (:require [leiningen.core.utils]
            [hello-lwjgl.alpha :as alpha]
            [hello-lwjgl.alpha-live-reload :as alpha-live-reload]
            [hello-lwjgl.beta  :as beta]
            [hello-lwjgl.gamma :as gamma]
            ;;[hello-lwjgl.omega :as omega]
            )
  (:import [org.lwjgl Version])
  (:gen-class))



;; ==================================================
;; OSX glfw + repl compatability hack

(defn start-cider-nrepl-in-new-thread
  "Required on OSX -- glfw MUST be run from main thread, so this fn creates a
  repl on a child thread for live code reloading.
  Connect with cider by M-x cider-connect instead of M-x cider-jack-in"
  []
  (.start
   (Thread.
    (fn []
      (println "Starting Cider Nrepl Server Port 7888")
      (eval `(do
               (require 'nrepl.server)
               (require 'cider.nrepl)
               (nrepl.server/start-server
                :port 7888
                :handler cider.nrepl/cider-nrepl-handler)))))))




;; ==================================================
;; Dispatch multimethod for easy future extensibility

(defmulti main-dispatch identity)


(defmethod main-dispatch "alpha" [_]
  (alpha/main))

(defmethod main-dispatch "alpha-live-reload" [_]
  (alpha-live-reload/main))

(defmethod main-dispatch "beta"  [_]
  (beta/main))
(defmethod main-dispatch "gamma" [_]
  (gamma/main))

(defmethod main-dispatch "omega" [_]
  ((comment (omega/main))
   (println "omega is not yet implemented")))

(defmethod main-dispatch :default [_]
  (throw (IllegalArgumentException. "Unknown Parameter")))




;; ==================================================
;; Main entry for program


(defn -main
  [& args]
  (let [demo-name              (first args)
        nrepl-in-new-thread?   (= "cider" (second args))
        os                     (leiningen.core.utils/get-os)]
    
    (println "Hello, Lightweight Java Game Library! V" (Version/getVersion))
    (println "Running on" (name os))
    (case os
      
      ;; required for osx: -lwjgl from main thread
      ;;                   -repl from child thread
      :macosx
      (do (println    "-- Main Thread: lwjgl")
          (when nrepl-in-new-thread?
            (println  "-- Child Thread: nrepl")
            (start-cider-nrepl-in-new-thread))
          (main-dispatch demo-name))
      
      ;; else: start lwjgl in child thread
      (do (println "-- Main Thread: nrepl")
          (println "-- Child Thread: lwjgl")

          (.start (Thread. (fn [] (main-dispatch demo-name))))))))



(comment

  ;; --- Live Code Reloading --- 

  ;; You have the ability to live reload code using the repl,
  ;; and thus dynamically change the render, while your app is running.
  ;; It's pretty neat :).
  
  ;; If you want to take advantage of live reloading, your opengl app must
  ;; run in a thread separate from the thread your repl is running in.
  
  ;; Use an atom to communicate between threads by using reset! on the draw
  ;; function (or associated logic) within the repl thread, and dereference
  ;; the draw function in your main opengl loop.
  ;; See alpha-live-reload/main for an example.

  ;; remember to use
  ;; (.start (Thread. (fn []))) instead of  (future & body)
  ;; for example, call the following within cider
  ;; to be able to play with live reloading
  (.start (Thread. (fn [] (alpha-live-reload/main))))
  ;; and try redefining the hot-draw atom within that namespace in your repl

  ;; For OSX: lwjgl must be called from the main thread, so
  ;; start this project with 'lein run alpha-live-reload cider' and connect
  ;; to the repl in cider with M-x cider-connect.

  ;; Reference
  ;; https://stackoverflow.com/questions/38961679/how-to-inject-code-from-one-thread-to-another-in-clojure-for-live-opengl-editin
  
  )

