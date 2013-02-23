(ns hello-lwjgl.core
  (:require [hello-lwjgl.alpha :as alpha]
            [hello-lwjgl.beta :as beta])
  (:import (java.nio ByteBuffer FloatBuffer)
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl ContextAttribs Display DisplayMode GL11 GL15 GL20 GL30 PixelFormat)
           (org.lwjgl.util.glu GLU))
  (:gen-class))

;; ======================================================================
(defn -main
  "main entry point."
  [& args]
  (println "Hello, Lightweight Java Game Library!")
  (cond
   (= "alpha" (first args)) (alpha/main)
   (= "beta" (first args)) (beta/main)
   true (alpha/main)))
  ;;(-main ["alpha"])
  ;;@alpha-globals
