(ns user
  (:require [mount.core :as mount]
            [demo.figwheel :refer [start-fw stop-fw cljs]]
            ))

(defn start []
  (mount/start-without
    (eval (str "(do (require '[demo.core]) #'demo.core/repl-server)"))
    ;    #'demo.core/repl-server
    )
  )

(defn stop []

    (mount/stop-except
      (eval (str "(do (require '[demo.core]) #'demo.core/repl-server )"))
      ;      #'demo.core/repl-server
      )
  )

(defn restart []
  (stop)
  (start))


