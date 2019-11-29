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
      [keychannel (chan)
       filechannel (chan)
       imagechannel (chan)
             
       initstate {:glstate (webgl/init)
                  :desc_file "level0.svg"
                  :font-file "font.png"
                  :keypresses {}}

       label1 (ui/label 10.0 10.0 200.0 50.0 "Károly Király" 35.0)
       label2 (ui/label 10.0 200.0 250.0 50.0 "Gróf Lyukasgeci" 35.0)
       labels (concat label1 label2)]

    (println labels)
    
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
                         -10.0
                         10.0)
             
             keyevent (poll! keychannel)
             
             newglstate (webgl/draw! (:glstate state) 
                                     projection
                                     labels)]

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
