(ns clean-air.portfolio
  "Pure, review-first portfolio selection for the India Airshed Clean Air
  Portfolio pilot (NCAP-aligned, per `blueprint.edn`). Ranks only
  candidate interventions the independent `clean-air.governor` COMMITS
  against a separately-supplied evidence ledger and approval ledger --
  never a candidate's own self-reported flags (see
  `clean-air.registry`/`clean-air.governor` docstrings for the ground-
  truth-not-self-report discipline this fixes). No network, payment,
  regulatory, or publication side effects anywhere in this namespace."
  (:require [clean-air.governor :as governor]))

(defn eligible?
  "A candidate intervention (`{:keys [id] :as candidate}`) is eligible
  only if the independent `clean-air.governor` COMMITS its `:id` against
  `evidence-ledger`/`approval-ledger` -- the candidate map itself carries
  no `:evidence`/`:approval` flags this function trusts."
  [evidence-ledger approval-ledger {:keys [id]}]
  (governor/committed? {:id id :evidence-ledger evidence-ledger :approval-ledger approval-ledger}))

(defn priority-score
  "Transparent score; all inputs are 0..1, weights sum to 1.0.
  Exposure-reduction and equity are weighted equally (0.35 each) ahead
  of feasibility and confidence (0.15 each) -- reflects NCAP's own dual
  mandate of PM-exposure-reduction impact (CPCB/WHO-referenced, see
  `clean-air.regulatory`) and equitable transition, per this repo's own
  `:itonami.blueprint/social-impact`."
  [{:keys [exposure-reduction equity feasibility confidence]
    :or {exposure-reduction 0 equity 0 feasibility 0 confidence 0}}]
  (+ (* 0.35 exposure-reduction)
     (* 0.35 equity)
     (* 0.15 feasibility)
     (* 0.15 confidence)))

(defn select-portfolio
  "Filters `interventions` to those the governor COMMITS against
  `evidence-ledger`/`approval-ledger`, attaches each one's
  `:priority-score`, and ranks highest-score-first (ties broken by
  `:id` for a deterministic order)."
  [interventions evidence-ledger approval-ledger]
  (->> interventions
       (filter (partial eligible? evidence-ledger approval-ledger))
       (map #(assoc % :priority-score (priority-score %)))
       (sort-by (juxt (comp - :priority-score) :id))
       vec))

(defn decisions
  "The FULL governance decision (`:commit`/`:hold`/`:escalate` +
  `:reasons`) for every candidate in `interventions`, independent of
  whether it makes the final ranked portfolio -- lets a human council
  member see WHY a candidate was held or escalated, not just that it
  was excluded from `select-portfolio`'s output."
  [interventions evidence-ledger approval-ledger]
  (mapv (fn [{:keys [id]}]
          (assoc (governor/decide {:id id :evidence-ledger evidence-ledger :approval-ledger approval-ledger})
                 :id id))
        interventions))
