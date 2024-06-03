(ns parenteser.portfolio
  (:require [portfolio.ui :as ui]
            parenteser.scenes.vcard-scenes))

:parenteser.scenes.vcard-scenes/keep

(ui/start!
 {:config
  {:css-paths ["/css/parenteser.css"]}})
