(ns gui.texmap
  (:require [gui.bitmap :as bitmap]))

(defn init [w h r g b a]
  (let [result {:bitmap (bitmap/init w h r g b a)
                :contents {}
                :rowh 0
                :rowx 0
                :rowy 0}]
    result))

(defn hasbmp? [ { contents :contents } id ]
  (contains? contents id))

(defn getbmp [ { contents :contents } id ]
  (contents get id))

(defn setbmp [{:keys [bitmap contents rowx rowy rowh] :as texmap}
              id
              {:keys [data width height] :as newbmp}]

  ;; get x and y position for the new content, check overflow
  (let [newy (if (> (+ rowx width) (bitmap :width))
               rowh
               rowy)
        
        newx (if (> (+ rowx width) (bitmap :width))
               0
               rowx)
        
        over? (or (> (+ newx width) (bitmap :width)) (> (+ newy height) (bitmap :height)))]

    (if over?
      nil
      (-> texmap
        (assoc-in [:contents id] [ [newx newy] [(+ newx width) (+ newy height)]])
        (assoc :bitmap (bitmap/insert bitmap newbmp newx newy))))))
