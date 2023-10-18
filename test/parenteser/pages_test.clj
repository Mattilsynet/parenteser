(ns parenteser.pages-test
  (:require [clojure.test :refer [deftest is testing]]
            [parenteser.pages :as sut]))

(deftest comma-separated-test
  (testing "Separates by comma"
    (is (= (sut/comma-separated ["banan" "båt" "hest"])
           ["banan" ", " "båt" " og " "hest"]))))
