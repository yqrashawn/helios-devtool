(ns client.views.home
  (:require
   ["@radix-ui/react-collapsible" :as T]
   ["react-json-view$default" :as RJsonView]
   [lambdaisland.glogi :as log]
   [promesa.core :as p]
   [datascript.core :as d]
   [client.db :refer [conn]]
   [re-frame.core :as rf]
   [re-posh.core :as rp])
  (:import [goog.date DateTime]))

(defn controllers []
  [{:start identity :stop identity}])

(rp/reg-query-sub
 ::rpcsets
 '[:find [?rpcset ...]
   :where [?rpcset :rpcset/id]])

(rp/reg-pull-sub
 ::rpcs
 '[*])

(rp/reg-sub
 ::rpcset-in
 (fn [_ [_ id]]
   {:type      :query
    :query     '[:find ?rpc-id .
                 :in $ ?id
                 :where
                 [?id :rpcset/rpc ?rpc-id]
                 (or [?rpc-id :rpc/type :popup-in]
                     [?rpc-id :rpc/type :inpage-in])]
    :variables [id]}))

(rp/reg-sub
 ::rpcset-out
 (fn [_ [_ id]]
   {:type      :query
    :query     '[:find ?rpc-id .
                 :in $ ?id
                 :where
                 [?id :rpcset/rpc ?rpc-id]
                 (or [?rpc-id :rpc/type :popup-out]
                     [?rpc-id :rpc/type :inpage-out])]
    :variables [id]}))

(rf/reg-sub
 ::rpcset
 (fn [[_ id] _]
   [(rp/subscribe [::rpcset-in id])
    (rp/subscribe [::rpcset-out id])])
 (fn [[in out] _]
   (when (and in out)
     {:in in :out out})))

(defn rpc-result [x]
  (if (coll? x)
    [:> RJsonView {:src (clj->js x) :collapsed 1 :name "result"}]
    [:p "result: " [:code (str x)]]))

(defn rpcset [id]
  (let [{:keys [in out]}                               @(rp/subscribe [::rpcset id])
        {:rpc/keys [method from] start-time :rpc/time} @(rp/subscribe [:ds/pull [:rpc/method :rpc/from :rpc/time] in])
        {:rpc/keys [result] end-time :rpc/time}        @(rp/subscribe [:ds/pull [:rpc/result :rpc/time] out])
        time-spend                                     (- end-time start-time)
        start-date-time                                (DateTime. (js/Date. start-time))]
    (and in out
         ^{:key id}
         [:> T/Root
          [:div.grid.border.mb-4.p-1
           [:> T/Trigger
            [:p.rs1.re2.cs1.grid.justify-start.gap-4.content-center.text-left
             [:code.cs1.w-min id]
             [:code.cs2 {:style {:minWidth "20rem"}} method]
             [:code.cs3 {:style {:minWidth "6.5rem"}} (str (.toIsoTimeString start-date-time) ":" (.getUTCMilliseconds start-date-time))]
             [:span.cs4 (name from)]
             [:data.cs5 {:value time-spend} (str time-spend " ms")]]]
           [:> T/Content
            ;; {:className }
            [:div.rs2.re3.mt-4
             [rpc-result result]]]]])))

(.getUTCMilliseconds (DateTime. (js/Date.)))

(defn main []
  (let [rpc-ids @(rp/subscribe [::rpcsets])]
    [:div.grid
     [into [:div] (take 20 (map rpcset (reverse (sort rpc-ids))))]]))

(comment
  (tap> (d/db conn))

  (p/do @(rf/subscribe [::rpcset [:rpcset/id 7511590879971114]]))
  (p/do @(rf/subscribe [::rpcset-result [:rpcset/id 7511590879971114]]))
  (p/do @(rf/subscribe [::rpcset-in [:rpcset/id 7511590879971114]]))

  (d/q '[:find ?method .
         :in $ ?id
         :where
         [?id :rpcset/rpc ?rpc-id]
         (or [?rpc-id :rpc/type :popup-in]
             [?rpc-id :rpc/type :inpage-in])
         [?rpc-id :rpc/method ?method]]
       (d/db conn) 100)

  (d/pull (d/db conn) '[*] 99)
  (tap> (d/pull (d/db conn) '[{:rpcset/rpc [*]}] [:rpcset/id 5893323330347158]))
  (d/q '[:find [?id ...]
         :where [?rpci :rpc/id ?id]]
       (d/db conn))
  (d/pull (d/db conn) '[*] 66))
