(ns server.core
  (:require
   [server.socket-handlers :as socket]
   [taoensso.timbre :as log]
   [config.core :refer [load-env]]
   [hiccup.page :as hiccup]
   [mount.core :refer [defstate] :as mount]
   [org.httpkit.server :as http]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.response :as response])
  (:gen-class))

;; Graal does not support reflection calls
(set! *warn-on-reflection* true)

(defstate handler
  :start
  (let [{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} (socket/init)]
    (ring/ring-handler
     (ring/router
      [["/chsk-lib" {:get  ring-ajax-get-or-ws-handshake
                     :post ring-ajax-post}]
       ["/chsk-client" {:get  ring-ajax-get-or-ws-handshake
                        :post ring-ajax-post}]
       ["/"
        {:get (fn [_]
                (-> (hiccup/html5
                     [:head (hiccup/include-css "/css/global.css")]
                     [:head (hiccup/include-css "/css/compiled/main.css")]
                     [:div#app]
                     [:div (hiccup/include-js "/js/compiled/shared.js")]
                     [:div (hiccup/include-js "/js/compiled/main.js")]
                     [:head (hiccup/include-css "https://cdn.jsdelivr.net/npm/water.css@2/out/water.min.css")])
                    (response/response)
                    (response/header "content-type" "text/html")))}]])))
  :stop (socket/stop))

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
  :start
  (let [port (or (:port env) 3113)]

    (log/info :server-started "starting on port:" port)
    (http/run-server
     (wrap-defaults
      handler
      (-> site-defaults
          (assoc-in [:params :multipart] false)
          (assoc-in [:params :nested] false)
          (assoc-in [:static :files] "resources/client/public")
          (assoc-in [:static :resources] "resources/client/public")
          (assoc-in [:security :anti-forgery] false)))
     {:port port}))
  :stop (when server
          (server :timeout 100)))

(defn -main [& args]
  (mount/start))

(comment
  (-main))
