(ns lib.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   ;; <other stuff>
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)] ; <--- Add this
   ))

(defn init []

  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk"
         nil
         {:type :auto
          :protocol :http :host "localhost" :port 3113})]

    (def chsk       chsk)
    (def ch-chsk    ch-recv)            ; ChannelSocket's receive channel
    (def chsk-send! send-fn)            ; ChannelSocket's send API fn
    (def chsk-state state)              ; Watchable, read-only atom
    ))
