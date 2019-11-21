(ns gui.webgl
  (:require [cljs-webgl.context :as context]
            [cljs-webgl.shaders :as shaders]
            [cljs-webgl.texture :as texture]
            [cljs-webgl.constants.texture-unit :as texture-unit]
            [cljs-webgl.constants.draw-mode :as draw-mode]
            [cljs-webgl.constants.data-type :as data-type]
            [cljs-webgl.constants.buffer-object :as buffer-object]
            [cljs-webgl.constants.shader :as shader]
            [cljs-webgl.constants.texture-target :as texture-target]
            [cljs-webgl.constants.texture-parameter-name :as texture-parameter-name]
            [cljs-webgl.constants.parameter-name :as parameter-name]
            [cljs-webgl.constants.texture-filter :as texture-filter]
            [cljs-webgl.constants.pixel-format :as pixel-format]
            [cljs-webgl.buffers :as buffers]
            [cljs-webgl.typed-arrays :as ta]))
  
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
  (let [context (context/get-context (.getElementById js/document "main"))

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
                     500.0 500.0 1.0 1.0]
                    )
                   buffer-object/array-buffer
                   buffer-object/static-draw)

        ui-location-pos (shaders/get-attrib-location context ui-shader "position")
        ui-location-texcoord (shaders/get-attrib-location context ui-shader "texcoord")]

    {:context context
     :textures {}
     :ui-shader ui-shader
     :ui-buffer ui-buffer
     :ui-location-pos ui-location-pos
     :ui-location-texcoord ui-location-texcoord }))


(defn loadtexture! [ {:keys [context ui-buffer] :as state} image id ]
    (let [ tex (texture/create-texture context :image image :generate-mipmaps? true)]
    (assoc-in state [:textures (keyword id)] tex)))

(defn loadtexture-bytearray-imp! [gl-context & {:keys [image
                                                  target
                                                  level-of-detail
                                                  internal-pixel-format
                                                  width
                                                  height
                                                  border
                                                  pixel-format
                                                  data-type
                                                  ;;pixel-store-modes
                                                  generate-mipmaps?
                                                  data-type
                                                  parameters] :as opts}]
  
  (let [texture (.createTexture gl-context)
        target (or target texture-target/texture-2d)]

    (.bindTexture gl-context target texture)
    
    (.texImage2D
      gl-context
      target
      (or level-of-detail 0)
      (or internal-pixel-format pixel-format/rgba)
      width
      height
      0
      (or pixel-format pixel-format/rgba)
      data-type/unsigned-byte
      (js/Uint8Array. image))

    (when generate-mipmaps?
      (.generateMipmap gl-context target))

    (.bindTexture gl-context target nil)
    
    texture))

(defn loadtexture-bytearray! [ {:keys [context] :as state} {bitmap :bitmap :as texmap} id ]
  (let [ tex (loadtexture-bytearray-imp! context :image (:data bitmap ) :width (:width bitmap) :height (:height bitmap) :generate-mipmaps? true)]
    (assoc-in state [:textures (keyword id)] tex)))

(defn draw-glyphs! [{:keys [context ui-shader ui-buffer ui-location-pos ui-location-texcoord textures] :as state } projection glyphs]

  (let [vertexes (flatten (map (fn [ { :keys [ x y wth hth ttl ttr tbl tbr ] } ]
                                 (concat
                                  [x y] ttl
                                  [(+ x wth) y] ttr
                                  [x (+ y hth)] tbl

                                  [(+ x wth) y] ttr
                                  [(+ x wth) (+ y hth)] tbr
                                  [x (+ y hth)] tbl)) glyphs))]

    (.bindBuffer context
                 buffer-object/array-buffer
                 ui-buffer)
    
    ;; load in new vertexdata
    
    (.bufferData context
                 buffer-object/array-buffer
                 (ta/float32 vertexes)
                 buffer-object/dynamic-draw)
    
    (.activeTexture context texture-unit/texture0)
    (.bindTexture context texture-target/texture-2d (textures :glyphmap))
    
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
    
    ;; return state
    state
    )
  )

(defn draw-quads! [{:keys [context ui-shader ui-buffer ui-location-pos ui-location-texcoord textures] :as state } projection quads]

  (let [vertexes (flatten (map (fn [ { :keys [ x y wth hth ttl ttr tbl tbr ] } ]
                                 (concat
                                  [x y] ttl
                                  [(+ x wth) y] ttr
                                  [x (+ y hth)] tbl

                                  [(+ x wth) y] ttr
                                  [(+ x wth) (+ y hth)] tbr
                                  [x (+ y hth)] tbl )
                        ) quads ) ) ]

    (.bindBuffer context
                 buffer-object/array-buffer
                 ui-buffer)
    
    ;; load in new vertexdata
    
    (.bufferData context
                 buffer-object/array-buffer
                 (ta/float32 vertexes)
                 buffer-object/dynamic-draw)
    
    (.activeTexture context texture-unit/texture0)
    (.bindTexture context texture-target/texture-2d (textures :uitexmap))
    
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
    
    ;; return state
    state
    )
  )
