(ns user
  (:require
   [shadow.cljs.devtools.api :as shadow]
   [shadow.cljs.devtools.cli :as scli]))

(comment
  (scli/-main :stop)
  (scli/-main :server)
  (shadow/watch :main)
  (shadow/stop-worker :main))
