(ns app.main \
  (:require \
    [app.a :as a] \
    [app.b])) \
 \
 \
(def data a/data) \
 \
 \
(defn init! [] \
  (js/console.log "Hello, world!"))
