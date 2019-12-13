(ns gui.ui
  (:require [gui.texmap :as texmap]
            [gui.bitmap :as bitmap]
            [gui.webgl :as webgl]
            [clojure.string :as str]))


(defn gen-hash [n]
   (let [chars (map char (concat (range 48 57) (range 65 90) (range 97 122)))
         password (take n (repeatedly #(rand-nth chars)))]
     (reduce str password)))


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


(defn gen-view [id name class width height color]
  (println "gen-view" id name class width height color)
  {:cl class
   :id id
   :na name
   :x 0
   :y 0
   :w width
   :h height
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
    {:cl "Label"
     :id (keyword (gen-hash 8))
     :na ""
     :x 0
     :y 0
     :w lw
     :h lh
     :te text
     :tx (str "Glyph " size "%" text)
     :ha "0"
     :va "0"}))


(defn add-view [{:keys [viewmap views] :as ui} view]
  (let [newviews (conj views (view :id))
        newviewmap (assoc viewmap (view :id) view)]
    (-> ui
        (assoc :viewmap newviewmap)
        (assoc :views newviews))))


(defn gen-from-desc [desc]
  (let [lines (str/split-lines desc)]
    (reduce
     (fn [{:keys [viewmap views] :as ui} line]
       ;; analyze lines, convert to view if not ends with |
       (if-not (or (= (count line) 0) (str/ends-with? line "|"))
         (let [desc (parse-desc line)
               hash (keyword (gen-hash 8))
               view (-> (gen-view hash (desc :id) (desc :cl) (desc :w) (desc :h) (desc :bc))
                        (add-align (desc :ta) (desc :ba) (desc :la) (desc :ra) (desc :ha) (desc :va)))]           
           (cond-> ui
             true (add-view view)
             (not= (desc :te) nil) (add-view (gen-label (desc :te) 40))))
         ui))
     {:viewmap {}
      :views [] }
     lines)))


(defn get-view [views id]
  (first (filter (fn [view] (= (view :id) id)) views)))

(defn get-index [views id]
  (first (remove nil? (keep-indexed #(if (= (%2 :id) id)
                   %1
                   nil) views))))

(defn align-view [views id width height]
  (let [view (get-view views id)
        {:keys [x y w h ta ba la ra va ha]} view
        taview (get-view views ta)
        baview (get-view views ba)
        laview (get-view views la)
        raview (get-view views ra)
        haview (get-view views ha)
        vaview (get-view views va)
        newview (-> view
            (assoc :x (cond
                        ;; align to view on the left or to screen edge
                        (not= la nil)
                        (if (= la "0")
                      0
                      (+ (laview :x) (laview :w)))
                        ;; align to view on the right or to screen edge
                        (not= ra nil)
                        (if (= ra "0")
                          (- width w)
                          (- (raview :x) w))
                        ;; align to horizontal center or between left align and right align view
                        (not= ha nil)
                        (if (= ha "0")
                          (- (/ width 2) (/ w 2))
                          (- (- (laview :x) (/ (- (raview :x)(+ (laview :x)(laview :w))) 2) ) (/ w 2)))
                        ;; or leave x position as is
                        :default
                    x))
            (assoc :y (cond
                        ;; align to view on the top or to screen edge
                        (not= ta nil)
                        (if (= ta "0")
                          0
                          (+ (taview :y)(taview :h)))
                        ;; align to view on the bottom or to screen edge
                        (not= ba nil)
                        (if (= ba "0")
                          (- height h)
                          (- (baview :y) h))
                        ;; align to vertical center or between bottom and top align view
                        (not= va nil)
                        (if (= va "0")
                          (- (/ height 2) (/ h 2))
                      (- (- (baview :y) (/ (- (baview :y)(+ (taview :y)(taview :h))) 2 )) (/ h 2)))
                        :default
                        y)))]
    (println "aligning" (view :id) ta ba la ra ha va newview)
    newview
    ))


(defn align [views width height]
  "iterate through all views and align them based on their alignment switches"
  (reduce (fn [result view]
            (let [index (get-index views (view :id))
                  newview (align-view result (view :id) width height)]
              (assoc result index newview)))
          views
          views))
