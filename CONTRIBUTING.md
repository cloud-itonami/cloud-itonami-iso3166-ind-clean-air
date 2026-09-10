# Contributing

`cloud-itonami-iso3166-ind-clean-air` accepts contributions to the OSS
blueprint, governance-decision tests, regulatory citations, documentation,
and the open business/operator model.

## Development

```bash
clojure -M:test
clojure -M:lint
```

Keep changes small and include tests for governor, ledger-verification,
or ranking behavior.

## Rules

- Do not commit real intervention, beneficiary, or personal data — this
  repo's own ledger fixtures under `test/clean_air/fixtures.kotoba` are
  synthetic/illustrative test data, and new tests should follow that
  convention.
- Never let `clean-air.portfolio` trust a candidate's own self-reported
  evidence/approval claim — every eligibility check must go through
  `clean-air.governor` against `clean-air.registry`'s ledger-backed
  verification.
- Never add a body to `clean-air.regulatory/recognized-evidence-authorities`
  or `recognized-approval-bodies` without a real, cited basis (an actual
  regulator, monitoring body, or documented NCAP institutional role) —
  these closed sets exist specifically to prevent an unverifiable or
  self-interested source from counting as independent corroboration.
- Ground any new regulatory number (a NAAQS limit, a WHO guideline, an
  NCAP target) in a source actually fetched and read; cite it in the
  var's docstring. If a number cannot be verified, leave it out or mark
  it explicitly unverified rather than guessing.
- Document any new domain assumption in the relevant namespace docstring
  or in `README.md`.

## Pull Requests

PRs should describe:

- what behavior changed
- which governance invariant (hold/escalate/commit) is affected
- how it was tested
- whether a new or changed regulatory citation needs a source URL
