(ns tasks
  (:require
   [babashka.fs :as fs]
   [babashka.process :as p]
   [clojure.string :as str]))

(defn -mk-cp [cp-paths]
  (->> cp-paths
       (map (comp str fs/absolutize))
       (str/join fs/path-separator)))

(defn make-graal [& args]
  ;; :compile-path target/generated-classes
  (let [opts (into {} (partition 2 args))]
    (when-not (get opts ":complie-path")
      ;; clean up only the default compile-path
      (fs/delete-tree "target/generated-classes"))
    (apply p/shell "clojure" "-X:make-graal" args)))

(defn -native-image [native-image jar-file cp-paths]
  (let [dir "target/native"
        opt {:dir dir}
        refelction-json (str (fs/absolutize "sci/reflection.json"))
        cmd [native-image
             "-jar" (str (fs/absolutize jar-file))
             "-cp" (-mk-cp cp-paths)
             "-H:Name=libsci"
             "--shared" "-H:+ReportExceptionStackTraces"
             "-J-Dclojure.spec.skip-macros=true"
             "-J-Dclojure.compiler.direct-linking=true"
             "-H:IncludeResources=SCI_VERSION"
             (str "-H:ReflectionConfigurationFiles=" refelction-json)
             "--initialize-at-build-time"
             "-H:Log=registerResource:"
             "--verbose"
             "--no-fallback"
             "--no-server"
             "--enable-preview"
             "-J-Xmx8g"]]
    (fs/create-dirs dir)
    (println opt cmd)
    (apply p/shell opt cmd)))

(defn compile-native
  []
  (let [graalvm-home (or (System/getenv "GRAALVM_HOME")
                         (throw (Exception. "Please set GRAALVM_HOME.")))
        java-home (str (fs/path graalvm-home "bin"))
        svm-jar (str (or (first (fs/glob graalvm-home "**/svm.jar"))
                         (throw (Exception. "Cannot find `svm.jar` in GRAALVM_HOME."))))
        native-image (str (fs/path graalvm-home "bin" "native-image"))

        javac (str (fs/file graalvm-home "bin" "javac"))
        graal-classes "target/generated-classes"]
    (make-graal)

    (prn :graalvm-home graalvm-home :java-home java-home :svm-jar svm-jar)
    ;; (p/shell "clojure" "-T:build" "uber")

    (let [version (slurp "target/VERSION")
          sci-jar (format "target/libsci-%s-standalone.jar" version)
          cp (-mk-cp [sci-jar svm-jar graal-classes])
          cmd [javac "-d" graal-classes "-cp" cp "src/a/LibSci.java"]]
      (println cmd)
      (apply p/shell cmd)
      ;; (p/shell {:dir graal-classes} "jar" "uvf" (str (fs/absolutize sci-jar)) "a")

      (-native-image native-image sci-jar ["libsci/src" graal-classes]))))

(defn c-make [& args]
  (let [opt {:dir "src-c"}
        cmd (into ["make"] args)]
    (println opt cmd)
    (apply p/shell opt cmd)))
