(ns hello-lwjgl.core
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
          (org.lwjgl.util.glu GLU))
  (:gen-class))

;; ======================================================================
;; example alpha:
;; behold, a simple, old-fashioned spinning triangle
(defn alpha-init-window
  [width height title]
  (def alpha-globals (ref {:width width
                           :height height
                           :title title
                           :angle 0.0
                           :next-time (System/currentTimeMillis)
                           :last-time (System/currentTimeMillis)}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create))

(defn alpha-init-gl
  []
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GLU/gluOrtho2D 0.0 (:width @alpha-globals)
                  0.0 (:height @alpha-globals))
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn alpha-draw
  []
  (let [{:keys [width height angle]} @alpha-globals
        w2 (/ width 2.0)
        h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    
    (GL11/glLoadIdentity)
    (GL11/glTranslatef w2 h2 0)
    (GL11/glRotatef angle 0 0 1)
    (GL11/glScalef 2 2 1)
    (GL11/glBegin GL11/GL_TRIANGLES)
    (do
      (GL11/glColor3f 1.0 0.0 0.0)
      (GL11/glVertex2i 100 0)
      (GL11/glColor3f 0.0 1.0 0.0)
      (GL11/glVertex2i -50 86.6)
      (GL11/glColor3f 0.0 0.0 1.0)      
      (GL11/glVertex2i -50 -86.6)
      )
    (GL11/glEnd)))

(defn alpha-update
  []
  (let [{:keys [width height angle next-time last-time]} @alpha-globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        sleep-time (max 4 (/ (- next-time cur-time) 2))
        do-draw (> cur-time next-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)
        next-time (if do-draw
                    (+ 8 cur-time)
                    next-time)]

    (dosync (ref-set alpha-globals
                     (assoc @alpha-globals
                       :angle next-angle
                       :next-time next-time
                       :last-time cur-time)))
    ;; not exactly sure this is the best way to control refresh rate
    ;; but it seems to work for me
    (if do-draw
      (alpha-draw)
      (Thread/sleep sleep-time))))

(defn alpha-run
  []
  (alpha-init-window 800 600 "alpha")
  (alpha-init-gl)
  (while (not (Display/isCloseRequested))
    (alpha-update)
    (Display/update))
  (Display/destroy))

(defn alpha-main []
  (println "Run example Alpha")
  (.start (Thread. alpha-run)))

;; ======================================================================
(defn -main
  "main entry point."
  [& args]
  (println "Hello, Lightweight Java Game Library!")
  (cond
   (= "alpha" (first args)) (alpha-main)
   true (alpha-main)))
;;(-main ["alpha"])
;;@alpha-globals
