(ns server.socket-handlers
  (:require
   [clojure.core.async :refer [<! put! go-loop]]
   [clojure.string :as s]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
   [taoensso.timbre :as log]))

(declare chsk-connected-uids)
(defonce ch-chsk (atom nil))
(defonce chsk-send-fn (atom identity))

(defn chsk-send! [u d]
  (@chsk-send-fn u d))

(defmulti -client-event-msg-handler :type)
(defmulti -lib-event-msg-handler :type)

(defn lib-users []
  (filter #(s/starts-with? % "chrome-extension://") (:any @chsk-connected-uids)))

(defn client-users []
  (filter #(not (s/starts-with? % "chrome-extension://")) (:any @chsk-connected-uids)))

(defn event-msg-handler [id data]
  (when data
    (case id
      :lib/lib       (-lib-event-msg-handler data)
      :client/client (-client-event-msg-handler data))))

(defmethod -client-event-msg-handler :default
  [{:keys [type]}]
  (log/error (str "No -client-event-msg-handler for type " type)))

;; (defmethod -lib-event-msg-handler :default
;;   [{:keys [type]}]
;;   (log/error (str "No -lib-event-msg-handler for type " type)))

(defmethod -lib-event-msg-handler :default
  [data]
  (doseq [uid (client-users)] (chsk-send! uid [:server/sync data])))

(defn stop []
  #_{:clj-kondo/ignore [:inline-def]}
  (def chsk-connected-uids {})
  (reset! ch-chsk nil)
  (reset! chsk-send-fn identity))

(defn init []
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket!
         (get-sch-adapter)
         {:packer        :edn
          :csrf-token-fn nil
          :user-id-fn
          (fn [{:keys [params cookies headers uri]}]
            (let [session    (get-in cookies ["ring-session" :value])
                  origin     (get headers "origin")
                  runtime-id (get params :runtime-id)]
              (cond
                (and
                 (= uri "/chsk-lib")
                 (s/starts-with? origin "chrome-extension://")
                 (some #{runtime-id} ["background" "inpage" "popup" "content-script"]))
                (str origin "/" runtime-id)

                (= uri "/chsk-client")
                session

                :else
                nil)))})]
    (def chsk-connected-uids connected-uids)
    (reset! ch-chsk ch-recv) ; ChannelSocket's receive channel
    (reset! chsk-send-fn send-fn)

    (go-loop [data nil]
      (when (and (or (-> data
                         :id
                         (= :lib/lib))
                     (-> data
                         :id
                         (= :client/client)))
                 (-> data :?data))
        (event-msg-handler (:id data) (:?data data)))
      (when @ch-chsk (recur (<! @ch-chsk))))

    {:ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
     :chsk-send! send-fn}))
