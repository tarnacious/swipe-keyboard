(ns test.wait
  (:require-macros [cemerick.cljs.test :as m
                    :refer (is test-var deftest run-tests done 
                            with-test-ctx block-or-done)]
                   [cljs.core.async.macros :refer [go]])
  (:require [cemerick.cljs.test :as t]
            [swipe.core :refer [compose]]
            [cljs.core.async :refer [<! put! chan close! timeout alts! alts!! >!]]))


(defn verify
  "For some reason the async tests seem to exect the `with-test-context` to be
  in a separate function"
  [-test-ctx result expected]
    (with-test-ctx -test-ctx
      (is (= result expected))))


(defn wait-for [test-chan ms]
  "Wraps a channel with a timeout of `ms` milliseconds. Returns the channel
  event if it happends before the timeout otherwise returns :timeout"
  (go (let [error (timeout ms)
           [v ch] (alts! [test-chan error])]
         (if (= ch error) :timeout v))))


(deftest ^:async test-timeout
  "The `wait-for` will return timeout if the channel takes longer than the
  timeout"
  (let [too-long (go (<! (timeout 10)) :success)
        wrapped (wait-for too-long 5)]
  (block-or-done 
    (go (let [result (<! wrapped)]
          (verify -test-ctx result :timeout))))))


(deftest ^:async test-success
  "The `wait-for` will return the result if the channel is quicker than the
  timeout"
  (let [quick (go (<! (timeout 5)) :success)
        wrapped (wait-for quick 10)]
  (block-or-done 
    (go (let [result (<! wrapped)]
          (verify -test-ctx result :success))))
    ))
