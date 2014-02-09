(ns server
  (:require [compojure.core :refer :all]
            [hiccup.middleware :refer (wrap-base-url)]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))
 
(defroutes main-routes
  (GET "/" [] (ring.util.response/redirect "/index.html"))
  (route/resources "/")
  (route/not-found "Page not found"))
 
(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
