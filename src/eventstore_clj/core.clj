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
           eventstore.ReadEventCompleted
           )
  (:require [eventstore-clj.connection :refer [start stop]]
            [eventstore-clj.subscribe :refer [subscribe subscribe-persistent]]
            [eventstore-clj.event :refer [build-event
                                          event->map
                                          content->map]]
            [clojure.reflect :as r]
            [clojure.core.async :refer [chan go go-loop <! >! <!!]]))

(defmacro actor
  "Macro used to define an actor without generating classes.
  Returns a Props object that can be passed to the .actorOf method of ActorSystem."
  [& forms]
  `(Props/create ~UntypedActor (proxy [Creator] []
                                 (~'create []
                                  (proxy [UntypedActor] []
                                    ~@forms)))))

;; 1. Take event off of channel
;; 2. Get Clojure map representing the event's data
;; 3. Append map to events vector
(defn log-events [events c]
  (go-loop []
    (let [msg  (<! c)
          json (event->map msg)]
      (swap! events conj json))))

(comment
  (def last-event (atom nil))       ; What was the last event?
  (def event-chan (chan))           ; Channel for received events
  (def events (atom []))            ; Vector of parsed events

  ;; Connect to EventStore
  (def connection (start {:host "localhost"
                          :port 1113
                          :user "admin"
                          :password "changeit"}))
  ;; Start the listener (takes events from channel, appends to events vector)
  (log-events events event-chan)

  ;; Subscribe to the Default ($All) stream.
  ;; When we receive a message:
  ;; 1. Print to console
  ;; 2. Assign it to last-event
  ;; 3. Put onto channel (event-chan)
  (subscribe connection "Default"
             (actor
              (onReceive [message]
                         (go
                           (prn message)
                           (reset! last-event message)
                           (>! event-chan message))))))

;; Util fn
(defn members [o]
  (->> o r/reflect :members (map :name) sort))
