(ns com.example.workspaces.fulcro-demo-cards
  (:require [com.fulcrologic.fulcro.components :as fp]
            [com.fulcrologic.fulcro-css.localized-dom :as dom]
            [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
            [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
            [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
            [com.fulcrologic.fulcro.mutations :as fm]))

(fp/defsc FulcroDemo
  [this {:keys [counter]}]
  {:initial-state (fn [_] {:counter 0})
   :ident         (fn [] [::id "singleton"])
   :query         [:counter]}
  (dom/div
   (str "Fulcro counter demo [" counter "]")
   (dom/button {:onClick #(fm/set-value! this :counter (inc counter))} "+")))

(ws/defcard fulcro-demo-card
            (ct.fulcro/fulcro-card
             {::ct.fulcro/root       FulcroDemo
              ::ct.fulcro/wrap-root? true}))
