(ns parenteser.router)

(defn prefix [locale url]
  (let [locale (or locale :nb)]
    (cond->> url
      (not= :nb locale)
      (str (name locale) "/"))))

(defn get-frontpage-url [page]
  (prefix (:page/locale page) "/"))

(defn get-tag-url [tag-entity]
  (prefix (:page/locale tag-entity)
          (str "/" (or (:tag/slug tag-entity)
                       (name (:tag/id tag-entity))) "/")))
