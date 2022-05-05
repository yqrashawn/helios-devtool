(ns client.css.grammer
  (:require
   [girouette.tw.common :refer [value-unit->css]]
   [girouette.tw.core :refer [make-api]]
   [girouette.tw.common :as common]
   [girouette.tw.color :as color]
   [girouette.tw.layout :as layout]
   [girouette.tw.flexbox :as flexbox]
   [girouette.tw.grid :as grid]
   [girouette.tw.box-alignment :as box-alignment]
   [girouette.tw.spacing :as spacing]
   [girouette.tw.sizing :as sizing]
   [girouette.tw.typography :as typography]
   [girouette.tw.background :as background]
   [girouette.tw.border :as border]
   [girouette.tw.effect :as effect]
    ; [girouette.tw.table :as table]
    ; [girouette.tw.animation :as animation]
    ; [girouette.tw.transform :as transform]
    ; [girouette.tw.interactivity :as interactivity]
    ; [girouette.tw.svg :as svg]
    ; [girouette.tw.accessibility :as accessibility]
   ))

(def my-custom-components
  [{:id :rainbow-text
    :rules "
    rainbow-text = <'rainbow-text'>
    "
    :garden (fn [_]
              {:background-image "linear-gradient(to left, violet, indigo, blue, green, yellow, orange, red)"
               :background-clip "text"
               ;:-webkit-background-clip "text"
               :color "transparent"})}])

(def my-grid-components
  (conj grid/components
        {:id     :grid-area
         :rules  "
         grid-area = <'grid-area-'> (#'[\\w-]+')
         "
         :garden (fn [{[param] :component-data}]
                   {:grid-area param})}
        {:id     :grid-column-start-short
         :rules  "
         grid-column-start-short = <'cs'> integer
         "
         :garden (fn [{[param] :component-data}]
                   {:grid-column-start (value-unit->css param)})}
        {:id     :grid-column-end-short
         :rules  "
         grid-column-end-short = <'ce'> integer
         "
         :garden (fn [{[param] :component-data}]
                   {:grid-column-end (value-unit->css param)})}

        {:id     :grid-row-start-short
         :rules  "
         grid-row-start-short = <'rs'> integer
         "
         :garden (fn [{[param] :component-data}]
                   {:grid-row-start (value-unit->css param)})}
        {:id     :grid-row-end-short
         :rules  "
         grid-row-end-short = <'re'> integer
         "
         :garden (fn [{[param] :component-data}]
                   {:grid-row-end (value-unit->css param)})}))

(def my-chosen-components
  [common/components
   layout/components
   flexbox/components
   my-grid-components
   box-alignment/components
   spacing/components
   sizing/components
   typography/components
   background/components
   border/components
   effect/components
   ;table/components
   ;animation/components
   ;transform/components
   ;interactivity/components
   ;svg/components
   ;accessibility/components
   my-custom-components])

;; Adds colors to the existing default ones.
(def my-color-map
  (assoc color/default-color-map
         "cat-white"  "eeeeee"
         "cat-orange" "e58c56"
         "cat-black"  "333333"))

;; This example shows how to Girouette on a custom grammar.
;; Here, we use only a subset of the Girouette components, and we add your own.
(def class-name->garden
  (:class-name->garden
   (make-api
    my-chosen-components
    {:color-map       my-color-map
     :font-family-map typography/default-font-family-map})))
