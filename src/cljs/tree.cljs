(ns swipe.tree
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [clojure.zip :as zip]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan close! timeout alts! >!]]))


(defn add-word [root word]
  (loop [letters (.split word "")
         node root]
   (let [letter (first letters)]
    (if letter
      (let [find-node (aget node letter)]
        (if find-node
          (recur (rest letters) find-node)
          (let [new-node (js-obj)]
            (aset node letter new-node)
            (recur (rest letters) new-node))))
      (do
        (aset node "meta" "end")))))
  root)

(defn text-to-words [s]
  (let [re (js/RegExp. "\\S+" "g")]
    (seq (.match s re))))

(defn grow-tree [node words]
  (doseq [word words]
        (add-word node word))
  node)

(defn build-tree [text]
  (let [root (js-obj)
        words (text-to-words text)]
    (grow-tree root words)
    root))

(defn zip-progress [col]
  (map-indexed (fn [idx itm]
    [(.round js/Math (* 100 (/ idx (count col))))
     itm]) col))

(defn build-tree-chan [s]
  (let [words (text-to-words s)
        words-lists (partition-all 100 words)
        words-lists1 (zip-progress words-lists)
        tree-chan (chan)
        tree (js-obj)]
    (go
      (doseq [[percent-done words] words-lists1]
        (>! tree-chan [:info percent-done])
        (grow-tree tree words))
      (>! tree-chan [:tree tree]))
    tree-chan))

(defn find-word [tree word]
  "Not actually used, warming up to the swipe search. Could be useful for
  testing the trees though"
  (loop [t tree
         w word]
    (if t
      (let [character (first w)]
        (if character
          (recur (aget t character) (rest w))
          (= (aget t "meta") "end")))
      false)))

; declare swipe-search1 to allow mutual recursing with expand search without
; generating warnings.
(declare swipe-search1)

(defn expand-search [tree letters path]
  "At every node the remaining tree is not only searched for the remaining
  keys, the possiblities of repeating and in some cases skipping keys are also
  expanded."
  (let [letter (first letters)]
    (if letter
      ; there is a letter, so we can generate some new searches
      (if (or (= (first path) letter) (= (first path) nil) (= (rest letters) '()))
        ; don't try skipping letter, we've already tried that or this is the
        ; first or last letters, which we do not allow to be skipped
        [(swipe-search1 (aget tree letter) (rest letters) (cons letter path))
         (swipe-search1 (aget tree letter) letters (cons letter path))]
        ; try normal, double and skip
        [(swipe-search1 (aget tree letter) (rest letters) (cons letter path))
         (swipe-search1 (aget tree letter) letters (cons letter path))
         (swipe-search1 tree (rest letters) path)])
      ; no remaining letters, return an empty list of results
      '())))

(defn swipe-search1 [tree letters path]
  "Recursively expanding search until either we have no more tree nodes,
  meaning this sequence was not contained in the root tree. Or we have no more
  keys to consume and this tree node is marked as the end of a word."
  (if (= tree nil)
    `()
    (if (and (= (aget tree "meta") "end") (= letters `()))
      (apply str (reverse path))
      (expand-search tree letters path))))

(defn swipe-search [tree key-sequence]
  "Take a tree and a key-sequence and return a list of all the possible words
  the key-sequence could represent in the tree. This follows some rules:
    Any key can be used used.
    Any key can be repeated.
    The first and last keys must be used. "
  (flatten (swipe-search1 tree key-sequence '())))
