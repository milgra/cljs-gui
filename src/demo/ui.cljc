(ns demo.ui
  (:require [goog.window :as window]
            [gui.kinetix :as kinetix]
            [gui.math4 :as math4]
            [gui.webgl :as uiwebgl]
            [demo.layouts :as layouts]))


(defn init
  "creates empty ui structure"
  []
  {:views {}
   :baseid nil 
   :viewids []
   :projection (math4/proj_ortho 0 (.-innerWidth js/window) (.-innerHeight js/window) 0 -10.0 10.0)})


(defn load-ui
  "load new ui stack"
  [state description]
  (let [views (kinetix/gen-from-desc {} description (/ 1.0 (:ui-ratio state)))
        baseid (keyword (:id description))
        viewids (kinetix/collect-visible-ids views [baseid] "")
        alignedviews (kinetix/align views [baseid] 0 0 (. js/window -innerWidth) (. js/window -innerHeight))]

    (assoc state :ui {:views alignedviews
                      :baseid baseid
                      :viewids viewids
                      :projection (math4/proj_ortho 0 (.-innerWidth js/window) (.-innerHeight js/window) 0 -10.0 10.0)})))

  
(defn update-ui
  "update view based on user actions"
  [{{:keys [baseid views commands-ui projection]} :ui :as state} msg time delta]
  (let [pressed-views (if-not (and msg (= (:id msg) "mouse"))
                        nil
                        (kinetix/collect-pressed-views views (:point msg)))

        altered-views (if-not pressed-views
                        []
                        (reduce (fn [result {:keys [class] :as view}]
                                  (cond
                                    (= class "Slider") (conj result (kinetix/touch-slider view views msg))
                                    (= class "Button") (conj result (kinetix/touch-button view views msg))
                                    :else result))
                                []
                                (map views pressed-views)))
        
        new-views (reduce #(assoc %1 (:id %2) %2) views (mapcat :views altered-views))

        new-commands (map :command altered-views)

        newnew-views (if-not msg
                      new-views
                      (kinetix/align new-views [baseid] 0 0 (. js/window -innerWidth) (. js/window -innerHeight)))

        new-projection (if-not (and msg (= (:id msg) "resize"))
                         projection
                         (math4/proj_ortho 0 (.-innerWidth js/window) (.-innerHeight js/window) 0 -10.0 10.0))]
    (-> state
        (assoc-in [:ui :projection] new-projection)
        (assoc-in [:ui :views] newnew-views)
        (assoc :commands-ui (concat commands-ui new-commands)))))


(defn align
  "align view stack, usually on context size change"
  [{{:keys [baseid views]} :ui :as state}]
  (assoc-in state [:ui :views] (kinetix/align views [baseid] 0 0 (. js/window -innerWidth) (. js/window -innerHeight))))


(defn set-slider-value [{{:keys [views]} :ui :as state} id value]
  "sets slider value"
  (assoc-in state [:ui :views] (kinetix/set-slider-value views (id views) value)))


(defn update-gen-sliders
  "updates generator sliders"
  [{:keys [ui world] :as state}]
  (let [views (:views ui)
        {:keys [height hitpower hitrate stamina speed]} (get-in world [:actors :hero :metrics :base])
        hpsl (:Hitpower views)
        hrsl (:Hitrate views)
        hesl (:Height views)
        spsl (:Speed views)
        stsl (:Stamina views)
        new-views (-> views
                      (kinetix/set-slider-value hpsl hitpower)
                      (kinetix/set-slider-value hrsl hitrate)
                      (kinetix/set-slider-value hesl height)
                      (kinetix/set-slider-value spsl speed)
                      (kinetix/set-slider-value stsl stamina))]
    (assoc-in state [:ui :views] new-views)))


(defn execute-commands
  "executes commands coming from the ui"
  [{:keys [level commands-ui ui-drawer] :as state} msg]
  (let [commands (if-not (and msg (= (:id msg) "redraw-ui"))
                   commands-ui
                   (conj commands-ui {:text "redraw-ui"}))]
    (reduce
     (fn [oldstate {text :text type :type ratio :ratio :as command}]
       (println "command" command)
       (cond
         (and (= text "show-menu") (= type "up")) ; shows menu view
         (load-ui oldstate layouts/menu)
         (and (= text "options") (= type "up")) ; shows options view
         (load-ui oldstate layouts/options)
         (and (= text "fullscreen") (= type "up")) ; randomizes generator values
         (do
           (if (.-fullscreenElement js/document)
             (.exitFullscreen js/document)
             (.requestFullscreen (.-documentElement js/document)))
           oldstate)
         (and (= text "donate") (= type "up")) ; opens donate link in browser
         (do
           (goog.window/open "https://paypal.me/milgra")
           oldstate)
         (and (= text "source code") (= type "up")) ; opens donate link in browser
         (do
           (goog.window/open "https://github.com/milgra/cljs-brawl")
           oldstate)
         (and (= text "options back") (= type "up")) ; opens menu view
         (load-ui oldstate layouts/menu)
         ;; font loaded, reset textures to force redraw with new fonts
         (= text "redraw-ui")
         (assoc oldstate :ui-drawer (uiwebgl/reset ui-drawer))
         :else oldstate))
     (assoc state :commands-ui [])
     commands)))
