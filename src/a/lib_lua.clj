(ns a.lib-lua
  (:require
   [tech.v3.datatype.ffi :as dt-ffi]))

(declare
 lua_tolstring lua_close lua_settop lua_pcallk luaL_openlibs
 luaL_newstate luaL_loadbufferx)

(defmacro check-error
  [_fn-data & body]
  `(let [retval# (do ~@body)]
     retval#))

(def lua-fn-defs
  {:lua_pcallk {:rettype :int32
                :argtypes [['L :pointer]
                           ['nargs :int32]
                           ['nresults :int32]
                           ['errfunc :int32]
                           ['ctx :int32]
                           ['k :pointer?]]}
   :lua_tolstring {:rettype :pointer
                   :argtypes [['L :pointer]
                              ['idx :int32]
                              ['len :pointer?]]}
   :lua_close {:rettype :void
               :argtypes [['L :pointer]]}
   :lua_settop
   {:rettype :void
    :argtypes [['L :pointer]
               ['idx :int32]]}})

(def lualib-fn-defs
  {:luaL_openlibs {:rettype :void
                   :argtypes [['L :pointer]]}})

(def lauxlib-fn-defs
  {:luaL_newstate {:rettype :pointer}
   :luaL_loadbufferx {:rettype :int32
                      :argtypes [['L :pointer]
                                 ['buff :pointer]         ; const char*
                                 ['sz :size-t]
                                 ['name :pointer]         ; const char*
                                 ['mode :pointer?]        ; const char*
                                 ]}})

(def fn-defs (merge lua-fn-defs
                    lualib-fn-defs
                    lauxlib-fn-defs))

(def lib (dt-ffi/library-singleton #'fn-defs))
(dt-ffi/library-singleton-reset! lib)

(dt-ffi/define-library-functions
  fn-defs
  #(dt-ffi/library-singleton-find-fn lib %)
  check-error)

(defn luaL_loadbuffer [L buff sz name]
  (luaL_loadbufferx L buff sz name nil))

(defn lua_pop [L n]
  (lua_settop L (- (- n) 1)))

(defn lua_tostring [L i]
  (lua_tolstring L i nil))

(defn lua_pcall [L n r f]
  (lua_pcallk L n r f 0 nil))
