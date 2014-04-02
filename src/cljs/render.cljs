(ns swipe.render
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [swipe.keypad :refer [key-values]]
            [swipe.settings :refer [step-x step-y font
                                    text-x text-y
                                    padding-x padding-y
                                    canvas-id]]))

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
  (let [results-view (dom/getElement "swipe-loading")]
    (set! (.-innerHTML results-view) string)))

(defn hide-loading []
  (let [results-view (dom/getElement "swipe-loading")]
    (dom/removeNode results-view)))

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

(defn draw-line [x1 y1 x2 y2]
  (doto (get-context)
    (.save)
    (.beginPath)
    (.moveTo x1 y1)
    (.lineTo x2 y2)
    (aset "lineWidth" 10)
    (aset "lineCap" "round")
    (.stroke)
    (.restore)))

(defn draw-letters []
  (loop [head (first key-values) tail (rest key-values)]
    (if head
      (let [x (:x head)
            y (:y head)
            height (- step-y padding-y)
            width (- step-x padding-x)
            radius 15]
      (doto (get-context)
       (.save)
       (aset "fillStyle" "#DDDDDD")
       (.beginPath)
       (.moveTo (+ x radius) y)
       (.lineTo (+ x (- width radius)) y)
       (.quadraticCurveTo (+ x width) y (+ x width) (+ y radius))
       (.lineTo (+ x width) (+ y (- height radius)))
       (.quadraticCurveTo (+ x width) (+ y height) (+ x (- width radius)) (+ y height))
       (.lineTo (+ x radius) (+ y height))
       (.quadraticCurveTo x (+ y height) x (+ y (- height radius)))
       (.lineTo x (+ y radius))
       (.quadraticCurveTo x y (+ x radius) y)
       (.closePath)
       (.fill)
       (aset "font" font)
       (aset "fillStyle" "#000000")
       (.fillText (.toUpperCase (:letter head)) (+ x text-x) (+ y text-y))
       (.restore))
       (recur (first tail) (rest tail))))))

(defn clear-canvas []
  (doto (get-context)
    (.save)
    (aset "fillStyle" "white")
    (.fillRect 0, 0, 900, 300)
    (.restore)))
