(ns parenteser.router)

(defn prefix [locale url]
  (let [locale (or locale :nb)]
    (cond->> url
      (not= :nb locale)
      (str (name locale) "/"))))

(defn get-frontpage-url [page]
  (prefix (:page/locale page) "/"))

(defn get-tag-url
  ([page]
   (get-tag-url (:page/locale page) (:tag/id (:page/tag page))))
  ([locale tag]
   (prefix locale (str "/" (name tag) "/"))))
