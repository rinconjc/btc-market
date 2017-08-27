(ns btc-market.trading
  (:require [re-frame.core :refer [subscribe]]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as r]
            [btc-market.db :refer [currencies]]
            [btc-market.common :as c]))


(defn trading-view []
  (let [trades (subscribe [:open-trades])
        new-trade (r/atom {})]
    (fn []
      [c/scroll-view {:style c/screen-style :height "100%"
                      :content-container-style {:align-items "center"}}
       [c/text {:style c/title-style} "Buy/Sell"]
       [c/view {:style c/form-style}
        [c/text "Buy/Sell"]
        [c/picker {:selected-value (:buy-sell @new-trade)
                   :on-value-change #(swap! new-trade assoc :buy-sell %1)}
         [c/picker-item {:label "Buy" :value :buy}]
         [c/picker-item {:label "Sell" :value :sell}]]
        [c/text "Currency"]
        [c/picker {:selected-value (:currency @new-trade)
                   :on-value-change #(swap! new-trade assoc :currency %1)}
         (for [[cur label] currencies]
           ^{:key cur}[c/picker-item {:label label :value cur}])]
        [c/text "Order Type"]
        [c/picker {:selected-value (:type @new-trade)
                   :on-value-change #(swap! new-trade assoc :type %1)}
         [c/picker-item {:label "Limit" :value :limit}]
         [c/picker-item {:label "Market" :value :market}]]
        [c/text "Volume"]
        [c/text-input {:keyboard-type "numeric"
                       :on-change-text #(swap! new-trade assoc :volume %)} (:volume @new-trade)]
        [c/text "Price"]
        [c/text-input {:keyboard-type "numeric"
                       :on-change-text #(swap! new-trade assoc :price %)} (:price @new-trade)]
        [c/text (str "Total: " (* (:price @new-trade) (:volume @new-trade)))]
        [c/button {:title (str "Buy " (or (:currency @new-trade) "..."))}]]])))
