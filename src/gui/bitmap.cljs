(ns gui.bitmap)

(defn init [w h]
  {:data (js/Uint32Array. (* w h))
   :width w
   :height h})

(defn fill [{:keys [data width height] :as bmp} color ]
  (let [newdata (.fill data color)]
    (assoc bmp :data newdata)))
