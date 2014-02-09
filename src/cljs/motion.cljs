(ns swipe.motion
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.net.XhrIo :as xhr]
            [swipe.tree :refer [build-tree]]
            [swipe.settings :refer [canvas-id]]
            [cljs.core.async :refer [<! put! chan close! >!]])) 

(defn listen [el type chan]
  "Binds a type of mouse event and emits the event messages on a channel. I
  need to scale the points for some reason to make the match the canvas
  co-ordinates, I am not sure why."
  (let [ dx (/ (.-width el) (.-offsetWidth el))
         dy (/ (.-height el) (.-offsetHeight el)) ]
    (events/listen el type
      (fn[e] (do 
        (put! chan { 
          :type type 
          :x (* dx (.-offsetX e)) 
          :y (* dy (.-offsetY e)) }))))))

(defn bind []
  "Creates a channel and binds several mouse events to it. Returns a new
  channel"
  (let [out (chan) 
        element (dom/getElement canvas-id)
        event-ids [(listen element "mousedown", out)
                   (listen element "mousemove", out)
                   (listen element "mouseup", out)
                   (listen element "mouseout", out)]]
      [event-ids out]))


(defn compose [in]
  "Composes sequences of mouse events (down, up and move) into a sequence of
  swipe events (mouse down, moves and release)."
  (let [swipe (chan)
        events (chan)]
  (go (while true
    (let [event (<! in)]
      (if (= "mousedown" (:type event))
        (do 
          (put! events event)
          (loop [event1 (<! in)
                 points (cons event '())]
              (put! events event1)
              (if (= (:type event1) "mousemove")
                (recur (<! in) (cons event1 points))
                (do 
                  (put! swipe (reverse points)))
                )))))))
    [events swipe]))
