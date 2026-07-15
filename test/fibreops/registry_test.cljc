(ns fibreops.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [fibreops.registry :as registry]))

(deftest cost-exceeds-threshold-test
  (testing "Cost within threshold"
    (is (false? (registry/cost-exceeds-threshold? 400 500))))

  (testing "Cost at threshold (inclusive boundary, not exceeded)"
    (is (false? (registry/cost-exceeds-threshold? 500 500))))

  (testing "Cost exceeds threshold"
    (is (true? (registry/cost-exceeds-threshold? 600 500)))))

(deftest acreage-non-positive-test
  (testing "Positive acreage is valid"
    (is (false? (registry/acreage-non-positive? 80))))

  (testing "Zero acreage is invalid"
    (is (true? (registry/acreage-non-positive? 0))))

  (testing "Negative acreage is invalid"
    (is (true? (registry/acreage-non-positive? -5)))))

(deftest quality-grade-unknown-test
  (testing "Recognized grade codes are known"
    (is (false? (registry/quality-grade-unknown? "premium")))
    (is (false? (registry/quality-grade-unknown? "grade-a")))
    (is (false? (registry/quality-grade-unknown? "grade-b")))
    (is (false? (registry/quality-grade-unknown? "grade-c")))
    (is (false? (registry/quality-grade-unknown? "below-grade")))
    (is (false? (registry/quality-grade-unknown? "ungraded"))))

  (testing "An unrecognized grade code is unknown"
    (is (true? (registry/quality-grade-unknown? "AAA+"))))

  (testing "nil grade is unknown"
    (is (true? (registry/quality-grade-unknown? nil)))))

(deftest confidence-below-floor-test
  (testing "Confidence above floor"
    (is (false? (registry/confidence-below-floor? 0.9 0.7))))

  (testing "Confidence at floor (inclusive, not below)"
    (is (false? (registry/confidence-below-floor? 0.7 0.7))))

  (testing "Confidence below floor"
    (is (true? (registry/confidence-below-floor? 0.5 0.7)))))
