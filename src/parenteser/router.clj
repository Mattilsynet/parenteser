(ns parenteser.router)

(defn get-frontpage-url [page]
  (if (= :en (:page/locale page))
    "/en/"
    "/"))
