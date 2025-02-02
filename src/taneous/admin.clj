(ns taneous.admin
  (:require [taneous.util :as util]))

(defn query
  [app q]
  (util/post app
        "/admin/query"
        {:query q}))

(defn- tx->transact-repr
  [tx]
  [(:tx tx) (:ns tx) (:id tx) (:q tx)])

(defn transact
  [app & txs]
  (util/post app
        "/admin/transact"
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

