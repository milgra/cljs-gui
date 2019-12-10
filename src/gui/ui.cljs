(ns gui.ui
  (:require [gui.texmap :as texmap]
            [gui.bitmap :as bitmap]
            [gui.webgl :as webgl]
            [clojure.string :as str]))

(defn label [x y w h text size]
  (let [{lw :width lh :height} (webgl/sizes-for-glyph text size)]
    [{:X x
      :Y y
      :WI w
      :HE h
      :TX "Color 0xFFFFFF99"}
     {:X (+ x (/ (- w lw) 2))
      :Y (+ y (/ (- h lh) 2))
      :WI lw
      :HE lh
      :TE text
      :TX (str "Glyph " size "%" text) 
      }
     ]))


;;C CLButton TEContinue BCFFFFFF55 FCFFFFFFFF BAN HA0 WI150 HE50
;;N CLButton TENew~Game BCFFFFFF55 FCFFFFFFFF BA0 HA0 WI150 HE50
;;O CLButton TEOptions BCFFFFFF55 FCFFFFFFFF VA0 HA0 WI150 HE50
;;D CLButton TEDonate BCFFFFFF55 FCFFFFFFFF TAO HA0 WI150 HE50

(defn generate [desc]
  (let [lines (str/split-lines desc)]
    (reduce
     (fn [descarray line]
       ;; analyze lines, convert to view if not ends with |
       (if-not (or (= (count line) 0) (str/ends-with? line "|" ))
         (let [words (str/split line #" ")
               desc (reduce
                     (fn [result word]
                       ;; analyze word, add extracted properties to final strucure
                       (cond
                         (= (count word) 1)
                         (assoc result :ID word)
                         (str/starts-with? word "TE")
                         (assoc result :TE (subs word 2))
                         (str/starts-with? word "CL")
                         (assoc result :CL (subs word 2))
                         (str/starts-with? word "BC")
                         (assoc result :BC (subs word 2))
                         (str/starts-with? word "FC")
                         (assoc result :FC (subs word 2))
                         (str/starts-with? word "WI")
                         (assoc result :WI (js/parseInt (subs word 2) 10))
                         (str/starts-with? word "HE")
                         (assoc result :HE (js/parseInt (subs word 2) 10))
                         (or (str/starts-with? word "TA")
                             (str/starts-with? word "BA")
                             (str/starts-with? word "LA")
                             (str/starts-with? word "RA")
                             (str/starts-with? word "VA")
                             (str/starts-with? word "HA"))
                         (assoc result (keyword (subs word 0 2)) (subs word 2)) 
                         :default
                         result)
                       )
                     {}
                     words)
               descf (-> desc
                         (assoc :X 0)
                         (assoc :Y 0)
                         (assoc :TX (str "Color " (desc :BC))))
               ]
           (conj descarray descf))
         descarray))
     []
     lines)))
