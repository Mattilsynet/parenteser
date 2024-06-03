(ns parenteser.portfolio
  (:require [portfolio.ui :as ui]
            parenteser.scenes.header-scenes
            parenteser.scenes.teaser-scenes
            parenteser.scenes.teaser-section-scenes
            parenteser.scenes.vcard-scenes))

:parenteser.scenes.header-scenes/keep
:parenteser.scenes.teaser-scenes/keep
:parenteser.scenes.teaser-section-scenes/keep
:parenteser.scenes.vcard-scenes/keep

(ui/start!
 {:config
  {:css-paths ["/css/parenteser.css"]}})
