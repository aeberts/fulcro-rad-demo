(ns com.example.model.project-todo
  (:require
   [com.fulcrologic.rad.attributes :refer [defattr]]
   [com.fulcrologic.rad.attributes-options :as ao]
   ))

(defattr id :project-todo/id :uuid
  {ao/identity? true
   ao/schema :production})

(defattr todo :project-todo/todo :ref
  {ao/target :todo/id
   ao/required? true
   ao/cardinality :one
   ao/identities #{:project-todo/id}
   ao/schema :production
   })