# ClojureScript as-alias + namespaced map bug

Currently, ClojureScript compilation fails when requiring a namespace using 
`:as-alias` and then using that alias in a namespaced map.

```
$ cat src/app/main.cljs
(ns app.a
  (:require
    [app.b :as-alias b]))

;; works
#_(def data {::b/x 1 ::b/y 2 ::b/z 3})

;; broken
(def data #::b {:x 1 :y 2 :z 3})

$ clj -M --main cljs.main --compile app.a
Unexpected error compiling at (REPL:1).
No namespace: app.b found

Full report at:
/var/folders/28/7xm2frlx1cb_gt4nrscjpttw0000gn/T/clojure-10509037535418893124.edn
```

This is regardless of whether `app.b` is on the classpath.

## shadow-cljs

shadow-cljs has additional behavior: if `app.b` is on the namespace _and is
compiled before `app.a`_, then it will pass compilation.

This persists even if `app.b` is removed as long as the cache for that ns still
exists.

```
$ shadow-cljs compile app
shadow-cljs - config: /Users/lilactown/Code/shadow-ns-map/shadow-cljs.edn
[:app] Compiling ...
------ ERROR -------------------------------------------------------------------
 File: /Users/lilactown/Code/shadow-ns-map/src/app/a.cljs:9:15
--------------------------------------------------------------------------------
   6 | #_(def data {::b/x 1 ::b/y 2 ::b/z 3})
   7 |
   8 | ;; broken
   9 | (def data #::b {:x 1 :y 2 :z 3})
---------------------^----------------------------------------------------------
No namespace: app.b found

--------------------------------------------------------------------------------
  10 |
--------------------------------------------------------------------------------

$ echo '(ns app.b)' > src/app/b.cljs

$ shadow-cljs compile app
shadow-cljs - config: /Users/lilactown/Code/shadow-ns-map/shadow-cljs.edn
[:app] Compiling ...
------ ERROR -------------------------------------------------------------------
 File: /Users/lilactown/Code/shadow-ns-map/src/app/a.cljs:9:15
--------------------------------------------------------------------------------
   6 | #_(def data {::b/x 1 ::b/y 2 ::b/z 3})
   7 |
   8 | ;; broken
   9 | (def data #::b {:x 1 :y 2 :z 3})
---------------------^----------------------------------------------------------
No namespace: app.b found

--------------------------------------------------------------------------------
  10 |
--------------------------------------------------------------------------------

$ echo '(ns app.main \
  (:require \
    [app.a :as a] \
    ;; require app.b and hope it is compiled before app.a \
    [app.b])) \
 \
 \
(def data a/data) \
 \
 \
(defn init! [] \
  (js/console.log "Hello, world!"))' > src/app/main.cljs
  
$ shadow-cljs compile app
shadow-cljs - config: /Users/lilactown/Code/shadow-ns-map/shadow-cljs.edn
[:app] Compiling ...
[:app] Build completed. (49 files, 4 compiled, 0 warnings, 1.24s)

$ git checkout -- src/app/main.cljs # reset main.cljs w/o require

$ shadow-cljs compile app
shadow-cljs - config: /Users/lilactown/Code/shadow-ns-map/shadow-cljs.edn
[:app] Compiling ...
[:app] Build completed. (48 files, 1 compiled, 0 warnings, 1.02s)

$ rm -rf .shadow-cljs/builds

$ shadow-cljs compile app
shadow-cljs - config: /Users/lilactown/Code/shadow-ns-map/shadow-cljs.edn
[:app] Compiling ...
------ ERROR -------------------------------------------------------------------
 File: /Users/lilactown/Code/shadow-ns-map/src/app/a.cljs:9:15
--------------------------------------------------------------------------------
   6 | #_(def data {::b/x 1 ::b/y 2 ::b/z 3})
   7 |
   8 | ;; broken
   9 | (def data #::b {:x 1 :y 2 :z 3})
---------------------^----------------------------------------------------------
No namespace: app.b found

--------------------------------------------------------------------------------
  10 |
--------------------------------------------------------------------------------
```
