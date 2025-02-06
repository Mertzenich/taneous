(ns taneous.experimental.util)

(defn extract-triples
  [id-nodes]
  ;; TODO: Wrote this quickly, will need to take another look later to see if
  ;;       improvements can be made (and if it actually handles child-nodes properly)
  (reduce
   (fn [acc id-node]
     (let [join-rows (get-in id-node [:data :datalog-result :join-rows])
           child-nodes (:child-nodes id-node)]
       (-> (into acc (mapcat identity join-rows))
           (into (extract-triples child-nodes)))))
   []
   id-nodes))

(defn triples->tmap
  [*state triples]
  (let [attrs (:attrs @*state)]
    (reduce
     (fn [acc triple]
       (let [[id attr v] triple
             [_ ns col] (get-in attrs [attr :forward-identity])]
         (assoc-in acc [(keyword ns) id (keyword col)] v)))
     {}
     triples)))

(defn tmap->qresult
  [tmap]
  (update-vals tmap (comp vec vals)))

(defn triples->query-result
  [*state triples]
  (-> (triples->tmap *state triples)
      (tmap->qresult)))

(defn uuid [] (str (random-uuid)))
