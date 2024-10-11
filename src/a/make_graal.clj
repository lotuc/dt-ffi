(ns a.make-graal
  (:require
   [a.expose-fns :as expose-fns]
   [a.lib0 :as lib0]
   [a.lib1 :as lib1]
   [tech.v3.datatype.ffi.graalvm :as graalvm]))

(def ^:dynamic *-compile-path-* "./target/generated-classes")

(defmacro with-compile-path [& body]
  `(with-bindings {#'*compile-path* *-compile-path-*}
     ~@body))

(defn make-lib0-bindings []
  (with-compile-path
    (println :lib0 *compile-path*)
    (graalvm/define-library
      lib0/fn-defs
      nil
      {:classname lib0/class-name
       :headers ["<string.h>"]
       :instantiate? true})))

(defn make-lib1-bindings []
  (with-compile-path
    (println :lib1 *compile-path*)
    (graalvm/define-library
      lib1/fn-defs
      nil
      {:classname lib1/class-name
       :headers ["<string.h>"]
       :instantiate? true})))

(defn make-expose-bindings []
  (with-compile-path
    (graalvm/expose-clojure-functions
     {#'expose-fns/add {:rettype :int64
                        :argtypes [['a :int64]
                                   ['b :int64]]}
      #'expose-fns/cmp {:rettype :int32
                        :argtypes [['a :float64]
                                   ['b :float64]]}}
     expose-fns/class-name
     nil)))

(defn make-bindings [{:keys [compile-path]}]
  (binding [*-compile-path-* (or (some-> compile-path str) *-compile-path-*)]
    (println (str "make bindings with classpath: " *-compile-path-*))
    (make-lib0-bindings)
    (make-lib1-bindings)
    (make-expose-bindings)))
