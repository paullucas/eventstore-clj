(ns eventstore-clj.event
  (:import [eventstore.j EventDataBuilder]
           java.util.UUID))

(defn build-event
  ([type] (.build (EventDataBuilder. "empty")))

  ([type data metadata]
   (let [data-fn (if (= type "json")
                   #(-> %
                        (.jsonData data)
                        (.jsonMetadata metadata))
                   #(-> %
                        (.data data)
                        (.metadata metadata)))]
     
     (-> (EventDataBuilder. type)
         (.eventId (java.util.UUID/randomUUID))
         (data-fn)
         (.build)))))

(comment
  (build-event "empty")
  (build-event "binary" (byte-array 4 [1 2 3 4]) (byte-array 4 [5 6 7 8]))
  (build-event "string" "data" "metadata")
  (build-event "json" "{\"data\":\"data\"}" "{\"metadata\":\"metadata\"}"))
