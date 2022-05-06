(ns client.core
  (:require
   [lambdaisland.glogi :as log]
   [lambdaisland.glogi.console :as glogi-console]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   ;; [re-pressed.core :as rp]
   ;; [breaking-point.core :as bp]
   [client.events :as events]
   [client.routes :as routes]
   [client.views :as views]
   ;; [fgl.app.views :as views]
   [client.config :as config]
   [client.socket :as socket]))

(glogi-console/install!)
(log/set-levels {:glogi/root (if goog.DEBUG :all :info)})

(defn dev-setup []
  (when config/debug?
    (log/debug :mode :dev-mode)))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el))
  (routes/init!)
  (socket/init!))

(defn init []
  (log/debug :init :root)
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

(comment
  (js/location.reload))
