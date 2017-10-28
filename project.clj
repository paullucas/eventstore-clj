(defproject eventstore-clj "0.1.0-SNAPSHOT"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [com.geteventstore/eventstore-client_2.12 "3.0.7"]
                 [t6/from-scala "0.3.0"]]
  :uberjar-merge-with {#"\.properties$" [slurp str spit] ;; https://github.com/deeplearning4j/deeplearning4j/issues/482
                       "reference.conf" [slurp str spit]}
  :main eventstore-clj.core)
