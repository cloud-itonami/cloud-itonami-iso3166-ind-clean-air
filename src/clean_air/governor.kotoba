(ns clean-air.governor
  "The India Airshed Clean Air Portfolio Governor -- the independent
  decision layer `clean-air.portfolio` consults before a candidate
  intervention may enter the funded, ranked portfolio.

  Why this is a decision LAYER and not a langgraph-clj StateGraph actor:
  most cloud-itonami actors (e.g. `hygaccess.governor`,
  `marketentry.governor`) exist to contain an LLM ADVISOR that proposes
  actions -- the Governor's job there is to independently re-derive
  ground truth because the advisor's own self-report cannot be trusted.
  This repo has no advisor: there is no LLM proposing which air-quality
  interventions to fund, and this blueprint's own
  `:itonami.blueprint/non-goals` explicitly excludes
  `:autonomous-enforcement` and `:autonomous-subsidy-disbursement` --
  candidate interventions arrive from a human process (a city/state NCAP
  action-plan pipeline), not from this actor. Standing up the full
  Advisor |- Governor StateGraph + phase + audit-ledger-store machinery
  other actors need would be a bigger pattern than this domain requires.

  What this domain DOES require -- and what was missing from this
  repo's original pure-function shape -- is INDEPENDENT verification:
  a candidate must not become part of the funded portfolio just because
  it claims to be evidence-complete and council-approved. `decide`
  re-derives both claims from `clean-air.registry`'s ledger-backed
  checks (never from the candidate's own flags) and returns one of
  three outcomes, matching the fleet's own HARD/SOFT vocabulary:

  - `:hold`     -- HARD. Evidence incomplete/unverified, or council
                   approval missing/unverified. Never overridden by a
                   human simply re-asserting the flag; the underlying
                   ledger record must actually be added.
  - `:escalate` -- SOFT. Independently verified, but carries a
                   high-stakes signal (a one-vote approval margin, or a
                   single-source evidence dossier) that should get a
                   human council member's eyes before committing public
                   funds.
  - `:commit`   -- Independently verified, no escalation signal. Eligible
                   to enter the ranked portfolio.

  Pure functions over pure, caller-supplied ledgers; no I/O, no
  mutation, no network."
  (:require [clean-air.registry :as registry]))

(defn decide
  "candidate: `{:keys [id evidence-ledger approval-ledger]}`. Returns
  `{:decision :commit|:hold|:escalate :reasons [...]}` -- `:reasons` is
  always present (empty for a clean `:commit`) so a caller can surface
  WHY a candidate was held or escalated, not just that it was excluded."
  [{:keys [id evidence-ledger approval-ledger]}]
  (let [evidence-ok? (registry/evidence-complete? evidence-ledger id)
        approval-ok? (registry/council-approved? approval-ledger id)]
    (cond
      (not evidence-ok?)
      {:decision :hold :reasons [:evidence-incomplete-or-unverified]}

      (not approval-ok?)
      {:decision :hold :reasons [:council-approval-missing-or-unverified]}

      :else
      (let [approvals (registry/approval-records-for approval-ledger id)
            narrow?    (boolean (some registry/narrow-margin? approvals))
            single-source? (registry/single-source-evidence? evidence-ledger id)
            reasons    (cond-> []
                         narrow? (conj :approval-vote-margin-narrow)
                         single-source? (conj :evidence-single-source))]
        (if (seq reasons)
          {:decision :escalate :reasons reasons}
          {:decision :commit :reasons []})))))

(defn committed?
  "Convenience predicate: did `decide` return `:commit` for this
  candidate? `clean-air.portfolio/eligible?` uses this directly."
  [candidate]
  (= :commit (:decision (decide candidate))))
