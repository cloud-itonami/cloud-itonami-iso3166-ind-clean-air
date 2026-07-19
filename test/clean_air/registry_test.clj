(ns clean-air.registry-test
  "Tests for `clean-air.registry`'s independent ledger verification --
  the core of this repo's governance hardening: a candidate's own
  self-reported flags must never be enough, only an independently
  populated, closed-authority-backed ledger record counts."
  (:require [clojure.test :refer [deftest is testing]]
            [clean-air.registry :as registry]
            [clean-air.fixtures :as fx]))

;; ----------------------------- valid-evidence-record? -----------------------------

(deftest valid-evidence-record-accepts-well-formed-records
  (doseq [r fx/complete-evidence-records]
    (is (true? (registry/valid-evidence-record? r)) (str "should be valid: " r))))

(deftest valid-evidence-record-rejects-unrecognized-authority
  (is (false? (registry/valid-evidence-record?
               (assoc (first fx/complete-evidence-records)
                      :evidence/submitted-by :random-blogger)))))

(deftest valid-evidence-record-rejects-blank-citation
  (is (false? (registry/valid-evidence-record?
               (assoc (first fx/complete-evidence-records) :evidence/citation "")))))

(deftest valid-evidence-record-rejects-missing-url
  (is (false? (registry/valid-evidence-record?
               (dissoc (first fx/complete-evidence-records) :evidence/url)))))

(deftest valid-evidence-record-rejects-unrecognized-type
  (is (false? (registry/valid-evidence-record?
               (assoc (first fx/complete-evidence-records) :evidence/type :vibes)))))

;; ----------------------------- evidence-complete? -----------------------------

(deftest evidence-complete-true-for-full-valid-dossier
  (is (true? (registry/evidence-complete? fx/complete-evidence-records fx/clean-cooking-id))))

(deftest evidence-complete-false-when-one-required-type-missing
  (let [missing-baseline (remove #(= :baseline (:evidence/type %)) fx/complete-evidence-records)]
    (is (false? (registry/evidence-complete? missing-baseline fx/clean-cooking-id)))))

(deftest evidence-complete-false-when-a-record-is-invalid
  (let [tampered (map (fn [r] (if (= :source (:evidence/type r))
                                 (assoc r :evidence/submitted-by :random-blogger)
                                 r))
                       fx/complete-evidence-records)]
    (is (false? (registry/evidence-complete? tampered fx/clean-cooking-id))
        "an invalid record does not count even though the :type key is present")))

(deftest evidence-complete-false-for-unrelated-intervention-id
  (is (false? (registry/evidence-complete? fx/complete-evidence-records "some-other-intervention"))))

(deftest evidence-complete-ignores-candidates-own-self-report
  (testing "a candidate map's own :evidence key is simply not part of this function's signature -- it cannot influence the result"
    (is (false? (registry/evidence-complete? [] fx/clean-cooking-id))
        "an empty ledger is incomplete no matter what a candidate might separately claim")))

;; ----------------------------- majority-quorum-approval? / council-approved? -----------------------------

(deftest majority-quorum-approval-true-for-clean-record
  (is (true? (registry/majority-quorum-approval? fx/clean-approval-record))))

(deftest majority-quorum-approval-false-for-unrecognized-body
  (is (false? (registry/majority-quorum-approval? fx/unrecognized-body-approval-record))))

(deftest majority-quorum-approval-false-without-quorum
  (is (false? (registry/majority-quorum-approval? fx/no-quorum-approval-record))))

(deftest majority-quorum-approval-false-for-tied-vote
  (is (false? (registry/majority-quorum-approval? fx/tied-vote-approval-record))))

(deftest majority-quorum-approval-false-for-against-majority
  (is (false? (registry/majority-quorum-approval? fx/against-majority-approval-record))
      "against > for must never count as approval even if quorum-met? is true"))

(deftest majority-quorum-approval-true-for-narrow-but-real-majority
  (is (true? (registry/majority-quorum-approval? fx/narrow-approval-record))
      "a one-vote margin is still a real majority -- narrowness is an ESCALATE signal, not a HOLD"))

(deftest council-approved-true-with-one-valid-record-among-noise
  (is (true? (registry/council-approved? [fx/unrecognized-body-approval-record fx/clean-approval-record]
                                          fx/clean-cooking-id))))

(deftest council-approved-false-with-only-invalid-records
  (is (false? (registry/council-approved? [fx/unrecognized-body-approval-record fx/no-quorum-approval-record]
                                           fx/clean-cooking-id))))

(deftest council-approved-false-for-empty-ledger
  (is (false? (registry/council-approved? [] fx/clean-cooking-id))))

;; ----------------------------- narrow-margin? -----------------------------

(deftest narrow-margin-detection
  (is (true? (registry/narrow-margin? fx/narrow-approval-record)))
  (is (false? (registry/narrow-margin? fx/clean-approval-record))
      "a 9-1 margin is not narrow")
  (is (false? (registry/narrow-margin? fx/tied-vote-approval-record))
      "a tie is not a one-vote MAJORITY margin"))

;; ----------------------------- single-source-evidence? -----------------------------

(deftest single-source-evidence-detection
  (is (true? (registry/single-source-evidence? fx/single-source-evidence-records fx/clean-cooking-id)))
  (is (false? (registry/single-source-evidence? fx/complete-evidence-records fx/clean-cooking-id))
      "the fixture dossier has three independent submitting bodies (cpcb, spcb, ncap-prana)"))

(deftest single-source-evidence-false-for-no-valid-records
  (is (false? (registry/single-source-evidence? [] fx/clean-cooking-id))
      "zero submitters is not 'one source' -- evidence-complete? is the check that should fail here, not this one"))
