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
           scala.Option)
  (:require [eventstore-clj.connection :refer [start stop]]
            [eventstore-clj.subscribe :refer [subscribe subscribe-persistent]]))

(defmacro actor
  "Macro used to define an actor without generating classes.
  Returns a Props object that can be passed to the .actorOf method of ActorSystem."
  [& forms]
  `(Props/create ~UntypedActor (proxy [Creator] []
                                 (~'create []
                                  (proxy [UntypedActor] []
                                    ~@forms)))))

(comment
  (def connection (start {:host "localhost" :port 1113 :user "admin" :password "changeit"}))
  (subscribe connection "Default"
             (actor
              (onReceive [message]
                         (prn message))))

  (stop connection))
