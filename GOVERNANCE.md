# Governance

`cloud-itonami-iso3166-ind-clean-air` is an OSS open-business blueprint for
evidence-gated, council-approval-gated portfolio selection of India
air-quality (NCAP-domain) interventions — a decision-support ranking
layer, not an execution layer.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- a candidate intervention's own self-reported `:evidence`/`:approval`
  flags are never trusted — eligibility is decided ONLY by
  `clean-air.governor` against independently-supplied ledgers
  (`clean-air.registry`).
- the Clean Air Portfolio Governor remains independent of
  `clean-air.portfolio`'s ranking logic (it is consulted, not bypassed).
- a fabricated or unverifiable evidence/approval record cannot be
  overridden by simply re-asserting the candidate's own claim — the
  underlying ledger record must actually exist and pass
  `clean-air.registry`'s closed-authority checks.
- `:hold` decisions are HARD (evidence or approval independently
  fails); `:escalate` decisions are SOFT (independently verified, but a
  narrow-margin or single-source signal is present) and route to a
  human council member, never auto-commit.
- every hold, escalate, and commit path is auditable via
  `clean-air.portfolio/decisions`.
- real intervention/beneficiary/personal data stays outside Git — this
  repo's own ledger fixtures are synthetic/illustrative test data.

## Decision Records

Architecture decisions for this repo's own history live with the
`com-junkawasaki/root` superproject's `90-docs/adr/` (EDN-only ADRs, per
that superproject's own documentation convention — see the registration
ADR that completed and registered this repo from an orphaned local
scaffold). Changes to the trust model, ledger schema, closed
authority/approval-body sets, or license should reference or add an ADR
there.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is
a separate trust mark and should require security, audit, and
data-provenance review — including proof that any real-world ledger this
tool is pointed at is actually independently populated by the
authorities in `clean-air.regulatory/recognized-evidence-authorities` /
`recognized-approval-bodies`, not by the same party proposing the
intervention.

Certified operators can lose certification for:

- bypassing `clean-air.governor` and ranking/funding a candidate that
  was not independently `:commit`-ed
- feeding the evidence/approval ledgers from an unrecognized or
  self-interested source and representing it as independently verified
- misrepresenting a `:hold` or `:escalate` outcome as a `:commit`
- mishandling beneficiary or other sensitive program data
- failing to respond to security incidents
