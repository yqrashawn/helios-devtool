(ns x.shadow
  (:require
   ;; [babashka.process :refer [process]]
   [babashka.tasks :refer [shell]]
   [lib.cider :refer [shadow-cljs]]
   [babashka.fs :as fs]
   [clojure.string :as s]
   ;; [taoensso.timbre :refer [warn]]
   [clojure.java.shell :refer [sh]]))

(defonce project-dir (-> *file* fs/parent fs/parent fs/parent))

(defn- shadow-edn []
  (-> project-dir
      (fs/path "shadow-cljs.edn")
      fs/file
      slurp
      read-string))

(defn run [& args]
  (let [args                (or args ["server"])
        shadow-cljs-command (shadow-cljs)]
    (println (s/join " " (concat [">" shadow-cljs-command] args)))
    (apply shell shadow-cljs-command args)))

(defn release-all []
  (let [{:keys [builds]} (shadow-edn)
        ids              (remove #{:cards :browser-test :karma-test} (keys builds))]
    (apply run "release" ids)))
