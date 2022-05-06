(ns client.events
  (:require
   [re-frame.core :as rf]
   ;; [re-pressed.core :as rp]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(rf/reg-event-db ::initialize-db (fn-traced [_ _] {:current-route nil}))
