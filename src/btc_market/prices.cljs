(ns btc-market.prices
  (:require [btc-market.common
             :refer
             [refresh-control screen-style scroll-view text title-style view]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

(defn coin-prices-view []
  (let [prices (subscribe [:coin-prices])]
    (fn []
      [scroll-view {:style screen-style
                    :height "100%"
                    :content-container-style {:align-items "center" }
                    :refresh-control (r/as-element
                                      [refresh-control {:on-refresh #(dispatch [:fetch-prices])
                                                        :refreshing false}])}
       [text {:style title-style} "Ticker"]
       [view {:style {:flex-direction "row" :width "100%"
                      :background-color "#818181"}}
        [text {:style {:font-size 16 :width "34%" :color "#fff" :padding 5}} "Currency"]
        [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} "Price"]
        [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} "Bid"]
        [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} "Ask"]]

       (for [[cur {:keys [price bid ask]}] @prices]
         ^{:key cur}
         [view {:style {:flex-direction "row" :width "100%"
                        :background-color "#818181"}}
          [text {:style {:font-size 16 :width "34%" :color "#fff" :padding 5}
                 :on-long-press #(js/console.log "long pressed!") } cur]
          [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} price]
          [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} bid]
          [text {:style {:font-size 16 :width "22%" :color "#fff" :padding 5}} ask]])])))
