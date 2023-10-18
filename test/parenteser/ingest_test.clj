(ns parenteser.ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [parenteser.ingest :as sut]))

(deftest ingest-blog-post-test
  (testing "Strips URI"
    (is (= (-> {:page/uri "/blog-posts/lange-flate-filer/"}
               sut/ingest-blog-post
               :page/uri)
           "/lange-flate-filer/")))

  (testing "Turns tags into refs"
    (is (= (-> {:blog-post/tags [:clojure :html]}
               sut/ingest-blog-post
               :blog-post/tags)
           [{:tag/id :clojure}
            {:tag/id :html}])))

  (testing "Defaults og:title to page/title"
    (is (= (-> {:page/title "Her blir'e liv, rai rai!"}
               sut/ingest-blog-post
               :open-graph/title)
           "Her blir'e liv, rai rai!")))

  (testing "Prefers specific og:title"
    (is (= (-> {:page/title "Her blir'e liv, rai rai!"
                :open-graph/title "Tittel"}
               sut/ingest-blog-post
               :open-graph/title)
           "Tittel")))

  (testing "Defaults og:description to blog-post/description"
    (is (= (-> {:blog-post/description "Dette er et spennende innlegg"}
               sut/ingest-blog-post
               :open-graph/description)
           "Dette er et spennende innlegg")))

  (testing "Prefers specific og:description"
    (is (= (-> {:blog-post/description "Dette er et spennende innlegg"
                :open-graph/description "Beskrivelse, kort"}
               sut/ingest-blog-post
               :open-graph/description)
           "Beskrivelse, kort"))))
