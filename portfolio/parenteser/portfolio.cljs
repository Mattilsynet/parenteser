(ns parenteser.portfolio
  (:require [portfolio.ui :as ui]
            parenteser.scenes.teaser-scenes
            parenteser.scenes.vcard-scenes))

:parenteser.scenes.teaser-scenes/keep
:parenteser.scenes.vcard-scenes/keep

(ui/start!
 {:config
  {:css-paths ["/css/parenteser.css"]}})
