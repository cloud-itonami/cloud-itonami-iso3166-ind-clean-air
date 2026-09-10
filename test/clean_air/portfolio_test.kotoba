(ns clean-air.portfolio-test
  "Tests for `clean-air.portfolio` -- priority scoring plus the
  ledger-backed eligibility filter that replaced the original
  self-reported-flag trust (see `clean-air.registry`/`clean-air.
  governor`). `council-approval-and-evidence-are-both-required` is the
  original 3-assertion test this repo shipped with, kept verbatim below
  under its old name against the new ledger-based API so the original
  intent stays traceable; everything else is new coverage."
  (:require [clojure.test :refer [deftest is testing]]
            [clean-air.portfolio :as portfolio]
            [clean-air.fixtures :as fx]))

(def approved-evidence fx/complete-evidence-records)
(def approved-approval [fx/clean-approval-record])

;; ----------------------------- original test, ported to the ledger API -----------------------------

(deftest council-approval-and-evidence-are-both-required
  (is (= [fx/clean-cooking-id]
         (mapv :id (portfolio/select-portfolio [fx/clean-candidate] approved-evidence approved-approval))))
  (is (empty? (portfolio/select-portfolio [fx/clean-candidate] approved-evidence [])) "no approval ledger record -> excluded")
  (is (empty? (portfolio/select-portfolio [fx/clean-candidate] [] approved-approval)) "no evidence ledger records -> excluded"))

;; ----------------------------- priority-score -----------------------------

(deftest priority-score-weights-sum-to-one
  (is (= 1.0 (+ 0.35 0.35 0.15 0.15))))

(deftest priority-score-all-zero-inputs
  (is (= 0.0 (portfolio/priority-score {}))))

(deftest priority-score-all-max-inputs
  (is (= 1.0 (portfolio/priority-score {:exposure-reduction 1 :equity 1 :feasibility 1 :confidence 1}))))

(deftest priority-score-weighted-correctly
  (is (= (+ (* 0.35 0.7) (* 0.35 0.9) (* 0.15 0.8) (* 0.15 0.8))
         (portfolio/priority-score fx/clean-candidate))))

(deftest priority-score-exposure-and-equity-weighted-equally-and-higher-than-feasibility-confidence
  (is (= (portfolio/priority-score {:exposure-reduction 1})
         (portfolio/priority-score {:equity 1})))
  (is (> (portfolio/priority-score {:exposure-reduction 1})
         (portfolio/priority-score {:feasibility 1}))))

;; ----------------------------- eligible? -----------------------------

(deftest eligible-true-for-clean-candidate
  (is (true? (portfolio/eligible? approved-evidence approved-approval fx/clean-candidate))))

(deftest eligible-false-when-evidence-ledger-lacks-this-candidates-id
  (is (false? (portfolio/eligible? [] approved-approval fx/clean-candidate))))

(deftest eligible-false-for-escalated-candidate
  (testing "eligible? is strict COMMIT-only -- an escalated candidate is not eligible for automatic ranking"
    (is (false? (portfolio/eligible? fx/single-source-evidence-records approved-approval fx/clean-candidate)))))

;; ----------------------------- select-portfolio: filtering + ranking -----------------------------

(def high-priority (assoc fx/clean-candidate :id "clean-cooking-bihar-pilot-2026"
                           :exposure-reduction 0.9 :equity 0.9 :feasibility 0.9 :confidence 0.9))
(def low-priority (assoc fx/clean-candidate :id "dust-control-lowpri-2026"
                          :exposure-reduction 0.2 :equity 0.2 :feasibility 0.2 :confidence 0.2))

(defn- ledger-for [ids]
  (mapcat (fn [id] (mapv #(assoc % :evidence/intervention-id id) fx/complete-evidence-records)) ids))

(defn- approvals-for [ids]
  (mapv (fn [id] (assoc fx/clean-approval-record :approval/intervention-id id)) ids))

(deftest select-portfolio-ranks-highest-score-first
  (let [ids [(:id high-priority) (:id low-priority)]
        result (portfolio/select-portfolio [low-priority high-priority] (ledger-for ids) (approvals-for ids))]
    (is (= [(:id high-priority) (:id low-priority)] (mapv :id result)))
    (is (every? #(contains? % :priority-score) result))))

(deftest select-portfolio-excludes-held-candidates-from-ranking
  (let [ids [(:id high-priority)]
        result (portfolio/select-portfolio [low-priority high-priority] (ledger-for ids) (approvals-for ids))]
    (is (= [(:id high-priority)] (mapv :id result))
        "low-priority has no ledger record at all -> held, not merely ranked lower")))

(deftest select-portfolio-empty-interventions
  (is (= [] (portfolio/select-portfolio [] approved-evidence approved-approval))))

(deftest select-portfolio-empty-ledgers-excludes-everyone
  (is (= [] (portfolio/select-portfolio [high-priority low-priority] [] []))))

(deftest select-portfolio-ties-broken-by-id
  (let [a (assoc fx/clean-candidate :id "bbb-2026")
        b (assoc fx/clean-candidate :id "aaa-2026")
        ids [(:id a) (:id b)]
        result (portfolio/select-portfolio [a b] (ledger-for ids) (approvals-for ids))]
    (is (= ["aaa-2026" "bbb-2026"] (mapv :id result))
        "equal priority-score -> lexicographic id order")))

;; ----------------------------- decisions -----------------------------

(deftest decisions-returns-one-entry-per-candidate-regardless-of-outcome
  (let [result (portfolio/decisions [fx/clean-candidate low-priority] approved-evidence approved-approval)]
    (is (= 2 (count result)))
    (is (= #{(:id fx/clean-candidate) (:id low-priority)} (set (map :id result))))))

(deftest decisions-surfaces-hold-reason-for-excluded-candidate
  (let [result (portfolio/decisions [low-priority] approved-evidence approved-approval)
        low-decision (first (filter #(= (:id low-priority) (:id %)) result))]
    (is (= :hold (:decision low-decision)))
    (is (= [:evidence-incomplete-or-unverified] (:reasons low-decision))
        "low-priority's :id has no matching record in the shared evidence ledger")))

(deftest decisions-surfaces-commit-for-clean-candidate
  (let [result (portfolio/decisions [fx/clean-candidate] approved-evidence approved-approval)]
    (is (= :commit (:decision (first result))))
    (is (empty? (:reasons (first result))))))
