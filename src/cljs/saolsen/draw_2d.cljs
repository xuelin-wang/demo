(ns saolsen.draw-2d)

;; Draw stuff (and never care about ie ever)
;; There's obviously a ton this lib doesn't do, just adding what
;; I need when I need it.
(def request-animation-frame
  (or js/requestAnimationFrame
      js/webkitRequestAnimationFrame))

(defn get-canvas-context-from-id
  "Gets the drawing context from the id of the canvas element.
   Actual context is in a map with the canvas element and some
   other info."
  [id]
  (let [canvas (.getElementById js/document id)]
    {:canvas canvas
     :width (.-width canvas)
     :height (.-height canvas)
     :ctx (.getContext canvas "2d")}))

(defn to-color [& rgbas]
  (let [csv (apply str (interpose ", " rgbas))]
    (str "rgb(" csv ")")))

(defmulti draw-object :type)

(defmethod draw-object :rectangle [{:keys [color pos size]} ctx]
  (let [[x y] pos
        [w h] size]
    (aset ctx "fillStyle" (apply to-color color))
    (.fillRect ctx x y w h)))

(def twopi (* 2 (.-PI js/Math)))

(defmethod draw-object :circle [{:keys [color pos size]} ctx]
  (let [[x y] pos]
    (aset ctx "fillStyle" (apply to-color color))
    (.beginPath ctx)
    (.arc ctx x y size 0 twopi)
    (.closePath ctx)
    (.fill ctx)))

(defmethod draw-object :line [{:keys [color width posns]} ctx]
  (let [[startx starty] (first posns)]
    (.beginPath ctx)
    (.moveTo ctx startx starty)
    (doseq [[x y] (rest posns)]
      (.lineTo ctx x y))
    (.closePath ctx)
    (aset ctx "lineWidth" width)
    (.stroke ctx)))

(defmethod draw-object :text [{:keys [color font text pos]} ctx]
  (let [[x y] pos]
    (aset ctx "font" font)
    (aset ctx "fillStyle" (apply to-color color))
    (.fillText ctx text x y)
    ))


(defn clear-canvas
  "Clears the canvas"
  [ctx width height]
  (.save ctx)
  (.setTransform ctx 1 0 0 1 0 0)
  (.clearRect ctx 0 0 width height)
  (.restore ctx))

(defn draw-scene
  "Draws a sequence of objects to the screen.
   Object must contain various keys depending on their type.
   {:type :rectangle :color [r g b a] :pos [x y] :size [w h]}
   {:type :circle :color [r g b a] :pos [x y] :size r}
   {:type :line :color [r g b a] :posns [x y ...]}
  Objects are drawn in the order received and the pos coordinate
  specifies the upper left corner."
  [objs {:keys [width height ctx]}]
  (clear-canvas ctx width height)
  ;; clear screen first?
  (doseq [obj objs]
    (draw-object obj ctx)))

(comment
  (def context (get-canvas-context-from-id "draw"))
  (print context)
  (draw-scene [{:type :rectangle :color [200 0 0] :pos [50 50] :size [100 20]}
               {:type :rectangle :color [0 0 200] :pos [55 55] :size [2 10]}
               {:type :line :color [0 200 0] :width 1 :posns [[10 10] [50 10] [10 50]]}
               ] context)
  )