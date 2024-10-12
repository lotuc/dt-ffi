(ns a.demo-lua
  (:require
   [a.lib-lua :as lib-lua]
   [tech.v3.datatype.ffi :as dt-ffi]
   [tech.v3.datatype.native-buffer :as native-buffer]))

(defn call-s [L n s]
  (let [buff (dt-ffi/string->c s)
        blen (dec (native-buffer/native-buffer-byte-len buff))
        err? (and (zero? (lib-lua/luaL_loadbuffer L buff blen n))
                  (zero? (lib-lua/lua_pcall L 0 0 0)))]
    (when-not err?
      (-> (lib-lua/lua_tostring L -1)
          (dt-ffi/c->string)
          (println))
      (lib-lua/lua_pop L 1))))

#_{:clj-kondo/ignore [:unresolved-var]}
(comment
  (dt-ffi/library-singleton-set!
   lib-lua/lib "/usr/local/lib/liblua.dylib")
  (def L (doto (lib-lua/luaL_newstate)
           (lib-lua/luaL_openlibs)))
  (def n (dt-ffi/string->c "line"))

  (call-s L n "x = 4 + 2")
  (call-s L n "print(\"x is: \", x)")

  (lib-lua/lua_pushstring L (dt-ffi/string->c "hello Lua!"))
  (lib-lua/lua_setglobal L (dt-ffi/string->c "vv"))
  (call-s L n "print(vv)")

  (lib-lua/lua_close L))
