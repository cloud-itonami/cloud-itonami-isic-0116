(ns fibreops.facts
  "Reference facts for fibre-crop-growing operations coordination: supply
  category cost policy, fibre-crop classification, field-operation
  vocabulary, and a closed fibre-quality-grade vocabulary. This namespace
  contains pure lookup functions for domain reference data -- the Governor
  and Advisor consult these instead of inventing thresholds. Mirrors
  `cerealops.facts` (cloud-itonami-isic-0111) in shape, adapted to fibre
  crops: a family of non-food row crops (cotton, jute, flax, hemp, sisal)
  whose harvest is graded on fibre quality (staple length / cleanliness /
  strength) rather than on a food-grain measure, and whose field-operation
  calendar includes defoliation (pre-harvest, cotton) and retting
  (post-harvest fibre/stalk separation, jute/flax/hemp) alongside the
  planting/harvest operations common to row crops.")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (farmer/ops-manager)."
  {"seed"
   {:id "seed" :name "種子" :cost-threshold 500}

   "fertilizer"
   {:id "fertilizer" :name "肥料" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def fibre-crops
  "Fibre crops this actor's field records may cover (ISIC 0116: growing of
  fibre crops -- cotton, jute, flax, hemp, sisal, and related bast/leaf
  fibre crops. Other non-perennial crops are out of scope, ISIC 0119)."
  {"cotton" {:id "cotton" :name "綿"}
   "jute"   {:id "jute" :name "ジュート（黄麻）"}
   "flax"   {:id "flax" :name "亜麻"}
   "hemp"   {:id "hemp" :name "大麻（繊維用）"}
   "sisal"  {:id "sisal" :name "サイザル麻"}})

(defn fibre-crop-by-id [id]
  (get fibre-crops id))

(def field-operation-types
  "Reference set of field-operation types this actor's
  schedule-field-operation proposals commonly cover, spanning the
  pre-harvest defoliation step used to prepare cotton for mechanical
  picking and the post-harvest retting step used to separate bast/leaf
  fibre from the plant stalk (jute/flax/hemp/sisal) before it can be
  spun. Informational only -- NOT a validated enum; the advisor/operator
  may propose other operation-type strings and the Governor does not
  reject unlisted values here."
  #{"planting" "defoliation" "retting" "harvest"})

(def fibre-quality-grades
  "Closed set of recognized fibre-quality grade codes a field record's
  :quality-grade may cite -- independently verified by the Governor.
  Fibre-quality grading spans commodity-specific systems (USDA cotton
  grade/staple-length, jute TD grades, flax scutched-line grades,
  hemp/sisal decortication grades); this is a generic closed vocabulary
  this actor's field records use to record a graded outcome, not a
  physical measurement or a substitute for the commodity-specific
  standard."
  #{"premium" "grade-a" "grade-b" "grade-c" "below-grade" "ungraded"})
