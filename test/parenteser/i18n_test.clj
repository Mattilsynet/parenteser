(ns parenteser.i18n-test
  (:require [clojure.test :refer [deftest is testing]]
            [parenteser.i18n :as sut]))

(deftest enumerate-test
  (testing "Separates by comma"
    (is (= (sut/enumerate ["banan" "båt" "hest"])
           ["banan" ", " "båt" " og " "hest"]))))
