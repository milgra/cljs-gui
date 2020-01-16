(ns ^:figwheel-hooks demo.core
  (:require [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :refer [<! chan put! take! poll!]]
            [cljs.core.async :refer-macros [go]]
            [gui.ui :as ui]
            [gui.webgl :as webgl]
            [gui.math4 :as math4]
            [gui.bitmap :as bitmap]
            [gui.texmap :as texmap]
            [demo.layouts :as layouts])
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


(defn gen-mouse-state [viewmap views mouseevent]
  "generates new view states for mouse event"
  (reduce
   (fn [result {:keys [id cl tx] :as view}]
     (let [newview (if (= cl "Button")
                     (-> view
                         (assoc :oldtx tx)
                         (assoc :tx "Color 0xFF0000FF"))
                     view)]
       (assoc result id newview)))
   viewmap
   views))


(defn get-mouse-commands [views mouseevent]
  "extract mouse commands from touched views"
  (remove nil? (map :co views)))


(defn main []
  "entering point"
  (let [keych (chan)

        tchch (chan)
        
        glstate (webgl/init)
        
        uimap (ui/gen-from-desc
               layouts/hud
               (get-in glstate [:tempcanvas]))

        state {:glstate glstate
               :uimap uimap}]

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
     (fn [event] (put! tchch {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "down"})))

    (events/listen
     js/document
     EventType.MOUSEUP
     (fn [event] (put! tchch {:code "mouse" :x (.-clientX event) :y (.-clientY event) :type "up"})))

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
             
             tchevent (poll! tchch)

             oneuimap (oldstate :uimap)
             
             viewmap (ui/align
                      (oneuimap :viewmap)
                      (oneuimap :views)
                      0
                      0
                      (. js/window -innerWidth)
                      (. js/window -innerHeight))
             
             viewids (ui/collect-visible-ids
                      viewmap
                      (oneuimap :views)
                      "")

             views (map viewmap viewids)
             
             newglstate (webgl/draw! (oldstate :glstate) 
                                     projection
                                     views)]
         
         (if tchevent
           (let [picked (ui/collect-pressed-views viewmap tchevent)
                 views (map viewmap picked)
                 ;;newviewmap (gen-mouse-state viewmap views mouseevent)
                 commands (get-mouse-commands views tchevent)

                 newuimap (if (= "ShowMenu" (first commands))
                            (ui/gen-from-desc
                             layouts/menu
                             (get-in glstate [:tempcanvas]))
                            oneuimap)]

             ;; change state if command wants you to do that
             
             (-> oldstate
                 (assoc :uimap newuimap)
                 (assoc :glstate newglstate)
                 ;;(assoc-in [:uimap :viewmap] newviewmap)
                 ))
           (-> oldstate
               (assoc :glstate newglstate)
               ;;(assoc-in [:uimap :viewmap] viewmap)
               )))))))

(main)
