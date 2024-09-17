(ns parenteser.ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [parenteser.ingest :as sut]))

(deftest ingest-blog-post-test
  (testing "Strips URI"
    (is (= (-> {:page/uri "/blog-posts/lange-flate-filer/"}
               sut/ingest-blog-post
               :page/uri)
           "/lange-flate-filer/")))

  (testing "Adds redirect URIs"
    (is (= (-> {:page/uri "/blog-posts/skrivefeil/"
                :page/alt-uris ["/skriveleif/"]}
               sut/ingest-blog-post-pages
               (->> (map #(select-keys % [:page/redirect-uri :page/uri]))))
           [{:page/uri "/skrivefeil/"}
            {:page/uri "/skriveleif/" :page/redirect-uri "/skrivefeil/"}])))

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

(deftest suggest-og-image-test
  (testing "Uses first image in post as og:image"
    (is (= (-> {:blog-post/body "Here is an image: ![A bird](/images/bird.jpg)"}
               sut/suggest-og-image
               last)
           "/images/bird.jpg")))

  (testing "Uses author image as og:image when no body image"
    (is (= (-> {:blog-post/body "Here is text"
                :blog-post/author {:person/photo "/images/magnar.jpg"}}
               sut/suggest-og-image
               last)
           "/images/magnar.jpg")))

  (testing "Prefers author's open graph image"
    (is (= (-> {:blog-post/body "Here is text"
                :blog-post/author {:person/photo "/images/magnar.jpg"
                                   :person/open-graph-photo "/images/magnar-og.jpg"}}
               sut/suggest-og-image
               last)
           "/images/magnar-og.jpg")))

  (testing "Prefers specific og:image"
    (is (= (-> {:blog-post/body "Here is an image: ![A bird](/images/bird.jpg)"
                :blog-post/author {:person/photo "/images/magnar.jpg"}
                :open-graph/image "/images/cow.jpg"}
               sut/suggest-og-image
               last)
           "/images/cow.jpg"))))
