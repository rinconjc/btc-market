(ns btc-market.android.core
  (:require [btc-market.account :refer [accounts-view]]
            [btc-market.common :refer [view]]
            btc-market.events
            [btc-market.prices :refer [coin-prices-view]]
            [btc-market.settings :refer [settings-view]]
            btc-market.subs
            [btc-market.trading :refer [buy-sell-view open-orders-view]]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r]
            [btc-market.prices :refer [dashboard-view]]
            [btc-market.common :as c]))

(def ReactNative (js/require "react-native"))

(set! (.-ignoredYellowBox js/console) #js ["Setting a timer" "HMR" "reframe"])

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
    [view {:style {:background-color (c/colors :dark-primary)}}
     [toolbar {:title "BTC Connect" :height 40 :title-color (c/colors :text-primary)
               :actions [{:title "Settings" :show "never"}
                         {:title "Accounts" :show "never"}
                         {:title "Dashboard" :show "never"}]
               :on-action-selected
               #(dispatch [:push-view
                           (nth [#'settings-view #'accounts-view
                                 #'dashboard-view] %)])}]
     (if @top-view
       @top-view)
     ;; [(or (first @top-view) #'dashboard-view)]
     ]))

(defn init []

  (.addEventListener (.-BackHandler ReactNative) "hardwareBackPress"
                     #(do
                        (dispatch [:pop-view])
                        true))
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "BtcMarket" #(r/reactify-component app-root)))
