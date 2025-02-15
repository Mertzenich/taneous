(ns taneous.admin
  (:require [taneous.util :as util]))

(defn get-schema
  [app]
  (util/get app
            "/admin/schema"
            {:app-id (:app-id app)}))

(defn query
  [app q]
  (util/post app
             "/admin/query"
             {:query q}))

(defn query-perms-check
  [app q]
  (util/post app
             "/admin/query_perms_check"
             {:app-id (:app-id app)
              :query q}))

(defn- tx->transact-repr
  [{:keys [tx ns id q]}]
  [tx ns id q])

(defn transact
  [app & txs]
  (util/post app
             "/admin/transact"
             {:steps (map tx->transact-repr txs)}))

(defn transact-perms-check
  [app & txs]
  (util/post app
             "/admin/transact_perms_check"
             {:steps (map tx->transact-repr txs)}))

(defn as-guest
  [app]
  (assoc-in app [:headers "as-guest"] "true"))

(defn as-email
  [app email]
  (assoc-in app [:headers "as-email"] email))

(defn as-token
  [app token]
  (assoc-in app [:headers "as-token"] token))

