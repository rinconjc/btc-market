(ns btc-market.db
  (:require [btc-market.prices :refer [coin-prices-view]]
            [clojure.spec.alpha :as s]))

(def currencies {"ETH" "Ethereum"
                 "BTC" "Bitcoin"
                 "LTC" "Litecoin"
                 "ETC" "Eth-Classic"
                 "XRP" "Ripple Exchange"
                 "BCH" "Bcash"})
;; spec of app-db
(s/def ::curpair string?)
(s/def ::cur-pairs (s/* ::curpair))
(s/def ::amount double?)
(s/def ::price ::amount)
(s/def ::bid ::amount)
(s/def ::ask ::amount)
(s/def ::ticker (s/keys :req-un [::price ::bid ::ask]))
(s/def ::prices  (s/map-of ::curpair ::ticker))
(s/def ::interval int?)
(s/def ::socket any?)
(s/def ::api-key string?)
(s/def ::api-secret string?)
(s/def ::view-stack (s/coll-of any?))
(s/def ::config (s/keys :opt-un [::api-key ::api-secret]))
(s/def ::currency string?)
(s/def ::pendingFunds ::amount)
(s/def ::balance ::amount)
(s/def ::balance-item (s/keys :req-un [::currency ::pendingFunds ::balance]))
(s/def ::balances (s/coll-of ::balance-item))
(s/def ::account (s/keys :req-un [::balances]))
(s/def ::instrument ::currency)
(s/def ::orderSide #{"Bid" "Ask"})
(s/def ::ordertype #{"Limit" "Market"})
(s/def ::created inst?)
(s/def ::status #{"New" "Placed" "Failed" "Error" "Cancelled" "Partially Cancelled"
                  "Fully Matched" "Partially Matched"})
(s/def ::errorMessage (s/or :nil nil? :some string?))
(s/def ::volume ::amount)
(s/def ::openVolume ::amount)
(s/def ::id int?)

(s/def ::order (s/keys :req-un [::id ::currency ::instrument ::orderSide
                                ::ordertype ::status ::price ::volume]
                       :opt-un [::openVolume ::errorMessage ::created]))
(s/def ::orders (s/coll-of ::order))
(s/def ::app-db
  (s/keys :req-un [::view-stack ::cur-pairs]
          :opt-un [::prices ::interval ::socket ::config
                   ::account ::orders]))

;; initialn state of app-db
(def app-db {:view-stack [#'coin-prices-view]
             :cur-pairs ["BTC/AUD" "ETH/AUD"]})
