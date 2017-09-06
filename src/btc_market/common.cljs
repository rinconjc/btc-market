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

(def storage (.-AsyncStorage ReactNative))

(def crypto-utils (when (exists? (.-NativeModules ReactNative))
                    (js/console.log "native modules exists!")
                    (.-CryptoUtils (.-NativeModules ReactNative))))

(def title-style {:font-size 30 :font-weight "100" :margin-bottom 20
                  :text-align "center" :color "#fff"})
(def screen-style {:flex-direction "column" :margin 0
                   :background-color "#4e4e4e"})
(def row-style {:flex-direction "row" :width "100%"
                :background-color "#818181"})
(def col-style {:font-size 20  :width "50%" :color "#fff" :padding 5})
(def form-style {:flex-direction "column" :padding 5 :width "100%"})


(defn save [key value]
  (.setItem storage (str key) (pr-str value)))

(defn retrieve [key on-ready on-fail]
  (.getItem storage (str key)
            #(do
               (if %1 (on-fail %1)
                   (if %2 (on-ready (read-string %2)))))))
