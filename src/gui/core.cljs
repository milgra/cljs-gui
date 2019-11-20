(ns ^:figwheel-hooks gui.core
  (:require [goog.dom :as gdom]
            [goog.events :as events]
            [tubax.core :refer [xml->clj]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! chan put! take! poll!]]
            [cljs.core.async :refer-macros [go]]
            [gui.webgl :as webgl]
            [gui.math4 :as math4]
            [gui.ui :as ui]
            [gui.bitmap :as bitmap]
            [gui.texmap :as texmap])
  (:import [goog.events EventType]))
  

(defn load-level! [channel name]
  (go
    (let [response (<! (http/get name
                                 {:with-credentials? false}))]
      (put! channel (:body response)))))


(defn load-image! [channel name]
  (let [img (js/Image.)]
    (set! (.-onload img)
          (fn [a]
            (put! channel img)))
    (set! (.-src img) name)))


(defn animate [state draw-fn]
  (letfn [(loop [oldstate frame]
            (fn [time]
              (let [newstate (draw-fn oldstate frame time)]
              (.requestAnimationFrame js/window (loop newstate (inc frame))))
              ))]
    ((loop state 0) 0 )))


(defn resize-context! [ ]
  (let [canvas (. js/document getElementById "main")]
        (set! (. canvas -width) (. js/window -innerWidth))
        (set! (. canvas -height) (. js/window -innerHeight))))


(defn main []

  (let
      [initstate {:glstate (webgl/init)
                  :desc_file "level0.svg"
                  :font-file "font.png"
                  :keypresses {}}
       filechannel (chan)
       imagechannel (chan)
       keychannel (chan)

       ;; create texture from glyphmap image and codepoints

       redbmp (bitmap/fill (bitmap/init 10 10) 0xFF000000)
       
       glyphmap (texmap/init 1024 256)
              
       ;; create texture for colors

       uitexmap (texmap/setbmp (texmap/init 1024 1024) redbmp "0xFF000000")
       
       label (ui/get-label-glyphs "Karoly Kiraly") ]

    (println "glyphmap")
    (println "uitexmap")
    
    ;; key listeners

    (events/listen
     js/document
     EventType.KEYDOWN
     (fn [event] (put! keychannel {:code (.-keyCode event) :value true})))

    (events/listen
     js/document
     EventType.KEYUP
     (fn [event] (put! keychannel {:code (.-keyCode event) :value false})))

    (events/listen
     js/window
     EventType.RESIZE
     (fn [event]
       (resize-context!)))

    (resize-context!)

    (load-image! imagechannel (:font-file initstate))
    
    ;; runloop
    
    (animate
     initstate
     (fn [state frame time]         
       (let [r (/ (.-innerWidth js/window) (.-innerHeight js/window) )
             h 300.0
             w (* h r)
             projection (math4/proj_ortho
                         0
                         (.-innerWidth js/window)
                         (.-innerHeight js/window)
                         0
                         -1.0
                         1.0)
             
             image (poll! imagechannel)               
             keyevent (poll! keychannel)
             
             newglstate (if image
                          (webgl/loadtexture! (:glstate state) image)
                          (:glstate state))
             
             newstate (-> state
                          (assoc :glstate newglstate))]
         
         (webgl/draw-glyphs! (:glstate state) projection label)
         
         ;; return with new state

         newstate
         )
       )
     )
    )
  )

;; template functions

;;(println "AA This text is printed from src/brawl~/core.cljs. Go ahead and edit it and see reloading in action.")

(def app-state (atom {}))

(defn multiply [a b] (* a b))

(defn get-app-element []
  (gdom/getElement "app"))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  (println "app-state" app-state)
)

;; start entry point, can we do this from project.clj?
(main)
