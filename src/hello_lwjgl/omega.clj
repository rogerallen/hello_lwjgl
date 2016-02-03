(ns hello-lwjgl.omega
  (:import
   (java.nio FloatBuffer IntBuffer)
   (java.util List)
   (org.lwjgl BufferUtils PointerBuffer)
   (org.lwjgl.opencl CL CL10 CLUtil CLContextCallback CLDevice CLPlatform CLProgramCallback)
   (org.lwjgl.system Configuration MemoryUtil)
   ))

;; Tried to follow this code, but it is apparently LWJGL2
;;   http://wiki.lwjgl.org/wiki/Sum_Example
;; Then found this code
;;   http://yarenty.blogspot.com/2015/03/lwjgl3-use-gpu-opencl-for-multithreaded.html
;; using that...

(defn to-float-buffer
  [floats]
  (let [ary (float-array floats)]
    (-> (BufferUtils/createFloatBuffer (alength ary))
        (.put ary)
        (.rewind))))

(defn print-buffer
  [buffer]
  (doseq [i (range (.capacity buffer))]
    (print (.get buffer) " "))
  (println))

(defn main
  []
  (println "Run example Omega")
  (println "  (a simple OpenCL test)")

  (let [source (str "kernel void sum(global const float *a, "
                    "                global const float *b, "
                    "                global float *answer) "
                    "{"
                    "  unsigned int xid = get_global_id(0); "
                    "  answer[xid] = a[xid] + b[xid]; "
                    "}")
        context-callback (proxy [CLContextCallback] []
                                  (invoke [errinfo private_info cb user_data]
                                    (println "[LWJGL] cl_context_callback")
                                    (println "\tInfo:"  (MemoryUtil/memDecodeUTF8 errinfo))))
        program-callback (proxy [CLProgramCallback] []
                                  (invoke [program user_data]
                                    (println "[LWJGL] cl_program_callback") ;; FIXME?
                                    ;;(println "\tInfo:"  (MemoryUtil/memDecodeUTF8 errinfo))
                                    ))
        ;; data & memory buffers
        a      (to-float-buffer [1 2 3 4 5 6 7 8 9 10])
        b      (to-float-buffer [9 8 7 6 5 4 3 2 1 0])
        answer (BufferUtils/createFloatBuffer 10)

        ;; Initialize OpenCL and create a context and command queue
        _ (println "init")
        ;_ (System/setProperty "org.lwjgl.opencl.explicitInit" "true")
        _ (.set Configuration/EXPLICIT_INIT_OPENCL true)
        _ (CL/create)
        _ (println "CL created")
        platform (-> (CLPlatform/getPlatforms)
                     (.get 0))
        _ (println "platform created")
        ctx-props (-> (BufferUtils/createPointerBuffer 3)
                      (.put CL10/CL_CONTEXT_PLATFORM)
                      (.put platform)
                      (.put 0)
                      (.flip))
        _ (println "ctx-props created")
        errcode-ret (BufferUtils/createIntBuffer 1)
        _ (println "errcode created")
        devices  (.getDevices platform CL10/CL_DEVICE_TYPE_GPU)
        _ (println "get devices" devices)
        _ (println "device 0" (.get devices 0))
        context  (CL10/clCreateContext ctx-props (.address (.get devices 0)) context-callback (long 0) errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        _ (println "context created" context)
        queue    (CL10/clCreateCommandQueue context (.address (.get devices 0)) (long CL10/CL_QUEUE_PROFILING_ENABLE) errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        _ (println "queue created" queue)

        ;; Allocate memory for our two input buffers and our result buffer
        a-mem (CL10/clCreateBuffer context (long (bit-or CL10/CL_MEM_READ_ONLY CL10/CL_MEM_COPY_HOST_PTR)) a errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        tmp-ptr-buf1 (BufferUtils/createPointerBuffer 1)
        tmp-ptr-buf2 (BufferUtils/createPointerBuffer 1)
        err   (CL10/clEnqueueWriteBuffer queue a-mem 1 (long 0) a nil tmp-ptr-buf1)
        b-mem (CL10/clCreateBuffer context (long (bit-or CL10/CL_MEM_READ_ONLY CL10/CL_MEM_COPY_HOST_PTR)) b errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        err   (CL10/clEnqueueWriteBuffer queue b-mem 1 (long 0) b nil tmp-ptr-buf2)
        answer-mem (CL10/clCreateBuffer context (long (bit-or CL10/CL_MEM_WRITE_ONLY CL10/CL_MEM_COPY_HOST_PTR)) answer errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        _     (CL10/clFinish queue);
        _ (println "allocated memory")

        ;; Create our program and kernel
        program (CL10/clCreateProgramWithSource context source errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        _ (CLUtil/checkCLError (CL10/clBuildProgram program (.address (.get devices 0)) "" program-callback (long 0)))
        ;; sum has to match a kernel method name in the OpenCL source
        kernel (CL10/clCreateKernel program "sum" errcode-ret)
        _ (CLUtil/checkCLError errcode-ret)
        _ (println "kernel created" (.get errcode-ret 0))

        work-size (-> (BufferUtils/createPointerBuffer 1)
                       (.put (long (.capacity a)))
                       (.flip))
        _ (println "kernel-work-size created")
        ]

    ;; Execute our kernel
    ;; FIXME -- current error: java.lang.Long cannot be cast to org.lwjgl.PointerBuffer
    ;; I don't get it...
    (CL10/clSetKernelArg1p kernel 0 a-mem)
    (CL10/clSetKernelArg1p kernel 1 b-mem)
    (CL10/clSetKernelArg1p kernel 2 answer-mem)

    (CL10/clEnqueueNDRangeKernel queue kernel 1 0 work-size 0 0 0)

    ;; Read the results memory back into our result buffer
    (CL10/clEnqueueReadBuffer queue answer-mem 1 0 answer 0 0)
    (CL10/clFinish queue)

    ;; Print the result memory
    (print-buffer a);
    (println "+")
    (print-buffer b);
    (println "=")
    (print-buffer answer);

    ;; Clean up OpenCL resources
    (CL10/clReleaseKernel kernel)
    (CL10/clReleaseProgram program)
    (CL10/clReleaseMemObject a-mem)
    (CL10/clReleaseMemObject b-mem)
    (CL10/clReleaseMemObject answer-mem)
    (CL10/clReleaseCommandQueue queue)
    (CL10/clReleaseContext context)
    (CL/destroy)

    ))
