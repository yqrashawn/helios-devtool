(ns lib.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [lambdaisland.glogi :as log]
   [lambdaisland.glogi.console :as glogi-console]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))

(glogi-console/install!)
(log/set-levels {:glogi/root (if goog.DEBUG :all :info)})

(declare chsk ch-chsk chsk-send! chsk-state)

(defn init [runtime-id]
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk-lib"
         nil
         {:type     :auto
          :protocol :http
          :host     "localhost"
          :port     3113
          :params   {:runtime-id runtime-id}})]

    (def chsk       chsk)
    (def ch-chsk    ch-recv)            ; ChannelSocket's receive channel
    (def chsk-send! send-fn)            ; ChannelSocket's send API fn
    (def chsk-state state)              ; Watchable, read-only atom
    ))
(defn send [type data]
  (try
    (chsk-send! (log/spy
                 [(keyword "lib" "lib")
                  {:type (keyword type)
                   :data (js->clj data :keywordize-keys true)}]))
    (catch js/Error e
      (log/debug :error e))))
