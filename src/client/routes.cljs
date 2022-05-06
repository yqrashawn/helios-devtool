(ns client.routes
  (:require
   ;; [promesa.core :as p]
   [reitit.core :as r]
   [shadow.loader :as loader]
   [reitit.frontend :as rtf]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend.controllers :as rfc]
   ;; https://cljdoc.org/d/metosin/reitit/0.5.16/doc/coercion/malli
   [reitit.coercion.malli :as m]
   [reitit.coercion :as coercion]
   [client.views.home :as home]
   ;; [reitit.exception :as exception]
   ;; [reitit.core :as r]
   [re-frame.core :as rf]
   [lambdaisland.glogi :as log]))

(declare router)

;; https://github.com/russmatney/starters/blob/master/fullstack/src/fullstack/ui/routes.cljs
;;; Subs
(rf/reg-sub
 ::current-route
 (fn [db] (:current-route db)))

;;; Events
;; navigate with (rf/dispatch [:navigate :routes/home])
(rf/reg-event-fx
 :navigate
 (fn [_cofx [_ & route]] {::navigate! route}))

;; Triggering navigation from events.
(rf/reg-fx
 ::navigate!
 (fn [route] (apply rfe/push-state route)))

(rf/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [{:keys [lazy view controllers]}
         (:data new-match)

         controllers
         (if (and lazy (fn? controllers)) ((controllers)) controllers)

         new-match
         (if lazy
           (-> new-match
               (assoc-in [:data :view] (view))
               (assoc-in [:data :controllers] controllers))
           new-match)

         old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

;;; Routes
(def routes
  [""
   {:name        :route/home
    :view        home/main
    :controllers (home/controllers)
    :conflicting true}
   ["/"
    ["" {:name        :route/home
         :view        home/main
         :controllers (home/controllers)
         :conflicting true}]]])

(def router
  (rtf/router
   routes
   {:data    {:coercion m/coercion}
    :compile coercion/compile-request-coercers
    :conflicts
    (fn [conflicts]
      ;; (warn (exception/format-exception :path-conflicts nil conflicts))
      )}))

(defn- fetch-router-view!
  "Load lazy route with pages-conf"
  [route-name dispatch-fn]
  (if (loader/loaded? (name route-name))
    (dispatch-fn)
    ;; load lazy page
    (->
     (loader/load (name route-name))
     (.then dispatch-fn #(do
                           (rf/dispatch [:navigate :route/server-error])
                           ;; (log/error %)
                           )))))

;;; init
(defn on-navigate [new-match]
  (when new-match
    (let [{:keys [name lazy]} (:data new-match)]
      (log/debug :route-match name)
      (if lazy
        (fetch-router-view! name #(rf/dispatch [::navigated new-match]))
        (rf/dispatch [::navigated new-match])))))

(defn init! []
  (log/debug :init :routes)
  (rfe/start! router on-navigate {:use-fragment true}))
