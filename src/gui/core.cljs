(ns ^:figwheel-hooks gui.core
  (:require [goog.dom :as gdom]
            [goog.events :as events]
            [tubax.core :refer [xml->clj]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! chan put! take! poll!]]
            [cljs.core.async :refer-macros [go]]
            [gui.ui :as ui]
            [gui.webgl :as webgl]
            [gui.math4 :as math4]
            [gui.bitmap :as bitmap]
            [gui.texmap :as texmap]
            [gui.layouts :as layouts])
  (:import [goog.events EventType]))
  

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
      [keychannel (chan)
       mousechannel (chan)
             
       initstate {:glstate (webgl/init)
                  :desc_file "level0.svg"
                  :font-file "font.png"
                  :keypresses {}}
       
       uimap (ui/gen-from-desc layouts/hud (get-in initstate [:glstate :tempcanvas]))
       viewids (ui/collect-visible-ids uimap (uimap :views) "")
       newuimap (ui/align uimap (uimap :views) 0 0 (. js/window -innerWidth) (. js/window -innerHeight))
       views (map (newuimap :viewmap) viewids)]
    
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
     js/document
     EventType.MOUSEDOWN
     (fn [event] (put! mousechannel {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "down"})))

    (events/listen
     js/document
     EventType.MOUSEUP
     (fn [event] (put! mousechannel {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "up"})))

    (events/listen
     js/window
     EventType.RESIZE
     (fn [event]
       (resize-context!)))

    (resize-context!)
    
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
                         -10.0
                         10.0)
             
             keyevent (poll! keychannel)
             mouseevent (poll! mousechannel)
             
             newglstate (webgl/draw! (:glstate state) 
                                     projection
                                     views)]

         (when mouseevent
           (println "mouseevent" mouseevent))

         (assoc state :glstate newglstate))))))

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
