(ns test.motion
  (:require-macros [cemerick.cljs.test :as m
                    :refer (is test-var deftest run-tests done 
                            with-test-ctx block-or-done)]
                   [cljs.core.async.macros :refer [go]]
                   )
  (:require [cemerick.cljs.test :as t]
            [test.wait :refer [wait-for verify]]
            [swipe.motion :refer [compose]]
            [cljs.core.async :refer [<! chan >!]]
            ))

(def expected-swipe 
  '({:x 1, :y 2, :type "mousedown"} 
   {:x 3, :y 4, :type "mousemove"} 
   {:x 5, :y 6, :type "mousemove"}))

(deftest ^:async test-swipe
  "Test the correct swipe events is put on the queue for a series of mouse
  events.  The swipe event should include from the mouse down position until
  before the mouse up event"
  (let [input-events (chan)
       [events swipe] (compose input-events)]
    (go (>! input-events { :type "mousemove" :x 0 :y 0 }) ; should be ignored
        (>! input-events { :type "mousedown" :x 1 :y 2 }) ; start sequence
        (>! input-events { :type "mousemove" :x 3 :y 4 })
        (>! input-events { :type "mousemove" :x 5 :y 6 })
        (>! input-events { :type "mouseup" :x 7 :y 8 })   ; end sequence
        (>! input-events { :type "mousemove" :x 0 :y 0 })); should be ignored
    (block-or-done
      (go (let [swipe-event (<! (wait-for swipe 100))]
            (verify -test-ctx swipe-event expected-swipe))))))

(deftest ^:async test-swipe-with-result
  "Test no swipe events are put in the queue if there is no mouse down event."
  (let [input-events (chan)
       [events swipe] (compose input-events)]
    (go (>! input-events { :type "mousemove" :x 3 :y 4 })
        (>! input-events { :type "mousemove" :x 5 :y 6 })
        (>! input-events { :type "mouseup" :x 7 :y 8 })) 
    (block-or-done 
      (go (let [swipe-event (<! (wait-for swipe 100))]
            (verify -test-ctx swipe-event :timeout))))))
