(ns eventstore-clj.core
  (:import [akka.actor ActorSystem Props UntypedActor]
           akka.japi.Creator
           akka.pattern.Patterns
           [eventstore.j EventDataBuilder SettingsBuilder WriteEventsBuilder]
           eventstore.StreamSubscriptionActor
           eventstore.Position
           eventstore.EventStream
           eventstore.tcp.ConnectionActor
           java.net.InetSocketAddress
           java.util.concurrent.TimeUnit
           java.util.UUID
           scala.concurrent.Await
           scala.concurrent.duration.Duration
           eventstore.UserCredentials
           eventstore.PersistentSubscriptionActor
           eventstore.PersistentSubscriptionSettings
           eventstore.j.PersistentSubscriptionSettingsBuilder
           eventstore.EventNumber
           akka.actor.Props
           scala.Option
           eventstore.ReadEventCompleted)
  (:require [eventstore-clj.connection :refer [start stop]]
            [eventstore-clj.subscribe :refer [subscribe
                                              subscribe-persistent
                                              subscribe-chan]]
            [eventstore-clj.event :refer [build-event
                                          event->map
                                          content->map]]
            [clojure.reflect :as r]
            [clojure.core.async :refer [chan go go-loop <! >! <!!]])
  (:use [eventstore-clj.subscribe :only [actor]]))

;; Util fn
(defn members [o]
  (->> o r/reflect :members (map :name) sort))

;; 1. Take event off of channel
;; 2. Get Clojure map representing the event's data
;; 3. Append map to events vector
(defn log-events [events c]
  (go-loop []
    (let [msg (<! c)
          json (event->map msg)]
      (swap! events conj json))
    (recur)))

(comment
  (def event-chan (chan))               ; Channel for received events
  (def events (atom []))                ; Vector of parsed events

  ;; Connect to EventStore
  (def connection (start {:host "localhost"
                          :port 1113
                          :user "admin"
                          :password "changeit"}))

  ;; Start the listener (takes events from channel, appends to events vector)
  (log-events events event-chan)

  ;; Subscribe to Default stream
  (subscribe-chan connection "Default" event-chan))
