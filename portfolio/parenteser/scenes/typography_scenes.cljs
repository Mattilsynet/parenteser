(ns parenteser.scenes.typography-scenes
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene headings
  [:div.text-content
   [:h1 "This is a h1!"]
   [:h2 "This is a h2"]
   [:h3 "This is a h3"]
   [:h4 "This is a h4"]
   [:h5 "This is a h5"]
   [:h6 "This is a h6"]])

(defscene paragraph
  [:div.text-content
   [:p "This right here is a paragraph. It should look OK and be nicely
   readable. After all, paragraphs will make up most of the text on the
   site."]])

(defscene kbd
  [:div.text-content
   [:p "This right here is a paragraph. It contains some keyboard shortcuts, like "
    [:kbd "C-c C-k"] " and " [:kbd "C-u <space>"]]])

(defscene code
  [:div.text-content
   [:p "This right here is a paragraph. It contains some inline code snippets, like "
    [:code "(map identity numbers)"] " and " [:kbd ":namespaced/keyword"]]])

(defscene small
  [:div.text-content
   [:p.text-s
    "This is a paragraph with the class " [:code ".text-s"] ", which makes the
    text just a little bit smaller, which can occasionally be useful."]])

(defscene really-small
  [:div.text-content
   [:p.text-s
    "This is a paragraph with the class " [:code ".text-xs"] ", which makes the
    text quite a bit smaller, which should probably be used with care."]])
