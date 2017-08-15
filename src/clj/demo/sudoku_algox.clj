(ns demo.sudoku-algox
  (:require [clojure.string])
  (:import [xl.learn AlgoX]))

(defn- one-hot [l i]
  (assoc l i (int 1))
  )

(defn- sudoku-matrix [sudoku-grid]
  (let [zeros-81 (into [] (repeat (* 9 9) (int 0)))
        matrix
        (reduce
          (fn [grid [cell-r cell-c cell-n]]
            (let [cell-cols (one-hot zeros-81 (+ (* 9 cell-r) cell-c))
                  row-cols (one-hot zeros-81 (+ (* 9 cell-r) cell-n))
                  col-cols (one-hot zeros-81 (+ (* 9 cell-c) cell-n))
                  region-r (int (/ cell-r 3))
                  region-c (int (/ cell-c 3))
                  region-cell (+ (* 3 region-r) region-c)
                  region-cols (one-hot zeros-81 (+ (* region-cell 9) cell-n))
                  ]
              (conj grid (concat cell-cols row-cols col-cols region-cols))
              )
            )
          []
          (for [cell-r (range 9) cell-c (range 9) cell-n (range 9)]
            [cell-r cell-c cell-n]
            )
          )
        removed-rows
        (remove nil?
                (flatten
                  (map-indexed
                    (fn [row sudoku-row]
                      (map-indexed
                        (fn [col n]
                          (if (nil? n)
                            nil (int (+ (dec n) (* 9 (+ (* 9 row) col)))))
                          )
                        sudoku-row)
                      )
                    sudoku-grid)))
        ]
    {:matrix matrix :removed-rows removed-rows}
    )
  )

(defn- to-sudoku-grid [rows-solution sudoku-grid]
  (reduce
    (fn [grid row]
      (let [cell-n (inc (mod row 9))
            row-col (int (/ row 9))
            cell-col (mod row-col 9)
            cell-row (int (/ row-col 9))]
        (assoc-in grid [cell-row cell-col] cell-n)
        )
      )
    sudoku-grid
    rows-solution
    )
  )

(defn sudoku [sudoku-grid use-heuristic find-first]
  (let [{:keys [matrix removed-rows]} (sudoku-matrix sudoku-grid)
        rows-solutions (AlgoX/solveGrid matrix nil removed-rows use-heuristic find-first)
        ]
    (for [rows-solution rows-solutions]
      (to-sudoku-grid rows-solution sudoku-grid)
      )
    )
  )

(defn- output-grid [grid]
  (doseq [row grid]
    (println (clojure.string/join " " row))
    )
  )

(defn- samples []
  (doseq [sudoku-grid
          [
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

            [
             [5   nil nil 8   nil nil 7   6   nil]
             [nil 9   nil 4   5   nil nil 1   nil]
             [3   nil nil nil nil nil nil nil nil]
             [nil nil 9   6   1   nil nil nil nil]
             [nil nil nil nil nil nil nil nil nil]
             [nil nil nil nil 3   8   9   nil nil]
             [nil nil nil nil nil nil nil nil 3  ]
             [nil 3   nil nil 2   4   nil 8   nil]
             [nil 4   7   nil nil 1   nil nil 2  ]
             ]

           ]
          ]
    (let [grids (sudoku sudoku-grid true true)]
      (doseq [grid grids]
        (println "=======================")
        (output-grid grid)
        )
      )
    )
  )

(samples)

(comment
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  [nil nil nil nil nil nil nil nil nil]
  )
