(ns swipe.webworker
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan close! >!]]))

(defn create-worker [script]
  (let [worker (js/Worker. script)
        read-chan (chan)
        write-chan (chan)]
    (.addEventListener worker "message"
     #(go
        (let [data (.-data %1)]
          (>! read-chan data)
        )))
    (go (while true
          (let [message (<! write-chan)]
            (.postMessage worker message))))
    [write-chan read-chan]))

(defn decode-message [channel event]
  (let [message (.-data event)]
    (put! channel message)))

(defn create-client-proxy []
  (let [read-chan (chan)
        write-chan (chan)]
    (set! (.-onmessage js/self) (partial decode-message read-chan))
    (go (while true (let [message (<! write-chan)]
      (.postMessage js/self message))))
    [read-chan write-chan]
  ))
