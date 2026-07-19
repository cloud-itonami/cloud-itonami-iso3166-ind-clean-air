(ns clean-air.governor-test
  "Tests for `clean-air.governor/decide` -- the hold/escalate/commit
  decision the rest of this repo's rigor claim rests on. Every case
  builds its own ledgers explicitly (rather than mutating shared
  fixtures) so each test is legible on its own."
  (:require [clojure.test :refer [deftest is testing]]
            [clean-air.governor :as governor]
            [clean-air.fixtures :as fx]))

(defn- candidate
  ([] (candidate fx/complete-evidence-records [fx/clean-approval-record]))
  ([evidence-ledger approval-ledger]
   {:id fx/clean-cooking-id
    :evidence-ledger evidence-ledger
    :approval-ledger approval-ledger}))

(deftest commit-when-fully-clean
  (let [{:keys [decision reasons]} (governor/decide (candidate))]
    (is (= :commit decision))
    (is (empty? reasons))))

(deftest committed-predicate-mirrors-decide
  (is (true? (governor/committed? (candidate))))
  (is (false? (governor/committed? (candidate [] [fx/clean-approval-record])))))

(deftest hold-when-evidence-incomplete
  (let [missing-implementer (remove #(= :implementer (:evidence/type %)) fx/complete-evidence-records)
        {:keys [decision reasons]} (governor/decide (candidate missing-implementer [fx/clean-approval-record]))]
    (is (= :hold decision))
    (is (= [:evidence-incomplete-or-unverified] reasons))))

(deftest hold-when-evidence-ledger-empty
  (let [{:keys [decision]} (governor/decide (candidate [] [fx/clean-approval-record]))]
    (is (= :hold decision))))

(deftest hold-when-approval-missing
  (let [{:keys [decision reasons]} (governor/decide (candidate fx/complete-evidence-records []))]
    (is (= :hold decision))
    (is (= [:council-approval-missing-or-unverified] reasons))))

(deftest hold-when-approval-from-unrecognized-body
  (let [{:keys [decision]} (governor/decide (candidate fx/complete-evidence-records [fx/unrecognized-body-approval-record]))]
    (is (= :hold decision))))

(deftest hold-takes-priority-over-approval-when-both-fail
  (testing "evidence is checked first -- a candidate missing BOTH evidence and approval is held for evidence, not approval"
    (let [{:keys [decision reasons]} (governor/decide (candidate [] []))]
      (is (= :hold decision))
      (is (= [:evidence-incomplete-or-unverified] reasons)))))

(deftest escalate-on-narrow-vote-margin
  (let [{:keys [decision reasons]} (governor/decide (candidate fx/complete-evidence-records [fx/narrow-approval-record]))]
    (is (= :escalate decision))
    (is (= [:approval-vote-margin-narrow] reasons))))

(deftest escalate-on-single-source-evidence
  (let [{:keys [decision reasons]} (governor/decide (candidate fx/single-source-evidence-records [fx/clean-approval-record]))]
    (is (= :escalate decision))
    (is (= [:evidence-single-source] reasons))))

(deftest escalate-with-both-reasons-when-both-signals-present
  (let [{:keys [decision reasons]} (governor/decide (candidate fx/single-source-evidence-records [fx/narrow-approval-record]))]
    (is (= :escalate decision))
    (is (= #{:approval-vote-margin-narrow :evidence-single-source} (set reasons)))
    (is (= 2 (count reasons)))))

(deftest escalate-never-silently-becomes-commit
  (testing "escalate is never the same as commit even though both mean 'independently verified'"
    (is (not= :commit (:decision (governor/decide (candidate fx/single-source-evidence-records [fx/clean-approval-record])))))))

(deftest a-second-independent-approval-record-does-not-launder-a-bad-one
  (testing "one VALID record among noise is still enough to approve -- council-approved? is an EXISTS check, not requiring every record be clean"
    (let [{:keys [decision]} (governor/decide (candidate fx/complete-evidence-records
                                                          [fx/unrecognized-body-approval-record fx/clean-approval-record]))]
      (is (= :commit decision)))))
