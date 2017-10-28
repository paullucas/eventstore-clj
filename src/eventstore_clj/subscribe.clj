(ns eventstore-clj.subscribe
  (:import eventstore.StreamSubscriptionActor
           eventstore.EventStream
           eventstore.PersistentSubscriptionActor
           eventstore.EventNumber
           akka.actor.Props
           scala.Option))

(defrecord PersistentSubscription [subscription-actor observer-actor])
(defrecord Subscription [subscription-actor observer-actor])

(defn subscribe-persistent [^Connection {creds :credentials conn :actor settings :settings}
                            ^String stream
                            ^String group
                            ^Props observer]
  (let [system (.system conn)
        observer-actor (.actorOf system observer)
        subscription-actor (.actorOf system
                                     (. Props create
                                        PersistentSubscriptionActor
                                        (into-array Object [conn
                                                            observer-actor
                                                            (EventStream/apply stream)
                                                            group
                                                            (Option/apply creds)
                                                            settings
                                                            true])))] ;; autoack
    (PersistentSubscription. subscription-actor observer-actor)))

(defn subscribe [^Connection {creds :credentials conn :actor}
                 ^String stream observer
                 & [{:keys [position resolve-links? read-batch-size]
                     :or {position nil resolve-links? false read-batch-size 100}}]]
  (let [system (.system conn)
        observer-actor (.actorOf system observer)
        subscription-actor (.actorOf system (. StreamSubscriptionActor
                                               getProps
                                               conn
                                               observer-actor
                                               (EventStream/apply stream)
                                               (if position
                                                 (Option/apply (EventNumber/apply position))
                                                 (Option/empty))
                                               resolve-links?
                                               (Option/apply creds)
                                               read-batch-size))]
    (Subscription. subscription-actor observer-actor)))
