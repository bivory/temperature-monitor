(ns temperature_monitor.monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s])
  (:use [midje.open-protocols]))

(defprotocol Monitor
  "Monitor a list of sensors for a threshold value."
  (start [this] "Start monitoring the sensors.")
  (stop [this] "Stop monitoring the sensors."))


(defrecord-openly ThresholdMonitor [threshhold-fn duration log alarm sensors])

(defn create-peak-monitor
  "Create a Monitor that sounds an alarm if the temperature readings from two
   or more sensors are above the threshold temperature for at least two
   seconds."
  [threshold duration log alarm sensors]
  {:pre [(number? threshold)
         (number? duration)
         (pos? duration)
         (satisfies? l/Log log)
         (satisfies? a/Alarm alarm)
         (not (nil? sensors))
         (> (count sensors) 2)
         (every? true? (map (partial satisfies? s/Sensor) sensors))]}
  (->ThresholdMonitor (fn [t] (> t threshold)) duration log alarm sensors))

(extend-type ThresholdMonitor Monitor
  (start [this] :undefined)
  (stop [this] :undefined))


(defn- ^{:testable true} get-exceeded-sensors
  "Returns a list of the sensors that have exceeded the threshold."
  [threshold-fn sensors]
  (->> sensors
       (map (juxt s/get-id s/get-temperature))
       (filter (fn [[id t]] (threshold-fn t)))
       (map (fn [[id t]] {:id id :temperature t}))))


