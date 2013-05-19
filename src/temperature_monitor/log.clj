(ns temperature_monitor.log
  (:use [midje.open-protocols]))

(defprotocol TemperatureLog
  "Allows temperature readings from the monitored sensors to be recorded."
  (add-entry [this sensor-id temperature timestamp]
             "Creates a new log entry, logging the time at which a temperature
              was read from a sensor."))

(defrecord-openly ConsoleLog [])

(extend-type ConsoleLog TemperatureLog
  (add-entry [this sensor-id temperature timestamp]
             {:pre [(number? sensor-id)
                    (number? temperature)
                    (number? timestamp)]}
             :undefined))
