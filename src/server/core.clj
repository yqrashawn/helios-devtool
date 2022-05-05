(ns server.core
  (:require
   [taoensso.timbre :as log]
   [taoensso.sente :as sente]
   [ring.middleware.anti-forgery]
   [taoensso.sente.server-adapters.http-kit      :refer (get-sch-adapter)]
   [config.core :refer [load-env]]
   [hiccup.page :as hiccup]
   [mount.core :refer [defstate] :as mount]
   [org.httpkit.server :as http]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.util.response :as response])
  (:gen-class))

;; Graal does not support reflection calls
(set! *warn-on-reflection* true)

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(def handler
  (ring/ring-handler
   (ring/router
    [["/chsk" {:get  ring-ajax-get-or-ws-handshake
               :post ring-ajax-post}]
     ["/"
      {:get (fn [request]
              (-> (hiccup/html5
                   [:head (hiccup/include-css "public/css/compiled/main.css")]
                   [:head (hiccup/include-js "public/js/compiled/shared.js")]
                   [:head (hiccup/include-js "public/js/compiled/main.js")]
                   [:div.content
                    [:h2 (str "Hello " (:remote-addr request) " 🔥🔥🔥")]])
                  (response/response)
                  (response/header "content-type" "text/html")))}]])))

(defmethod response/resource-data :resource
  [^java.net.URL url]
  ;; GraalVM resource scheme
  (let [resource (.openConnection url)
        len      (#'ring.util.response/connection-content-length resource)]
    (when (pos? len)
      {:content        (.getInputStream resource)
       :content-length len
       :last-modified  (#'ring.util.response/connection-last-modified resource)})))

(defstate env :start (load-env))

(defstate server
  :start (let [port (or (:port env) 3113)]
           (log/info :server-started "starting on port:" port)
           (http/run-server
            (wrap-defaults
             handler
             (assoc api-defaults :static {:resources "public"}))
            {:port port}))
  :stop (when server (server :timeout 100)))

(defn -main [& args]
  (mount/start))
