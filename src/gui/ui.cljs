(ns gui.ui)

;; https://evanw.github.io/font-texture-generator/

(def image "font.png")
(def font
  {:name "Hack"
   :size 64
   :width 1024
   :height 256
   :count 95})

;; codePoint, x, y, width, height, originX, originY;
(defn getinfo [char]
  (case char
   " " [ 863, 118, 3, 3, 1, 1]
   "!" [ 45, 67, 13, 51, -13, 48]
   "\"" [ 703, 118, 20, 20, -9, 48]
   "#" [ 978, 67, 41, 48, 1, 47]
   "$" [ 171, 0, 32, 60, -4, 50]
   "%" [ 0, 118, 39, 47, 0, 46]
   "&" [ 465, 0, 40, 51, 0, 49]
   "'" [ 723, 118, 8, 20, -15, 48]
   "(" [ 203, 0, 17, 59, -8, 50]
   ")" [ 220, 0, 17, 59, -14, 50]
   "*" [ 582, 118, 33, 34, -3, 46]
   "+" [ 461, 118, 36, 36, -1, 39]
   "," [ 615, 118, 15, 26, -11, 12]
   "-" [ 798, 118, 29, 8, -5, 23]
   "." [ 785, 118, 13, 13, -13, 11]
   "/" [ 276, 0, 33, 55, -2, 48]
   "0" [ 642, 0, 33, 51, -3, 49]
   "1" [ 889, 67, 30, 49, -6, 48]
   "2" [ 97, 67, 32, 50, -3, 49]
   "3" [ 675, 0, 33, 51, -3, 49]
   "4" [ 592, 67, 35, 49, -2, 48]
   "5" [ 129, 67, 32, 50, -3, 48]
   "6" [ 708, 0, 33, 51, -3, 49]
   "7" [ 161, 67, 32, 50, -3, 49]
   "8" [ 741, 0, 33, 51, -3, 49]
   "9" [ 540, 0, 34, 51, -2, 49]
   ":" [ 497, 118, 13, 35, -13, 36]
   ";" [ 29, 67, 16, 51, -11, 35]
   "<" [ 510, 118, 36, 34, -1, 37]
   "=" [ 667, 118, 36, 20, -1, 30]
   ">" [ 546, 118, 36, 34, -1, 37]
   "?" [ 437, 0, 28, 52, -6, 50]
   "@" [ 237, 0, 39, 56, 1, 45]
   "A" [ 366, 67, 39, 49, 0, 48]
   "B" [ 662, 67, 33, 49, -4, 48]
   "C" [ 840, 0, 32, 51, -3, 49]
   "D" [ 695, 67, 33, 49, -3, 48]
   "E" [ 858, 67, 31, 49, -5, 48]
   "F" [ 919, 67, 30, 49, -6, 48]
   "G" [ 574, 0, 34, 51, -2, 49]
   "H" [ 728, 67, 33, 49, -3, 48]
   "I" [ 949, 67, 29, 49, -5, 48]
   "J" [ 256, 67, 29, 50, -2, 48]
   "K" [ 482, 67, 37, 49, -3, 48]
   "L" [ 794, 67, 32, 49, -5, 48]
   "M" [ 556, 67, 36, 49, -1, 48]
   "N" [ 761, 67, 33, 49, -3, 48]
   "O" [ 608, 0, 34, 51, -2, 49]
   "P" [ 826, 67, 32, 49, -4, 48]
   "Q" [ 134, 0, 37, 61, -2, 49]
   "R" [ 519, 67, 37, 49, -3, 48]
   "S" [ 774, 0, 33, 51, -3, 49]
   "T" [ 58, 67, 39, 50, 0, 49]
   "U" [ 193, 67, 32, 50, -3, 48]
   "V" [ 444, 67, 38, 49, 0, 48]
   "W" [ 285, 67, 41, 49, 1, 48]
   "X" [ 326, 67, 40, 49, 1, 48]
   "Y" [ 405, 67, 39, 49, 0, 48]
   "Z" [ 627, 67, 35, 49, -2, 48]
   "[" [ 112, 0, 22, 62, -9, 53]
   "\\" [ 309, 0, 33, 55, -3, 48]
   "]" [ 89, 0, 23, 62, -7, 53]
   "^" [ 630, 118, 37, 20, -1, 48]
   "_" [ 827, 118, 36, 6, -1, -5]
   "`" [ 767, 118, 18, 14, -7, 53]
   "a" [ 73, 118, 33, 39, -3, 37]
   "b" [ 406, 0, 31, 52, -5, 50]
   "c" [ 139, 118, 30, 39, -4, 37]
   "d" [ 342, 0, 32, 52, -2, 50]
   "e" [ 39, 118, 34, 39, -2, 37]
   "f" [ 935, 0, 30, 51, -4, 50]
   "g" [ 374, 0, 32, 52, -3, 38]
   "h" [ 995, 0, 29, 51, -5, 50]
   "i" [ 0, 67, 29, 51, -7, 50]
   "j" [ 8, 0, 22, 64, -6, 50]
   "k" [ 807, 0, 33, 51, -6, 50]
   "l" [ 965, 0, 30, 51, -4, 50]
   "m" [ 235, 118, 35, 38, -2, 37]
   "n" [ 270, 118, 29, 38, -5, 37]
   "o" [ 106, 118, 33, 39, -3, 37]
   "p" [ 872, 0, 32, 51, -4, 37]
   "q" [ 904, 0, 31, 51, -3, 37]
   "r" [ 328, 118, 28, 38, -8, 37]
   "s" [ 169, 118, 29, 39, -5, 37]
   "t" [ 225, 67, 31, 50, -3, 49]
   "u" [ 299, 118, 29, 38, -5, 36]
   "v" [ 397, 118, 35, 37, -2, 36]
   "w" [ 356, 118, 41, 37, 1, 36]
   "x" [ 198, 118, 37, 38, -1, 37]
   "y" [ 505, 0, 35, 51, -2, 37]
   "z" [ 432, 118, 29, 37, -5, 36]
   "{" [ 30, 0, 30, 62, -2, 53]
   "|" [ 0, 0, 8, 67, -15, 50]
   "}" [ 60, 0, 29, 62, -7, 53]
   "~" [ 731, 118, 36, 17, -1, 27]
   [ 0, 0, 0, 0, 0, 0]
   ))


(defn get-label-glyphs [ text ]
  (reduce (fn [result letter]
         (let [[x y wth hth ox oy] (getinfo letter)
               {lx :rx ly :ry lwth :wth lhth :hth} (if (empty? result)
                                                   {:rx 0.0 :ry 100.0 :wth 0.0 :hth 0.0 }
                                                   (last result))
               tlx (/ x (:width font))
               tly (/ y (:height font))
               brx (/ (+ x wth) (:width font))
               bly (/ (+ y hth) (:height font))
               glyph {:img "font.png"
                      :str letter
                      :rx (+ lx lwth 1)
                      :ry ly
                      :x (- (+ lx lwth 5) ox)
                      :y (- ly oy)
                      :wth wth
                      :hth hth
                      :ttl [tlx tly]
                      :ttr [brx tly]
                      :tbl [tlx bly]
                      :tbr [brx bly]}]
           (conj result glyph))) [] text))


(defn set-label-glyphs-position [ glyphs [ px py ] ]
  (let [ { sx :x sy :y } ( first glyphs )
        dx (- px sx)
        dy (- py sy)]
    (map (fn [{ x :x y :y :as glyph }]
           (-> glyph
               (update :x (+ x dx))
               (update :y (+ y dy)))) glyphs)))


(defn get-rect [ x y w h col texmap ]
  ;; if texmap contains the color, get the coords
  ;; (let [ coords ( if (textmap hastex str color)
  ;; (texmap tex str color)
  ;; (texmap add str color colorbmp)
  
  ;;texmap get str color)]
  ;; 
  ;;(let [result {:tex texmap
    ;;            :x x
      ;;          :y y
        ;;        :wth w
          ;;      :hth h
            ;;    :ttl [tlx tly]
              ;;  :ttr [brx tly]
              ;;  :tbl [tlx bly]
              ;;  :tbr [brx bly]}]
   ;; )
  )
