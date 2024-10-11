(ns a.libsci
  (:require
   [a.demo :as demo]
   [cheshire.core :as cheshire]
   [sci.core :as sci]
   [tech.v3.datatype.ffi.graalvm-runtime])
  (:gen-class
   :methods [^{:static true} [evalString [String] String]]))

(defn -evalString [s]
  (sci/binding [sci/out *out*]
    (let [opts {:namespaces
                {'cheshire.core
                 {'generate-string cheshire/generate-string}
                 'demo
                 {'lib0-test-with-graall-binding demo/lib0-test-with-graall-binding
                  'lib0-test-with-libpath demo/lib0-test-with-libpath
                  'lib1-test-with-graall-binding demo/lib1-test-with-graall-binding
                  'lib1-test-with-libpath demo/lib1-test-with-libpath
                  'lib0-ffi demo/lib0-ffi}}}]
      (str (try (sci/eval-string s opts)
                (catch Exception e
                  {:error (str (type e))
                   :message (.getMessage e)}))))))

(defn -main [& _args]
  (println (demo/lib0-ffi)))
