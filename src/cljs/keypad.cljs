(ns swipe.keypad
  (:require-macros [cljs.core.async.macros :refer [go]]) 
  (:require [swipe.settings :refer [step-x step-y font text-x text-y]]))

(defn create-key [letter index row-x row-y]
  "Calculate a map of properties for a key"
  {:letter letter
   :x (+ row-x (* index step-x))
   :y (+ row-y 0)})

(defn create-row-of-keys [letters x y]
  "Create a row of keys"
  (map-indexed (fn [index value] (create-key value index x y)) 
    letters))

(def key-values
  "Create a lists of maps of key properties. The keys are used fro drawing the
  keyboard and detecting mouse events"
  (concat (create-row-of-keys "qwertyuiop" 0 0)
          (create-row-of-keys "asdfghjkl" (* 0.5 step-x) step-y)
          (create-row-of-keys "zxcvbnm" (* 1.5 step-y) (* 2 step-x))))

(defn add-if-not-head [col item] 
  "Add `item` to the collection `col` if the head of `col` is not equal to
  `item`" 
  (let [head (first col)]
    (if (= head item) col (cons item col))))

(defn combine-consecutive-items [col]
  "Return a collection from `col` with consecutive values combined to a single
  value"
  (reverse (reduce add-if-not-head [] col)))

(defn in-key? [point letter]
  "Is `point` within the `letter` geometry"
  (let [px (:x point) py (:y point)
        lx (:x letter) ly (:y letter)
        lx1 (+ lx step-x) ly1 (+ ly step-y)]
    (and (> px lx) (> py ly) (< px lx1) (< py ly1))))

(defn point-to-keys [point] 
  "Returns a list of letters from `letters` where `point` is within the key
  geometry"
  (map (fn [letter] (:letter letter)) 
    (filter (partial in-key? point) key-values)))

(defn get-letters [points]
  (flatten (combine-consecutive-items 
    (map point-to-keys points))))
