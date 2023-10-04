(ns parenteser.elements)

(defn byline [{:keys [published updated tags]}]
  [:p.byline.text-s
   (cond
     (and published updated)
     [:span.date (str updated " (published " published ")")]

     published [:span.date published])
   (when tags
     [:span.subtle (->> (for [{:keys [title url]} tags]
                          [:a {:href url} title])
                        (interpose ", "))])])

(defn teaser [{:keys [url title description kind] :as props}]
  [:div.teaser {:class kind}
   [:div.teaser-content
    [:h4.h4 {} [:a {:href url} title]]
    description
    (byline props)]])

(defn teaser-section [{:keys [teasers]}]
  [:div.section.teasers
   [:div.content
    [:div.section-content
     [:div.teaser-list (map teaser teasers)]]]])

(defn header-section [{:keys [title description]}]
  [:div.section.header
   [:div.content
    [:div.section-content
     (when title [:h1.h1 title])
     (when description [:p.desc  description])]]])
