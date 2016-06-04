(ns hello-lwjgl.gamma
  (:import (org.lwjgl BufferUtils)
           (org.lwjgl.opengl GL GL11)
           (org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback)))

;; ======================================================================
;; gamma = alpha + input events
;; fullscreen spinning, moving triangle in OpenGL 1.1
;; red point of the triangle is supposed to point to the mouse

(defonce globals
  (atom {:errorCallback nil
         :keyCallback   nil
         :window        nil
         :width         0
         :height        0
         :title         "none"
         :tri-x         0
         :tri-y         0
         :angle         0.0
         :last-time     0
         :mouse-x-buf   (BufferUtils/createDoubleBuffer 1)
         :mouse-y-buf   (BufferUtils/createDoubleBuffer 1)
         }))

(defn init-fullscreen-window
  [title]

  (swap! globals assoc
         :errorCallback (GLFWErrorCallback/createPrint System/err))
  (GLFW/glfwSetErrorCallback (:errorCallback @globals))
  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  (let [monitor (GLFW/glfwGetPrimaryMonitor)
        vidmode (GLFW/glfwGetVideoMode monitor)
        width   (.width  vidmode)
        height  (.height vidmode)]

    (swap! globals assoc
           :width     width
           :height    height
           :title     title
           :tri-x     (/ width 2)
           :tri-y     (/ height 2)
           :last-time (System/currentTimeMillis))

    (GLFW/glfwDefaultWindowHints)
    (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
    (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
    (swap! globals assoc
           :window (GLFW/glfwCreateWindow width height title monitor 0))
    (when (= (:window @globals) nil)
      (throw (RuntimeException. "Failed to create the GLFW window")))

    (swap! globals assoc
           :keyCallback
           (proxy [GLFWKeyCallback] []
             (invoke [window key scancode action mods]
               (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                          (= action GLFW/GLFW_RELEASE))
                 (GLFW/glfwSetWindowShouldClose (:window @globals) true)))))
    (GLFW/glfwSetKeyCallback (:window @globals) (:keyCallback @globals))

    (GLFW/glfwMakeContextCurrent (:window @globals))
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow (:window @globals))))

(defn init-gl
  []
  (GL/createCapabilities)
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glOrtho 0.0 (:width @globals)
                (:height @globals) 0.0 ;; Y is 0 at the top to match mouse coords
                -1.0 1.0)
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

(defn key-pressed?
  [window key]
  (= (GLFW/glfwGetKey window key) GLFW/GLFW_PRESS))

(defn or1 [a b] (if (or a b) 1.0 0.0))

(defn keyboard
  []
  (let [{:keys [tri-x tri-y window]} @globals
        up (or1 (key-pressed? window GLFW/GLFW_KEY_UP)
                (key-pressed? window GLFW/GLFW_KEY_W))
        down (or1 (key-pressed? window GLFW/GLFW_KEY_DOWN)
                  (key-pressed? window GLFW/GLFW_KEY_S))
        left (or1 (key-pressed? window GLFW/GLFW_KEY_LEFT)
                  (key-pressed? window GLFW/GLFW_KEY_A))
        right (or1 (key-pressed? window GLFW/GLFW_KEY_RIGHT)
                   (key-pressed? window GLFW/GLFW_KEY_D))
        dx (+ (* left -1.0) (* right 1.0))
        ;; Y decreases as you go up.  Y=0 at top
        dy (+ (* up -1.0) (* down 1.0))
        tri-x (+ tri-x dx)
        tri-y (+ tri-y dy)]
    (swap! globals assoc :tri-x tri-x :tri-y tri-y)))

(defn mouse
  []
  (let [{:keys [window mouse-x-buf mouse-y-buf]} @globals]
    (GLFW/glfwGetCursorPos window mouse-x-buf mouse-y-buf)))

(defn update-globals
  []
  (let [{:keys [width height tri-x tri-y mouse-x-buf mouse-y-buf]} @globals
        cur-time (System/currentTimeMillis)
        mouse-x (.get mouse-x-buf 0)
        mouse-y (.get mouse-y-buf 0)
        dx (- mouse-x tri-x)
        dy (- mouse-y tri-y)
        next-angle (+ 90.0 (* (/ -180.0 Math/PI) (Math/atan2 dx dy)))]
    (swap! globals assoc :angle next-angle :last-time cur-time)))

(defn main-loop
  []
  (while (not (GLFW/glfwWindowShouldClose (:window @globals)))
    (keyboard)
    (mouse)
    (update-globals)
    (draw)
    (GLFW/glfwSwapBuffers (:window @globals))
    (GLFW/glfwPollEvents)))

(defn main
  []
  (println "Run example Gamma")
  (println "  Hit Esc to quit")
  (println "  move triangle with cursor keys or mouse")
  (Thread/sleep 1000)
  (try
    (init-fullscreen-window "gamma")
    (init-gl)
    (main-loop)
    (.free (:keyCallback @globals))
    (.free (:errorCallback @globals))
    (GLFW/glfwDestroyWindow (:window @globals))
    (finally
      (GLFW/glfwTerminate))))
