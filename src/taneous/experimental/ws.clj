(ns taneous.experimental.ws
  (:require [taneous.experimental.util :as u]
            [gniazdo.core :as ws]
            [cheshire.core :as json]
            [taoensso.timbre :as t]
            [clojure.pprint :refer [pprint]]))

(defn on-open
  [_]
  (t/info "[WS] Open"))

(defn on-close
  [status reason]
  (t/info "[WS] Close" status reason))

(defn on-error
  [error]
  (t/info "[WS] Error" error))

(defn try-send!
  [*state data]
  (ws/send-msg
   (:ws @*state)
   (json/encode data)))

(defn set-attrs!
  [*state attrs]
  (swap! *state
         assoc :attrs
         (into {}
               (map (juxt :id identity))
               attrs)))

(defn set-session-id!
  [*state session-id]
  (swap! *state assoc :session-id session-id))

(defn subscribe!
  [*state q cb]
  (let [watch-key (u/uuid)]
    (add-watch *state watch-key
               (fn [_key _atom old new]
                 (when (not= (get-in old [:subs q])
                             (get-in new [:subs q]))
                   (cb (get-in new [:subs q])))))
    (try-send! *state {:client-event-id watch-key :op "add-query" :q q})
    (fn []
      (try-send! *state {:client-event-id watch-key :op "remove-query" :q q})
      (remove-watch *state watch-key))))

(defmulti handle-receive
  (fn [_*state msg]
    (t/info "[RECEIVE]" (:op msg) msg)
    (:op msg)))

(defmethod handle-receive "init-ok"
  [*state msg]
  (set-attrs!      *state (:attrs msg))
  (set-session-id! *state (:session-id msg)))

(defn- set-query-result!
  [*state q qresult]
  (swap! *state assoc-in [:subs q] qresult))

(defmethod handle-receive "add-query-ok"
  [*state msg]
  (let [{q :q result :result} msg
        triples (u/extract-triples result)
        qresult (u/triples->query-result *state triples)]
    (set-query-result! *state q qresult)))

(defmethod handle-receive "refresh-ok"
  [*state msg]
  (let [{computations :computations
         attrs :attrs} msg]
    (set-attrs! *state attrs)
    (doseq [comp computations
            :let [q (:instaql-query comp)
                  result (:instaql-result comp)
                  triples (u/extract-triples result)
                  qresult (u/triples->query-result *state triples)]]
      (set-query-result! *state q qresult))))

(defmethod handle-receive "add-query-exists"
  [*state msg]
  '...)

(defmethod handle-receive "transact-ok"
  [*state msg]
  '...)

(defmethod handle-receive "patch-presence"
  [*state msg]
  '...)

(defmethod handle-receive "refresh-presence"
  [*state msg]
  '...)

(defmethod handle-receive "server-broadcast"
  [*state msg]
  '...)

(defmethod handle-receive "join-room-ok"
  [*state msg]
  '...)

(defmethod handle-receive "join-room-error"
  [*state msg]
  '...)

(defmethod handle-receive "error"
  [*state msg]
  '...)

(defmethod handle-receive :default
  [_*state _msg]
  '...)

(defn on-message
  [*state msg]
  (let [msg (json/decode (.toString msg) true)]
    (handle-receive *state msg)))

(defn init!
  [{:keys [app-id api-url]}]
  (let [api-url (or api-url "wss://api.instantdb.com/runtime/session")
        *state (atom {:ws nil :session-id nil :subs {} :attrs {}})
        _ (swap!
           *state assoc :ws
           (ws/connect api-url
                       :on-connect on-open
                       :on-close   on-close
                       :on-error   on-error
                       :on-receive #(on-message *state %)))]
    (try-send!
     *state {:client-event-id (u/uuid)
             :op "init"
             :app-id app-id
             :refresh-token nil
             :versions {"@instantdb/core" "0.17.12"}
             :__admin-token nil})
    *state))

(comment

  (t/set-min-level! :warn) ; Disable most logging

  ;; Initialize an app
  (def *app (init! {:app-id "a0df8ec5-d58b-46b4-89e4-ebfa3c759d68"}))

  ;; Query Examples
  (def q {:example {}})
  (def q {:example {:$ {:where {:id "32cd1087-d6f2-49c5-bbc3-4f3d2a38fbc9"}}}})

  ;; Subscribe to query `q` and print the content of the first element
  ;; `subscribe!` returns a nullary function to stop the subscription
  (def unsub
    (subscribe! *app q
                #(println "Message:" (get-in % [:example 0 :content]))))

  ;; Perform unsubscribe action
  (unsub)

  ;; Close connection
  (ws/close (:ws @*app))

  '...)

;; ---------
;; todo list
;; TODO: Add docstrings
;; TODO: Ensure *state dereferences avoid state changes during operations
;;       For example, in the "refresh-ok" handler the state is currently
;;       being derefferenced repeatedly in different steps.
;; TODO: Handle websocket disconnects/retries
;; TODO: Remove :subs when there are no longer any watchers
;; TODO: Add error handling
;; TODO: Add schema and validation support
