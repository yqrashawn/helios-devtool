(ns client.socket
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [taoensso.encore :refer [assoc-some]]
   [client.db :refer [conn]]
   [datascript.core :as d]
   [re-posh.core :as rp]
   [lambdaisland.glogi :as log]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]
   [clojure.string :as s]))

(def ch-chsk (atom nil))
(def chsk-send! (atom nil))
(def chsk-state (atom nil))

(defn- maybe-rpcset-id [db id]
  (or (d/q '[:find ?rpcset .
             :in $ ?id
             :where [?rpcset :rpcset/id ?id]]
           db id)
      (str "rpcset-" id)))

(rp/reg-event-ds
 ::rpc-res
 (fn [ds [_ type {:keys [payload time]}]]
   (if (and time (:id payload))
     (let [{:keys [result id]} payload
           rpcset-id           (maybe-rpcset-id ds id)
           rpc-id              (str "rpc-" id)]
       [{:db/id         rpc-id
         :rpc/type      type
         :rpc/from (if (s/starts-with? (name type) "popup") :popup :inpage)
         :rpc/direction :res
         :rpc/result    result
         :rpc/time      time}
        {:db/id rpcset-id :rpcset/id id :rpcset/rpc rpc-id}])
     [])))

(rp/reg-event-ds
 ::rpc-req
 (fn [ds [_ type {:keys [payload time]}]]
   (if (and time (:id payload))
     (let [{:keys [id method params]} payload
           rpc-id                     (str "rpc-" id)
           rpcset-id                  (maybe-rpcset-id ds id)]
       [{:db/id         rpc-id
         :rpc/type      type
         :rpc/from (if (s/starts-with? (name type) "popup") :popup :inpage)
         :rpc/direction :req
         :rpc/method    method
         :rpc/params    (or params [])
         :rpc/time      (js/Date. time)}
        {:db/id      rpcset-id
         :rpcset/id  id
         :rpcset/rpc rpc-id}])
     [])))

(defmulti -event-msg-handler :type)

(defn event-msg-handler
  [{:keys [id ?data]}]
  (let [[idd data] ?data]
    (when (and (= id :chsk/recv) (= idd :server/sync))
      (-event-msg-handler data))))

(defmethod -event-msg-handler :default
  [{:keys [type]}]
  (log/error :msg-handler (str "No handler for type: " type)))

(defmethod -event-msg-handler :popup-out
  [{:keys [type data]}]
  (rp/dispatch [::rpc-res type data]))

(defmethod -event-msg-handler :inpage-out
  [{:keys [type data]}]
  (rp/dispatch [::rpc-res type data]))

(defmethod -event-msg-handler :popup-in
  [{:keys [type data]}]
  (rp/dispatch [::rpc-req type data]))

(defmethod -event-msg-handler :inpage-in
  [{:keys [type data]}]
  (rp/dispatch [::rpc-req type data]))

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
      (when data (event-msg-handler data))
      (when @ch-chsk (recur (<! @ch-chsk))))))
