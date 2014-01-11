(ns hello-lwjgl.delta
  (:require [clojure.pprint :as pprint])
  (:import (java.nio ByteBuffer FloatBuffer)
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl ContextAttribs Display DisplayMode GL11 GL15 GL20 PixelFormat)
           (org.lwjgl.util.glu GLU)))

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
                       :indices-count 0
                       ;; shader program ids
                       :vs-id 0
                       :fs-id 0
                       :p-id 0
                       ::angle-loc 0}))
    (Display/setDisplayMode (DisplayMode. width height))
    (Display/setTitle title)
    (Display/setVSyncEnabled true)
    (Display/setLocation 0 0)
    (Display/create pixel-format context-attributes)))

(defn init-buffers
  []
  ;; FIXME – DRY!
  (let [vertices (float-array
                  [ 0.00  0.00 0.0 1.0
                   -0.50 -0.50 0.0 1.0
                    0.50 -0.50 0.0 1.0])
        vertices-count (count vertices)
        vertices-buffer (-> (BufferUtils/createFloatBuffer vertices-count)
                            (.put vertices)
                            (.flip))
        ;; create & bind Vertex Buffer Object for vertices
        vbo-id (GL15/glGenBuffers)
        _ (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo-id)
        _ (GL15/glBufferData GL15/GL_ARRAY_BUFFER vertices-buffer GL15/GL_STATIC_DRAW)
        ]
    (dosync (ref-set globals
                     (assoc @globals
                       :vbo-id vbo-id
                       :vertices-count vertices-count)))))

(def vs-shader
  (str "#version 120\n"
       "\n"
       "attribute vec4 in_Position;\n"
       "uniform float in_Angle;\n"
       "\n"
       "void main(void) {\n"
       "    float angle = in_Angle*(3.1415926535/180);\n"
       "    mat4x4 mvp = mat4x4(0.0);\n"
       "    mvp[0] = vec4( cos(angle), sin(angle), 0.0, 0.0);\n"
       "    mvp[1] = vec4(-sin(angle), cos(angle), 0.0, 0.0);\n"
       "    mvp[2] = vec4(0.0, 0.0, 1.0, 0.0);\n"
       "    mvp[3] = vec4(0.0, 0.0, 0.0, 1.0);\n"
       "    gl_Position = mvp*in_Position;\n"
       "}\n"
       ))

(def fs-shader
  (str "#version 120\n"
       "\n"
       "void main(void) {\n"
       "    gl_FragColor = vec4(1.0, 0.7, 0.5, 1.0);\n"
       "}\n"
       ))

(defn load-shader
  [shader-str shader-type]
  (let [shader-id (GL20/glCreateShader shader-type)
        _ (GL20/glShaderSource shader-id shader-str)
        ;;_ (println "init-shaders glShaderSource errors?" (GL11/glGetError))
        _ (GL20/glCompileShader shader-id)
        ;;_ (println "init-shaders glCompileShader errors?" (GL11/glGetError))
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
        _ (GL20/glAttachShader p-id vs-id)
        _ (GL20/glAttachShader p-id fs-id)
        _ (GL20/glLinkProgram p-id)
        gl-link-status (GL20/glGetProgrami p-id GL20/GL_LINK_STATUS)
        _ (when (== gl-link-status GL11/GL_FALSE)
            (println "ERROR: Linking Shaders:")
            (println (GL20/glGetProgramInfoLog p-id 10000)))
        angle-loc (GL20/glGetUniformLocation p-id "in_Angle")
        ;;_ (println "init-shaders errors?" (GL11/glGetError))
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
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glViewport 0 0 width height)
    (init-buffers)
    (init-shaders)
    ;;(print "@globals")
    ;;(pprint/pprint @globals)
    ;;(println "")
    ))

(defn draw
  []
  (let [{:keys [width height angle angle-loc
                p-id vbo-id vertices-count]} @globals
                w2 (/ width 2.0)
                h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))

    (GL20/glUseProgram p-id)
    ;; setup our uniform
    (GL20/glUniform1f angle-loc angle)

    (GL11/glEnableClientState GL11/GL_VERTEX_ARRAY)
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo-id)
    (GL11/glVertexPointer 3 GL11/GL_FLOAT 0 0)
    (GL11/glDrawArrays GL11/GL_TRIANGLES 0 vertices-count)

    ;; Put everything back to default (deselect)
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)
    (GL11/glDisableClientState GL11/GL_VERTEX_ARRAY)
    (GL20/glUseProgram 0)
    ;;(println "draw errors?" (GL11/glGetError))
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
