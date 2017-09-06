(ns btc-market.trading
  (:require [btc-market.common :as c :refer [refresh-control]]
            [btc-market.db :refer [currencies]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

(defn buy-sell-view []
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

(defn open-orders-view []
  (let [trades (subscribe [:open-orders])
        col-style {:font-size 16  :color "#fff" :padding 5}]
    (dispatch [:fetch-open-orders])
    (fn []
      [c/scroll-view {:style c/screen-style :height "100%"
                      :content-container-style {:align-items "center"}
                      :refresh-control (r/as-element
                                        [refresh-control {:on-refresh #(dispatch [:fetch-open-orders])
                                                          :refreshing false}])}
       [c/text {:style c/title-style} "Open Orders"]

       (for [{:keys [id instrument orderSide ordertype price status openVolume]} @trades]
         ^{:key id}
         [c/view {:style (assoc c/col-style :width "100%")}
          [c/view {:style c/row-style}
           [c/text {:style col-style} (str orderSide "/" ordertype)]
           [c/text {:style col-style} instrument]
           [c/text {:style col-style} status]]
          [c/view {:style c/row-style}
           [c/text {:style col-style} "Price:"]
           [c/text {:style col-style} price]
           [c/text {:style col-style} "Open Volume:"]
           [c/text {:style col-style} openVolume]]])])))
