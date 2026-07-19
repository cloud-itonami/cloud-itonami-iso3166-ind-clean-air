(ns clean-air.registry
  "Independent evidence-ledger and approval-ledger verification for the
  India Airshed Clean Air Portfolio.

  The original shape of this repo's `clean-air.portfolio` trusted a
  candidate intervention's own INLINE `:evidence`/`:approval` keys --
  exactly the kind of self-report every other cloud-itonami actor's own
  registry explicitly refuses to trust (see e.g. `hygaccess.registry`'s
  'ground truth, not self-report' discipline, or `marketentry.registry`'s
  independent statute lookups). Nothing stopped a caller from writing
  `{:evidence #{:source :baseline :beneficiary :implementer}
    :approval :approved-by-council}` on a candidate map without any real
  evidence dossier ever being filed or any real council vote ever
  happening.

  This namespace fixes that: `evidence-complete?`/`council-approved?`
  consult a SEPARATE, independently-supplied ledger -- a plain vector of
  append-only record maps, the same shape every other fleet actor's own
  audit ledger uses -- keyed by intervention id, and verify each record
  was originated by a body in `clean-air.regulatory`'s CLOSED
  recognized-authority sets, cites a real, non-blank source, and (for
  approval) was actually decided by a quorum-backed for>against
  majority vote. A candidate's own self-reported `:evidence`/`:approval`
  keys are never read anywhere in this namespace.

  Pure functions over pure, caller-supplied data; no I/O, no mutation,
  no network -- the ledgers themselves are assumed to already be
  independently populated (e.g. from a real audit-ledger store) before
  they reach here; this namespace only verifies their CONTENT, it does
  not fetch or trust it."
  (:require [clojure.string :as str]
            [clean-air.regulatory :as regulatory]))

(def required-evidence-types
  "The four evidence-dossier pieces every candidate intervention must
  have an independently-verifiable ledger record for before it can be
  evidence-complete: a cited data `:source`, a `:baseline` measurement,
  a documented `:beneficiary` population, and a registered
  `:implementer`. Duplicated here (rather than required FROM
  `clean-air.portfolio`) so this namespace stays the independent
  ground-truth layer `clean-air.portfolio` depends on, not the reverse."
  #{:source :baseline :beneficiary :implementer})

(defn- records-for [ledger id id-key]
  (filterv #(= (get % id-key) id) ledger))

(defn evidence-records-for
  "All evidence-ledger records (valid or not) citing `intervention-id`."
  [evidence-ledger intervention-id]
  (records-for evidence-ledger intervention-id :evidence/intervention-id))

(defn approval-records-for
  "All approval-ledger records (valid or not) citing `intervention-id`."
  [approval-ledger intervention-id]
  (records-for approval-ledger intervention-id :approval/intervention-id))

(defn- non-blank-string? [s]
  (and (string? s) (not (str/blank? s))))

(defn valid-evidence-record?
  "A ledger record is a VALID piece of evidence only if it was
  independently originated by a recognized authority (never the
  candidate's own self-report), carries a real citation and source URL,
  and declares one of the required evidence types. Any one of these
  missing means the record does not count, even if it superficially
  looks complete."
  [{:evidence/keys [submitted-by citation url type]}]
  (and (contains? regulatory/recognized-evidence-authorities submitted-by)
       (non-blank-string? citation)
       (non-blank-string? url)
       (contains? required-evidence-types type)))

(defn evidence-complete?
  "INDEPENDENT verification: does `evidence-ledger` contain, for
  `intervention-id`, at least one VALID record (per
  `valid-evidence-record?`) for EVERY type in `required-evidence-types`?
  A candidate's own self-reported `:evidence` set is never read here --
  this is the whole point of separating the ledger from the candidate."
  [evidence-ledger intervention-id]
  (let [valid-types (->> (evidence-records-for evidence-ledger intervention-id)
                          (filter valid-evidence-record?)
                          (map :evidence/type)
                          set)]
    (every? valid-types required-evidence-types)))

(defn- valid-vote? [{:keys [for against]}]
  (and (number? for) (number? against) (>= for 0) (>= against 0) (> for against)))

(defn majority-quorum-approval?
  "A ledger record is a VALID council approval only if it was granted by
  a recognized approval body (per `clean-air.regulatory/
  ncap-institutional-structure`), the record itself declares quorum was
  met, and the recorded vote is a genuine for>against majority (a tie or
  against-majority is never approval, regardless of `:quorum-met?`)."
  [{:approval/keys [body quorum-met? vote]}]
  (and (contains? regulatory/recognized-approval-bodies body)
       (true? quorum-met?)
       (valid-vote? vote)))

(defn council-approved?
  "INDEPENDENT verification: does `approval-ledger` contain, for
  `intervention-id`, at least one VALID approval record (per
  `majority-quorum-approval?`)? A candidate's own self-reported
  `:approval` keyword is never read here."
  [approval-ledger intervention-id]
  (boolean (some majority-quorum-approval? (approval-records-for approval-ledger intervention-id))))

(defn narrow-margin?
  "A vote decided by exactly one ballot (for - against = 1) is a SOFT
  high-stakes signal, not a HARD block -- the approval still stands, but
  it is surfaced for a human council member to double-check rather than
  silently treated identically to a comfortable majority. Matches the
  fleet's HARD/SOFT distinction (e.g. `hygaccess.governor`'s
  confidence/high-stakes gate)."
  [{:approval/keys [vote]}]
  (let [{:keys [for against]} vote]
    (and (number? for) (number? against) (= 1 (- for against)))))

(defn single-source-evidence?
  "Every VALID evidence record for `intervention-id` came from the SAME
  single originating authority -- i.e. no independent second body
  (a different regulator, a different SPCB, an academic study, WHO...)
  corroborated ANY piece of the dossier. Not disqualifying on its own
  (an implementer legitimately reports its own registration, a state
  SPCB legitimately reports its own monitoring baseline), but a
  single-source dossier is a real risk signal before committing public
  funds -- surfaced as a SOFT escalation, not a HARD hold."
  [evidence-ledger intervention-id]
  (let [submitters (->> (evidence-records-for evidence-ledger intervention-id)
                         (filter valid-evidence-record?)
                         (map :evidence/submitted-by)
                         set)]
    (= 1 (count submitters))))
