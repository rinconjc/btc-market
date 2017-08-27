(ns btc-market.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
 :coin-prices
 (fn [db _]
   (:prices db)))

(reg-sub
 :top-view
 (fn [db _]
   (first (:view-stack db))))

(reg-sub
 :config
 (fn [db _]
   (:config db)))

(reg-sub
 :account
 (fn [db _]
   (:account db)))

(reg-sub
 :open-trades
 (fn [db _]
   (:trades db)))
