(ns temperature_monitor.sensor
  (:use [midje.open-protocols]))

(defprotocol TemperatureSensor
  "A sensor that allows sampling a temperature."
  (get-temperature [this] "Sample of the temperature the sensor is monitoring.")
  (get-id [this] "Get the id of this sensor."))
