(ns parenteser.i18n-test
  (:require [clojure.test :refer [deftest is testing]]
            [parenteser.i18n :as sut]))

(deftest enumerate-test
  (testing "Separates by comma"
    (is (= (sut/enumerate :nb ["banan" "båt" "hest"])
           ["banan" ", " "båt" " og " "hest"])))

  (testing "Enumerates in english"
    (is (= (sut/enumerate :en ["banana" "boat" "horse"])
           ["banana" ", " "boat" " and " "horse"]))))
