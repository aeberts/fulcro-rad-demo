(ns com.example.ui.semantic-testing
  (:require
   #?@(:cljs [[com.fulcrologic.fulcro.mutations :as m]
              [com.fulcrologic.semantic-ui.factories :as sf]
              [com.fulcrologic.semantic-ui.icons :as sfi]
              [com.fulcrologic.semantic-ui.collections.menu.ui-menu :refer [ui-menu]]
              ])
   #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
      :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom.html-entities :as ent]
   [com.fulcrologic.fulcro.routing.dynamic-routing :refer [defrouter]]
   [com.fulcrologic.rad.authorization :as auth]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.ids :refer [new-uuid]]
   [com.fulcrologic.rad.routing :as rroute]
   [taoensso.timbre :as log]
   #?(:cljs ["semantic-ui-react" :as js-sui])
   [com.fulcrologic.fulcro.mutations :as m]
   ))

(m/defmutation toggle-sidebar-react-visibility
               "Mutation: Toggle sidebar visibility"
               [{visibile? [:component/id :sidebar-react :sidebar-react/visible?]}]
               (action [{:keys [state]}]
                       (swap! state update-in [:component/id :sidebar-react :sidebar-react/visible?] not)))

(m/defmutation toggle-sidebar-vanilla-visibility
               "Mutation: Toggle sidebar visibility"
               [{visibile? [:component/id :sidebar-vanilla :sidebar-vanilla/visible?]}]
               (action [{:keys [state]}]
                       (swap! state update-in [:component/id :sidebar-vanilla :sidebar-vanilla/visible?] not)))

(defsc MenuItem [this {:menu-item/keys [id label icon] :as props}]
       {:query         [:menu-item/id :menu-item/label :menu-item/icon]
        :initial-state (fn [_] [{:menu-item/id 1 :menu-item/label "Home" :menu-item/icon sfi/home-icon}
                                {:menu-item/id 2 :menu-item/label "Gamepad" :menu-item/icon sfi/gamepad-icon}
                                {:menu-item/id 3 :menu-item/label "Channels" :menu-item/icon sfi/camera-icon}])})

(defn ui-menu-item [{:menu-item/keys [id label icon]}]
  (sf/ui-menu-item {:as "a"}
                   (sf/ui-icon {:name icon}) label))

(defsc SidebarReact [this {:sidebar-react/keys [visible? menu-items]}]
       {:query         [:sidebar-react/visible?
                        {:sidebar-react/menu-items (comp/get-query MenuItem)}]
        :ident         (fn [] [:component/id :sidebar-react])
        :initial-state {:sidebar-react/visible?   true
                        :sidebar-react/menu-items {}}}
       (sf/ui-container
        {}
        (sf/ui-grid
         {:columns 1}
         (sf/ui-grid-column
          {}
          (sf/ui-checkbox
           {:toggle  true :checked visible? :label "Sidebar"
            :onClick #(comp/transact! this [(toggle-sidebar-react-visibility {})])}))
         (sf/ui-grid-column
          {}
          (sf/ui-sidebar-pushable
           {:as js-sui/Segment}
           (sf/ui-sidebar
            {:as js-sui/Menu :animation "overlay" :icon "labeled" :inverted true :vertical true :visible visible? :width "thin"}
            (mapv ui-menu-item
                  menu-items))
           (sf/ui-sidebar-pusher
            {}
            (sf/ui-segment
             {:basic true}
             (sf/ui-header
              {:as "h3"}
              "Application Content")
             (sf/ui-image
              {:src "https://react.semantic-ui.com/images/wireframe/paragraph.png"}))))))))

(def ui-sidebar-react (comp/factory SidebarReact))

(defsc SidebarVanilla [this {:sidebar-vanilla/keys [visible? menu-items]}]
       {:query         [:sidebar-vanilla/visible?
                        {:sidebar-vanilla/menu-items (comp/get-query MenuItem)}]
        :ident         (fn [] [:component/id :sidebar-vanilla])
        :initial-state {:sidebar-vanilla/visible?   true
                        :sidebar-vanilla/menu-items {}}}
       (dom/div
        :.ui.container
        (dom/div
         :.ui.one.column.grid
         (dom/div
          :.ui.column
          (dom/div
           :.ui.toggle.checkbox
           (dom/input
            {:type "checkbox"
             :onClick #(comp/transact! this [(toggle-sidebar-vanilla-visibility {})])
             :checked (if visible? "checked" "")})
           (dom/label "Sidebar")))
         (dom/div :.column
                  (dom/div
                   :.ui.segment.pushable
                   (dom/div :.ui.sidebar.menu.overlay.left.vertical.inverted.thin.labeled.icon
                            {:classes [(when visible? "visible")]}
                            (mapv ui-menu-item
                                  menu-items))
                   (dom/div :.ui.pusher
                            (dom/div :.ui.segment
                                     (dom/h3 :.ui.header
                                             "Application Content")
                                     (dom/img :.ui.image
                                              {:src "https://react.semantic-ui.com/images/wireframe/paragraph.png"}))))))))

(def ui-sidebar-vanilla (comp/factory SidebarVanilla))