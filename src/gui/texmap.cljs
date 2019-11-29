(ns gui.texmap
  (:require [gui.bitmap :as bitmap]))

(defn init [w h r g b a]
  (let [result {:bitmap (bitmap/init w h r g b a)
                :contents {}
                :changed true
                :rowh 0
                :rowx 0
                :rowy 0}]
    result))

(defn hasbmp? [ { contents :contents } id ]
  (contains? contents id))

(defn getbmp [ { contents :contents } id ]
  (get contents id))

(defn setbmp [{:keys [bitmap contents rowx rowy rowh] :as texmap}
              id
              {:keys [data width height] :as newbmp}
              inset]

  (let [newy (if (> (+ rowx width) (bitmap :width))
               rowh
               rowy)
        
        newx (if (> (+ rowx width) (bitmap :width))
               0
               rowx)

        neww  (+ newx width)

        newh  (+ newy height)

        newtlx (/ (+ newx inset) (bitmap :width))
        newtly (/ (+ newy inset) (bitmap :height))
        newbrx (/ (- neww inset) (bitmap :width))
        newbry (/ (- newh inset) (bitmap :height))
        
        over? (or (> neww (bitmap :width)) (> newh (bitmap :height)))]
    
    (if over?
      nil
      (-> texmap
          (assoc-in [:contents id] [ newtlx newtly newbrx newbry])
          (assoc :rowx neww)
          (assoc :rowy newy)
          (assoc :rowh rowh)
          (assoc :bitmap (bitmap/insert bitmap newbmp newx newy))
          (assoc :changed true)))))
