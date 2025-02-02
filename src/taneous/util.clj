(ns taneous.util
  (:require [clj-http.client :as http]))

(defn post
  [app path params]
  (-> (http/post
       (str (:api-url app) path)
       (assoc app :form-params params))
      :body))

(defn get
  [app path params]
  (-> (http/get
       (str (:api-url app) path)
       (assoc app :query-params params))
      :body))

(defn delete
  [app path params]
  (-> (http/delete
       (str (:api-url app) path)
       (assoc app :query-params params))
      :body))
