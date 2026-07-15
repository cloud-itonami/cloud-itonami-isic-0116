(ns fibreops.sim
  "Simple simulation/demo runner for the Fibre-Crop-Growing Operations
  Coordinator actor. Used to validate that the actor flow compiles and
  basic proposal flow works. Mirrors `cerealops.sim`
  (cloud-itonami-isic-0111)."
  (:require [fibreops.operation :as operation]
            [fibreops.store :as store]))

(defn demo
  "Run a simple demo scenario: register a field, propose a field-record
  log, and check the disposition flow."
  []
  (let [;; Create store with a registered field
        st (store/mem-store
            {:initial-fields
             {"field-001"
              {:id "field-001"
               :name "Test Farm North Field"
               :crop "cotton"}}})

        ;; Build actor
        actor (operation/build st)

        ;; Create a request to log a field record
        request {:op :log-field-record
                 :field-id "field-001"
                 :acreage 80
                 :crop "cotton"
                 :quality-grade "grade-a"
                 :record-type "harvest"}

        ;; Context with phase 0 (simulation)
        context {:actor-id "fibre-ops-01"
                 :role :farm-operator
                 :phase :phase-0}]

    (println "=== Fibre-Crop-Growing Operations Coordinator Demo ===")
    (println "Demo field: field-001")
    (println "Request: log-field-record")
    (println "Phase: phase-0 (simulation)")
    (println "Expected: escalate (phase-0 forces human review of all commits)")
    (println)
    (let [result (actor request context)]
      (println "Result disposition:" (:disposition result))
      result)))

(defn -main
  "clojure -M:run entrypoint."
  [& _args]
  (demo))

(comment
  ;; In a real REPL:
  (demo)
)
