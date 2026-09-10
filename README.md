# cloud-itonami-iso3166-ind-clean-air: India Airshed Clean Air Portfolio

Open Business Blueprint for **evidence-gated, council-approval-gated portfolio
selection of India air-quality interventions** — a thematic (`clean-air`)
sub-repo of the `cloud-itonami-iso3166-ind` (India) family, scoped to the
National Clean Air Programme (NCAP) domain. This is a **decision-support
ranking tool**, not an actor that itself files, funds, enforces, or
disburses anything: it ranks candidate interventions a real NCAP
city/state action-plan process has already produced, filtering out any
candidate whose evidence dossier or council approval cannot be
**independently verified against a ledger** — never a candidate's own
self-reported flags.

## What this repo does

- `clean-air.portfolio/select-portfolio` — ranks candidate interventions
  by a transparent `priority-score` (exposure-reduction + equity weighted
  equally at 0.35 each, feasibility + confidence at 0.15 each), but ONLY
  among candidates the independent `clean-air.governor` **commits**.
- `clean-air.governor/decide` — for one candidate, independently
  re-derives evidence-completeness and council-approval from two
  separately-supplied ledgers (`clean-air.registry`) and returns
  `:hold` / `:escalate` / `:commit` with reasons.
- `clean-air.registry` — the ground-truth layer: a candidate's evidence
  is only "complete" if an append-only evidence ledger carries a valid
  record (real citation, real source URL, originated by a body in a
  CLOSED recognized-authority set) for every required type; a candidate
  is only "council-approved" if an append-only approval ledger carries a
  record from a CLOSED recognized-approval-body set with quorum met and
  a genuine for>against majority vote.
- `clean-air.regulatory` — citation-grounded India air-quality regulatory
  facts (CPCB NAAQS, WHO 2021 Global Air Quality Guidelines, NCAP
  institutional facts) the closed sets above are built from. See that
  namespace's own docstrings for every source URL.

## What this repo does NOT do

**This is a decision-support / ranking layer, not an execution layer** —
see `:itonami.blueprint/non-goals`:

- Does NOT autonomously enforce anything against any polluter, facility,
  or individual.
- Does NOT autonomously disburse any subsidy, grant, or payment — a
  `:commit` decision means "eligible to enter the ranked list a human
  funding body reviews," never "funds released."
- Does NOT publish personal data — evidence/approval records in this
  repo's own examples cite institutions (CPCB, SPCBs, PRANA, NCAP
  steering committees), never named individuals.
- Does NOT file anything with, or fetch anything from, any real
  government system — `clean-air.regulatory`'s NAAQS/WHO/NCAP facts are
  static, citation-grounded reference data captured at a point in time,
  not a live feed.
- Is NOT a real NCAP City Action Plan, a real CPCB determination, or a
  real council vote — every example ledger record in this repo's own
  tests (`test/clean_air/fixtures.kotoba`) is SYNTHETIC/ILLUSTRATIVE test
  data, not a claim that any specific intervention is a real,
  currently-approved grant.
- Is NOT a langgraph-clj StateGraph actor with an LLM advisor. There is
  no advisor proposing interventions in this domain — candidates arrive
  from a human NCAP action-plan process — so the full Advisor⊣Governor
  StateGraph + phase + audit-ledger-store machinery other cloud-itonami
  actors use (e.g. `cloud-itonami-hygiene-access`) would be a bigger
  pattern than this domain needs. See "Architecture" below for why the
  simpler shape still carries real governance rigor.

## Why "ledger-backed", not "self-reported flags" — the hardening this build made

The original scaffold's `eligible?` trusted a candidate map's own inline
`:evidence #{...}` and `:approval :approved-by-council` keys — nothing
stopped a caller from writing those keys directly with no real dossier or
vote behind them. This build separates "what a candidate CLAIMS" from
"what an independent ledger CONFIRMS," the same "ground truth, not
self-report" discipline every other cloud-itonami actor's own registry
establishes (e.g. `hygaccess.registry`'s equipment/batch verification):
`clean-air.portfolio/select-portfolio` takes the candidate list AND two
separately-sourced ledgers as distinct arguments, and a candidate's own
map carries no evidence/approval fields at all anymore — only scoring
inputs (`:exposure-reduction`/`:equity`/`:feasibility`/`:confidence`).

## Architecture

Not a langgraph-clj StateGraph actor (see "What this repo does NOT do"
above) — a three-namespace pure decision LAYER:

1. **`clean-air.regulatory`** — citation-grounded domain facts (CPCB
   NAAQS 2009, WHO 2021 AQG, NCAP institutional facts) plus the closed
   `recognized-evidence-authorities` / `recognized-approval-bodies` sets,
   the latter modeled on NCAP's real approval chain: city-level review
   committee (Municipal Commissioner) → district-level committee
   (District Collector/Magistrate) → state-level Steering Committee
   (state Chief Secretary) → national MoEF&CC-Secretary-chaired NCAP
   steering committee with CPCB Chairman sign-off on City Action Plans.
2. **`clean-air.registry`** — independent ledger verification:
   `evidence-complete?`/`council-approved?` look up a caller-supplied
   evidence-ledger/approval-ledger (plain vectors of append-only record
   maps) by intervention id, never a candidate's own flags. Also
   `narrow-margin?` (a one-vote approval margin — SOFT escalate signal)
   and `single-source-evidence?` (every evidence record from the same
   single originating body — SOFT escalate signal, no independent
   corroboration).
3. **`clean-air.governor`** — `decide` composes the registry checks into
   `:hold` (HARD — evidence or approval independently fails) /
   `:escalate` (SOFT — verified, but a narrow-margin or single-source
   signal present) / `:commit` (independently clean).
4. **`clean-air.portfolio`** — `eligible?`/`select-portfolio` consume the
   governor's `:commit` decisions only; `priority-score` is unchanged
   pure ranking math; `decisions` surfaces every candidate's full
   governance outcome (including holds/escalations) for a human council
   member to review, not just the final ranked list.

## Regulatory grounding

Every number `clean-air.regulatory` carries cites a source actually
fetched and read (2026-07) — see that namespace's own docstrings for the
full citation on each var:

- **CPCB NAAQS (notified 2009)**: PM2.5 annual 40 µg/m³ / 24-hr 60 µg/m³;
  PM10 annual 60 µg/m³ / 24-hr 100 µg/m³ (plus SO2/NO2/CO/O3/NH3/Pb).
  Source: https://cpcb.nic.in/air-quality-standard/
- **WHO 2021 Global Air Quality Guidelines**: PM2.5 annual AQG 5 µg/m³
  (down from 10 in the 2005 guideline) with four stepwise Interim
  Targets (IT-1..IT-4: 35/25/15/10 µg/m³); PM10 annual AQG 15 µg/m³
  (down from 20). Source:
  https://www.who.int/news-room/questions-and-answers/item/who-global-air-quality-guidelines
- **NCAP**: launched January 2019 (MoEF&CC), ~130 non-attainment/
  million-plus cities across 24 states/UTs, target revised to up to a
  40% PM10 reduction OR meeting the PM10 NAAQS (60 µg/m³) by FY2025-26
  (from an original 20-30% by FY2024-25 vs a 2017-18 baseline), funded
  via NCAP + XV Finance Commission grants, tracked via the PRANA portal.
  Per the CREA "Tracing the Hazy Air 2026" progress report: only 23 of
  130 covered cities have met the revised target, and 190 of 229
  monitored cities nationwide still exceed the PM10 NAAQS — **a
  documented, ongoing gap, not a solved problem**. Sources:
  https://energyandcleanair.org/publication/2026-progress-report-on-national-clean-air-programme/ ,
  https://prana.cpcb.gov.in/

## Development

```bash
clojure -M:test
clojure -M:lint
```

61 tests / 114 assertions as of this build (`clean_air.regulatory-test`,
`clean_air.registry-test`, `clean_air.governor-test`,
`clean_air.portfolio-test`), covering the hold/escalate/commit decision
space, the ledger-verification predicates, priority-score math, and
portfolio ranking/filtering — not just the original 3-assertion happy
path.

## Status

`:implemented` — see `blueprint.edn`'s `:itonami.blueprint/implemented-slice`
for the architecture summary and `90-docs/adr/` in the `com-junkawasaki/root`
superproject for the registration ADR (this repo was completed and
registered from an orphaned local scaffold; see that ADR for the audit
trail).

## License

AGPL-3.0-or-later.
