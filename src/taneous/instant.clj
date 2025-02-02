(ns taneous.instant)

(defn init
  ([admin-token app-id]
   (init admin-token app-id "https://api.instantdb.com"))
  ([admin-token app-id api-url]
   {:headers {"Authorization" (str "Bearer " admin-token),
              "App-Id" (str app-id)}
    :content-type :json
    :app-id app-id
    :api-url api-url
    :as :json}))
