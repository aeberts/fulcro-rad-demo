(ns com.example.ui.project
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.rad.picker-options :as po]
    [com.fulcrologic.rad.form :as form :refer [defsc-form]]
    [com.fulcrologic.rad.report :as report :refer [defsc-report]]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report-options :as ro]
    [com.example.model.project :as project]
    [com.example.model.todo :as todo]
    [com.example.ui.todo :refer [TodoForm]]))

(defsc TodoQuery [_ _]
  {:query [:todo/id :todo/label :todo/status]
   :ident :todo/id})

(defsc-form ProjectForm [this props]
  {fo/id            project/id
   fo/attributes    [project/label project/project-todos]
   fo/field-styles  {:project/project-todos :pick-many}
   fo/field-options {:project/project-todos {po/query-key       :todo/all-todos
                                             po/query-component TodoQuery
                                             po/options-xform   (fn [_ options]
                                                                  (mapv
                                                                    (fn [{:todo/keys [id label status]}]
                                                                      {:text (str label ", " status) :value [:todo/id id]})
                                                                    (sort-by :todo/label options)))
                                             po/cache-time-ms   30000}}
   fo/route-prefix  "projects"
   fo/title         "Create Project"
   })

(defsc-report ProjectReport [this props]
  {ro/title            "All Projects"
   ro/route            "project"
   ro/source-attribute :project/all-projects
   ro/row-pk           project/id
   ro/columns          [project/label project/todo-labels]
   ro/form-links       {:project/label ProjectForm :project/todo-labels TodoForm}
   ro/column-headings  {:project/todo-labels "Project Todos"}
   ro/run-on-mount?    true
   })

(comment

  )