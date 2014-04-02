(ns swipe.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.net.XhrIo :as xhr]
            [swipe.tree :refer [build-tree-chan swipe-search]]
            [swipe.motion :refer [compose bind unbind]]
            [swipe.keypad :refer [get-letters]]
            [swipe.render :refer [clear-canvas draw-letters hide-loading
                                  display-results clear-results draw-line
                                  display-str display-processing]]
            [cljs.core.async :refer [<! put! chan close! timeout alts! >!]]))

(defn web-get [url]
  "Returns a channel immediately and puts the response of an xhr/send on the
  channel when it occurs"
  (let [ch (chan)]
    (xhr/send url
      (fn [event]
        (let [res (-> event .-target .getResponseText)]
          (go
            (>! ch res)
            (close! ch)))))
    ch))

(defn load-worker [message-chan]
  "Download training text and build index. Updates `message-chan` with updates.
  Returns a channel immediatly and puts the index on it when built."
  (let [loading-chan (chan)]
   (go
    (>! message-chan "Downloading Data")
    (let [text (<! (web-get "/training.txt")) ]
      (>! message-chan "Downloaded Data")
      ; allow message to be sent
      (<! (timeout 0))
      (>! message-chan "Building tree")
      (<! (timeout 0))
      (let [tree-chan (build-tree-chan text)]
        (loop [status (<! tree-chan)]
          (let [status-type (first status)
                info (rest status)]
            (if (= :info status-type)
               (do
                 (>! message-chan (str "Building tree "
                                       (first info) "%"))
                 (recur (<! tree-chan))))
            (>! message-chan "Built-tree")
            (>! loading-chan (first info)))))))
    loading-chan))


(defn get-swipe [command-chan message-chan]
  "webworker process to convert sequences of keys a list of possible words.
  Currently working in a very limited sence, no normalized words, no keys
  expansion into caps or umlauts"
  (go (let [tree-chan (load-worker message-chan)
            tree (<! tree-chan)]
    (>! message-chan "ready")
    (while true
      (let [message (<! command-chan)
            letters message
            key-sequence (seq letters)
            result (swipe-search tree key-sequence)]
        (>! message-chan (clj->js result)))))))


(defn loading [message-chan]
  "Render loading status updates from `message-chan` until 'ready' message is
  sent. Returns a channel immediately and puts a value on it once loading
  is complete."
  (let [loading-chan (chan)]
    (go
      (loop [msg (<! message-chan)]
        (if (not= msg "ready")
          (do
            (display-str msg)
            (recur (<! message-chan)))
          (>! loading-chan "ready")
          )))
    loading-chan))


(defn take-swipe []
  "Bind mouse events and draw swipe path. Returns a channel immediately and
  puts the sequence of swiped keys in it when the swipe action is complete"
  (let [swipe-chan (chan)
       [events event-chan] (bind)
       [clicks swipes] (compose event-chan)]
    (go
      (loop [prev nil]
        (let [[value select-chan] (alts! [clicks swipes])]
          (if (= select-chan clicks)
            (do
              (if prev
                (draw-line (:x value) (:y value) (:x prev) (:y prev))
                (draw-line (:x value) (:y value) (:x value) (:y value)))
              (recur value))
            (do
              (let [letters (get-letters value)]
                (>! swipe-chan (clj->js letters))
                (unbind events)))))))
    swipe-chan))


(defn start-app [write-chan read-chan]
  "Takes a `write-chan` and `read-chan` to communicate with webworker. Waits
  for worker to load and then loops reading a swipe, sending key sequence to
  worker channel, reading response from worker channel and the displaying the
  results"
  (go
    (clear-canvas)
    (draw-letters)
    ; wait for loading to complete
    (<! (loading read-chan))
    (hide-loading)
    ; start main swipe / process / result loop
    (while true
      (clear-canvas)
      (draw-letters)
      ; take a swipe and send the swiped key sequence to the background worker
      (let [letters (<! (take-swipe))]
        (>! write-chan letters))
      ; wait for the background worker to respond with result
      (let [value (<! read-chan)]
        (display-results (seq value))
      ; pause before clearing the swipe
      (<! (timeout 1000))
      ))))
