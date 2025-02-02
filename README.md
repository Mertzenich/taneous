# taneous

Taneous is a lightweight wrapper for the backend
[InstantDB](https://www.instantdb.com) API using
[clj-http](https://github.com/dakrone/clj-http). This is a work-in-progress,
there is little documentation, no tests, no error-handling, and no built-in
async support at this time.

## Usage

Initialize an application config. This is just a `clj-http` request map.

```clojure
(def app
  (instant/init "00000000-0000-0000-0000-000000000000" ; Admin Token
                "00000000-0000-0000-0000-000000000000" ; App ID
                ;; Optional: specify an api url
                "https://api.instantdb.com"))
```

### Transact

Transactions are represented using a map.

- `:tx` => transaction type (i.e. update, merge, etc)
- `:ns` => namespace you are working with
- `:id` => identifer
- `:q` => instaml query as a clojure map

```clojure
(admin/transact
 app
 {:tx :update
  :ns :books
  :id (str (random-uuid))
  :q {:title "The Innocence of Father Brown"
      :author "G. K. Chesterton"
      :pages 256}})
;; => {:tx-id ...}
```

You can use multiple transactions at once.

```clojure
(admin/transact
 app
 {:tx :update
  :ns :books
  :id (str (random-uuid))
  :q {:title "The Time Machine"
      :author "H. G. Wells"
      :pages 113}}
 {:tx :update
  :ns :books
  :id (str (random-uuid))
  :q {:title "Death Comes for the Archbishop"
      :author "Willa Cather"
      :pages 297}})
;; => {:tx-id ...}
```

An example of linking data:

```clojure
(let [workout-id (str (random-uuid))
      health-id (str (random-uuid))]
  (admin/transact
   app
   {:tx :update :ns :todos :id workout-id :q {:title "Go on a run"}}
   {:tx :update :ns :goals :id health-id  :q {:title "Get fit"}}
   {:tx :link   :ns :goals :id health-id  :q {:todos workout-id}}))
;; => {:tx-id ...}
```

### Query

Querying data is simple:

```clojure
(admin/query app {:goals {}})
;; => {:goals [{:id "00000000-0000-0000-0000-000000000000"
;;              :title "Get fit"}]}
```

Example of fetching a specific entity:

```clojure

(admin/query
 app
 {:goals {:$ {:where {:id "00000000-0000-0000-0000-000000000000"}}}})
;; => {:goals [{:id "00000000-0000-0000-0000-000000000000"
;;              :title "Get fit"}]}
```

Example of fetching associations:

```clojure
(admin/query
 app
 {:goals {:todos {}}})
;; =>  {:goals [{:id "d7b2791c-e851-4ae7-95ec-36a064d80c87",
;;               :title "Get fit",
;;               :todos [{:title "Go on a run",
;;                        :id "c2c6bb3f-7db9-466b-ade0-ce25fd205dbc"}]}]}
```

There is support for impersonation (`as-guest`, `as-email`, and `as-token`)
These functions take your `app` structure and add the appropriate headers.

```clojure
(let [guest (admin/as-guest app)]
  ;; Use `guest` for performing queries/transactions
  (admin/query guest {:goals {}}))

(let [user (admin/as-email app "john@example.com")]
  (admin/query user {:goals {}}))
```

### Auth

See `src/taneous/auth.clj` for functions pertaining to authentication.

## Credits

This project was made possible by:

- [Unofficial Admin HTTP API](https://www.dropbox.com/scl/fi/2yjy6xvqa0459hqeqg950/Unofficial-Admin-HTTP-API.paper)
- [Python InstantDB Admin
  Client](https://gist.github.com/aphexcx/d6120bd3aeb4a60ea566715c67d92297) by
  aphexcx

<!--  LocalWords:  Taneous InstantDB
 -->
