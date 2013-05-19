(ns temperature_monitor.sensor
  (:use [midje.open-protocols]))

(defprotocol Sensor
  "A sensor that allows sampling a temperature."
  (get-temperature [this] "Sample of the temperature the sensor is monitoring.")
  (get-id [this] "Get the id of this sensor."))


(defrecord-openly QueueSensor [id temperatures])

(extend-type QueueSensor Sensor
  (get-id [this] (get this :id))
  (get-temperature [this] (first (get this :temperatures))))
