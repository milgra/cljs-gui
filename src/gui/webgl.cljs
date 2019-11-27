(ns gui.webgl
  (:require [cljs-webgl.context :as context]
            [cljs-webgl.shaders :as shaders]
            [cljs-webgl.texture :as texture]
            [cljs-webgl.constants.capability :as capability]
            [cljs-webgl.constants.texture-unit :as texture-unit]
            [cljs-webgl.constants.blending-factor-dest :as bldest]
            [cljs-webgl.constants.blending-factor-src :as bldsrc]
            [cljs-webgl.constants.draw-mode :as draw-mode]
            [cljs-webgl.constants.data-type :as data-type]
            [cljs-webgl.constants.buffer-object :as buffer-object]
            [cljs-webgl.constants.shader :as shader]
            [cljs-webgl.constants.texture-target :as texture-target]
            [cljs-webgl.constants.texture-parameter-name :as texture-parameter-name]
            [cljs-webgl.constants.parameter-name :as parameter-name]
            [cljs-webgl.constants.texture-filter :as texture-filter]
            [cljs-webgl.constants.pixel-format :as pixel-format]
            [cljs-webgl.constants.texture-wrap-mode :as wrap-mode]
            [cljs-webgl.buffers :as buffers]
            [cljs-webgl.typed-arrays :as ta]
            [gui.bitmap :as bitmap]
            [gui.texmap :as texmap]
            [clojure.string :as str]))

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
      gl_FragColor = texture2D( texture_main , texcoordv , 0.0 );
   }")

(defn init []
  (let [context (context/get-context (.getElementById js/document "main") {:premultiplied-alpha true})

        ui-shader (shaders/create-program
                   context
                   (shaders/create-shader context shader/vertex-shader ui-vertex-source)
                   (shaders/create-shader context shader/fragment-shader ui-fragment-source))
        
        ui-buffer (buffers/create-buffer
                   context
                   (ta/float32
                    [0.0   0.0   0.0 0.0
                     500.0 500.0 1.0 1.0
                     500.0 0.0   1.0 0.0

                     0.0   0.0   0.0 0.0
                     0.0   500.0 0.0 1.0
                     500.0 500.0 1.0 1.0])
                   buffer-object/array-buffer
                   buffer-object/static-draw)

        ui-texmap (texmap/init 1024 1024 0 0 0 0)

        ui-texture (.createTexture context) 

        ui-location-pos (shaders/get-attrib-location context ui-shader "position")
        ui-location-texcoord (shaders/get-attrib-location context ui-shader "texcoord")]

    (.enable context capability/blend)
    (.blendFunc context bldest/src-alpha bldest/one-minus-src-alpha)
    
    {:context context
     :textures {}
     :ui-shader ui-shader
     :ui-buffer ui-buffer
     :ui-texmap ui-texmap
     :ui-texture ui-texture
     :ui-location-pos ui-location-pos
     :ui-location-texcoord ui-location-texcoord }))


(defn bitmap-for-glyph [ ]
  (let [context (.getContext (. js/document getElementById "temp") "2d" )]
    (set! (.-font context) "40px Cantarell")
    (let [width (int (.-width (.measureText context "Tóth Milán")))
          height 40]
      (.fillText context "Tóth Milán" 0 30)
      {:data (.-data (.getImageData context 0 0 width 40))
       :width width
       :height 40})))


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
                        (str/starts-with? id "image")
                        (tmap)
                        (str/starts-with? id "color")
                        (let [rem (subs id 8)
                              r (js/parseInt (subs rem 0 2) 16)
                              g (js/parseInt (subs rem 2 4) 16)
                              b (js/parseInt (subs rem 4 6) 16)
                              a (js/parseInt (subs rem 6 8) 16)]
                          (texmap/setbmp tmap id (bitmap/init 10 10 r g b a)))
                        (str/starts-with? id "glyph")
                        (let [bmp (bitmap-for-glyph)]
                          (texmap/setbmp tmap id bmp))
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
        newtexmap (tex-gen-for-ids ui-texmap newtexids)

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

    (.activeTexture context texture-unit/texture0)
    (.bindTexture context texture-target/texture-2d ui-texture)
 
    ;; upload texturemap if needed 
    (if (newtexmap :changed)
      (do
        
         (.texImage2D
         context
         texture-target/texture-2d
         0
         pixel-format/rgba
         1024
         1024
         0
         pixel-format/rgba
         data-type/unsigned-byte
         (:data (:bitmap newtexmap)))

         (.texParameteri context texture-target/texture-2d texture-parameter-name/texture-wrap-s wrap-mode/clamp-to-edge)
         (.texParameteri context texture-target/texture-2d texture-parameter-name/texture-wrap-t wrap-mode/clamp-to-edge)
         (.texParameteri context texture-target/texture-2d texture-parameter-name/texture-min-filter texture-filter/linear)
         (.texParameteri context texture-target/texture-2d texture-parameter-name/texture-mag-filter texture-filter/linear)))
    
    (.bindBuffer context
                 buffer-object/array-buffer
                 ui-buffer)
    
    (.bufferData context
                 buffer-object/array-buffer
                 (ta/float32 vertexes)
                 buffer-object/dynamic-draw)
    
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
                 :values 0}
                ])

    (assoc state :ui-texmap (assoc newtexmap :changed false))))
