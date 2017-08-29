(ns btc-market.trading
  (:require [re-frame.core :refer [subscribe]]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as r]
            [btc-market.db :refer [currencies]]
            [btc-market.common :as c]))


(defn trading-view []
  (let [prices (subscribe [:coin-prices])
        field-style (-> c/col-style (dissoc :width :padding))
        picker-style (dissoc field-style :font-size)
        new-trade (r/atom {})]

    (fn []
      [c/scroll-view {:style c/screen-style :height "100%"
                      :content-container-style {:align-items "center"}}
       [c/text {:style c/title-style} "Buy/Sell"]
       [c/view {:style (merge c/row-style c/form-style {:height "100%"})}
        [c/text {:style field-style} "Buy/Sell"]
        [c/picker {:style picker-style :selected-value (:buy-sell @new-trade)
                   :on-value-change #(swap! new-trade assoc :buy-sell %1)}
         [c/picker-item {:label "Select" }]
         [c/picker-item {:label "Buy" :value :buy}]
         [c/picker-item {:label "Sell" :value :sell}]]
        [c/text {:style field-style} "Currency"]
        [c/picker {:style picker-style :selected-value (:currency @new-trade)
                   :on-value-change #(swap! new-trade assoc :currency %1)}
         [c/picker-item {:label "Select" }]
         (for [[cur label] currencies]
           ^{:key cur}[c/picker-item {:label label :value cur}])]
        [c/text {:style field-style} "Order Type"]
        [c/picker {:style picker-style :selected-value (:type @new-trade)
                   :on-value-change #(swap! new-trade assoc :type %1
                                            :price (some-> @new-trade :currency (str "/AUD")
                                                           ((deref prices)) :price))}
         [c/picker-item {:label "Select" }]
         [c/picker-item {:label "Limit" :value :limit}]
         [c/picker-item {:label "Market" :value :market}]]
        [c/text {:style field-style} "Volume"]
        [c/text-input {:style field-style :keyboard-type "numeric"
                       :on-change-text #(swap! new-trade assoc :volume %)} (:volume @new-trade)]
        [c/text {:style field-style} "Price"]
        [c/text-input {:style field-style :keyboard-type "numeric"
                       :editable (= :limit (:type @new-trade))
                       :on-change-text #(swap! new-trade assoc :price %)}
         (:price @new-trade)]
        [c/text {:style field-style} (str "Total: " (* (:price @new-trade) (:volume @new-trade)))]
        [c/button {:title (str "Buy " (or (:currency @new-trade) "..."))
                   :on-press #(js/console.log "buy!")}]]])))
