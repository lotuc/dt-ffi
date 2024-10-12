(ns a.lib-lua
  {:clj-kondo/ignore [:unresolved-symbol]}
  (:refer-clojure :exclude [remove printf])
  (:require
   [clojure.string :as string]
   [com.phronemophobic.clong.clang :as clang]
   [com.phronemophobic.clong.gen.dtype-next :as gen.dtype-next]
   [tech.v3.datatype.ffi :as dt-ffi]))

(defonce lua-api (clang/easy-api "/usr/local/include/lua/lua.h"))
(defonce lauxlib-api (clang/easy-api "/usr/local/include/lua/lauxlib.h"))
(defonce lualib-api  (clang/easy-api "/usr/local/include/lua/lualib.h"))

(defmacro check-error
  [_fn-data & body]
  `(let [retval# (do ~@body)]
     retval#))

(defn- lua-api->fn-defs [clong-api]
  (let [f gen.dtype-next/coffi-type->dtype]
    (with-redefs [gen.dtype-next/coffi-type->dtype
                  (fn [t]
                    (if (and (vector? t)
                             (= [:coffi.mem/array :clong/__va_list_tag]
                                [(first t) (second t)]))
                      :pointer?
                      (f t)))]
      (->> (:functions clong-api)
           (filter #(string/starts-with? (:symbol %) "lua"))
           (map gen.dtype-next/clong-fn->dt-type-fn)
           (reduce merge)))))

(def lua-fn-defs (lua-api->fn-defs lua-api))
(def lualib-fn-defs (lua-api->fn-defs lualib-api))
(def lauxlib-fn-defs (lua-api->fn-defs lauxlib-api))
(def fn-defs (merge lua-fn-defs lualib-fn-defs lauxlib-fn-defs))

;;; (dt-ffi/define-library! lib fn-defs nil check-error)
(def lib-fns fn-defs)
(def lib-symbols nil)
(defonce lib
  (dt-ffi/library-singleton #'lib-fns #'lib-symbols nil))
(dt-ffi/define-library-functions
  lib-fns
  #(dt-ffi/library-singleton-find-fn lib %)
  check-error)
(dt-ffi/library-singleton-reset! lib)

(defn luaL_loadbuffer [L buff sz name]
  (luaL_loadbufferx L buff sz name nil))

(defn lua_pop [L n]
  (lua_settop L (- (- n) 1)))

(defn lua_tostring [L i]
  (lua_tolstring L i nil))

(defn lua_pcall [L n r f]
  (lua_pcallk L n r f 0 nil))
