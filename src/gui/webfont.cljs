(ns gui.webfont
  (:require [gui.bitmap :as bitmap]))

(defn bitmap-for-glyph [ ]
  (let [context (.getContext (. js/document getElementById "temp") "2d")
        width (int (.-width (.measureText context "Tóth Milán")))
        height 40]
    (set! (.-font context) "40px Cantarell")
    (.fillText context "Tóth Milán" 0 30)
    {:data (.-data (.getImageData context 0 0 width 40))
     :width width
     :height 40}))
