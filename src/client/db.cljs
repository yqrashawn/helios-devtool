(ns client.db
  (:require
   [promesa.core :as p]
   [datascript.core    :as d]
   [re-posh.core :as rp :refer [connect!]]))

(def schema
  {:rpcset/id  #:db{:unique :db.unique/identity}
   :rpcset/rpc #:db{:valueType   :db.type/ref
                    :cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

(connect! conn)

(rp/reg-sub
 :ds/pull
 (fn [_ [_ pattern id]]
   {:type :pull
    :pattern pattern
    :id id}))

(comment
  (p/do
    (d/create-conn schema)))
