(ns temperature_monitor.sensor
  (:use [midje.open-protocols]))

(defprotocol Sensor
  "A sensor that allows sampling a temperature."
  (get-temperature [this] "Sample of the temperature the sensor is monitoring.")
  (get-id [this] "Get the id of this sensor."))


(defrecord-openly QueueSensor [id offset temperatures])

(defn create-queue-sensor
  [id temperatures]
  (let [offset (atom 0)]
    (->QueueSensor id offset temperatures)))

(extend-type QueueSensor Sensor
  (get-id [this] (get this :id))
  (get-temperature [this]
    (let [offset (get this :offset)
          n @offset
          ts (get this :temperatures)]
      (swap! offset (fn [x] (mod (inc x) (count ts))))
      (nth ts n))))
