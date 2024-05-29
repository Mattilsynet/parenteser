(ns parenteser.blog-post-page
  (:require [clojure.string :as str]
            [dev.onionpancakes.chassis.core :as chassis]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.i18n :as i18n]
            [parenteser.layout :as layout]
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
        {:prelude "Lyst til å lese videre? Her er det neste innlegget i serien:"
         :post next-post}
        (when (seq other-posts)
          {:prelude "Gikk du glipp av starten? Her er det første innlegget i serien:"
           :post (first other-posts)}))
      (when-let [newest-post (last other-posts)]
        {:prelude "Her er det siste innlegget i serien:"
         :post newest-post}))))

(defn render-series-conclusion [blog-post series]
  (let [blurb (get-series-blurb series)
        {:keys [prelude post]} (get-relevant-post blog-post series)]
    (when post
      [:div.section.slim
       [:div.content.info-section
        [:div.section-content.text-content
         (list
          [:p.mbl blurb " " prelude]
          (e/teaser (-> (blog-posts/prepare-blog-post-teaser post)
                        (dissoc :kicker))))]]])))

(defn render-blog-post [blog-post]
  (let [series (:blog-post/series blog-post)
        published (:blog-post/published blog-post)]
    (layout/layout
     {:title (str (:page/title blog-post) " - Parenteser")}
     (e/header-section
      {:title "Parenteser"
       :slogan "Betraktninger fra Mat-teamets grønne enger"
       :href "/"})
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
                                         (i18n/format-ymd published)]]]))
           e/vcard)]]
     (when series
       (render-series-conclusion blog-post series)))))
