(ns com.example.model.project
  (:require
   #?@(:clj
       [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
        [com.example.model.authorization :as exauth]
        [com.example.components.database-queries :as queries]]
       :cljs
       [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
   [clojure.string :as str]
   ;[com.example.model.timezone :as timezone]
   [com.wsscode.pathom.connect :as pc]
   #?(:clj [datomic.api :as d])
   [com.fulcrologic.rad.database-adapters.datomic :as datomic]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
   [com.fulcrologic.rad.attributes-options :as ao]
   [taoensso.timbre :as log]
   ))

(defattr id :project/id :uuid
  {ao/identity? true
   ;; NOTE: These are spelled out so we don't have to have either on classpath, which allows
   ;; independent experimentation. In a normal project you'd use ns aliasing.
   ao/schema    :production
   ;;:com.fulcrologic.rad.database-adapters.sql/table "account"
   })

(defattr label :project/label :string
  {fo/field-label "Label"
   ;::report/field-formatter (fn [v] (str "ATTR" v))
   ao/identities  #{:project/id}
   ao/schema      :production
   ao/required?   true})

(defattr project-todos :project/project-todos :ref
  {ao/target                                                       :todo/id
   :com.fulcrologic.rad.database-adapters.datomic/attribute-schema {:db/isComponent true}
   ao/cardinality                                                  :many
   ao/identities                                                   #{:project/id}
   ao/schema                                                       :production})

(defattr all-projects :project/all-projects :ref
  {ao/target     :project/id
   ;ao/pc-output  [{:project/all-projects [:project/id :project/label {:project/project-todos [:todo/label]}]}]
   ao/pc-output  [{:project/all-projects [:project/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:project/all-projects (queries/get-all-projects env query-params)}))
   })

;;#?(:clj
;;   (pc/defresolver project-details
;;     [{:keys [parser] :as env} {:project/keys [id]}]
;;     {::pc/input #{:project/id}
;;      :pc/output [:todo/id :todo/label :todo/status]}
;;     (let [result (parser env [{[:project/id id] [{:project/project-todos [:todo/id :todo/label :todo/status]}]}])]
;;       (get-in (log/spy :info result) [[:project/id id] :todo/label])))
;;   )

;;(defattr category :line-item/category :ref
;;  {ao/target      :category/id
;;   ao/pc-input    #{:line-item/id}
;;   ao/pc-output   [{:line-item/category [:category/id]}]
;;   ao/pc-resolve  (fn [env {:line-item/keys [id]}]
;;                    #?(:clj
;;                       (when-let [cid (queries/get-line-item-category env id)]
;;                         {:line-item/category {:category/id cid}})))
;;   ao/cardinality :one})

(def attributes [id label project-todos all-projects])

#?(:clj
   (def resolvers []))

(comment
 ;; EQL Testing
 ;; [{[:person/id 42] [:person/first-name]}]

 [{[:project/id ffffffff-ffff-ffff-ffff-000000000400] [:project/label]}]

 [{:project/all-projects [[:project/id "ffffffff-ffff-ffff-ffff-000000000400"] [:project/label]]}]
 )