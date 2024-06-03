(ns parenteser.elements)

(defn vcard [{:keys [image image-alt url title body class]}]
  [:div.vcard {:class class}
   (when image
     (let [img [:img.img {:src image
                          :alt image-alt
                          :width 92}]]
       (if url
         [:a {:href url} img]
         img)))
   [:div
    [:h5.h5.vcard-title
     (if url
       [:a {:href url} title]
       title)]
    [:p.vcard-body body]]])

(defn teaser [{:keys [url title kicker description kind published aside]}]
  [:div.teaser {:class kind}
   [:article.teaser-content
    [:h4.h4 {}
     (when kicker
       [:div.h6 kicker])
     [:a {:href url} title]]
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

(defn info-section [{:keys [title content]}]
  [:div.section.slim
   [:div.content.info-section
    [:div.section-content.text-content
     (when title [:h2.h4 title])
     content]]])

(defn footer-section [{:keys [text]}]
  [:div.section.footer-section.slim
   [:div.content
    [:div.section-content
     text]]])
