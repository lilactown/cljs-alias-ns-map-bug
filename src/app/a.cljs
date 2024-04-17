(ns app.a
  (:require
    [app.b :as-alias b]))

;; works
#_(def data {::b/x 1 ::b/y 2 ::b/z 3})

;; broken
(def data #::b {:x 1 :y 2 :z 3})
