(ns demo.core
  (:require [saolsen.draw-2d :as draw]
            [goog.events :as events]
            [ajax.core]
            [cljs.reader])
  )


(defn mount-components []
  (let [content (js/document.getElementById "app")]
    (while (.hasChildNodes content)
      (.removeChild content (.-lastChild content)))
    (.appendChild content (js/document.createTextNode "Just a sudoku demo"))))

;(print context)

(defn draw []
  (let [context (draw/get-canvas-context-from-id "draw")]
    (draw/draw-scene [{:type :rectangle :color [200 200 0] :pos [50 50] :size [100 20]}
                      {:type :rectangle :color [0 200 200] :pos [55 55] :size [20 10]}
                      {:type :line :color [0 200 0] :width 1 :posns [[10 10] [50 10] [10 50]]}
                      {:type :text :color [0 200 0] :pos [50 50] :text "1"}
                      ] context)

    )
  )

(def context (draw/get-canvas-context-from-id "draw"))

(defn draw-grid [{:keys [x y color width gap n]}]
  (let [l (* gap (dec n))]
    (doseq [i (range n)]
      (let [yi (+ y (* i gap))]
        (draw/draw-object  {:type :line :color color :width width :posns [[x yi] [(+ x l) yi]]} (:ctx context))
        ))

    (doseq [i (range n)]
      (let [xi (+ x (* i gap))]
        (draw/draw-object  {:type :line :color color :width width :posns [[xi y] [xi (+ y l)]]} (:ctx context))
        ))
    )
  )


(def sudoku
  [
   [3   nil nil nil nil 6   7   9   nil]
   [4   nil nil nil nil nil nil nil nil]
   [nil nil 1   nil nil 7   nil nil 6  ]
   [nil nil nil 9   nil 5   2   nil nil]
   [9   nil 4   2   nil 8   3   nil 7  ]
   [nil nil 2   4   nil 1   nil nil nil]
   [8   nil nil 1   nil nil 9   nil nil]
   [nil nil nil nil nil nil nil nil 2  ]
   [nil 3   9   6   nil nil nil nil 8  ]
   ]
  )

(defn draw-puzzle [sudoku {:keys [x y color font gap]}]
  (dorun
    (map-indexed
      (fn [i row]
         (dorun
           (map-indexed
             (fn [j col]
                (when col
                  (draw/draw-object {:type :text :color color :font font :pos [(+ x (* gap j)) (+ y (* gap i))] :text (str col)} (:ctx context)))
               )
             row)
           )
         )
      sudoku)
    )
  )

(defn diff-row [a b]
  (map (fn [x y] (if y nil x)) a b)
  )

(defn diff-grid [a b]
  (map diff-row a b)
  )

(defn solution-handler [[ok response]]
  (if ok
    (let [solution (cljs.reader/read-string (str response))
          delta (diff-grid solution sudoku)
          ]
      (.log js/console (str response))
      (draw-puzzle delta {:x 70 :y 80 :color [200 0 0] :font "30px Arial" :gap 50})
      )
    (do
      (.error js/console (str response))
      (js/alert "error!")
      )
         )
  )


(defn solve []


  (ajax.core/ajax-request
    {:uri "/solution"
     :method :get
     :params {}
     :handler solution-handler
     :format (ajax.core/url-request-format	)
     :response-format (ajax.core/text-response-format )})



  )

(defn init! []
  (mount-components)
  (let [button (js/document.getElementById "solveButton")]
    (events/listen button "click"
                   (fn [event] (solve)))
    )
  (draw-grid {:x 50 :y 50 :color [200 200 0] :width 1 :gap 50 :n 10})
  (draw-puzzle sudoku {:x 70 :y 80 :color [200 200 0] :font "30px Arial" :gap 50})
  )
