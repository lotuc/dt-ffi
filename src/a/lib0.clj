(ns a.lib0
  (:require
   [tech.v3.datatype.ffi :as dt-ffi]))

;;; These are all functions from stdlib

(def fn-defs
  {:memset {:rettype :pointer
            :argtypes [['buffer :pointer]
                       ['byte-value :int32]
                       ['n-bytes :size-t]]}
   :memcpy {:rettype :pointer
            ;;dst src size-t
            :argtypes [['dst :pointer]
                       ['src :pointer]
                       ['n-bytes :size-t]]}
   :qsort {:rettype :void
           :argtypes [['data :pointer]
                      ['nitems :size-t]
                      ['item-size :size-t]
                      ['comparator :pointer]]}})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(declare memset memcpy qsort)

(def lib (dt-ffi/library-singleton #'fn-defs))

(def class-name 'a.bindings.Lib0GraalBindings)

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
   ;; setup via library path
   ;; macOS: otool -L $(which passwd)
   (setup-lib-dev "/usr/lib/libSystem.B.dylib"))
  ([lib-path]
   (dt-ffi/library-singleton-set! lib lib-path)))
