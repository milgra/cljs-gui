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
                    [0.0   0.0   0.0 1.0 0.0 0.0
                     500.0 500.0 0.0 1.0 1.0 1.0
                     500.0 0.0   0.0 1.0 1.0 0.0

                     0.0   0.0   0.0 1.0 0.0 0.0
                     0.0 500.0   0.0 1.0 0.0 1.0
                     500.0 500.0 0.0 1.0 1.0 1.0]
                    )
                   buffer-object/array-buffer
                   buffer-object/static-draw)

        ui-location-pos (shaders/get-attrib-location context ui-shader "position")
        ui-location-texcoord (shaders/get-attrib-location context ui-shader "texcoord")]

    {:context context
     :ui-shader ui-shader
     :ui-buffer ui-buffer
     :ui-location-pos ui-location-pos
     :ui-location-texcoord ui-location-texcoord }))


(defn loadtexture! [ {:keys [context ui-buffer] :as state} image ]
    (let [ tex (texture/create-texture context :image image :generate-mipmaps? true)]
    (assoc state :texture tex)))


(defn draw-ui-quads! [{:keys [context ui-shader ui-buffer ui-location-pos ui-location-texcoord texture] :as state } projection]

  (.activeTexture context texture-unit/texture0)
  (.bindTexture context texture-target/texture-2d texture)

  (buffers/draw!
   context
   :count 6
   :first 0
   :shader ui-shader
   :draw-mode draw-mode/triangles
   :attributes [{:buffer ui-buffer
                 :location ui-location-pos
                 :components-per-vertex 4
                 :type data-type/float
                 :offset 0
                 :stride 24}
                {:buffer ui-buffer
                 :location ui-location-texcoord
                 :components-per-vertex 2
                 :type data-type/float
                 :offset 16
                 :stride 24}]
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
