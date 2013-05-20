(defproject temperature-monitor "0.0.1-SNAPSHOT"
  :description "Temperature Monitor allows you to monitor the temperature of sensors."
  :main temperature_monitor.core
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [overtone/at-at "1.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]
                   :plugins [[lein-midje "3.0.1"]
                             [lein-cloverage "1.0.2"]]}}
  :url "http://github.com/bivory/temperature-monitor"
  :license "Eclipse Public License 1.0")
