(ns eventstore-clj.connection
  (:import [akka.actor ActorSystem]
           [eventstore.j SettingsBuilder]
           eventstore.tcp.ConnectionActor
           eventstore.UserCredentials
           java.net.InetSocketAddress))

(defrecord Connection [credentials settings actor])

(defn start [{:keys [host port user password]}]
  (let [system (. ActorSystem create)
        credentials (UserCredentials. user password)
        settings (.. (SettingsBuilder.)
                     (address (new InetSocketAddress host port))
                     (defaultCredentials user password)
                     (build))
        actor (.actorOf system (. ConnectionActor getProps settings))]
    (Connection. credentials settings actor)))

(defn stop [^Connection {actor :actor}]
  (-> actor
      .system
      .shutdown))
