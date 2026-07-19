(ns clean-air.fixtures
  "Shared test ledger fixtures reused across this repo's test
  namespaces. SYNTHETIC/ILLUSTRATIVE example data for exercising the
  governance logic -- not a claim that any of these specific
  interventions is a real, currently-approved NCAP grant. The
  REGULATORY FACTS these fixtures cite (CPCB, NAAQS, SPCB, PRANA,
  state-level steering committees) are real and grounded in
  `clean-air.regulatory`'s own citations; the intervention ids, meeting
  ids, and household counts below are illustrative test data only.")

(def clean-cooking-id "clean-cooking-bihar-pilot-2026")

(def complete-evidence-records
  "One valid record per required evidence type, each independently
  originated by a recognized authority."
  [{:evidence/intervention-id clean-cooking-id :evidence/type :source
    :evidence/citation "CPCB National Air Quality Monitoring Programme, Patna station 2025 annual report"
    :evidence/url "https://cpcb.nic.in/" :evidence/submitted-by :cpcb}
   {:evidence/intervention-id clean-cooking-id :evidence/type :baseline
    :evidence/citation "Bihar SPCB baseline PM2.5 annual mean 2024: 95 ug/m3 (exceeds CPCB NAAQS annual limit of 40 ug/m3)"
    :evidence/url "https://cpcb.nic.in/air-quality-standard/" :evidence/submitted-by :state-pollution-control-board}
   {:evidence/intervention-id clean-cooking-id :evidence/type :beneficiary
    :evidence/citation "NCAP Patna City Action Plan household enumeration: 45,000 households in non-attainment wards"
    :evidence/url "https://prana.cpcb.gov.in/" :evidence/submitted-by :ncap-prana}
   {:evidence/intervention-id clean-cooking-id :evidence/type :implementer
    :evidence/citation "Implementer registration on file with Bihar SPCB"
    :evidence/url "https://prana.cpcb.gov.in/" :evidence/submitted-by :state-pollution-control-board}])

(def clean-approval-record
  "A comfortable-majority, quorum-met approval by a recognized body."
  {:approval/intervention-id clean-cooking-id
   :approval/body :state-level-steering-committee
   :approval/meeting-id "Bihar-NCAP-SLSC-2026-Q1"
   :approval/vote {:for 9 :against 1}
   :approval/quorum-met? true
   :approval/decided-at "2026-03-15"})

(def narrow-approval-record
  "Same body/quorum, but decided by a single ballot."
  (assoc clean-approval-record :approval/vote {:for 6 :against 5}))

(def unrecognized-body-approval-record
  "A body outside `clean-air.regulatory/recognized-approval-bodies`."
  (assoc clean-approval-record :approval/body :self-appointed-panel))

(def no-quorum-approval-record
  (assoc clean-approval-record :approval/quorum-met? false))

(def tied-vote-approval-record
  (assoc clean-approval-record :approval/vote {:for 5 :against 5}))

(def against-majority-approval-record
  (assoc clean-approval-record :approval/vote {:for 3 :against 7}))

(def single-source-evidence-records
  "All four required types present, but every record from the SAME body
  (no independent corroboration)."
  (mapv #(assoc % :evidence/submitted-by :state-pollution-control-board)
        complete-evidence-records))

(def clean-candidate
  {:id clean-cooking-id
   :exposure-reduction 0.7 :equity 0.9 :feasibility 0.8 :confidence 0.8})
