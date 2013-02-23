(ns hello-lwjgl.core
  (:require [hello-lwjgl.alpha :as alpha]
            [hello-lwjgl.beta :as beta])
  (:gen-class))

;; ======================================================================
(defn -main
  "main entry point."
  [& args]
  (println "Hello, Lightweight Java Game Library!")
  (cond
   (= "alpha" (first args)) (alpha/main)
   (= "beta" (first args)) (beta/main)
   true (alpha/main))) ;; run alpha by default
