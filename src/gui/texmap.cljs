(ns gui.texmap)

(defn init [w h]
  {:data (js/Uint32Array. (* w h))
   :width w
   :height h
   :contents {}
   :rowh 0
   :rowx 0
   :rowy 0})

(defn hasbmp? [ { contents :contents } id ]
  (contains? contents id))

(defn getbmp [ { contents :contents } id ]
  (contents get id))

(defn setbmp! [{:keys [data width height contents rowx rowy rowh] :as texmap}
               {newdata :data newwidth :width newheight :height :as newbmp}
               id]
  
  ;; get x and y position for the new content, check overflow
  (let [newy (if (> (+ rowx newwidth) width)
               rowh
               rowy)
        
        newx (if (> (+ rowx newwidth) width)
               0
               rowx)
        
        over? (or (> (+ newx newwidth) width ) (> (+ newy newheight) height))]

    (if over?
      nil
      ;; copy new bitmap line by line to texture
      (loop [index 0]

        (println "index" index)

        (let [linestart (* index newwidth)
              lineend (+ (* index newwidth) newwidth)
              line (.slice newdata linestart lineend)
              
              prevline (if (= newy 0)
                         0
                         (- newy 1))
              nextstart (+ (* (+ prevline index) width) newx)]

          (.set data line nextstart)

          (println "start end next nextdata" linestart lineend nextstart)
          
          (if (< index newheight)
            (do
              (println "ONE")
              (recur (inc index))
              )
            (-> texmap
                (assoc-in [:contents id] [ [newx newy] [(+ newx newwidth) (+ newy newheight)]])
                (assoc :data data))
            )
          )
        )
      )
    )
  )
