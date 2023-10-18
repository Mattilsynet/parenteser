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

(defn vcard [{:keys [image url title body]}]
  [:div.vcard
   (when image
     (let [img [:img.img {:src image :width 92}]]
       (if url
         [:a {:href url} img]
         img)))
   [:div
    [:h5.h5.vcard-title
     (if url
       [:a {:href url} title]
       title)]
    [:p.vcard-body body]]])

(defn teaser [{:keys [url title description kind published aside]}]
  [:div.teaser {:class kind}
   [:article.teaser-content
    [:h4.h4 {} [:a {:href url} title]]
    [:div.teaser-body
     description]
    [:p [:span.byline.text-s published]]]
   [:aside.teaser-aside
    (vcard aside)]])

(defn teaser-section [{:keys [teasers]}]
  [:div.section.teasers
   [:div.content
    [:div.section-content
     [:div.teaser-list (map teaser teasers)]]]])

(defn header-section [{:keys [title slogan href]}]
  [:div.section.header
   [:div.banana-header
    [:div.banana-1]
    [:div
     [:h1.h1.banana-h1
      (if href
        [:a {:href href} title]
        title)]
     [:p.slogan slogan]]
    [:div.banana-2]]])

(defn info-section [{:keys [title text]}]
  [:div.section.slim
   [:div.content.info-section
    [:div.section-content.text-content
     [:h2.h4 title]
     text]]])

(defn footer-section [{:keys [text]}]
  [:div.section.footer-section.slim
   [:div.content
    [:div.section-content
     text]]])
