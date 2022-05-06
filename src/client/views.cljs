(ns client.views
  (:require
   [re-frame.core :as rf]
   [client.routes :as routes]
   [client.ui.body :as body]
   [client.ui.header :as header]
   [client.events :as events]))

(defn main-panel []
  (let [current-route                 @(rf/subscribe [::routes/current-route])
        {:keys [view] :as route-data} (get current-route :data {})]
    [:div.grid.h-100vh
     {:style {:gridTemplate    "\"header\" min-content \"main\""}}
     [header/ui]
     [body/ui (if view [view route-data] [:div])]]))
