(ns temperature_monitor.monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s])
  (:use [midje.open-protocols]))

(defprotocol Monitor
  "Monitor a list of sensors for a threshold value."
  (start [this] "Start monitoring the sensors.")
  (stop [this] "Stop monitoring the sensors."))


(defrecord-openly ThresholdMonitor [threshold-fn max-exceeded duration log alarm sensors])

(defn create-peak-monitor
  "Create a Monitor that sounds an alarm if the temperature readings from two
   or more sensors are above the threshold temperature for at least two
   seconds."
  [threshold max-exceeded duration log alarm sensors]
  {:pre [(number? threshold)
         (number? max-exceeded)
         (pos? max-exceeded)
         (number? duration)
         (pos? duration)
         (satisfies? l/Log log)
         (satisfies? a/Alarm alarm)
         (not (nil? sensors))
         (> (count sensors) 2)
         (every? true? (map (partial satisfies? s/Sensor) sensors))]}
  (->ThresholdMonitor (fn [t] (> t threshold)) max-exceeded duration log alarm sensors))

(extend-type ThresholdMonitor Monitor
  (start [this poll-interval] :undefined)
  (stop [this] :undefined))


(defn- ^{:testable true} get-exceeded-sensors
  "Given a map of sensor temperatures, it returns a list of the sensors
   that have exceeded the threshold."
  [threshold-fn sensors]
  {:pre [(fn? threshold-fn)
         (not (nil? sensors))]}
  (filter (fn [{:keys [temperature]}] (threshold-fn temperature)) sensors))

(defn- ^{:testable true} check-sensors
  "Returns a list of sensor ids and readings."
  [sensors]
  {:pre [(not (nil? sensors))
         (coll? sensors)
         (every? true? (map (partial satisfies? s/Sensor) sensors))]}
  (->> sensors
       (map (juxt s/get-id s/get-temperature))
       (map (fn [[id t]] {:id id :temperature t}))))
