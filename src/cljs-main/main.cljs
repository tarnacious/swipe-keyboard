(ns swipe.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [swipe.core :refer [start-app]]
            [swipe.webworker :refer [create-worker]]))

; create a webworker and start the application
(let [[write-chan read-chan] (create-worker "/js/worker-dev.js")]
  (start-app write-chan read-chan))
