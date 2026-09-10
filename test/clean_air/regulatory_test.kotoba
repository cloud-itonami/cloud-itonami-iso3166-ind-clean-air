(ns clean-air.regulatory-test
  "Sanity/regression tests for `clean-air.regulatory`'s citation-grounded
  domain facts -- catches transcription typos in the NAAQS/WHO tables
  and confirms the closed authority sets contain what the docstrings
  claim."
  (:require [clojure.test :refer [deftest is testing]]
            [clean-air.regulatory :as regulatory]))

(deftest naaqs-2009-pm-values
  (testing "CPCB NAAQS 2009 PM2.5/PM10 limits (cpcb.nic.in/air-quality-standard/)"
    (is (= 40 (get-in regulatory/naaqs-2009 [:pm2.5 :annual-ug-m3])))
    (is (= 60 (get-in regulatory/naaqs-2009 [:pm2.5 :24hr-ug-m3])))
    (is (= 60 (get-in regulatory/naaqs-2009 [:pm10 :annual-ug-m3])))
    (is (= 100 (get-in regulatory/naaqs-2009 [:pm10 :24hr-ug-m3])))))

(deftest naaqs-2009-other-pollutants-present
  (doseq [pollutant [:so2 :no2 :co :o3 :nh3 :pb]]
    (is (contains? regulatory/naaqs-2009 pollutant)
        (str pollutant " should have a NAAQS entry"))))

(deftest who-2021-aqg-tightened-vs-2005
  (testing "2021 revision lowered PM2.5 annual AQG 10->5 and PM10 20->15"
    (is (= 5 (get-in regulatory/who-2021-aqg [:pm2.5 :aqg-annual-ug-m3])))
    (is (= 15 (get-in regulatory/who-2021-aqg [:pm10 :aqg-annual-ug-m3])))
    (is (= [35 25 15 10] (get-in regulatory/who-2021-aqg [:pm2.5 :interim-targets-annual-ug-m3])))))

(deftest who-aqg-is-stricter-than-cpcb-naaqs
  (testing "the whole reason India has 'interim targets' at all: WHO's own guideline is far below CPCB's legal limit"
    (is (< (get-in regulatory/who-2021-aqg [:pm2.5 :aqg-annual-ug-m3])
           (get-in regulatory/naaqs-2009 [:pm2.5 :annual-ug-m3])))
    (is (< (get-in regulatory/who-2021-aqg [:pm10 :aqg-annual-ug-m3])
           (get-in regulatory/naaqs-2009 [:pm10 :annual-ug-m3])))))

(deftest ncap-facts-shape
  (is (= "2019-01" (:launched regulatory/ncap-facts)))
  (is (= :moefcc (:ministry regulatory/ncap-facts)))
  (is (= :pm10 (get-in regulatory/ncap-facts [:revised-target :pollutant])))
  (is (= 40 (get-in regulatory/ncap-facts [:revised-target :reduction-pct])))
  (is (= 60 (get-in regulatory/ncap-facts [:revised-target :or-meets-naaqs-ug-m3])))
  (testing "revised target's NAAQS reference matches the actual PM10 NAAQS annual limit"
    (is (= (get-in regulatory/ncap-facts [:revised-target :or-meets-naaqs-ug-m3])
           (get-in regulatory/naaqs-2009 [:pm10 :annual-ug-m3])))))

(deftest ncap-progress-2026-shows-a-real-gap-not-a-solved-problem
  (testing "documented 2026 progress: most covered cities have NOT met either target"
    (is (< (get-in regulatory/ncap-facts [:progress-2026 :cities-met-revised-target])
           (get-in regulatory/ncap-facts [:progress-2026 :cities-total-covered])))
    (is (pos? (get-in regulatory/ncap-facts [:progress-2026 :cities-still-exceeding-pm10-naaqs])))))

(deftest recognized-evidence-authorities-closed-set
  (is (contains? regulatory/recognized-evidence-authorities :cpcb))
  (is (contains? regulatory/recognized-evidence-authorities :state-pollution-control-board))
  (is (contains? regulatory/recognized-evidence-authorities :who))
  (is (not (contains? regulatory/recognized-evidence-authorities :self-appointed-panel))
      "a fabricated authority must not be in the closed set"))

(deftest recognized-approval-bodies-matches-ncap-institutional-structure
  (is (= (set (map :body (vals regulatory/ncap-institutional-structure)))
         (disj regulatory/recognized-approval-bodies :cpcb-chairman-approval))
      "every non-signoff body in the documented NCAP chain is a recognized approval body")
  (is (contains? regulatory/recognized-approval-bodies :state-level-steering-committee)))

(deftest pm25-exceeds-naaqs-annual-boundary
  (is (false? (regulatory/pm25-exceeds-naaqs-annual? 40)) "exactly at the limit is not an exceedance")
  (is (true? (regulatory/pm25-exceeds-naaqs-annual? 40.1)))
  (is (true? (regulatory/pm25-exceeds-naaqs-annual? 95)) "a realistic non-attainment-city reading"))

(deftest who-interim-target-tier-cases
  (is (= :aqg (regulatory/who-interim-target-tier 5)))
  (is (= :aqg (regulatory/who-interim-target-tier 2)))
  (is (= 4 (regulatory/who-interim-target-tier 10)))
  (is (= 3 (regulatory/who-interim-target-tier 12)))
  (is (= 3 (regulatory/who-interim-target-tier 15)))
  (is (= 2 (regulatory/who-interim-target-tier 20)))
  (is (= 1 (regulatory/who-interim-target-tier 35)))
  (is (nil? (regulatory/who-interim-target-tier 40))
      "exceeds even the loosest interim target")
  (is (nil? (regulatory/who-interim-target-tier 95))
      "a realistic non-attainment-city reading exceeds every WHO interim target"))
