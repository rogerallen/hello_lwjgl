(ns hello-lwjgl.alpha
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.util.glu GLU)))

;; ======================================================================
;; spinning triangle in OpenGL 1.1
(defn init-window
  [width height title]
  (def globals (ref {:width width
                     :height height
                     :title title
                     :angle 0.0
                     :last-time (System/currentTimeMillis)}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create))

(defn init-gl
  []
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GLU/gluOrtho2D 0.0 (:width @globals)
                  0.0 (:height @globals))
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn draw
  []
  (let [{:keys [width height angle]} @globals
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

(defn update
  []
  (let [{:keys [width height angle last-time]} @globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (dosync (ref-set globals
                     (assoc @globals
                       :angle next-angle
                       :last-time cur-time)))
    (draw)))

(defn run
  []
  (init-window 800 600 "alpha")
  (init-gl)
  (while (not (Display/isCloseRequested))
    (update)
    (Display/update)
    (Display/sync 60))
  (Display/destroy))

(defn main
  []
  (println "Run example Alpha")
  (.start (Thread. run)))
