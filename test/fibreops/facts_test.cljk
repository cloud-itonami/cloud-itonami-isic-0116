(ns fibreops.facts-test
  (:require [clojure.test :refer [deftest is are testing]]
            [fibreops.facts :as facts]))

(deftest supply-category-lookup
  (testing "Lookup valid supply category"
    (let [c (facts/supply-category-by-id "seed")]
      (is (= "seed" (:id c)))
      (is (= "種子" (:name c)))))

  (testing "Lookup invalid supply category"
    (is (nil? (facts/supply-category-by-id "unknown")))))

(deftest supply-category-cost-thresholds
  (testing "Category-specific cost thresholds"
    (are [id expected] (= expected (:cost-threshold (facts/supply-category-by-id id)))
      "seed"        500
      "fertilizer"  500
      "equipment"   1000)))

(deftest default-cost-threshold-value
  (testing "Default fallback threshold matches the conservative baseline"
    (is (= 500 facts/default-cost-threshold))))

(deftest fibre-crop-lookup
  (testing "Lookup valid fibre crop"
    (are [id expected-name] (= expected-name (:name (facts/fibre-crop-by-id id)))
      "cotton" "綿"
      "jute"   "ジュート（黄麻）"
      "flax"   "亜麻"
      "hemp"   "大麻（繊維用）"
      "sisal"  "サイザル麻"))

  (testing "Lookup invalid fibre crop"
    (is (nil? (facts/fibre-crop-by-id "unknown"))))

  (testing "Cereal crops are out of scope (ISIC 0111, not this actor)"
    (is (nil? (facts/fibre-crop-by-id "wheat")))))

(deftest field-operation-types-reference-set
  (testing "Fibre-crop-specific field operation types are present"
    (is (contains? facts/field-operation-types "planting"))
    (is (contains? facts/field-operation-types "defoliation"))
    (is (contains? facts/field-operation-types "retting"))
    (is (contains? facts/field-operation-types "harvest")))

  (testing "Not a validated enum -- an unlisted operation type is simply absent"
    (is (not (contains? facts/field-operation-types "ratooning")))))

(deftest fibre-quality-grades-closed-set
  (testing "Recognized fibre-quality grade codes are present"
    (is (contains? facts/fibre-quality-grades "premium"))
    (is (contains? facts/fibre-quality-grades "grade-a"))
    (is (contains? facts/fibre-quality-grades "grade-b"))
    (is (contains? facts/fibre-quality-grades "grade-c"))
    (is (contains? facts/fibre-quality-grades "below-grade"))
    (is (contains? facts/fibre-quality-grades "ungraded")))

  (testing "An unrecognized grade code is absent from the closed vocabulary"
    (is (not (contains? facts/fibre-quality-grades "AAA+")))
    (is (not (contains? facts/fibre-quality-grades "")))))
