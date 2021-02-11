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
   ao/pc-output  [{:project/all-projects [:project/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:project/all-projects (queries/get-all-projects env query-params)}))})

(defattr todo-labels :project/todo-labels :string
  {ao/target     :project/id                              ;; What attr is this "part of"?
   ao/pc-input   #{:project/id}
   ao/pc-output  [:project/todo-labels]
   ao/pc-resolve (fn [{:keys [parser] :as env} {project-id :project/id}]
                   #?(:clj
                      {:project/todo-labels
                       (-> (parser env [{[:project/id project-id] [{:project/project-todos [:todo/label]}]}])
                           (get [:project/id project-id])
                           :project/project-todos
                           (->>
                            (map :todo/label)
                            (str/join ", ")))}))})

(def attributes [id label project-todos todo-labels all-projects])

#?(:clj
   (def resolvers []))

(comment
 ;; EQL Testing
 ;; [{[:person/id 42] [:person/first-name]}]

 [{[:project/id ffffffff-ffff-ffff-ffff-000000000400] [:project/label]}]

 [{:project/all-projects [[:project/id "ffffffff-ffff-ffff-ffff-000000000400"] [:project/label]]}]

 ;;(pc/defresolver all-emails
 ;;  [env _]
 ;;  {::pc/output [{:all-emails [:email]}]}
 ;;  {:all-emails (->> email-db keys (mapv #(hash-map :email %)))})

 ;; ro/row-query-inclusion [{:project/project-todos [:todo/id :todo/label]}]

 ;; (def fake-env {::datomic/databases {:production (:main datomic-connections)}})

 ;; given (pc/defresolver MyThing) or (defattr MyThing):
 ;; ((:com.wsscode.pathom.connect/resolve MyResolver) ;; or Attribute
 ;; fake-env input

 ;; (pc/resolve ProjectList (get-fake-env) {} )

 ;; Manually resolve an attribute or a resolver (attr needs to have ao/input, output, resolve)
 ;; Remember: datomic-env is an atom so you need to de-ref it each time you use it.
 ((:com.wsscode.pathom.connect/resolve all-projects)
  development/datomic-env {})

 (development/get-fake-env)

 )