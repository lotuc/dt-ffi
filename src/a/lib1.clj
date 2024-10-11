(ns a.lib1
  (:require
   [clojure.java.io :as io])
  (:require
   [tech.v3.datatype.ffi :as dt-ffi]))

;;; these are functions from our home made test library lib1 (checkout `src-c`)

(def fn-defs
  {:qsort1 {:rettype :void
            :argtypes [['data :pointer]
                       ['nitems :size-t]
                       ['item-size :size-t]
                       ['comparator :pointer]]}})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(declare qsort1)

(def lib (dt-ffi/library-singleton #'fn-defs))

(def class-name 'a.bindings.Lib1GraalBindings)

(dt-ffi/library-singleton-reset! lib)

(defmacro check-error
  [_fn-data & body]
  `(let [retval# (do ~@body)]
     retval#))

(dt-ffi/define-library-functions
  fn-defs
  #(dt-ffi/library-singleton-find-fn lib %)
  check-error)

(defn setup-lib-dev
  ([]
   (setup-lib-dev (str (.getAbsoluteFile (io/file "target/native/libLib1.dylib")))))
  ([libpath]
   (dt-ffi/library-singleton-set! lib libpath)))
