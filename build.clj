(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'a/libsci)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def version-file "target/VERSION")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path class-dir})
  (b/delete {:path uber-file})
  (b/delete {:path version-file}))

(defn uber [_]
  (clean nil)
  (spit version-file version)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[a.libsci]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'a.libsci}))
