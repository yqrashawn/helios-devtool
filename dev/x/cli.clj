(ns x.cli
  (:require
   [babashka.tasks :refer [shell]]
   [clojure.string :as s]
   [lib.cider :refer [clojure-cli]]))

(defn run [& args]
  (let [args    (or args [])
        command (clojure-cli)]
    (println (s/join " " (concat [">" command] args)))
    (apply shell command args)))
