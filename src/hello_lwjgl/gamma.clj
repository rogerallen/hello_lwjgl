(ns hello-lwjgl.gamma
  (:import (org.lwjgl.input Keyboard Mouse)
           (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.util.glu GLU)))

;; ======================================================================
;; fullscreen spinning, moving triangle in OpenGL 1.1
(defn init-fullscreen-window
  []
  ;; find biggest, deepest screen
  (let [fullscreen-mode (last (sort-by
                               #(* (.getWidth %) (.getHeight %) (.getBitsPerPixel %)) 
                               (Display/getAvailableDisplayModes)))
        width (.getWidth fullscreen-mode)
        height (.getHeight fullscreen-mode)]

    (def globals (ref {:active true
                       :width width
                       :height height
                       :tri-x (/ width 2)
                       :tri-y (/ height 2)
                       :angle 0.0
                       :last-time (System/currentTimeMillis)}))
    (Display/setDisplayMode fullscreen-mode)
    (if (.isFullscreenCapable fullscreen-mode)
      (do
        (Display/setFullscreen true)
        (Display/setVSyncEnabled true))
      (println "Sorry, cannot go fullscreen"))
    (Display/create)))

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
  (let [{:keys [tri-x tri-y angle]} @globals]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    
    (GL11/glLoadIdentity)
    (GL11/glTranslatef tri-x tri-y 0)
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

(defn keyboard
  []
  (let [{:keys [tri-x tri-y]} @globals
        make-inactive (Keyboard/isKeyDown Keyboard/KEY_ESCAPE)
        up (or (Keyboard/isKeyDown Keyboard/KEY_UP)
               (Keyboard/isKeyDown Keyboard/KEY_W))
        down (or (Keyboard/isKeyDown Keyboard/KEY_DOWN)
                 (Keyboard/isKeyDown Keyboard/KEY_S))
        left (or (Keyboard/isKeyDown Keyboard/KEY_LEFT)
                 (Keyboard/isKeyDown Keyboard/KEY_A))
        right (or (Keyboard/isKeyDown Keyboard/KEY_RIGHT)
                  (Keyboard/isKeyDown Keyboard/KEY_D))
        dx (+ (if left -1 0.0) (if right 1 0.0))
        dy (+ (if up 1 0.0) (if down -1 0.0))
        tri-x (+ tri-x dx)
        tri-y (+ tri-y dy)
        ]
    (dosync (ref-set globals (assoc @globals :tri-x tri-x :tri-y tri-y)))
    (if make-inactive ;; note Cmd-Q on Mac also works
      (dosync (ref-set globals (assoc @globals :active false))))))

(defn update
  []
  (let [{:keys [width height tri-x tri-y]} @globals
        cur-time (System/currentTimeMillis)
        dx (- (Mouse/getX) tri-x)
        dy (- (Mouse/getY) tri-y)
        next-angle (+ 90.0 (* (/ -180.0 Math/PI) (Math/atan2 dx dy)))]
    (dosync (ref-set globals
                     (assoc @globals
                       :angle next-angle
                       :last-time cur-time)))
    (keyboard)
    (draw)))

(defn run
  []
  (init-fullscreen-window)
  (init-gl)
  (while (and (:active @globals)
              (not (Display/isCloseRequested)))
    (update)
    (Display/update)
    (Display/sync 60))
  (Display/destroy))

(defn main
  []
  (println "Run example Gamma")
  (println "  Cmd-Q or Esc to quit")
  (println "  move triangle with cursor keys")
  (Thread/sleep 1000)
  (.start (Thread. run)))
