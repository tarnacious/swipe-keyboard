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

(deftest add-inital-char 
  (let [result (add-word nil "a")
        expected [ nil { "a" { :meta :end }}]]
    (is (= result expected)))) 

(deftest add-second-char 
  (let [result (add-word (add-word nil "a") "b")
       expected [ nil { "a" { :meta :end } "b" { :meta :end }}]]
    (is (= result  expected)))) 

(deftest add-inital-word 
  (let [result (add-word nil "abc")
        expected [ nil { "a" { "b" { "c" { :meta :end }}}}]]
    (is (= result expected)))) 

(deftest add-completely-different-word 
  (let [result (add-word (add-word nil "abc") "dfg")
        expected [ nil { "a" { "b" { "c" { :meta :end }}}, 
                   "d" { "f" { "g" { :meta :end }}}}]]
    (is (= result expected))))

(deftest add-similar-word
  (let [result (add-word (add-word nil "abc") "abd")
        expected [ nil { "a" { "b" { "c" { :meta :end } 
                   "d" { :meta :end }}}}]]
    (is (= result expected)))) 

(deftest add-sub-word
  (let [result (add-word
               (add-word nil "abc") "ab")
        expected [ nil { "a" { "b" { "c" { :meta :end }
                        :meta :end }}}]]
    (is (= result expected)))) 

(deftest add-word-with-double-letter 
  (let [result (add-word nil "xxx")
        expected [ nil { "x" { "x" { "x" { :meta :end }}}}]]
    (is (= result expected)))) 

(deftest test-build-tree
  (let [tree (build-tree "one two tree")
        expected [nil {"o" {"n" {"e" { :meta :end }}}, 
                   "t" {"w" {"o" { :meta :end }}, 
                      "r" {"e" {"e" { :meta :end }}}}}]]
    (is (= tree expected))))

(deftest test-build-tree-with-extra-spaces
  (let [tree (build-tree "one   two   tree")
        expected [nil {"o" {"n" {"e" { :meta :end }}}, 
             "t" {"w" {"o" { :meta :end }}, 
                  "r" {"e" {"e" { :meta :end }}}}}]]
  (is (= tree expected))))

(deftest test-find-word-finds-word
  (let [tree (build-tree "one two tree")]
    (is (= (find-word tree "one") true))))

(deftest test-find-word-does-not-find-word
  (let [tree (build-tree "one two tree")]
    (is (= (find-word tree "four") false))))

(deftest test-find-word-does-not-find-big-word
  (let [tree (build-tree "one two tree")]
    (is (= (find-word tree "fourteen") false))))

(deftest test-find-word-does-not-find-small-word
  (let [tree (build-tree "one two tree")]
    (is (= (find-word tree "on") false))))

; should find exact word
(deftest swipe-search-1
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["o" "n" "e"])) '("one"))))

; should not find non-existing word
(deftest swipe-search-2
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["f" "o" "u" "r"]) '()))))

; should try double letters
(deftest swipe-search-3
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["t" "r" "e"])) '("tree"))))

; should find multiple words
(deftest swipe-search-4
  (let [tree (build-tree "one two tree tre")]
    (is (= (swipe-search tree ["t" "r" "e"]) '("tre" "tree")))))

; should skip intermediate letters
(deftest swipe-search-5
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["o" "z" "y" "z" "n" "e"]) '("one")))))
  
; must use last character
(deftest swipe-search-6
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["o" "n" "e" "!"]) '()))))

; must use first character
(deftest swipe-search-7
  (let [tree (build-tree "one two tree")]
    (is (= (swipe-search tree ["d" "o" "n" "e"]) '()))))
