(ns gui.math2-test
    (:require
     [cljs.test :refer-macros [deftest is testing]]
     [gui.math2 :as math2]))

(deftest segment2-test
  (is (= (math2/segment2 [1.0 0.0] [10.0 10.0]) {:trans [0.0 0.0] :basis [10.0 10.0]} )))

;;(deftest multiply-test-2
;;  (is (= (* 75 10) (multiply 10 75))))
