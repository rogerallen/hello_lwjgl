(ns hello-lwjgl.delta
  (:require [clojure.pprint :as pprint])
  (:import (java.nio ByteBuffer FloatBuffer)
           (java.lang.reflect Field)
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl ContextAttribs Display DisplayMode GL11 GL15 GL20 PixelFormat)
           (org.lwjgl.util.glu GLU)))

;; ======================================================================
;; some code cribbed from
;; https://github.com/ztellman/penumbra/blob/master/src/penumbra/opengl/core.clj
;; Causes errors in glGetError to throw an exception
(def containers [GL11 GL15 GL20])
(defn- get-fields [#^Class static-class]
  (. static-class getFields))
(defn- enum-name
  "Takes the numeric value of a gl constant (i.e. GL_LINEAR), and gives the name"
  [enum-value]
  (if (= 0 enum-value)
    "NONE"
    (.getName
     #^Field (some
              #(if (= enum-value (.get #^Field % nil)) % nil)
              (mapcat get-fields containers)))))
(defn- except-gl-errors
  [msg]
  (let [error (GL11/glGetError)
        error-string (str "OpenGL Error(" error "):" (enum-name error) ": " msg)]
    (if (not (zero? error))
      (throw (Exception. error-string)))))

;; ======================================================================
;; spinning triangle in OpenGL 2.1
;; >250 lines vs. <100 lines...progress?
(defn init-window
  [width height title]
  (let [pixel-format (PixelFormat.)
        context-attributes (-> (ContextAttribs. 2 1))
        current-time-millis (System/currentTimeMillis)]
    (def globals (ref {:width width
                       :height height
                       :title title
                       :angle 0.0
                       :last-time current-time-millis
                       ;; geom ids
                       :vbo-id 0
                       :vertices-count 0
                       :cbo-id 0
                       ;; shader program ids
                       :vs-id 0
                       :fs-id 0
                       :p-id 0
                       :angle-loc 0}))
    (Display/setDisplayMode (DisplayMode. width height))
    (Display/setTitle title)
    (Display/setVSyncEnabled true)
    (Display/setLocation 0 0)
    (Display/create pixel-format context-attributes)))

(defn init-buffers
  []
  ;; FIXME – DRY!
  (let [vertices (float-array
                  [ 0.0  0.0  -0.5
                    0.5 -0.5  -0.5
                   -0.5 -0.5  -0.5

                    0.0  0.0  -0.5
                   -0.5  0.5  -0.5
                    0.5  0.5  -0.5
                   ])
        vertices-count (count vertices)
        vertices-buffer (-> (BufferUtils/createFloatBuffer vertices-count)
                            (.put vertices)
                            (.flip))
        ;;colors (float-array
        ;;          [1.0  1.0  1.0
        ;;           1.0  1.0  0.0
        ;;           1.0  1.0  0.0
        ;;
        ;;           1.0  1.0  1.0
        ;;           1.0  1.0  0.0
        ;;           1.0  1.0  0.0])
        ;;colors-count (count vertices) ;; better be the same as vertices-count
        ;;colors-buffer (-> (BufferUtils/createFloatBuffer colors-count)
        ;;                    (.put colors)
        ;;                    (.flip))
        ;; create & bind Vertex Buffer Object for vertices
        vbo-id (GL15/glGenBuffers)
        _ (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo-id)
        _ (GL15/glBufferData GL15/GL_ARRAY_BUFFER vertices-buffer GL15/GL_STATIC_DRAW)
        _ (except-gl-errors "glBufferData 1")
        ;;cbo-id (GL15/glGenBuffers)
        ;;_ (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER cbo-id)
        ;;_ (GL15/glBufferData GL15/GL_ARRAY_BUFFER colors-buffer GL15/GL_STATIC_DRAW)
        ;;_ (except-gl-errors "glBufferData 2")
        ]
    (dosync (ref-set globals
                     (assoc @globals
                       :vbo-id vbo-id
                       :vertices-count vertices-count
                       ;;:cbo-id cbo-id
                       )))))

(def vs-shader
  (str "#version 120\n"
       "\n"
       "uniform float in_Angle;\n"
       "\n"
       "void main(void) {\n"
       "    float angle = in_Angle*(3.1415926535/180);\n"
       "    mat4x4 mvp = mat4x4(0.0);\n"
       "    mvp[0] = vec4( cos(angle), sin(angle), 0.0, 0.0);\n"
       "    mvp[1] = vec4(-sin(angle), cos(angle), 0.0, 0.0);\n"
       "    mvp[2] = vec4(0.0, 0.0, 1.0, 0.0);\n"
       "    mvp[3] = vec4(0.0, 0.0, 0.0, 1.0);\n"
       "    gl_Position = mvp*gl_Vertex;\n"
       ;;"    gl_FrontColor = gl_Color;\n"
       "}\n"
       ))

(def fs-shader
  (str "#version 120\n"
       "\n"
       "void main(void) {\n"
       "    gl_FragColor = vec4(1.0, 1.0, 0.5, 1.0);\n"
       ;;"    gl_FragColor = gl_Color;\n"
       "}\n"
       ))

(defn load-shader
  [shader-str shader-type]
  (let [shader-id (GL20/glCreateShader shader-type)
        _ (except-gl-errors "glCreateShader")
        _ (GL20/glShaderSource shader-id shader-str)
        _ (except-gl-errors "glShaderSource")
        _ (GL20/glCompileShader shader-id)
        _ (except-gl-errors "glCompileShader")
        gl-compile-status (GL20/glGetShaderi shader-id GL20/GL_COMPILE_STATUS)
        ]
    (when (== gl-compile-status GL11/GL_FALSE)
      (println "ERROR: Loading a Shader:")
      (println (GL20/glGetShaderInfoLog shader-id 10000)))
    shader-id))

(defn init-shaders
  []
  (let [vs-id (load-shader vs-shader GL20/GL_VERTEX_SHADER)
        fs-id (load-shader fs-shader GL20/GL_FRAGMENT_SHADER)
        p-id (GL20/glCreateProgram)
        _ (except-gl-errors "glCreateProgram")
        _ (GL20/glAttachShader p-id vs-id)
        _ (except-gl-errors "glAttachShader")
        _ (GL20/glAttachShader p-id fs-id)
        _ (except-gl-errors "glAttachShader2")
        ;;_ (GL20/glBindAttribLocation p-id 0 "in_Position")
        ;;_ (except-gl-errors "glBindAttribLocation pos")
        ;;_ (GL20/glBindAttribLocation p-id 1 "in_Color")
        ;;_ (except-gl-errors "glBindAttribLocation col")
        _ (GL20/glLinkProgram p-id)
        _ (except-gl-errors "glLinkProgram")
        gl-link-status (GL20/glGetProgrami p-id GL20/GL_LINK_STATUS)
        _ (when (== gl-link-status GL11/GL_FALSE)
            (println "ERROR: Linking Shaders:")
            (println (GL20/glGetProgramInfoLog p-id 10000)))
        angle-loc (GL20/glGetUniformLocation p-id "in_Angle")
        _ (except-gl-errors "glGetUniformLocation")
        ]
    (dosync (ref-set globals
                     (assoc @globals
                       :vs-id vs-id
                       :fs-id fs-id
                       :p-id p-id
                       :angle-loc angle-loc)))))

(defn init-gl
  []
  (let [{:keys [width height]} @globals]
    (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
    (GL11/glClearColor 0.5 0.7 1.0 0.0)
    (GL11/glViewport 0 0 width height)
    (GL11/glDisable GL11/GL_CULL_FACE)
    (init-buffers)
    (init-shaders)))

(defn draw
  []
  (let [{:keys [width height angle angle-loc
                p-id vbo-id vertices-count cbo-id]} @globals
                w2 (/ width 2.0)
                h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    (except-gl-errors "glClear")

    (GL20/glUseProgram p-id)
    (except-gl-errors "glUseProgram")
    (GL20/glUniform1f angle-loc angle)
    (except-gl-errors "glUniform1f")

    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo-id)
    (except-gl-errors "glBindBuffer v")
    (GL11/glVertexPointer 3 GL11/GL_FLOAT 0 0)
    (except-gl-errors "glVertexPointer")

    ;;(GL15/glBindBuffer GL15/GL_ARRAY_BUFFER cbo-id)
    ;;(except-gl-errors "glBindBuffer c")
    ;;(GL11/glColorPointer 3 GL11/GL_FLOAT 0 0)
    ;;(except-gl-errors "glColorPointer")

    (GL11/glEnableClientState GL11/GL_VERTEX_ARRAY)
    (except-gl-errors "glEnableClientState v")
    ;;(GL11/glEnableClientState GL11/GL_COLOR_ARRAY)
    ;;(except-gl-errors "glEnableClientState c")

    (GL11/glDrawArrays GL11/GL_TRIANGLES 0 vertices-count)
    (except-gl-errors "glDrawArrays")

    ;; Put everything back to default (deselect)
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)
    (except-gl-errors "glBindBuffer 0")
    (GL11/glDisableClientState GL11/GL_VERTEX_ARRAY)
    (except-gl-errors "glDisableClientState v")
    ;;(GL11/glDisableClientState GL11/GL_COLOR_ARRAY)
    ;;(except-gl-errors "glDisableClientState c")
    (GL20/glUseProgram 0)
    (except-gl-errors "glUseProgram 0")
    ))

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

(defn destroy-gl
  []
  (let [{:keys [p-id vs-id fs-id vbo-id]} @globals]
    ;; Delete the shaders
    (GL20/glUseProgram 0)
    (GL20/glDetachShader p-id vs-id)
    (GL20/glDetachShader p-id fs-id)

    (GL20/glDeleteShader vs-id)
    (GL20/glDeleteShader fs-id)
    (GL20/glDeleteProgram p-id)

    ;; Select the VAO
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)
    (GL15/glDeleteBuffers vbo-id)
    ))

(defn run
  []
  (init-window 800 800 "delta")
  (init-gl)
  (while (not (Display/isCloseRequested))
    (update)
    (Display/update)
    (Display/sync 60))
  (destroy-gl)
  (Display/destroy))

(defn main []
  (println "Run example Delta")
  (.start (Thread. run)))
