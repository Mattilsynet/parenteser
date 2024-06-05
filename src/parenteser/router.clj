(ns parenteser.router)

(defn prefix [page url]
  (let [locale (:page/locale page :nb)]
    (cond->> url
      (not= :nb locale)
      (str (name locale) "/"))))

(defn get-frontpage-url [page]
  (prefix page "/"))

(defn get-tag-url [page]
  (prefix page (str "/tag/" (name (:page/tag page)) "/")))
