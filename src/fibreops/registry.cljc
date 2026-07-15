(ns fibreops.registry
  "Pure validation functions for fibre-crop-growing operations. These are
  called by the Governor to independently verify proposal parameters --
  the LLM advisor's confidence is NOT sufficient to override these checks.
  Mirrors `cerealops.registry` (cloud-itonami-isic-0111) in shape, adding
  a `quality-grade-unknown?` check (this crop family's own grading-specific
  measure: a fibre-crop harvest record cites a quality-grade code, and
  that code must be one of the actor's recognized closed vocabulary --
  see `fibreops.facts/fibre-quality-grades`)."
  (:require [fibreops.facts :as facts]))

(defn cost-exceeds-threshold?
  "Independently verify a proposed spend against its category/default
  threshold. Inclusive at the boundary (exactly-at-threshold does not
  escalate)."
  [cost threshold]
  (> cost threshold))

(defn acreage-non-positive?
  "A logged planting/harvest acreage of zero or negative is not a real
  observation -- reject it as a HARD violation rather than silently
  accepting bad data into the field record."
  [acreage]
  (<= acreage 0))

(defn quality-grade-unknown?
  "A logged fibre-quality grade that isn't in the actor's recognized
  closed vocabulary (`fibreops.facts/fibre-quality-grades`) is not a
  plausible observation -- reject it as a HARD violation (mirrors
  `caneops.registry/ratoon-cycle-invalid?`: an independent structural
  plausibility check on a domain-specific field, not an agronomic
  judgment about the harvest's actual quality)."
  [grade]
  (not (contains? facts/fibre-quality-grades grade)))

(defn confidence-below-floor?
  "Independently verify a proposal's stated confidence against the
  Governor's confidence floor."
  [confidence floor]
  (< confidence floor))
