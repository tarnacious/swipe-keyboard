(ns swipe.render
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [swipe.keypad :refer [key-values]]
            [swipe.settings :refer [step-x step-y font 
                                    text-x text-y 
                                    padding-x padding-y
                                    canvas-id]])) 

(defn log [s]
  (.log js/console (str s)))

(defn render-query [results]
  (if results
    (str
      "<ul>"
      (apply str
        (for [result results]
          (str "<li>" result "</li>")))
      "</ul>")
    (str "No results")))

(defn display-results [results]
  (let [results-view (dom/getElement "swipe-results")]
    (set! (.-innerHTML results-view) (render-query results))))

(defn display-str [string]
  (let [results-view (dom/getElement "swipe-results")]
    (set! (.-innerHTML results-view) string)))

(defn clear-results []
  (let [results-view (dom/getElement "swipe-results")]
    (set! (.-innerHTML results-view) "")))

(defn display-processing []
  (let [results-view (dom/getElement "swipe-results")]
    (set! (.-innerHTML results-view) "Processing...")))

; Canvas functions
(defn get-context []
  (let [element (.getElementById js/document canvas-id)]
      (.getContext element "2d")))

(defn draw-square [x y]
  (let [context (get-context)]
    (.fillRect context (- x 1) (- y 1) 2 2)))

(defn draw-letters []
  (loop [head (first key-values) tail (rest key-values)]
    (if head
      (let [x1 (:x head) 
            y1 (:y head) 
            x2 (+ x1 (- step-x padding-x)) 
            y2 (+ y1 (- step-y padding-y))]
      (doto (get-context)
       (aset "font" font)
       (.fillText (:letter head) (+ x1 text-x) (+ y1 text-y))
       (.beginPath)
       (.moveTo x1 y1)
       (.lineTo x2 y1)
       (.lineTo x2 y2)
       (.lineTo x1 y2)
       (.lineTo x1 y1)
       (.stroke)
       (.restore))
       (recur (first tail) (rest tail))))))

(defn clear-canvas []
  (doto (get-context)
    (.save)
    (aset "fillStyle" "white")
    (.fillRect 0, 0, 800, 300)
    (.restore)))
