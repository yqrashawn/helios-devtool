(ns client.socket
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [lambdaisland.glogi :as log]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))

(def ch-chsk (atom nil))
(def chsk-send! (atom nil))
(def chsk-state (atom nil))

(defn init! []
  (let [{:keys [ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk-client"
         nil
         {:type     :auto
          :protocol :http
          :packer   :edn
          :host     "localhost"
          :port     3113})]

    (reset! ch-chsk ch-recv) ; ChannelSocket's receive channel
    (reset! chsk-send! send-fn)            ; ChannelSocket's send API fn
    (reset! chsk-state state)              ; Watchable, read-only atom
    (go-loop [data nil]
      (when data
        (log/debug :rev data))
      (when @ch-chsk (recur (<! @ch-chsk))))))
