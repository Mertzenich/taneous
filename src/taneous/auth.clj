(ns taneous.auth
  (:require [taneous.util :as util]))

(defn create-token
  [app email]
  (util/post app
             "/admin/refresh_tokens"
             {:email email}))

(defn verify-token
  [app token]
  (util/post app
             "/runtime/auth/verify_refresh_token"
             {:app-id (:app-id app) :refresh-token token}))

(defn generate-magic-token
  [app email]
  (util/post app
             "/admin/magic_code"
             {:email email}))

#_{:clj-kondo/ignore [:unused-binding]}
(defn get-user
  [app & {:keys [id email refresh-token] :as params}]
  (util/get app
            "/admin/users"
            params))

#_{:clj-kondo/ignore [:unused-binding]}
(defn delete-user
  [app & {:keys [id email refresh-token] :as params}]
  (util/delete app
               "/admin/users"
               params))

(defn sign-out-user
  [app email]
  (util/post app
             "/admin/sign_out"
             {:email email}))
