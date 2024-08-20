(ns parenteser.blog-posts
  (:require [clojure.set :as set]
            [datomic-type-extensions.api :as d]
            [parenteser.router :as router]
            [powerpack.markdown :as md]))

(defn get-entities [db eids]
  (->> eids
       (map #(d/entity db %))
       (sort-by :blog-post/published)
       reverse))

(defn get-blog-posts
  ([db]
   (get-blog-posts db nil))
  ([db locales]
   (->> (d/q '[:find [?e ...]
               :in $ [?locales ...]
               :where
               [?e :page/uri]
               [?e :page/kind :page.kind/blog-post]
               [?e :page/locale ?locales]]
             db
             (cond
               (keyword? locales) #{locales}
               (nil? locales) #{:nb :en}
               :else locales))
        (get-entities db))))

(defn get-relevant-posts
  "Picks posts that share at least one tag with the reference post. Returns a list
  of posts sorted by the most relevant first (most shared techs). Ties are
  weighted in favor of posts with the same author."
  [blog-post]
  (let [tags (set (map :tag/id (:blog-post/tags blog-post)))
        author (:blog-post/author blog-post)
        db (d/entity-db blog-post)]
    (when-not (empty? tags)
      (->> (d/q '[:find [?e ...]
                  :in $ ?url ?locale [?tag ...]
                  :where
                  [?t :tag/id ?tag]
                  [?e :blog-post/tags ?t]
                  [?e :page/locale ?locale]
                  (not [?e :page/uri ?url])]
                db (:page/uri blog-post) (:page/locale blog-post) tags)
           (get-entities db)
           (map (fn [p]
                  [(cond-> (* 2 (count (set/intersection tags (:blog-post/tags p))))
                     (= (:blog-post/author p) author) inc)
                   p]))
           (sort-by (comp - first))
           (map second)))))

(defn get-adjacent-posts [xs x]
  (loop [newer nil
         [curr & xs] xs]
    (cond
      (nil? curr) nil
      (= x curr) (remove nil? [newer (first xs)])
      :else (recur curr xs))))

(defn distinct-by [k xs]
  (loop [used #{}
         res []
         xs xs]
    (let [x (first xs)
          xk (k x)]
      (cond
        (empty? xs) res
        (contains? used xk) (recur used res (rest xs))
        :else (recur (conj used xk) (conj res x) (rest xs))))))

(defn get-related-posts
  "Tries to pick n related posts to present as further reading. It's a bit
  involved, but here's our goals for this:

  1. It should be possible to eventually visit every post by following these
     links from post to post
  2. If there are relevant posts, like part 2, same topic etc, some of those
     should be included
  3. Most posts should have something new as related

  At the very least, include the post published before this one - if this is the
  first post, include the latest one published. This creates a circle, so one
  can reach every post. Fill the remaining spots with topically related posts,
  if possible, and pad out the remaining spots with the post published after
  this one, then just some newly published posts."
  [blog-post & [n]]
  (let [n (or n 5)
        latest (get-blog-posts (d/entity-db blog-post) (:page/locale blog-post))
        relevant (get-relevant-posts blog-post)
        [next-post previous-post] (get-adjacent-posts latest blog-post)]
    (->> (concat (take (dec n) relevant)
                 [(or previous-post (first latest)) next-post]
                 (take (inc n) latest))
         (remove nil?)
         (distinct-by :db/id)
         (remove #(= (:db/id %) (:db/id blog-post)))
         (take n)
         (sort-by :blog-post/published)
         reverse)))

(defn prepare-tags [tags]
  (seq (for [tag tags]
         [:a {:href (router/get-tag-url tag)}
          (:tag/name tag)])))

(defn get-blog-post-vcard [{:blog-post/keys [author authors tags vcard-photo] :as page}]
  {:image (some->> (or vcard-photo (:person/photo author))
                   (str "/round-small"))
   :images (seq (map #(str "/round-small" (:person/photo %)) authors))
   :image-alt (:person/given-name author)
   :title [:i18n ::vcard-authors {:authors (map :person/given-name (or authors [author]))}]
   :body (when-let [tags (prepare-tags tags)]
           [:i18n ::vcard-tags {:tags tags}])})

(defn prepare-blog-post-teaser [{:blog-post/keys [description published series]
                                 :page/keys [title uri]
                                 :as blog-post}]
  (cond-> {:title {:text title
                   :uri uri}
           :kicker {:text (some-> (:series/name series) (str ": "))
                    :uri (:page/uri series)}
           :description (md/render-html description)
           :aside (get-blog-post-vcard blog-post)
           :kind :teaser-article}
    published (assoc :footer [:i18n :datetime/short-date published])))

(comment

  (def db (d/db (:datomic/conn (powerpack.dev/get-app))))

  (->> (d/entity db [:page/uri "/nats-import-eksport/"])
       (get-related-posts)
       (map :page/title))
)
