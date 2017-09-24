(ns btc-market.common
  (:require [cljs.reader :refer [read-string]]
            [reagent.core :as r]))

(def ReactNative (js/require "react-native"))
(def socket-io (js/require "socket.io-client"))

(def list-view (r/adapt-react-class (.-ListView ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
(def refresh-control (r/adapt-react-class (.-RefreshControl ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def button (r/adapt-react-class (.-Button ReactNative)))
(def picker (r/adapt-react-class (.-Picker ReactNative)))
(def picker-item (r/adapt-react-class (.-Picker.Item ReactNative)))
(def toolbar (r/adapt-react-class (.-ToolbarAndroid ReactNative)))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def storage (.-AsyncStorage ReactNative))

(defn truncate [n d]
  (let [i (Math/floor n)]
    (str i "." (-> (- n i) (* (Math/pow 10 d)) (Math/round)))))

(defn alert [title message & more] (.alert (.-Alert ReactNative) title message))

(def crypto-utils (when (exists? (.-NativeModules ReactNative))
                    (js/console.log "native modules exists!")
                    (.-CryptoUtils (.-NativeModules ReactNative))))

(def colors {:dark-primary     "#455A64"
             :default-primary  "#607D8B"
             :light-primary    "#CFD8DC"
             :text-primary     "#FFFFFF"
             :accent           "#9E9E9E"
             :primary-text     "#212121"
             :secondary-text   "#757575"
             :divider          "#BDBDBD"})

(def title-style {:font-size 30 :font-weight "100" :margin-bottom 10
                  :text-align "center" :color (colors :text-primary)})
(def screen-style {:flex-direction "column" :margin 0
                   :background-color (colors :default-primary)})
(def row-style {:flex-direction "row" :width "100%" :background-color (colors :light-primary)})
(def col-style {:font-size 20  :width "50%" :color (colors :secondary-text) :padding 5})
(def form-style {:flex-direction "column" :padding 5 :width "100%"})
(def text-style {:color (colors :primary-text) :font-size 15 :text-align-vertical "center" :text-align "center"})
(def small-text {:color (colors :text-primary) :font-size 10 :text-align-vertical "center" :text-align "center"})
(def button-style {:background-color (colors :dark-primary)})
(def button-text (assoc text-style :font-size 20 :color (colors :text-primary)))


(defn save [key value]
  (.setItem storage (str key) (pr-str value)))

(defn retrieve [key on-ready on-fail]
  (.getItem storage (str key)
            #(do
               (if %1 (on-fail %1)
                   (if %2 (on-ready (read-string %2)))))))
