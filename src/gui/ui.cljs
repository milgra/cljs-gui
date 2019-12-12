(ns gui.ui
  (:require [gui.texmap :as texmap]
            [gui.bitmap :as bitmap]
            [gui.webgl :as webgl]
            [clojure.string :as str]))

(defn label [x y w h text size]
  (let [{lw :width lh :height} (webgl/sizes-for-glyph text size)]
    [{:x x
      :y y
      :w w
      :h h
      :tx "Color 0xFFFFFF99"}
     {:x (+ x (/ (- w lw) 2))
      :y (+ y (/ (- h lh) 2))
      :w lw
      :h lh
      :te text
      :tx (str "Glyph " size "%" text)}]))


(defn gen-view [class width height color]
  {:x 0
   :y 0
   :w width
   :h height
   :cl class
   :tx (str "Color 0x" color)})


(defn add-align [view ta ba la ra ha va]
  (-> view
      (assoc :ta ta)
      (assoc :ba ba)
      (assoc :la la)
      (assoc :ra ra)
      (assoc :ha ha)
      (assoc :va va)))


;;C CLButton TEContinue BCFFFFFF55 FCFFFFFFFF BAN HA0 WI150 HE50
;;N CLButton TENew~Game BCFFFFFF55 FCFFFFFFFF BA0 HA0 WI150 HE50
;;O CLButton TEOptions BCFFFFFF55 FCFFFFFFFF VA0 HA0 WI150 HE50
;;D CLButton TEDonate BCFFFFFF55 FCFFFFFFFF TAO HA0 WI150 HE50


(defn parse-desc [desc]
  (let [words (str/split desc #" ")]
        (reduce
         (fn [result word]
           ;; analyze word, add extracted properties to final strucure
           (cond
             (= (count word) 1)
             (assoc result :id word)
             (str/starts-with? word "WI")
             (assoc result :w (js/parseInt (subs word 2) 10))
             (str/starts-with? word "HE")
             (assoc result :h (js/parseInt (subs word 2) 10))
             :default
             (assoc result (keyword (str/lower-case (subs word 0 2))) (subs word 2))))
         {}
         words)))


(defn gen-label [text size]
  (let [{lw :width lh :height} (webgl/sizes-for-glyph text size)]
    {:x 0
     :y 0
     :w lw
     :h lh
     :te text
     :tx (str "Glyph " size "%" text)
     :ha "0"
     :va "0"}))


(defn gen-from-desc [desc]
  (let [lines (str/split-lines desc)]
    (reduce
     (fn [views line]
       ;; analyze lines, convert to view if not ends with |
       (if-not (or (= (count line) 0) (str/ends-with? line "|"))
         (let [desc (parse-desc line)
               view (-> (gen-view (desc :cl) (desc :w) (desc :h) (desc :bc))
                        (add-align (desc :ta) (desc :ba) (desc :la) (desc :ra) (desc :ha) (desc :va)))]
           (cond-> views
             true (conj view)
             (not= (desc :te) nil) (conj (gen-label (desc :te) 40))))
         views))
     []
     lines)))


(defn get-view [views id]

  )


(defn align-view [views id width height]
  (let [{:keys [x y w h ta ba la ra va ha]} view
        taview (get-view views ta)
        baview (get-view views ba)
        laview (get-view views la)
        raview (get-view views ra)
        haview (get-view views ha)
        vaview (get-view views va)])
  views
  )


(defn align [views width height]
  "iterate through all views and align them based on their alignment switches"
  (map (fn [{:keys [x y w h ta ba la ra va ha] :as view}]
         (-> view
             (assoc :x (cond
                         (= la "0")
                         0
                         (= ra "0")
                         (- width w)
                         (= ha "0")
                         (- (/ width 2) (/ w 2))
                         :default
                         x))
             (assoc :y (cond
                         (= ta "0")
                         0
                         (= ba "0")
                         (- height h)
                         (= va "0")
                         (- (/ height 2) (/ h 2))
                         :default
                         y))))
       views))
