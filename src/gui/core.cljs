(ns ^:figwheel-hooks gui.core
  (:require [goog.dom :as gdom]
            [goog.events :as events]
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
  "main runloop, syncs animation to display refresh rate"
  (letfn [(loop [oldstate frame]
            (fn [time]
              (let [newstate (draw-fn oldstate frame time)]
              (.requestAnimationFrame js/window (loop newstate (inc frame))))
              ))]
    ((loop state 0) 0)))


(defn resize-context! [ ]
  "resizes canvas on window size change"
  (let [canvas (. js/document getElementById "main")]
        (set! (. canvas -width) (. js/window -innerWidth))
        (set! (. canvas -height) (. js/window -innerHeight))))


(defn main []
  "entering point"
  (let [keych (chan)

        mousech (chan)
        
        glstate (webgl/init)
        
        uimap (ui/gen-from-desc
               layouts/hud
               (get-in glstate [:tempcanvas]))

        aligned (ui/align
                 (uimap :viewmap)
                 (uimap :views)
                 0
                 0
                 (. js/window -innerWidth)
                 (. js/window -innerHeight))
    
        viewids (ui/collect-visible-ids
                 aligned
                 (uimap :views)
                 "")

        state {:glstate glstate
               :uimap (assoc uimap :viewmap aligned)}]

    (events/listen
     js/document
     EventType.KEYDOWN
     (fn [event] (put! keych {:code (.-keyCode event) :value true})))

    (events/listen
     js/document
     EventType.KEYUP
     (fn [event] (put! keych {:code (.-keyCode event) :value false})))

    (events/listen
     js/document
     EventType.MOUSEDOWN
     (fn [event] (put! mousech {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "down"})))

    (events/listen
     js/document
     EventType.MOUSEUP
     (fn [event] (put! mousech {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "up"})))

    (events/listen
     js/window
     EventType.RESIZE
     (fn [event] (resize-context!)))

    (resize-context!)
    
    (animate
     state
     (fn [oldstate frame time]
       (let [projection (math4/proj_ortho
                         0
                         (.-innerWidth js/window)
                         (.-innerHeight js/window)
                         0
                         -10.0
                         10.0)
             
             keyevent (poll! keych)
             
             mouseevent (poll! mousech)

             views (map (get-in oldstate [:uimap :viewmap]) viewids)
             
             newglstate (webgl/draw! (oldstate :glstate) 
                                     projection
                                     views)]

         (when mouseevent
           (let [picked (ui/collect-pressed-views (get-in oldstate [:uimap :viewmap]) mouseevent)]
             ;; if picked is menu, switch to menu layout
           (println "mouseevent" mouseevent "picked" picked)))

         (assoc oldstate :glstate newglstate))))))

(main)
