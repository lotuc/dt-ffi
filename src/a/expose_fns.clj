(ns a.expose-fns
  (:require
   [tech.v3.datatype.ffi :as dt-ffi])
  (:import
   [org.graalvm.nativeimage.c.function CEntryPointLiteral]))

(defn add [a b] (+ a b))

(defn cmp [a b] (compare a b))

(def class-name 'a.bindings.GraalExposeBindings)

(extend-protocol dt-ffi/PToPointer
  CEntryPointLiteral
  (convertible-to-pointer? [_item] true)
  (->pointer [item] (tech.v3.datatype.ffi.Pointer. (.rawValue (.getFunctionPointer item)))))
