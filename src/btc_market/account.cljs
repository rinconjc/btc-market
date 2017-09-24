(ns btc-market.account
  (:require [btc-market.common
             :refer
             [col-style
              refresh-control
              row-style
              screen-style
              scroll-view
              text
              title-style
              view]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [btc-market.common :as c]))

(defn accounts-view []
  (let [accounts (subscribe [:account])]
    (dispatch [:fetch-account-balance])
    (fn []
      [scroll-view {:style screen-style :height "100%"
                    :content-container-style {:align-items "center"}
                    :refresh-control (r/as-element
                                      [refresh-control {:on-refresh #(dispatch [:fetch-account-balance])
                                                        :refreshing false}])}
       [text {:style title-style} "Account"]
       (for [{:keys [currency balance]} (:balances @accounts)]
         ^{:key currency}
         [view {:style row-style}
          [text {:style col-style} currency]
          [text {:style col-style} (c/truncate balance 3)]])])))
