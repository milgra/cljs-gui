(ns ^:figwheel-hooks demo.core
  (:require
   [cljs.core.async :refer [<! chan put! take! poll!]]
   [cljs-webgl.context :as context]
   [demo.layouts :as layouts]
   [goog.dom :as dom]
   [goog.events :as events]
   [gui.math4 :as math4]
   [gui.webgl :as uiwebgl]
   [demo.ui :as brawlui]
   [gui.floatbuffer :as floatbuffer])
  (:import [goog.events EventType]))


(defn resize-context!
  "vresize canvas on window resize"
  []
  (let [canvas (dom/getElement "main")
        context (context/get-context canvas)
        rect (.getBoundingClientRect canvas)
        width (.-width rect)
        height (.-height rect)
        ratio (or (.-devicePixelRatio js/window) 1.0)]
    (dom/setProperties canvas
                       (clj->js {:width (* width ratio)
                                 :height (* height ratio)}))))


(defn load-font!
  "load external font"
  [state name url]
  (let [font (js/FontFace. name (str "url(" url ")"))]
    (.then
     (.load font)
     (fn []
       (.add (.-fonts js/document) font)
       (put! (:msgch state) {:id "redraw-ui"})))))


(defn init-events!
  "start event listening"
  [state]
  (let [key-codes (atom {})
        mouse-down (atom false)]
    
    (events/listen
     js/document
     EventType.KEYDOWN
     (fn [event]
       (let [code (.-keyCode event)
             prev (get @key-codes code)]
         (swap! key-codes assoc code true)
         (if (not prev) (put! (:msgch state) {:id "key" :code (.-keyCode event) :value true})))))

    (events/listen
     js/document
     EventType.KEYUP
     (fn [event]
       (let [code (.-keyCode event)
             prev (get @key-codes code)]
         (swap! key-codes assoc code false)
         (if prev (put! (:msgch state) {:id "key" :code (.-keyCode event) :value false})))))

    (events/listen
     js/document
     EventType.POINTERDOWN
     (fn [event]
       (swap! mouse-down not)
       (put! (:msgch state) {:id "mouse" :type "down" :point [(.-clientX event) (.-clientY event)]})))

    (events/listen
     js/document
     EventType.POINTERUP
     (fn [event]
       (swap! mouse-down not)
       (put! (:msgch state) {:id "mouse" :type "up" :point [(.-clientX event) (.-clientY event)]})))

    (events/listen
     js/document
     EventType.POINTERMOVE
     (fn [event]
       (if @mouse-down (put! (:msgch state) {:id "mouse" :type "down" :point [(.-clientX event) (.-clientY event)]}))))

    (events/listen
     js/document
     EventType.TOUCHSTART
     (fn [event] nil))

    (events/listen
     js/window
     EventType.RESIZE
     (fn [event]
       (put! (:msgch state) {:id "resize"})
       (resize-context!)))))


(defn draw-ui
  "draw ui elements with ui-drawer"
  [{{:keys [views viewids projection] :as ui} :ui ui-drawer :ui-drawer :as state}]
  (assoc state :ui-drawer (uiwebgl/draw! ui-drawer projection (map views viewids))))


(defn simulate [state time]
  (let [message (poll! (:msgch state))
        new-state (-> state
                      ;; ui
                      (brawlui/execute-commands message)
                      (brawlui/update-ui message time 1.0))]
    new-state))


(defn animate
  "main runloop, syncs animation to display refresh rate"
  [state draw-fn]
  (letfn [(loop [prestate frame]
            (fn [time]
              (let [delta (- time (:time prestate))
                    state (if (< delta time)
                            (draw-fn prestate frame time delta)
                            prestate)]
                (.requestAnimationFrame js/window (loop (assoc state :time time) (inc frame))))))]
    ((loop state 0) 0)))


(defn main
  "entering point"
  []
  (let [state {:ui (brawlui/init)
               :ui-drawer (uiwebgl/init)
               :ui-ratio (min 2.0 (or (.-devicePixelRatio js/window) 1.0))
               :time 0
               :msgch (chan)
               :buffer (floatbuffer/create!)
               :commands-ui []}
        
        final (-> state
                  (brawlui/load-ui layouts/generator))]

    (load-font! final "Ubuntu Bold" "css/Ubuntu-Bold.ttf")
    (init-events! final)
    (resize-context!)

    (animate
     final
     (fn [prestate
          frame
          time
          delta]
       (if-not (= (mod frame 1) 0 ) ;; frame skipping for development
         prestate
         (-> prestate
             (simulate time)
             ;; drawing
             (draw-ui)))))))

;; start main once, avoid firing new runloops with new reloads
(defonce mainloop (main))
