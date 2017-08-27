(ns btc-market.settings
  (:require [btc-market.common :refer [button text text-input view]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

(defn settings-view []
  (let [conf (subscribe [:config])
        temp (r/atom @conf)]
    (fn []
      [view {:style {:flex-direction "column" :padding 5 :width "100%"}}
       [text {:style {:width "100%"}} "BTC Markets - API Key"]
       [text-input {:style {:width "100%"}
                    :on-change-text #(swap! temp assoc :api-key %)} (:api-key @temp)]
       [text {:style {:width "100%"}} "BTC Markets - API Secret"]
       [text-input {:style {:width "100%"}
                    :on-change-text #(swap! temp assoc :api-secret %)} (:api-secret @temp)]
       [button {:title "Save" :on-press #(dispatch [:update-config @temp])}]])))
