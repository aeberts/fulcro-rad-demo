(ns development
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.repl :refer [doc source]]
   [clojure.tools.namespace.repl :as tools-ns :refer [disable-reload! refresh clear set-refresh-dirs]]
   [com.example.components.datomic :refer [datomic-connections]]
   [com.example.components.ring-middleware]
   [com.example.components.server]
   [com.example.components.parser :as parser]
   [com.wsscode.pathom.connect :as pc]
   [com.example.model.seed :as seed]
   [com.example.model.account :as account]
   [com.example.model.address :as address]
   [com.example.model.todo :as todo]
   [com.example.model.project :as project]
   [com.fulcrologic.rad.ids :refer [new-uuid]]
   [com.fulcrologic.rad.database-adapters.datomic :as datomic]
   [com.fulcrologic.rad.resolvers :as res]
   [mount.core :as mount]
   [taoensso.timbre :as log]
   [datomic.api :as d]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.type-support.date-time :as dt]
   [com.fulcrologic.rad.attributes-options :as ao]))

(set-refresh-dirs "src/main" "src/datomic" "src/dev" "src/shared")

(defn seed! []
  (dt/set-timezone! "America/Los_Angeles")
  (let [connection (:main datomic-connections)
        date-1 (dt/html-datetime-string->inst "2020-01-01T12:00")
        date-2 (dt/html-datetime-string->inst "2020-01-05T12:00")
        date-3 (dt/html-datetime-string->inst "2020-02-01T12:00")
        date-4 (dt/html-datetime-string->inst "2020-03-10T12:00")
        date-5 (dt/html-datetime-string->inst "2020-03-21T12:00")]
    (when connection
      (log/info "SEEDING data.")
      @(d/transact connection [(seed/new-address (new-uuid 1) "111 Main St.")
                               (seed/new-account (new-uuid 100) "Tony" "tony@example.com" "letmein"
                                                 :account/addresses ["111 Main St."]
                                                 :account/primary-address (seed/new-address (new-uuid 300) "222 Other")
                                                 :time-zone/zone-id :time-zone.zone-id/America-Los_Angeles)
                               (seed/new-account (new-uuid 101) "Sam" "sam@example.com" "letmein")
                               (seed/new-account (new-uuid 102) "Sally" "sally@example.com" "letmein")
                               (seed/new-account (new-uuid 103) "Barbara" "barb@example.com" "letmein")
                               (seed/new-category (new-uuid 1000) "Tools")
                               (seed/new-category (new-uuid 1002) "Toys")
                               (seed/new-category (new-uuid 1003) "Misc")
                               (seed/new-item (new-uuid 200) "Widget" 33.99
                                              :item/category "Misc")
                               (seed/new-item (new-uuid 201) "Screwdriver" 4.99
                                              :item/category "Tools")
                               (seed/new-item (new-uuid 202) "Wrench" 14.99
                                              :item/category "Tools")
                               (seed/new-item (new-uuid 203) "Hammer" 14.99
                                              :item/category "Tools")
                               (seed/new-item (new-uuid 204) "Doll" 4.99
                                              :item/category "Toys")
                               (seed/new-item (new-uuid 205) "Robot" 94.99
                                              :item/category "Toys")
                               (seed/new-item (new-uuid 206) "Building Blocks" 24.99
                                              :item/category "Toys")
                               (seed/new-invoice "invoice-1" date-1 "Tony"
                                                 [(seed/new-line-item "Doll" 1 5.0M)
                                                  (seed/new-line-item "Hammer" 1 14.99M)])
                               (seed/new-invoice "invoice-2" date-2 "Sally"
                                                 [(seed/new-line-item "Wrench" 1 12.50M)
                                                  (seed/new-line-item "Widget" 2 32.0M)])
                               (seed/new-invoice "invoice-3" date-3 "Sam"
                                                 [(seed/new-line-item "Wrench" 2 12.50M)
                                                  (seed/new-line-item "Hammer" 2 12.50M)])
                               (seed/new-invoice "invoice-4" date-4 "Sally"
                                                 [(seed/new-line-item "Robot" 6 89.99M)])
                               (seed/new-invoice "invoice-5" date-5 "Barbara"
                                                 [(seed/new-line-item "Building Blocks" 10 20.0M)])
                               (seed/new-todo (new-uuid 300) "Buy Milk" :todo.status/not-started)
                               (seed/new-todo (new-uuid 301) "Learn Fulcro" :todo.status/not-started)
                               (seed/new-todo (new-uuid 302) "Master Clojure" :todo.status/not-started)
                               (seed/new-project (new-uuid 400) "Clojure"
                                                 [(seed/new-todo (new-uuid 303) "Learn Pathom" :todo.status/in-progress)
                                                  (seed/new-todo (new-uuid 304) "Write Fulcro App" :todo.status/in-progress)])
                               (seed/new-project (new-uuid 401) "Home"
                                                 [(seed/new-todo (new-uuid 305) "Take out garbage" :todo.status/not-started)])
                               ]))))

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  (seed!)
  :ok)

(defn stop
  "Stop the server."
  []
  (mount/stop))

(def go start)

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))

(def reset #'restart)

(defn get-fake-env
  "Returns a 'fake' env with the datomic datasource at the expected location to
  manually run queries against the parser.

  E.g: (parser/parser (get-fake-env) [:your/query :goes/here])
  "
  []
  {::datomic/databases {:production (:main datomic-connections)}})

(defn mparse
  "Utility function to manually execute a query with a fake datomic connection"
  [query]
  (parser/parser (get-fake-env) query))

(defn mresolve
  "Manually execute a resolver with 'fake' pathom environment"
  [resolver]
  ((:com.wsscode.pathom.connect/resolve resolver)
   {::datomic/databases {:production (atom (d/db (:main datomic-connections)))}}
   {}))

(defn transact-item
  "Transact an item to the DB for development and testing"
  [item]
  (let [connection (:main datomic-connections)]
    @(d/transact connection
                 [item])))

(comment

 ;(mresolve project/app-env)

 (def datomic-env
   {::datomic/databases
    {:production (atom (d/db (:main datomic-connections)))}})

 (def denv
   {::datomic/databases
    {:production (:main datomic-connections)}})

 ((:com.wsscode.pathom.connect/resolve project/all-projects)
  development/datomic-env {})

 ((:com.wsscode.pathom.connect/resolve project/todo-labels)
  development/datomic-env {})

 ((:com.wsscode.pathom.connect/resolve project/project-by-id)
  development/datomic-env {})

 (-> (mparse [{[:project/id #uuid"ffffffff-ffff-ffff-ffff-000000000400"] [{:project/project-todos [:todo/label]}]}])
     (get [:project/id #uuid"ffffffff-ffff-ffff-ffff-000000000400"]))

 ;;((:com.wsscode.pathom.connect/resolve project/app-env)
 ;; development/datomic-env {})

 (go)
 (restart)

 (let [db (d/db (:main datomic-connections))]
   (d/pull db '[*] [:account/id (new-uuid 100)]))

 (parser/parser (get-fake-env) [:project/all-projects])

 ;; example from pathom docs https://blog.wsscode.com/pathom/v2/pathom/2.2.0/connect/resolvers.html
 ;; [{[:person/id 42] [:person/first-name]}]

 (def tid1 [{[:todo/id #uuid"ffffffff-ffff-ffff-ffff-000000000303"] [:todo/label]}] )
 (def pid1 [{[:project/id #uuid"c1886414-00de-4a35-bf2d-b4ff21286886"] [:project/label]}])
 (def pid2 [{[:project/id #uuid"7086f20c-3e61-46bd-ad99-fea82f67fe8b"] [:project/label {:project/project-todos [:todo/id :todo/label :todo/status]}]}])

 (mparse tid1)
 (mparse [{:project/all-projects [:project/label {:project/project-todos [:todo/id :todo/label]}]}])
 (mparse [{[:project/id #uuid"c1886414-00de-4a35-bf2d-b4ff21286886"] [{:project/project-todos [:todo/id :todo/label :todo/status]}]}])

 (def np1 (seed/new-project (new-uuid 401) "New"
                     [(seed/new-todo (new-uuid 505) "something" :todo.status/in-progress)
                      (seed/new-todo (new-uuid 503) "Another" :todo.status/in-progress)]))

 (def np2 (seed/new-project (new-uuid 401) "Home"
                                    [(seed/new-todo (new-uuid 305) "Take out garbage" :todo.status/not-started)]))

 (transact-item np1)

 (map transact-item [np1 np2])

 )