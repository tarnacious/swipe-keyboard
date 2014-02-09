(ns swipe.tree
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [clojure.zip :as zip]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan close! timeout alts! >!]])) 


(defn col-to-map [col]
  "Convert a collection `col` of two item vectors to a map." 
  (loop [l col 
         m {}]
    (let [item (first l)]
      (if item 
        (recur (rest l) (assoc m (first item) (second item))) 
        m))))

(defn zipper [root] 
  "Create a zipper of vectors for a tree of maps? I think I have got this
  horribly wrong, it produces the result I want but does not seem an ideal use
  of zippers and is very slow (probably converting cols to maps)"
  (zip/zipper 
    (fn [_] true) 
    (fn [node] (seq (second node)))
    (fn [node children] [(first node) (col-to-map children)])
    root))

(defn find-child [node value]
  "So much looping through children seems crazy when the structure is maps at
  the end. Building the tree needs be revisited."
  (loop [child (zip/leftmost node)]
    (if child 
      (if (= (first (zip/node child)) value)
        child
        (recur (zip/right child))))))

(defn add-word [root word]
  "Adds a word to a root and returns a new root with the root included in the
  tree and a :meta key in the final node of the path with a value :end."
  (let [letters (.split word "")]
    (loop [tree (zipper root)
           lpath letters]
      (let [value (first lpath)
            node (zip/down tree)
            found (find-child node value)]
        (if value
          (if found
            (do
              (recur found (rest lpath)))
            (do 
              (let [update (zip/append-child tree (first lpath))]
                 (recur update lpath))))
          (zip/root (zip/append-child tree [:meta :end])))))))

(defn string-to-words [s]
  "Converts string to collection of words"
  (let [split (seq (.split (.join (.split s " ") "\n") "\n"))
       words (filter (fn [w] (not= (.-length w) 0)) split)
       trimmed (map (fn [w] (.trim w)) words)]
    trimmed))
        
(defn build-tree-int [root-tree all-words]
  "Build an index from a string of words. Returns a channel imediately and puts
  vectors either starting with :info or :tree. The rest of the info vector
  contain progress updates and the rest of the tree value is the completed
  tree."
  (loop [words all-words
         tree root-tree]
    (if (first words)
      (recur (rest words) (add-word tree (first words)))
      tree)))

(defn build-tree [all-words]
  (build-tree-int nil (string-to-words all-words)))

(defn build-tree-chan [s]
  "Build an index from a string of words. Returns a channel imediately and puts
  vectors either starting with :info or :tree. The rest of the info vector
  contain progress updates and the rest of the tree value is the completed
  tree."
  (let [all-words (string-to-words s)
        tree-chan (chan)]
    (go 
      (loop [words all-words
             tree nil]
        (>! tree-chan [:info (count words) (count all-words)])
        (if (not= 0 (count words))
          (recur (drop 100 words) (build-tree-int tree (take 100 words)))
          (>! tree-chan [:tree tree]))))
    tree-chan))

(defn find-word [tree word]
  "Not actually used, warming up to the swipe search. Could be useful for
  testing the trees though"
  (loop [t (second tree)
         w word]
    (if tree
      (let [character (first w)]
        (if character
          (recur (get t character) (rest w))
          (= (get t :meta) :end)))))) 

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
        [(swipe-search1 (get tree letter) (rest letters) (cons letter path))
         (swipe-search1 (get tree letter) letters (cons letter path))]
        ; try normal, double and skip
        [(swipe-search1 (get tree letter) (rest letters) (cons letter path))
         (swipe-search1 (get tree letter) letters (cons letter path))
         (swipe-search1 tree (rest letters) path)])
      ; no remaining letters, return an empty list of results 
      '())))

(defn swipe-search1 [tree letters path]
  "Recursively expanding search until either we have no more tree nodes,
  meaning this sequence was not contained in the root tree. Or we have no more
  keys to consume and this tree node is marked as the end of a word."
  (if (= tree nil)
    `()
    (if (and (= (get tree :meta) :end) (= letters `())) 
      (apply str (reverse path))
      (expand-search tree letters path))))

(defn swipe-search [tree key-sequence]
  "Take a tree and a key-sequence and return a list of all the possible words
  the key-sequence could represent in the tree. This follows some rules:
    Any key can be used used.
    Any key can be repeated. 
    The first and last keys must be used.
    Any" 
  (flatten (swipe-search1 (second tree) key-sequence '())))
