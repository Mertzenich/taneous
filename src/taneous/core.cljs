(ns taneous.core
  (:require ["@instantdb/admin" :refer [init id]]))

(def APP_ID js/process.env.INSTANT_APP_ID)

(def APP_SECRET js/process.env.INSTANT_APP_SECRET)

(comment

  (def db (init #js {:appId APP_ID
                     :adminToken APP_SECRET}))

  (def data (.query db #js {:goals #js {}
                            :todos #js {}}))

  (.then (.query db #js {:goals #js {}
                         :todos #js {}})
         #(js/console.log (.-todos %)))

  (def goals (.-goals data))
  (def todos (.-todos data))

  '...)

(defn -main
  [& _args]
  (println "Taneous Started"))
