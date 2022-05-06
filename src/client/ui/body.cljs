(ns client.ui.body)

(defn ui [& children]
  [:main.grid-area-main.grid.justify-strech
   {:style {:gridTemplateColumns "1fr auto 1fr"
            :justifyContent      "strech"}}
   [into
    [:div.cs2.ce3
     {:style {:minWidth "64vw"}}]
    children]])
