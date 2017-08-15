(ns demo.routes.home
  (:require [demo.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [demo.sudoku-algox :as sudoku]
            ))

(defn home-page []
  (layout/render "home.html"))




(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/solution" []
    (let [puzzle
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
          solutions (sudoku/sudoku puzzle true true)
          s (str
            (first solutions)
            )
          ]
      (-> (response/ok s)
          (response/header "Content-Type" "text/plain; charset=utf-8")
          )
      )
    )
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))

