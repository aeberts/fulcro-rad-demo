(ns com.example.ui.todo
  (:require
    #?@(:cljs [[com.fulcrologic.semantic-ui.factories :as sf]
               [com.fulcrologic.semantic-ui.icons :as i]
               [com.fulcrologic.fulcro.dom.events :as evt]
               ])
    [com.fulcrologic.rad.form :as form :refer [defsc-form]]
    [com.fulcrologic.rad.report :as report :refer [defsc-report]]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div p a]]
    [com.example.model.todo :as todo]))

(defsc-form TodoForm [this props]
  {fo/id           todo/id
   fo/attributes   [todo/label todo/status]
   fo/route-prefix "todo"
   fo/title        "Create Todo"
   })

(defsc-report TodoList [this props]
  {ro/title             "All Todos"
   ro/route             "todo"
   ro/source-attribute  :todo/all-todos
   ro/row-pk            todo/id
   ro/columns           [todo/label todo/status]
   ro/form-links        {:todo/label TodoForm :todo/status TodoForm}
   ro/column-formatters {:todo/status (fn [this v] (v todo/todo-status))}
   ro/run-on-mount?     true})

(defsc TodoPane [this props]
  {:query         ['*]
   :ident         (fn [] [:component/id ::todo-pane])
   :route-segment ["todo-pane"]}
  (div :.ui.container
    (div :.ui.grid
      (div :.row
        (div :.sixteen.wide.mobile.four.wide.computer.column
          (dom/h2 "Projects")
          (div :.row
            (sf/ui-menu {:pointing true :secondary true :vertical true}
              (sf/ui-menu-item {:name "Home" :active true})
              (sf/ui-menu-item {:name "Work" :active false}))))
        (div :.sixteen.wide.mobile.twelve.wide.computer.column
          (dom/h2 #js {:style #js {:marginBottom "25px"}} "Tasks")
          (div :.row
            (sf/ui-list {:verticalAlign "middle"}
              (sf/ui-list-item {:className "todo-item"}
                (sf/ui-list-content {:className "todo-content-button" :floated "right" :verticalAlign "middle"}
                  (sf/ui-button {:className "todo-button" :compact true} "Delete"))
                (sf/ui-list-content {:floated "left"}
                  (sf/ui-checkbox {:className "todo-checkbox"}))
                (sf/ui-list-content {:verticalAlign "middle"} "Take out the Trash"))
              (sf/ui-list-item {}
                (sf/ui-image {:avatar true :src "/images/lena.png"})
                (sf/ui-list-content {:verticalAlign "middle"} "Paint the shed")))))))))

(def ui-todo-pane (comp/factory TodoPane {:keyfn :id}))

(defsc TodoPaneButtons [this props]
  {:query         ['*]
   :ident         (fn [] [:component/id ::todo-pane])
   :route-segment ["todo-pane"]}
  (div :.ui.container
    (div :.ui.grid
      (div :.row
        (div :.sixteen.wide.mobile.four.wide.computer.column
          (dom/h2 "Projects")
          (div :.row
            (sf/ui-button {:basic true :icon true :labelPosition "left" :fluid true}
              (sf/ui-icon {:name i/home-icon})
              "Home"))
          (div :.row #js {:style #js {:marginTop "2px" :marginBottom "4px"}}
            (sf/ui-button {:basic true :icon true :labelPosition "left" :fluid true}
              (sf/ui-icon {:name i/camera-icon})
              "Work")))
        (div :.sixteen.wide.mobile.twelve.wide.computer.column
          (dom/h2 "Tasks"))))))

