(ns clean-air.regulatory
  "Real, citation-grounded India air-quality regulatory facts this
  actor's governance layer (`clean-air.registry`/`clean-air.governor`)
  treats as ground truth: CPCB National Ambient Air Quality Standards
  (NAAQS), WHO 2021 Global Air Quality Guidelines interim targets, and
  National Clean Air Programme (NCAP) institutional facts, plus the
  closed sets of authorities this repo recognizes as able to originate
  an evidence citation or a council-approval record.

  Every fact below cites the source actually fetched and read (2026-07,
  see each var's docstring) -- never fabricated, per this workspace's
  standing rule against inventing domain facts. Numbers that could not
  be independently confirmed are simply not included here rather than
  guessed.

  Pure data plus small pure predicates only -- no I/O, no network, no
  mutation. This namespace does NOT itself decide anything; it is the
  citation layer `clean-air.registry`'s closed-set checks and this
  repo's docs both read from, so a regulatory number is defined in
  exactly one place.")

;; ----------------------------- CPCB NAAQS -----------------------------

(def naaqs-2009
  "CPCB National Ambient Air Quality Standards (NAAQS), notified in the
  Gazette of India on 18 November 2009 -- the current, still-in-force
  national standard (no revision since). Same limit for
  industrial/residential/rural AND ecologically-sensitive areas for
  PM10/PM2.5 (unlike SO2/NO2, which have a stricter ecologically-
  sensitive-area limit not modeled here since this actor's own scope is
  PM-focused, per `:itonami.blueprint/domain`).

  Units: ug-m3 = micrograms per cubic meter, mg-m3 = milligrams per
  cubic meter, ng-m3 = nanograms per cubic meter.

  Source: https://cpcb.nic.in/air-quality-standard/ (CPCB official
  standards page; cpcb.nic.in was unreachable at fetch time so this was
  read via the mirror
  https://www.apsed.in/post/naaqs-india-cpcb-ambient-air-quality-standards-all-pollutants,
  cross-checked against the NAAQS 2009 PDF mirrored at
  https://scclmines.com/env/docs/naaqs-2009.pdf and
  https://hppcb.nic.in/standards/aqs.pdf -- all three agree on the
  PM2.5/PM10 figures below)."
  {:pm2.5 {:annual-ug-m3 40 :24hr-ug-m3 60}
   :pm10  {:annual-ug-m3 60 :24hr-ug-m3 100}
   :so2   {:annual-ug-m3 50 :24hr-ug-m3 80}
   :no2   {:annual-ug-m3 40 :24hr-ug-m3 80}
   :co    {:8hr-mg-m3 2 :1hr-mg-m3 4}
   :o3    {:8hr-ug-m3 100 :1hr-ug-m3 180}
   :nh3   {:annual-ug-m3 100 :24hr-ug-m3 400}
   :pb    {:annual-ug-m3 0.5 :24hr-ug-m3 1}})

;; ----------------------------- WHO 2021 AQG -----------------------------

(def who-2021-aqg
  "WHO Global Air Quality Guidelines, 2021 revision -- the annual-mean
  Air Quality Guideline (AQG) level plus the four stepwise Interim
  Targets (IT-1 .. IT-4, loosest to tightest) WHO defines for countries
  that significantly exceed the AQG. The 2021 revision LOWERED the
  PM2.5 annual AQG from 10 to 5 ug/m3 and the PM10 annual AQG from 20 to
  15 ug/m3 versus the prior 2005 guideline.

  `:interim-targets-annual-ug-m3` is ordered IT-1 -> IT-4 (35, 25, 15,
  10 for PM2.5): meeting IT-4 is the same numeric level as the OLD 2005
  AQG and WHO estimates meeting it would cut PM2.5-attributed deaths by
  roughly 48% relative to no reduction.

  Source: https://www.who.int/news-room/questions-and-answers/item/who-global-air-quality-guidelines
  (WHO Q&A on the 2021 Global Air Quality Guidelines)."
  {:pm2.5 {:aqg-annual-ug-m3 5 :interim-targets-annual-ug-m3 [35 25 15 10]}
   :pm10  {:aqg-annual-ug-m3 15}})

;; ----------------------------- NCAP institutional facts -----------------------------

(def ncap-facts
  "National Clean Air Programme (NCAP) institutional facts. NCAP is
  India's national air-quality-improvement program, launched by the
  Ministry of Environment, Forest and Climate Change (MoEF&CC) in
  January 2019, covering non-attainment and million-plus
  cities/urban-agglomerations (cities that persistently exceed NAAQS).

  `:revised-target` reflects the target revision reported in 2026
  progress coverage: the original 20-30% PM10-reduction-by-2024-25
  target (vs a 2017-18 baseline) was revised upward to up to 40%
  reduction OR meeting the PM10 NAAQS (60 ug/m3), by FY2025-26.

  `:progress-2026` is from the same 2026 progress report: most covered
  cities have NOT yet met either target, and a large majority of
  monitored cities nationwide still exceed the PM10 NAAQS -- i.e. this
  is a program with real, documented, ongoing gaps, not a solved
  problem; a portfolio-selection tool for it should not assume success.

  Sources:
  - https://energyandcleanair.org/publication/2026-progress-report-on-national-clean-air-programme/
    (Centre for Research on Energy and Clean Air, 'Tracing the Hazy Air
    2026' NCAP progress report -- launch date, target revision, funding
    total, and 2026 progress figures)
  - https://prana.cpcb.gov.in/ (PRANA -- Portal for Regulation of
    Air-pollution in Non-Attainment cities -- CPCB's own NCAP
    implementation-tracking portal; also the source for the ~130-132
    covered-cities figure and the City Action Plan funding/utilization
    mechanics)"
  {:launched "2019-01"
   :ministry :moefcc
   :covered-cities-approx 130
   :covered-states-uts 24
   :original-target {:pollutant :pm10 :reduction-pct-range [20 30]
                      :by-fy "2024-25" :baseline-fy "2017-18"}
   :revised-target {:pollutant :pm10 :reduction-pct 40
                     :or-meets-naaqs-ug-m3 60 :by-fy "2025-26"}
   :funding-sources [:ncap :xv-finance-commission]
   :tracking-portal "https://prana.cpcb.gov.in/"
   :city-action-plan-review-categories
   ;; the categories NCAP steering-committee meetings actually review
   ;; per city action plan, per the CREA 2026 report
   [:dust-control :vehicular-and-ev-charging :public-transport
    :waste-management :urban-greening]
   :progress-2026 {:cities-met-original-target 51
                    :cities-met-revised-target 23
                    :cities-total-covered 130
                    :cities-still-exceeding-pm10-naaqs 190
                    :cities-monitored-nationwide 229
                    :source "CREA 'Tracing the Hazy Air 2026' NCAP progress report"}})

(def ncap-institutional-structure
  "The real, documented NCAP governance/approval chain a City Action Plan
  intervention proposal moves through: a city-level review committee
  (chaired by the Municipal Commissioner), a district-level committee
  (chaired by the District Collector/Magistrate), a state-level Steering
  Committee (chaired by the state Chief Secretary) that reviews and
  approves state/city action-plan matters, and national oversight by an
  MoEF&CC-Secretary-chaired NCAP steering committee with the CPCB
  Chairman signing off on City Action Plans themselves. `clean-air.
  registry/recognized-approval-bodies` models this chain as the closed
  set of bodies whose sign-off this repo will accept as council
  approval.

  Source: search-aggregated from NCAP governance coverage, cross-checked
  across https://garhwalpost.in/4th-state-level-steering-committee-meeting-of-ncap-chaired-by-chief-secretary-radha-raturi/
  (a reported State Level Steering Committee meeting chaired by a state
  Chief Secretary) and https://prana.cpcb.gov.in/assets/pdf/Resources/OM_and_Funding_Guidelines.pdf
  (CPCB's own NCAP fund release/utilization guidelines, which describe
  the City Action Plan approval chain)."
  {:city-level {:body :city-level-review-committee :chair "Municipal Commissioner"}
   :district-level {:body :district-level-committee :chair "District Collector/Magistrate"}
   :state-level {:body :state-level-steering-committee :chair "State Chief Secretary"}
   :national-level {:body :moefcc-ncap-steering-committee :chair "MoEF&CC Secretary"
                     :sign-off :cpcb-chairman-approval}})

;; ----------------------------- recognized closed sets -----------------------------

(def recognized-evidence-authorities
  "Closed set of bodies this repo recognizes as able to ORIGINATE an
  evidence citation for an intervention dossier -- ground truth, not
  self-report (the same 'ground truth, not self-report' discipline
  every sibling cloud-itonami actor's own registry establishes). An
  evidence record whose `:evidence/submitted-by` is outside this set
  fails independent verification even if every required evidence
  `:type` is nominally present -- see `clean-air.registry/evidence-complete?`.

  - `:cpcb` -- Central Pollution Control Board, the national regulator
    that sets NAAQS and runs the National Air Quality Monitoring
    Programme.
  - `:state-pollution-control-board` -- the state-level counterpart
    (e.g. Delhi Pollution Control Committee, Bihar SPCB) that operates
    local monitoring stations and administers City Action Plans.
  - `:moefcc` -- Ministry of Environment, Forest and Climate Change,
    NCAP's owning ministry.
  - `:ncap-prana` -- the PRANA portal's own recorded City Action Plan
    data (physical/financial implementation status).
  - `:caqm` -- Commission for Air Quality Management in NCR and
    Adjoining Areas, the statutory body (2021 Act) for the Delhi-NCR
    region specifically.
  - `:who` -- World Health Organization (guideline/epidemiological
    evidence, not a domestic regulator).
  - `:peer-reviewed-study` / `:academic-institution` -- independent
    scientific literature or a university/research-institute study."
  #{:cpcb :state-pollution-control-board :moefcc :ncap-prana :caqm
    :who :peer-reviewed-study :academic-institution})

(def recognized-approval-bodies
  "Closed set of bodies this repo recognizes as able to grant council
  approval for a candidate intervention, mirroring the real NCAP
  institutional chain in `ncap-institutional-structure` above. An
  approval record whose `:approval/body` is outside this set fails
  independent verification even if it claims quorum and a majority
  vote -- see `clean-air.registry/council-approved?`."
  #{:city-level-review-committee :district-level-committee
    :state-level-steering-committee :moefcc-ncap-steering-committee
    :cpcb-chairman-approval})

;; ----------------------------- small pure predicates -----------------------------

(defn pm25-exceeds-naaqs-annual?
  "Does an annual-mean PM2.5 reading (ug/m3) exceed CPCB's own NAAQS
  annual limit (40 ug/m3)? Reference predicate for citing a baseline
  against the actual national standard -- not itself part of the
  eligibility gate (see `clean-air.registry` for what IS gated)."
  [pm25-annual-ug-m3]
  (> pm25-annual-ug-m3 (get-in naaqs-2009 [:pm2.5 :annual-ug-m3])))

(defn who-interim-target-tier
  "Given an annual-mean PM2.5 reading (ug/m3), returns the tightest WHO
  2021 interim target (1..4) it already meets, `:aqg` if it meets the
  AQG itself (<=5 ug/m3), or nil if it exceeds even IT-1 (35 ug/m3).
  `its` is ordered IT-1 (loosest, 35) .. IT-4 (tightest, 10); the
  tightest tier met is the largest index whose own level the reading is
  <= to. Reference/citation predicate only -- see
  `pm25-exceeds-naaqs-annual?` docstring for scope."
  [pm25-annual-ug-m3]
  (let [aqg (get-in who-2021-aqg [:pm2.5 :aqg-annual-ug-m3])
        its (get-in who-2021-aqg [:pm2.5 :interim-targets-annual-ug-m3])
        met (->> its
                 (map-indexed (fn [i level] [(inc i) level]))
                 (filter (fn [[_ level]] (<= pm25-annual-ug-m3 level))))]
    (cond
      (<= pm25-annual-ug-m3 aqg) :aqg
      (seq met) (first (apply max-key first met))
      :else nil)))
