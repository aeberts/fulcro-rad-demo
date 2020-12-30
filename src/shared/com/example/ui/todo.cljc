(ns com.example.ui.todo
  (:require
   [com.fulcrologic.rad.form :as form :refer [defsc-form]]
   [com.fulcrologic.rad.report :as report :refer [defsc-report]]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.report-options :as ro]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.example.model.todo :as todo]))

(defsc-form TodoForm [this props]
            {fo/id todo/id
             fo/attributes [todo/label todo/status]
             fo/route-prefix "todo"
             fo/title "Create Todo"
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