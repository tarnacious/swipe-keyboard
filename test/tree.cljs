(ns test.tree
    (:require-macros [cemerick.cljs.test
     :refer (is deftest with-test run-tests testing test-var)])
    (:require [cemerick.cljs.test :as t] 
              [swipe.tree :refer [string-to-words
                                  add-word build-tree 
                                  find-word swipe-search]]))


(deftest test-string-to-words-1
  (let [string "one two three"
        words (string-to-words string)]
    (is (= words '("one" "two" "three"))))) 

(deftest test-string-to-words-2
  (let [string "one\ntwo\nthree"
        words (string-to-words string)]
    (is (= words '("one" "two" "three"))))) 

(deftest test-string-to-words-3
  (let [string " one  \n two\n     three"
        words (string-to-words string)]
    (is (= words '("one" "two" "three"))))) 

(deftest test-find-word-finds-word
  (let [tree (build-tree "one two three")]
    (is (= (find-word tree "one") true))))

(deftest test-find-word-finds-another-word
  (let [tree (build-tree "one two three")]
    (is (= (find-word tree "two") true))))

(deftest test-find-word-does-not-find-word
  (let [tree (build-tree "one two three")]
    (is (= (find-word tree "four") false))))

(deftest test-find-word-does-not-find-big-word
  (let [tree (build-tree "one two three")]
    (is (= (find-word tree "fourteen") false))))

(deftest test-find-word-does-not-find-small-word
  (let [tree (build-tree "one two three")]
    (is (= (find-word tree "on") false))))

(deftest test-empty-tree
  (let [tree (build-tree "")]
    (is (= (aget tree "a") nil)
    (is (= (aget tree "meta") nil)))))

(deftest test-one-letter-tree
  (let [tree (build-tree "a")]
    (is (= (aget (aget tree "a") "meta") "end"))
    (is (= (aget tree "b") nil))))

(deftest test-two-one-letter-tree
  (let [tree (build-tree "a b")]
    (is (= (aget (aget tree "a") "meta") "end"))
    (is (= (aget (aget tree "b") "meta") "end"))))

; should find exact word
(deftest swipe-search-1
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["o" "n" "e"]) '("one")))))

(deftest swipe-search-1-1
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["t" "w" "o"]) '("two")))))

; should not find non-existing word
(deftest swipe-search-2
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["f" "o" "u" "r"]) '()))))

; should try double letters
(deftest swipe-search-3
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["t" "r" "e"])) '("three"))))

; should find multiple words
(deftest swipe-search-4
  (let [tree (build-tree "one two three the")]
    (is (= (swipe-search tree ["t" "h" "r" "e"]) '("three" "the")))))

; should skip intermediate letters
(deftest swipe-search-5
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["o" "z" "y" "z" "n" "e"]) '("one"))))) 

; must use last character
(deftest swipe-search-6
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["o" "n" "e" "!"]) '()))))

; must use first character
(deftest swipe-search-7
  (let [tree (build-tree "one two three")]
    (is (= (swipe-search tree ["d" "o" "n" "e"]) '()))))
