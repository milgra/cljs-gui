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

       hudlayout
;; CL Class TE text BC back color FC fore color WI width HE height
;; TA top BA bottom LA left RA right HA horizontal VA vertical align
;; 0 : edges or screen"
"
X             M|
               |
P              |
BD           LR|
KG    HSP     D|
X CLIndicator TELevel BCFFFFFF55 FCFFFFFFFF TA0 LA0 WI150 HE50
M CLButton TEMenu BCFFFFFF55 FCFFFFFFFF TA0 RA0 WI150 HE50
P CLButton TEPunch BCFFFFFF55 FCFFFFFFFF BAB LA0 WI100 HE100
B CLButton TEBlock BCFFFFFF55 FCFFFFFFFF BAK LA0 WI100 HE100
K CLButton TEKick BCFFFFFF55 FCFFFFFFFF BA0 LA0 WI100 HE100
D CLButton TEDown BCFFFFFF55 FCFFFFFFFF VAB LAB WI100 HE100
G CLButton TEGun BCFFFFFF55 FCFFFFFFFF BA0 LAK WI100 HE100
H CLIndicator TEHealth BCFFFFFF55 FCFFFFFFFF BA0 RAS WI200 HE50
S CLLabel TEBullets BCFFFFFF55 FCFFFFFFFF BA0 HA0 WI50 HE50
P CLIndicator TEPower BCFFFFFF55 FCFFFFFFFF BA0 LAS WI200 HE50
L CLButton TELeft BCFFFFFF55 FCFFFFFFFF VAR RAR WI100 HE100
R CLButton TERight BCFFFFFF55 FCFFFFFFFF BAD RA0 WI100 HE100
D CLButton TEDown BCFFFFFF55 FCFFFFFFFF BA0 RA0 WI100 HE100
"
       
       menulayout
"
 C |
 N |
 O |
 D |
O CLButton TEOptions BC00FFFFFF FCFFFFFFFF VA0 HA0 WI150 HE50
N CLButton TENew~Game BCFFFF00FF FCFFFFFFFF BAO HA0 WI150 HE50
D CLButton TEDonate BCFFFFFFFF FCFFFFFFFF TAO HA0 WI150 HE50
C CLButton TEContinue BCFF00FFFF FCFFFFFFFF BAN HA0 WI150 HE50
"

       optslayout
"
 M |
 S |
 A |
 P |
 B |
M CLSlider TEMusic~Volume BCFFFFFF55 FCFFFFFFFF BAS HA0 WI150 HE50
S CLSlider TESound~Volume BCFFFFFF55 FCFFFFFFFF BAA HA0 WI150 HE50
A CLSlider TEControls~Alpha BCFFFFFF55 FCFFFFFFFF VA0 HA0 WI150 HE50
P CLToggle TEShow/Hide~Physics BCFFFFFF55 FCFFFFFFFF TAA HA0 WI150 HE50
B CLButton TEBack BCFFFFFF55 FCFFFFFFFF TAP HA0 WI150 HE50
"
       
       label1 (ui/label 10.0 10.0 200.0 50.0 "Károly Király" 35.0)
       label2 (ui/label 10.0 200.0 250.0 50.0 "Gróf Lyukas" 35.0)
       labels (concat label1 label2)

       uimap (ui/gen-from-desc menulayout)
       viewids (ui/collect-visible-ids uimap (uimap :views))
       views (map (uimap :viewmap) viewids)
       newviews (ui/align uimap (uimap :views) (. js/window -innerWidth)  (. js/window -innerHeight))
       ]

    (println "labels" labels)
    (println "uimap" uimap)
    (println "views" views)
    (println "newviews" newviews)
    
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
                                     views)]

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
