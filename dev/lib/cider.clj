(ns lib.cider
  (:require
   [clojure.string :as s]
   [clojure.java.shell :refer [sh]]))

(defn- ->p [x]
  (-> x
      :out
      s/trim-newline
      read-string))

(defn g [type]
  (str
   (-> (sh "emacsclient" "--eval" (str "(cider-jack-in-resolve-command '" (name type) ")"))
       ->p)
   " "
   (-> (sh "emacsclient" "--eval" (str "(cider-inject-jack-in-dependencies nil nil '" (name type) ")"))
       ->p)))

(defn clojure-cli []
  (g :clojure-cli))

(defn shadow-cljs []
  (g :shadow-cljs))
