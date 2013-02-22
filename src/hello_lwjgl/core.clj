(ns hello-lwjgl.core
  (:import (org.lwjgl.opengl ContextAttribs Display DisplayMode GL11 PixelFormat)
          (org.lwjgl.util.glu GLU))
  (:gen-class))

;; ======================================================================
;; example alpha:
;; behold, a simple, old-fashioned (OpenGL 1.1) spinning triangle
(defn alpha-init-window
  [width height title]
  (def alpha-globals (ref {:width width
                           :height height
                           :title title
                           :angle 0.0
                           :last-time (System/currentTimeMillis)}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create))

(defn alpha-init-gl
  []
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
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
  (let [{:keys [width height angle last-time]} @alpha-globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (dosync (ref-set alpha-globals
                     (assoc @alpha-globals
                       :angle next-angle
                       :last-time cur-time)))
    (alpha-draw)))

(defn alpha-run
  []
  (alpha-init-window 800 600 "alpha")
  (alpha-init-gl)
  (while (not (Display/isCloseRequested))
    (alpha-update)
    (Display/sync 60)
    (Display/update))
  (Display/destroy))

(defn alpha-main []
  (println "Run example Alpha")
  (.start (Thread. alpha-run)))

;; ======================================================================
;; beta 
;;   same spinning triangle in OpenGL 3.2
(defn beta-init-window
  [width height title]
  (let [pixel-format (PixelFormat.)
        context-attributes (-> (ContextAttribs. 3 2)
                               (.withForwardCompatible true)
                               (.withProfileCore true))
        current-time-millis (System/currentTimeMillis)]
  (def beta-globals (ref {:width width
                          :height height
                          :title title
                          :angle 0.0
                          :last-time current-time-millis}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create pixel-format context-attributes)))

(defn beta-init-gl
  []
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION)))

(defn beta-draw
  []
  (let [{:keys [width height angle]} @beta-globals
        w2 (/ width 2.0)
        h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))

    (comment "FIXME!"
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
             (GL11/glEnd))
    ))

(defn beta-update
  []
  (let [{:keys [width height angle last-time]} @beta-globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (dosync (ref-set beta-globals
                     (assoc @beta-globals
                       :angle next-angle
                       :last-time cur-time)))
    (beta-draw)))

(defn beta-run
  []
  (beta-init-window 800 600 "beta")
  (beta-init-gl)
  (Thread/sleep 2000)
  (while (not (Display/isCloseRequested))
    (beta-update)
    (Display/sync 60)
    (Display/update))
  (Display/destroy))

(defn beta-main []
  (println "Run example Beta")
  (.start (Thread. beta-run)))

;; ======================================================================
(defn -main
  "main entry point."
  [& args]
  (println "Hello, Lightweight Java Game Library!")
  (cond
   (= "alpha" (first args)) (alpha-main)
   (= "beta" (first args)) (beta-main)
   true (alpha-main)))
;;(-main ["alpha"])
;;@alpha-globals
