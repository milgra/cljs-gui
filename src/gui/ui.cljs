(ns gui.ui
  (:require [gui.texmap :as texmap]
            [gui.bitmap :as bitmap]
            [gui.webgl :as webgl]))

(defn label [x y w h text size]
  (let [{lw :width lh :height} (webgl/sizes-for-glyph text size)]
    [{:x x
      :y y
      :wth w
      :hth h
      :id (str "color 0xFFFFFF99") }
     {:x (+ x (/ (- w lw) 2))
      :y (+ y (/ (- h lh) 2))
      :wth lw
      :hth lh
      :id (str "glyph%" size "%" text)}
     ]))
