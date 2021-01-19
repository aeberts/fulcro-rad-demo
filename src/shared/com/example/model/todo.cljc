(ns com.example.model.todo
  (:require
   #?@(:clj
       [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
        [com.example.model.authorization :as exauth]
        [com.example.components.database-queries :as queries]]
       :cljs
       [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
   [clojure.string :as str]
   [com.example.model.timezone :as timezone]
   [com.wsscode.pathom.connect :as pc]
   #?(:clj [datomic.api :as d])
   [com.fulcrologic.rad.database-adapters.datomic :as datomic]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
   [com.fulcrologic.rad.attributes-options :as ao]
   #? (:clj [com.example.components.database-queries :as queries])
   ))

(defattr id :todo/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr label :todo/label :string
  {fo/field-label "Label"
   ;::report/field-formatter (fn [v] (str "ATTR" v))
   ao/identities  #{:todo/id}
   ao/schema      :production
   ao/required?   true})

(def todo-status
  {:todo.status/not-started "Not Started"
   :todo.status/in-progress "In Progress"
   :todo.status/completed   "Completed"
   :todo.status/archived    "Archived"})

(defattr status :todo/status :enum
  {ao/identities        #{:todo/id}
   ao/enumerated-values (set (keys todo-status))
   ao/enumerated-labels todo-status
   ao/schema            :production
   ao/required?         true})

(defattr all-todos :todo/all-todos :ref
  {ao/pc-output  [{:todo/all-todos [:todo/id]}]
   ao/target      :todo/id
   ao/pc-resolve (fn [{:keys [query-params] :as env } _]
                   #?(:clj
                      {:todo/all-todos (queries/get-all-todos env query-params)}))})

(def attributes [id label status all-todos])

(def resolvers [])

(comment

 ; ((::pc/resolve all-todos) (development/get-fake-env) nil)

 nil)

