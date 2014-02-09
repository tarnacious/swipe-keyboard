(ns test.stroke
    (:require-macros [cemerick.cljs.test
     :refer (is deftest with-test run-tests testing test-var)])
    (:require [cemerick.cljs.test :as t] 
              [swipe.keypad :refer [combine-consecutive-items letters]]
              ))

(deftest test-combine-consecutive-duplicates
  (is (= (combine-consecutive-items '(1 2 3 3 4 4 5)) '(1 2 3 4 5))))
