(ns gui.webgl
  (:require [gui.bitmap :as bitmap]
            [gui.texmap :as texmap]
            [clojure.string :as str]
            [cljs-webgl.context :as context]
            [cljs-webgl.shaders :as shaders]
            [cljs-webgl.texture :as texture]
            [cljs-webgl.buffers :as buffers]
            [cljs-webgl.typed-arrays :as arrays]
            [cljs-webgl.constants.shader :as shader]
            [cljs-webgl.constants.buffer-object :as buffer-object]
            [cljs-webgl.constants.draw-mode :as draw-mode]
            [cljs-webgl.constants.data-type :as data-type]
            [cljs-webgl.constants.capability :as capability]
            [cljs-webgl.constants.blending-factor-dest :as blend]
            [cljs-webgl.constants.texture-unit :as texture-unit]))

(def ui-vertex-source
  "attribute highp vec4 position;
   attribute highp vec2 texcoord;
   varying highp vec2 texcoordv;
   uniform mat4 projection;
   void main ( )
   {
      gl_Position = projection * position;
      texcoordv = texcoord;
   }")

(def ui-fragment-source
  "varying highp vec2 texcoordv;
   uniform sampler2D texture_main;
   void main( )
   {
      gl_FragColor = texture2D( texture_main , texcoordv);
}")

(defn init []
  (let [context (context/get-context (.getElementById js/document "main"))

        ui-shader (shaders/create-program
                   context
                   (shaders/create-shader context shader/vertex-shader ui-vertex-source)
                   (shaders/create-shader context shader/fragment-shader ui-fragment-source))
        
        ui-buffer (buffers/create-buffer
                   context
                   (arrays/float32 [0.0 0.0 0.0 0.0])
                   buffer-object/array-buffer
                   buffer-object/static-draw)

        ui-texmap (texmap/init 1024 1024 0 0 0 0)
        ui-texture (.createTexture context) 
        ui-location-pos (shaders/get-attrib-location context ui-shader "position")
        ui-location-texcoord (shaders/get-attrib-location context ui-shader "texcoord")]
    
    {:context context
     :textures {}
     :ui-shader ui-shader
     :ui-buffer ui-buffer
     :ui-texmap ui-texmap
     :ui-texture ui-texture
     :ui-location-pos ui-location-pos
     :ui-location-texcoord ui-location-texcoord}))


(defn width-for-glyph [height text]

  )

(defn bitmap-for-glyph [height text]
  (let [context (.getContext (. js/document getElementById "temp") "2d" )]
    (set! (.-font context) (str height "px Cantarell"))
    (set! (.-fillStyle context) "#000000")
    (set! (.-textBaseline context) "middle")
    (let [width (int (.-width (.measureText context text)))]
      (.fillText context text 0 (/ height 2))
      {:data (.-data (.getImageData context 0 0 width height))
       :width width
       :height height})))


(defn tex-gen-for-ids [ui-texmap texids]
  "generates textures for descriptor"
  (loop [ids texids
         tmap ui-texmap]
    (if (empty? ids)
      tmap
      (let [id (first ids)
            newtmap (if (texmap/hasbmp? tmap id)
                      tmap
                      (cond
                        
                        ;; show full texture in quad
                        (str/starts-with? id "debug")
                        (assoc-in tmap [:contents id] [0 0 1 1])

                        ;; show image in quad
                        (str/starts-with? id "image")
                        (tmap)

                        ;; show color in quad
                        (str/starts-with? id "color")
                        (let [rem (subs id 8)
                              r (js/parseInt (subs rem 0 2) 16)
                              g (js/parseInt (subs rem 2 4) 16)
                              b (js/parseInt (subs rem 4 6) 16)
                              a (js/parseInt (subs rem 6 8) 16)]
                          (texmap/setbmp tmap id (bitmap/init 10 10 r g b a)))

                        ;; show glyph
                        (str/starts-with? id "glyph")
                        (let [bmp (bitmap-for-glyph 50 "Milcsike")]
                          (texmap/setbmp tmap id bmp))

                        ;; return empty texmap if unknown
                        :default
                        tmap))]
        (recur (rest ids) newtmap)))))


(defn draw! [{:keys [context
                     textures
                     ui-shader
                     ui-buffer
                     ui-location-pos
                     ui-location-texcoord
                     ui-texmap
                     ui-texture] :as state }
             projection
             views]

  "draw views defined by x y width height and texure requirements." 

  (let [newtexids (map :id views)

        ;; generate textures for new views
        newtexmap (tex-gen-for-ids ui-texmap newtexids)

        ;; generate vertex data from views
        vertexes (flatten
                  (map
                   (fn [{:keys [x y wth hth id]}]
                     (let [[tlx tly brx bry] (texmap/getbmp newtexmap id)]
                        (concat
                        [x y] [tlx tly]
                        [(+ x wth) y] [brx tly]
                        [x (+ y hth)] [tlx bry]
                        
                        [(+ x wth) y] [brx tly]
                        [(+ x wth) (+ y hth)] [brx bry]
                        [x (+ y hth)] [tlx bry] ))) views))] 

    ;; upload texture map if changed
    (if (newtexmap :changed)
      (texture/upload-texture
       context
       ui-texture
       (:data (:bitmap newtexmap))
       1024
       1024))

    ;; upload buffer 
    (buffers/upload-buffer
     context ui-buffer
     (arrays/float32 vertexes)
     buffer-object/array-buffer
     buffer-object/dynamic-draw)

    ;; clear canvas
    (buffers/clear-color-buffer
     context
     0.1
     0.1
     0.4
     1.0)

    ;; draw vertexes
    (buffers/draw!
     context
     :count (/ (count vertexes) 4)
     :first 0
     :shader ui-shader
     :draw-mode draw-mode/triangles
     :attributes [{:buffer ui-buffer
                   :location ui-location-pos
                   :components-per-vertex 2
                   :type data-type/float
                   :offset 0
                   :stride 16}
                  {:buffer ui-buffer
                   :location ui-location-texcoord
                   :components-per-vertex 2
                   :type data-type/float
                   :offset 8
                   :stride 16}]
     :uniforms [{:name "projection"
                 :type :mat4
                 :values projection}
                {:name "texture_main"
                 :type :sampler-2d
                 :values 0}]
     :capabilities {capability/blend true}
     :blend-function [[blend/src-alpha blend/one-minus-src-alpha]]
     :textures [{:texture ui-texture 
                :name "texture_main"
                :texture-unit texture-unit/texture0}])

    (assoc state :ui-texmap (assoc newtexmap :changed false))))
