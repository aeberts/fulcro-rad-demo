(ns com.example.ui
  (:require
   #?@(:cljs [[com.fulcrologic.fulcro.mutations :as m]
              [com.fulcrologic.semantic-ui.factories :as sf]
              [com.fulcrologic.semantic-ui.icons :as sfi]
              [semantic-ui-react :as suir]
              [com.fulcrologic.semantic-ui.collections.menu.ui-menu :refer [ui-menu]]
              ;;[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
              ;;[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
              ;;[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]
              ;;[com.fulcrologic.semantic-ui.collections.menu.ui-menu :refer [ui-menu]]
              ;;[com.fulcrologic.semantic-ui.collections.menu.ui-menu-item :refer [ui-menu-item]]
              ;;[com.fulcrologic.semantic-ui.elements.image.ui-image :refer [ui-image]]
              ;;[com.fulcrologic.semantic-ui.elements.button.ui-button :refer [ui-button]]
              ;;[com.fulcrologic.semantic-ui.modules.accordion.ui-accordion :refer [ui-accordion]]
              ])
   #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
      :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
   ;;[com.example.ui.account-forms :refer [AccountForm AccountList]]
   ;;[com.example.ui.invoice-forms :refer [InvoiceForm InvoiceList AccountInvoices]]
   ;;[com.example.ui.item-forms :refer [ItemForm InventoryReport]]
   ;;[com.example.ui.line-item-forms :refer [LineItemForm]]
   ;;[com.example.ui.login-dialog :refer [LoginForm]]
   ;;[com.example.ui.sales-report :as sales-report]
   ;;[com.example.ui.dashboard :as dashboard]
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

;;(defsc LandingPage [this props]
;;  {:query         ['*]
;;   :ident         (fn [] [:component/id ::LandingPage])
;;   :initial-state {}
;;   :route-segment ["landing-page"]}
;;  (dom/div "Welcome to the ToDo App. Please log in."))

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

(defsc Root [this {:root/keys [sidebar-react sidebar-vanilla]}]
  {:query         [{:root/sidebar-react (comp/get-query SidebarReact)}
                   {:root/sidebar-vanilla (comp/get-query SidebarVanilla)}]
   :initial-state {:root/sidebar-vanilla {}
                   :root/sidebar-react   {}}}
  (div :.ui.container
       (dom/h3 "React Semantic-UI")
       (ui-sidebar-react sidebar-react)
       (dom/h3 "Vanilla Semantic-UI")
       (ui-sidebar-vanilla sidebar-vanilla)))

(def ui-root (comp/factory Root))

(comment
 (comp/fragment)

 )
;;
;;;; This will just be a normal router...but there can be many of them.
;;(defrouter MainRouter [this {:keys [current-state route-factory route-props]}]
;;  {:always-render-body? true
;;   :router-targets      [LandingPage ItemForm InvoiceForm InvoiceList AccountList AccountForm AccountInvoices
;;                         sales-report/SalesReport InventoryReport
;;                         sales-report/RealSalesReport
;;                         dashboard/Dashboard]}
;;  ;; Normal Fulcro code to show a loader on slow route change (assuming Semantic UI here, should
;;  ;; be generalized for RAD so UI-specific code isn't necessary)
;;  (dom/div
;;   (dom/div :.ui.loader {:classes [(when-not (= :routed current-state) "active")]})
;;   (when route-factory
;;     (route-factory route-props))))
;;
;;(def ui-main-router (comp/factory MainRouter))
;;
;;(auth/defauthenticator Authenticator {:local LoginForm})
;;
;;(def ui-authenticator (comp/factory Authenticator))
;;
;;
;;(defsc Root [this {::auth/keys [authorization]
;;                   ::app/keys  [active-remotes]
;;                   :keys       [authenticator router]}]
;;  {:query         [{:authenticator (comp/get-query Authenticator)}
;;                   {:router (comp/get-query MainRouter)}
;;                   ::app/active-remotes
;;                   ::auth/authorization]
;;   :initial-state {:router        {}
;;                   :authenticator {}}}
;;  (let [logged-in? (= :success (some-> authorization :local ::auth/status))
;;        busy? (seq active-remotes)
;;        username (some-> authorization :local :account/name)]
;;    (dom/div
;;     (div :.ui.top.menu
;;          (div :.ui.item "Demo")
;;          (when logged-in?
;;            #?(:cljs
;;               (comp/fragment
;;                (ui-dropdown {:className "item" :text "Account"}
;;                             (ui-dropdown-menu {}
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this AccountList {}))} "View All")
;;                                               (ui-dropdown-item {:onClick (fn [] (form/create! this AccountForm))} "New")))
;;                (ui-dropdown {:className "item" :text "Inventory"}
;;                             (ui-dropdown-menu {}
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this InventoryReport {}))} "View All")
;;                                               (ui-dropdown-item {:onClick (fn [] (form/create! this ItemForm))} "New")))
;;                (ui-dropdown {:className "item" :text "Invoices"}
;;                             (ui-dropdown-menu {}
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this InvoiceList {}))} "View All")
;;                                               (ui-dropdown-item {:onClick (fn [] (form/create! this InvoiceForm))} "New")
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this AccountInvoices {:account/id (new-uuid 101)}))} "Invoices for Account 101")))
;;                (ui-dropdown {:className "item" :text "Reports"}
;;                             (ui-dropdown-menu {}
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this dashboard/Dashboard {}))} "Dashboard")
;;                                               (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this sales-report/RealSalesReport {}))} "Sales Report"))))))
;;          (div :.right.menu
;;               (div :.item
;;                    (div :.ui.tiny.loader {:classes [(when busy? "active")]})
;;                    ent/nbsp ent/nbsp ent/nbsp ent/nbsp)
;;               (if logged-in?
;;                 (comp/fragment
;;                  (div :.ui.item
;;                       (str "Logged in as " username))
;;                  (div :.ui.item
;;                       (dom/button :.ui.button {:onClick (fn []
;;                                                           ;; TODO: check if we can change routes...
;;                                                           (rroute/route-to! this LandingPage {})
;;                                                           (auth/logout! this :local))}
;;                                   "Logout")))
;;                 (div :.ui.item
;;                      (dom/button :.ui.primary.button {:onClick #(auth/authenticate! this :local nil)}
;;                                  "Login")))))
;;     (div :.ui.container.segment
;;          (ui-authenticator authenticator)
;;          (ui-main-router router)))))



