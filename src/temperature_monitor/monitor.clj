(ns temperature_monitor.monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s]
            [overtone.at-at :as at-at])
  (:use [midje.open-protocols]))

(defn- ^{:testable true} create-peak-monitor
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
         (>= (count sensors) max-exceeded)
         (every? true? (map (partial satisfies? s/Sensor) sensors))]}
  {:threshold-fn (fn [t] (> t threshold))
   :max-exceeded max-exceeded
   :duration duration
   :log log
   :alarm alarm
   :sensors sensors})

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


(defprotocol Monitor
  "Monitor a list of sensors for a threshold value."
  (start [this poll-interval] "Start monitoring the sensors.")
  (stop [this] "Stop monitoring the sensors."))


(defrecord-openly ATATMonitor [monitor])

(defn- ^{:testable true} wrap-atat-poll-fn
  "Wraps a callback function with the provided state. The At-At library doesn't
   provide a way to save state."
  [poll-fn state]
  (let [wrapped-state (atom state)]
    (fn []
      (->> (poll-fn @wrapped-state)
           (reset! wrapped-state)))))

(extend-type ATATMonitor Monitor

  (start [this poll-interval]
    (let [pool (or (get this :pool) (at-at/mk-pool))
          poll-fn (wrap-atat-poll-fn (get this :poll-fn))
          thread (at-at/every poll-interval poll-fn pool)]
      (-> this
          (assoc :thread thread)
          (assoc :pool pool))))

  (stop [this]
    (let [thread (get this :thread)]
      (when (not (nil? thread))
        (at-at/stop thread))
      (dissoc this :thread))))

(defn- atat-poll
  [this]
  (println "Hi")
  this)

(defn create-atat-monitor
  "Create an ATATMonitor that sounds an alarm if the temperature readings from
   two or more sensors are above the threshold temperature for at least two
   seconds."
  [threshold max-exceeded duration log alarm sensors & {:keys [poll-fn]
                                                        :or {poll-fn atat-poll}}]
  (let [m (create-peak-monitor threshold max-exceeded duration log alarm sensors)]
    (-> (->ATATMonitor m)
        (assoc :poll-fn poll-fn))))
