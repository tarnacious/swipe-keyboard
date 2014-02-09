(ns swipe.worker
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [swipe.webworker :refer [create-client-proxy]]
            [swipe.core :refer [get-swipe]]
            [cljs.core.async :refer [<! put! timeout chan close! >!]]))

; create a client proxy and start the worker
(let [[read-chan write-chan] (create-client-proxy)]
  (get-swipe read-chan write-chan)
)
