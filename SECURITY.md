# Security Policy

This project handles evidence-gated, council-approval-gated portfolio
selection for public-interest air-quality interventions. Treat
vulnerabilities as potentially high impact even when the demo data is
synthetic — a bypass here could route public funding attention toward an
unverified or unapproved intervention.

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real beneficiary, program, or personal data exposure
- authorization bypass
- Clean Air Portfolio Governor bypass (a candidate reaching
  `select-portfolio`'s output without an independent `:commit` decision)
- a bypass that lets a candidate's own self-reported `:evidence`/
  `:approval` flags be trusted instead of `clean-air.registry`'s
  ledger-backed verification
- a bypass that lets an unrecognized authority or approval body count as
  independently verified
- audit-trail (`clean-air.portfolio/decisions`) tampering or suppression

## Reporting

Use GitHub private vulnerability reporting when available for the
repository. If that is unavailable, contact the repository maintainers
through the cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on ledger verification, governance decisions, or audit output
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real beneficiary/program/personal data outside this repository.
- Run governor/registry tests before deployment.
- Export and review `clean-air.portfolio/decisions` output regularly for
  unexpected holds or escalations.
- Use least privilege for operators and service accounts.
- Any real deployment feeding real evidence/approval ledgers should
  independently confirm each ledger source is actually the recognized
  authority it claims to be — this repo verifies ledger CONTENT
  structurally, it does not itself authenticate who submitted a record.
