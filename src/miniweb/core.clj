(ns miniweb.core
  (:use [hiccup.core])
  (:use [compojure.core]))

(defn mockup-1 []
  (html [:head [:title "mini-browser"]]
        [:body {:id "browser"}]))

(defroutes mockup-routes
  (GET "/m1" [] (mockup-1)))

(defn start-jetty []
  (1run-jetty (var mockup-routes)
             {:port 8099
              :join? false}))
