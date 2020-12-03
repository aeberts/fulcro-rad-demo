(ns com.example.workspaces.main
  (:require [nubank.workspaces.core :as ws]
            [com.example.workspaces.cards]
            [com.example.workspaces.custom-card]
            [com.example.workspaces.fulcro-demo-cards]
            ))

(defonce init (ws/mount))
