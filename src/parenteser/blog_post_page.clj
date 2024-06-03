(ns parenteser.blog-post-page
  (:require [clojure.string :as str]
            [dev.onionpancakes.chassis.core :as chassis]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]
            [parenteser.router :as router]
            [powerpack.markdown :as md]))

(defn get-series-blurb [series]
  (-> (:series/blurb series)
      (str/replace #"\[(.+)\]" (fn [[_ content]]
                                 (str "<a href='" (:page/uri series) "'>" content "</a>")))
      chassis/raw))

(defn get-relevant-post [blog-post series]
  (let [other-posts (->> (:blog-post/_series series)
                         (remove #{blog-post})
                         (sort-by :blog-post/published))]
    (if (:series/sequential? series)
      (if-let [next-post (first (drop-while #(.isBefore (:blog-post/published %)
                                                        (:blog-post/published blog-post))
                                            other-posts))]
        {:prelude [:i18n ::series-next-prelude]
         :post next-post}
        (when (seq other-posts)
          {:prelude [:i18n ::series-start-prelude]
           :post (first other-posts)}))
      (when-let [newest-post (last other-posts)]
        {:prelude [:i18n ::series-latest-prelude]
         :post newest-post}))))

(defn render-series-conclusion [blog-post series]
  (let [blurb (get-series-blurb series)
        {:keys [prelude post]} (get-relevant-post blog-post series)]
    (when post
      (e/info-section
       {:content
        (list
         [:p.mbl blurb " " prelude]
         (e/teaser (-> (blog-posts/prepare-blog-post-teaser post)
                       (dissoc :kicker))))}))))

(defn render-blog-post [blog-post]
  (let [series (:blog-post/series blog-post)
        published (:blog-post/published blog-post)]
    (layout/layout
     {:title [:i18n ::layout/page-title {:title (:page/title blog-post)}]}
     (layout/header {:href (router/get-frontpage-url blog-post)})
     [:div.section
      [:div.content
       [:div.text-content
        [:h1.h1
         (when series
           [:div.h4.mbxs [:a {:href (:page/uri series)} (:series/name series)] ": "])
         [:span (:page/title blog-post)]]
        (md/render-html (:blog-post/body blog-post))]
       (-> (blog-posts/get-blog-post-vcard blog-post)
           (assoc :class "mtxxl")
           (update :body (fn [b] [:div b
                                  [:div [:time.byline.text-s {:datetime published}
                                         [:i18n :datetime/short-date published]]]]))
           e/vcard)]]
     (when series
       (render-series-conclusion blog-post series)))))
