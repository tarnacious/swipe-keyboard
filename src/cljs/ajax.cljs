(ns swipe.ajax
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.net.XhrIo :as xhr]
            [cljs.core.async :refer [<! put! chan close! >!]])) 

(defn web-get [url]
  (let [ch (chan)]
    (xhr/send url
      (fn [event]
        (let [res (-> event .-target .getResponseText)]
          (go 
            (>! ch res)
            (close! ch)))))
    ch))
