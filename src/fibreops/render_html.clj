(ns fibreops.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300,
  Wave2): this repo previously had NO demo page and no generator at all.
  This namespace drives the REAL actor stack
  (`fibreops.operation` -> `fibreops.governor` -> `fibreops.store`) through
  a scenario adapted from this repo's own domain (fibre-crop field
  records, crop-health concerns, supply orders, HARD field-not-registered
  hold) and rendered deterministically -- no invented numbers, no
  timestamps in the page content, byte-identical across reruns against
  the same seed (verify by diffing two consecutive runs).

  Styling follows the isic-9522/`applianceshop.render-html` reference:
  `jp-go-dds.skin/dds+skin` (デジタル庁デザインシステム + skin).

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [jp-go-dds.skin]
            [clojure.string :as str]
            [fibreops.store :as store]
            [fibreops.operation :as op]
            [langgraph.graph :as g]))

(def ^:private operator
  {:actor-id "fibre-ops-01" :role :farm-operator :phase :phase-3})

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "fibre-ops-01"}}
          {:thread-id tid :resume? true}))

(defn- reject! [actor tid]
  (g/run* actor {:approval {:status :rejected :by "fibre-ops-01"}}
          {:thread-id tid :resume? true}))

(defn run-demo!
  "Runs a freshly seeded store through a scenario mixing every disposition
  this actor can reach: field-001 logs a clean fibre-quality field record
  (phase-3 auto-commit), a crop-health concern (ALWAYS escalates -- farmer
  approves), an over-cost-threshold seed supply order (escalates -- farmer
  REJECTS -> hold), and a log-field-record against an UNREGISTERED field
  (field-999) which HARD-holds before any human. Every id/op/value is from
  fibreops.sim / fibreops.governor / fibreops.store -- no invented values.
  Returns the resulting store -- every field read by `render` below is real
  governor/store output, not a hand-typed copy."
  []
  (let [db (store/mem-store
            {:initial-fields
             {"field-001" {:id "field-001"
                           :name "Test Farm North Field"
                           :crop "cotton"}}})
        actor (op/build db)]
    (exec! actor "t1" {:op :log-field-record :field-id "field-001"
                       :acreage 80 :crop "cotton" :quality-grade "grade-a"
                       :record-type "harvest"})
    (exec! actor "t2" {:op :flag-crop-health-concern :field-id "field-001"
                       :concern "boll-weevil-suspected"})
    (approve! actor "t2")
    (exec! actor "t3" {:op :order-supplies :field-id "field-001"
                       :category "seed" :cost 900})
    (reject! actor "t3")
    (exec! actor "t4" {:op :log-field-record :field-id "field-999"
                       :acreage 50 :crop "jute" :record-type "planting"})
    db))

;; ----------------------------- rendering -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- last-fact-for [ledger field-id]
  (last (filter #(= (:subject %) field-id) ledger)))

(defn- status-cell [ledger field-id]
  (let [f (last-fact-for ledger field-id)]
    (cond
      (nil? f) "<span class=\"muted\">no activity</span>"
      (= :committed (:t f)) "<span class=\"ok\">committed</span>"
      (= :approval-granted (:t f)) "<span class=\"ok\">approved &amp; committed</span>"
      (= :governor-hold (:t f))
      (let [rule (-> f :basis first)]
        (str "<span class=\"critical\">HARD hold &middot; " (esc (name (or rule :unknown))) "</span>"))
      (= :approval-rejected (:t f)) "<span class=\"critical\">rejected</span>"
      (= :approval-requested (:t f)) "<span class=\"warn\">awaiting approval</span>"
      :else "<span class=\"muted\">in progress</span>")))

(defn- ledger-row [{:keys [t op subject disposition basis]}]
  (format "        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td></tr>"
          (esc (name t)) (esc (name (or op :n-a))) (esc subject)
          (esc (or (some->> basis (map name) (str/join ", "))
                   (some-> disposition name) ""))))

(def ^:private action-gate-rows
  ;; Static description of this actor's own closed op contract
  ;; (README HARD invariants / always-escalate ops) -- documentation of
  ;; fixed behavior, not runtime telemetry.
  ["        <tr><td><code>:log-field-record</code></td><td><span class=\"ok\">phase-3 auto-commit when clean + registered field; HARD hold on non-positive acreage / unknown quality-grade</span></td></tr>"
   "        <tr><td><code>:schedule-field-operation</code></td><td><span class=\"ok\">phase-3 auto-commit when clean (planting/defoliation/retting/harvest logistics only)</span></td></tr>"
   "        <tr><td><code>:flag-crop-health-concern</code></td><td><span class=\"warn\">ALWAYS human approval (pest/disease/drought-stress)</span></td></tr>"
   "        <tr><td><code>:order-supplies</code></td><td><span class=\"warn\">human approval over category cost threshold</span></td></tr>"
   "        <tr><td><code>:operate-field-equipment</code> / <code>:finalize-pesticide-application</code></td><td><span class=\"critical\">HARD permanent block &middot; never escalate</span></td></tr>"])

(defn render
  "Renders the full operator-console.html document from a store `db`
  that has already run `run-demo!` (or any other real scenario)."
  [db]
  (let [ledger (vec (store/ledger db))
        field-001 (store/registered-field db "field-001")
        ledger-rows (str/join "\n" (map ledger-row ledger))]
    (str
     "<html><head><meta charset=\"utf-8\"><title>cloud-itonami-isic-0116 &middot; fibre-crop-growing</title><style>"
     (jp-go-dds.skin/dds+skin)
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Fibre-crop growing ops (ISIC 0116) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · crop-health / high-cost supply always human-approved</span>\n"
     "</header>\n"
     "<main>\n"
     "  <section class=\"card\">\n"
     "    <h2>Scenario fields</h2>\n"
     "    <p class=\"muted\">Demo snapshot — build-time-generated from <code>fibreops.store</code> via <code>fibreops.render-html</code> (<code>clojure -M:dev:render-html</code>), regenerated nightly. No invented data.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Field</th><th>Name</th><th>Crop</th><th>Last op status</th></tr></thead>\n"
     "      <tbody>\n"
     "        <tr><td>" (esc (:id field-001)) "</td><td>" (esc (:name field-001)) "</td><td>" (esc (:crop field-001))
     "</td><td>" (status-cell ledger "field-001") "</td></tr>\n"
     "        <tr><td>field-999</td><td class=\"muted\">(unregistered)</td><td class=\"muted\">—</td><td>" (status-cell ledger "field-999") "</td></tr>\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Action gate (Field Operations Governor)</h2>\n"
     "    <p class=\"muted\">HARD holds cannot be overridden. Unregistered fields, non-positive acreage, unknown fibre quality grades, direct equipment operation, and pesticide-application finalization are blocked before any human; crop-health concerns and over-threshold supply orders always escalate.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Op</th><th>Gate</th></tr></thead>\n"
     "      <tbody>\n"
     (str/join "\n" action-gate-rows) "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Audit ledger (this run)</h2>\n"
     "    <p class=\"muted\">Append-only decision-fact log — every proposal, hold and commit this scenario produced.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Fact</th><th>Op</th><th>Subject</th><th>Basis</th></tr></thead>\n"
     "      <tbody>\n"
     ledger-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "</main>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        db (run-demo!)
        html (render db)
        out-file (java.io.File. out)]
    (.. out-file getParentFile mkdirs)
    (spit out-file html)
    (println "wrote" out "(" (count (store/ledger db)) "ledger facts )")))
