(ns btc-market.android.core
  (:require [btc-market.account :refer [accounts-view]]
            [btc-market.common :refer [view]]
            btc-market.events
            [btc-market.prices :refer [coin-prices-view]]
            [btc-market.settings :refer [settings-view]]
            btc-market.subs
            [btc-market.trading :refer [buy-sell-view open-orders-view]]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def toolbar (r/adapt-react-class (.-ToolbarAndroid ReactNative)))
(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [top-view (subscribe [:top-view])]
    [view
     [toolbar {:title "" :height 40
               :actions [{:title "Settings" :show "never"}
                         {:title "Accounts" :show "ifRoom"}
                         {:title "Prices" :show "always"}
                         {:title "Buy/Sell" :show "always"}
                         {:title "Open Orders" :show "ifRoom"}
                         {:title "Market Data" :show "ifRoom"}]
               :on-action-selected
               #(dispatch [:push-view
                           (nth [#'settings-view #'accounts-view
                                 #'coin-prices-view #'buy-sell-view
                                 #'open-orders-view] %)])}]
     [(or @top-view #'coin-prices-view)]]))

(defn init []
  (.addEventListener (.-BackHandler ReactNative) "hardwareBackPress"
                     #(do
                        (dispatch [:pop-view])
                        true))
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "BtcMarket" #(r/reactify-component app-root)))
