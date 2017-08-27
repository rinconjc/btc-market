(ns btc-market.events
  (:require [btc-market.db :as db :refer [app-db]]
            [clojure.spec.alpha :as s]
            [re-frame.core :refer [after dispatch reg-event-db reg-event-fx reg-fx]]
            [cljsjs.socket-io]
            [clojure.string :as str]
            [btc-market.common :refer [crypto-utils]]
            [btc-market.common :refer [retrieve]]
            [btc-market.common :refer [save]]
            [btc-market.common :refer [retrieve]]))

(def btc-url "https://api.btcmarkets.net/")
(def ROUND_NUM 100000000)

(def fetch (.-fetch js/window))
(defn json [data] (-> (.-JSON js/window) (.parse data) js->clj))
;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; ---------- web sockets ---------------------------------
(defn setup-websocket []
  (doto (js/io "https://socket.btcmarkets.net" #js {"secure" true "transports" #js ["websocket"]
                                                    "upgrade" false})
    (.on "connect" #(dispatch [:join-channel]))
    (.on "newTicker" #(dispatch [:new-ticker (js->clj % :keywordize-keys true) ROUND_NUM]))
    (.on "disconnect" #(dispatch [:ws-closed %]))))

;; -- FX handlers -----------------------------------------------------------

(defn http-fetch [{:keys [url headers success failure]}]
  (-> (fetch url #js {:headers (clj->js headers)})
      (.then #(if (.-ok %) (.json %)
                  (throw (.text %))))
      (.then #(as-> % data (js->clj data :keywordize-keys true)
                    (if (not= false (:success data)) (success data) (failure data))))
      (.catch failure)))

(reg-fx
 :http
 (fn [req]
   (doall (for [{:keys [url method success failure]} (if (map? req) [req] req)
                :let [[evt rfn & more] success]]
            (http-fetch {:url url :success #(dispatch (vec (concat [evt (rfn %)] more)))
                         :failure #(dispatch (vec (conj failure %)))})))))

(reg-fx
 :http-with-hmac
 (fn [req]
   (doall (for [{:keys [url path method success failure headers key secret]}
                (if (map? req) [req] req)
                :let [[evt rfn & more] success]]
            (let [timestamp (js/Date.now)]
              (js/console.log "timestamp:" timestamp "key" key)
              (.hmac crypto-utils secret (str path "\n" timestamp "\n")
                     (fn [signature]
                       (http-fetch {:url (str url path)
                                    :headers {"Accept" "application/json"
                                              "Accept-Charset" "UTF-8"
                                              "Content-Type" "application/json"
                                              "apikey" key
                                              "timestamp" timestamp
                                              "signature" signature}
                                    :success #(dispatch (vec (concat [evt (rfn %)] more)))
                                    :failure #(dispatch (vec (conj failure %)))}))))))))

(reg-fx
 :dispatch-interval
 (fn [[evt interval]]
   (js/setInterval #(dispatch evt) interval)))

(reg-fx
 :read-store
 (fn [[key success-event fail-event]]
   (retrieve key #(dispatch (conj success-event %)) #(dispatch (conj fail-event %)))))

(reg-fx
 :write-store
 (fn [[key value]]
   (if value (save key value))))

;; -- Handlers --------------------------------------------------------------

(reg-event-fx
 :initialize-db
 validate-spec
 (fn [_ _]
   (js/console.log "in initialize-db")
   {:db (assoc app-db
               :socket (setup-websocket))
    :dispatch [:fetch-prices]
    :read-store [:config [:update-config] [:log]]}))

(reg-event-db
 :set-greeting
 validate-spec
 (fn [db [_ value]]
   (assoc db :greeting value)))

(reg-event-db
 :join-channel
 validate-spec
 (fn [db [_]]
   (js/console.log "join channels!")
   (let [socket (:socket db)]
     (doseq [cur-pair (:cur-pairs db)]
       (.emit socket "join" (str "Ticker-BTCMarkets-" (str/replace-first cur-pair "/" "-")))))
   db))

(reg-event-db
 :new-ticker
 validate-spec
 (fn [db [_ ticker scale]]
   (let [{:keys [instrument currency lastPrice bestBid bestAsk]} ticker]
     (js/console.log "new-ticker:" currency lastPrice bestBid bestAsk)
     (update db :prices assoc (str instrument "/" currency)
             {:price (/ lastPrice scale)
              :bid (/ bestBid scale)
              :ask (/ bestAsk scale)}))))

(reg-event-db
 :ws-closed
 validate-spec
 (fn [db [_]]
   (js/console.log "ws closed!")))

;; (reg-event-db)
(reg-event-fx
 :fetch-prices
 validate-spec
 (fn [world [_]]
   (js/console.log "fetching prices")
   {:db (:db world)
    :http (for [curpair (:cur-pairs (:db world))]
            {:url (str btc-url "/market/" curpair "/tick")
             :success [:new-ticker identity 1]
             :failure [:log curpair]})}))

(reg-event-fx
 :fetch-account-balance
 validate-spec
 (fn [{:keys [db]} [_]]
   {:db db
    :http-with-hmac {:url btc-url :path "/account/balance"
                     :key (-> db :config :api-key)
                     :secret (-> db :config :api-secret)
                     :failure [:log]
                     :success [:account-updated identity]}}))

(reg-event-db
 :account-updated
 validate-spec
 (fn [db [_ balances]]
   (js/console.log "updating balances" balances)
   (update db :account assoc
           :balances (for [bal balances :when (not (zero? (:balance bal)))]
                       (-> bal (update :balance / 1000000000))))))

(reg-event-db
 :log
 (fn [db [_ value & more]]
   (js/console.log "log:" value more)
   db))

(reg-event-db
 :push-view
 validate-spec
 (fn [db [_ view]]
   (update db :view-stack #(cons view %))))

(reg-event-db
 :pop-view
 validate-spec
 (fn [db [_]]
   (update db :view-stack rest)))

(reg-event-fx
 :update-config
 validate-spec
 (fn [{:keys [db]} [_ config]]
   {:db (assoc db :config config)
    :write-store [:config config]}))
