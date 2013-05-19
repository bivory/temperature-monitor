(ns temperature_monitor.log
  (:use [midje.open-protocols]))

(defprotocol Log
  "Allows temperature readings from the monitored sensors to be recorded."
  (add-entry [this sensor-id temperature timestamp]
             "Creates a new log entry, logging the time at which a temperature
              was read from a sensor."))

(defrecord-openly ConsoleLog [])

(extend-type ConsoleLog Log
  (add-entry [this sensor-id temperature timestamp]
    {:pre [(number? sensor-id)
           (number? temperature)
           (number? timestamp)]}
    (let [output (str timestamp ": Read a temperature of "
                      temperature " from sensor " sensor-id ".")]
      (println output)
      output)))
