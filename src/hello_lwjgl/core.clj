(ns hello-lwjgl.core
  (:require [hello-lwjgl.alpha :as alpha]
            [hello-lwjgl.beta  :as beta]
            [hello-lwjgl.gamma :as gamma]
            ;;[hello-lwjgl.omega :as omega]
            )
  (:import (org.lwjgl Version))
  (:gen-class))

;; ======================================================================
(defn -main
  [& args]
  (println "Hello, Lightweight Java Game Library! V" (Version/getVersion))
  (cond
   (= "alpha" (first args)) (alpha/main)
   (= "beta"  (first args)) (beta/main)
   (= "gamma" (first args)) (gamma/main)
   ;;(= "omega" (first args)) (omega/main)
   :else (alpha/main)))
