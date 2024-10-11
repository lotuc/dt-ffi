 (ns a.demo
   (:require
    [a.expose-fns :as expose-fns]
    [a.lib0 :as lib0]
    [a.lib1 :as lib1]
    [tech.v3.datatype :as dtype]
    [tech.v3.datatype.ffi :as dt-ffi]
    [tech.v3.datatype.ffi.graalvm-runtime]
    [tech.v3.datatype.native-buffer :as native-buffer])
   (:import
    [a.bindings Lib0GraalBindings Lib1GraalBindings]
    [org.graalvm.nativeimage IsolateThread]
    [org.graalvm.nativeimage.c.function CEntryPointLiteral]
    [tech.v3.datatype.ffi Pointer]))

(def cmp-ptr
  (try (import '[a.bindings GraalExposeBindings])
       (let [r (->> (into-array Class [IsolateThread Double/TYPE Double/TYPE])
                    (CEntryPointLiteral/create (resolve expose-fns/class-name) "cmp"))]
         (println (str "cmp ptr found: " r))
         r)
       (catch Throwable _
         (println "error building cmp ptr: " _))))

(defn -lib0-test [lib0-inst]
  (when lib0-inst
    (dt-ffi/library-singleton-set-instance! lib0/lib lib0-inst))
  (let [nbuf (dtype/make-container :native-heap :float64 (shuffle (range 10)))]
    (println "init: " nbuf)
    (lib0/memset nbuf 0 80)
    (println "after memset" nbuf)
    0))

(defn -lib1-test [lib1-inst]
  (when lib1-inst
    (dt-ffi/library-singleton-set-instance! lib1/lib lib1-inst))
  (let [nbuf (dtype/make-container :native-heap :float64 (shuffle (range 10)))]
    (println "init: " nbuf)
    (lib1/qsort1 nbuf 10 8 cmp-ptr)
    (println "after qsort1: " nbuf)
    42))

(defn lib0-test-with-graall-binding []
  (-lib0-test (Lib0GraalBindings.)))

(defn lib0-test-with-libpath
  ([]
   (lib0/setup-lib-dev)
   (-lib0-test nil))
  ([libpath]
   (lib0/setup-lib-dev libpath)
   (-lib0-test nil)))

(defn lib1-test-with-graall-binding []
  (-lib1-test (Lib1GraalBindings.)))

(defn lib1-test-with-libpath
  ([]
   (lib1/setup-lib-dev nil)
   (-lib1-test nil))
  ([libpath]
   (lib1/setup-lib-dev libpath)
   (-lib1-test nil)))

(def comp-iface
  (dt-ffi/define-foreign-interface :int32 [:pointer :pointer]))

(defn lib0-ffi []
  (lib0/setup-lib-dev)
  (let [nbuf (dtype/make-container :native-heap :float64 (shuffle (range 10)))
        iface-inst (dt-ffi/instantiate-foreign-interface
                    comp-iface
                    (fn [^Pointer lhs ^Pointer rhs]
                      (let [lhs (.getDouble (native-buffer/unsafe) (.address lhs))
                            rhs (.getDouble (native-buffer/unsafe) (.address rhs))]
                        (Double/compare lhs rhs))))
        cmp-ptr (dt-ffi/foreign-interface-instance->c comp-iface iface-inst)]
    (println "init: " nbuf)
    (lib0/qsort nbuf 10 8 cmp-ptr)
    (println "after qsort: " nbuf)))

(comment
  (lib0-test-with-libpath)

  ;; f11 won't work (we cannot obtain the function pointer at Runtime, only during
  ;; native image generation).
  ;; https://www.graalvm.org/truffle/javadoc/org/graalvm/nativeimage/c/function/CEntryPointLiteral.html
  (lib1-test-with-libpath)

  (lib0-ffi))
