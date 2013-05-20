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

(defn- ^{:testable true} update-exceeded-times
  "Given a map of the current sensor temperatures that failed the threshold,
   the current timestamp and a map of the when the sensors first failed the
   threshold, the first failed map will be updated.
   If a sensor is not listed in the current sensor temperatures, it will be
   removed from the first failed map."
  [prev-times curr-time curr-temps]
  {:pre [(not (nil? prev-times))
         (coll? prev-times)
         (not (nil? curr-time))
         (number? curr-time)
         (not (nil? curr-temps))
         (coll? curr-temps)]}
  (let [curr-times (->> curr-temps
                        (map (fn [{:keys [id]}] [id curr-time]))
                        (into {}))]
    (->> (merge curr-times prev-times)
         (filter (fn [[id t]] (contains? curr-times id)))
         (into {}))))

(defn- ^{:testable true} get-exceeded-durations
  "Returns a list of sensors that have triggered the duration threshold
   function."
  [threshold-fn durations]
  {:pre [(fn? threshold-fn)
         (not (nil? durations))
         (coll? durations)]}
  :undefined)

(defn- ^{:testable true} check-sensors
  "Returns a list of sensor ids and readings."
  [sensors]
  {:pre [(not (nil? sensors))
         (coll? sensors)
         (every? true? (map (partial satisfies? s/Sensor) sensors))]}
  (->> sensors
       (map (juxt s/get-id s/get-temperature))
       (map (fn [[id t]] {:id id :temperature t}))))

(defn- ^{:testable true} sensor-loop
  "Polls all the sensors to sample the current temperature readings.
   When max-exceeded number of sensors trip the threshold function and
   it has lasted for the provided time duraction, an alarm will be raised
   and the temperatures will be logged."
  [{:keys [threshold-fn max-exceeded duration log alarm sensors] :as m}]
  (let [timestamp 0 ;; TODO
        sensor-temps (check-sensors sensors)
        exceeded-temps (get-exceeded-sensors threshold-fn sensor-temps)]
    (when (>= (count exceeded-temps) max-exceeded)
      (a/sound-alarm alarm)
      (dorun (map (fn [{:keys [id temperature]}] (l/add-entry log id temperature timestamp)) exceeded-temps)))
    m))


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
          poll-fn (wrap-atat-poll-fn (get this :poll-fn) this)
          thread (at-at/every poll-interval poll-fn pool)]
      (-> this
          (assoc :thread thread)
          (assoc :pool pool))))

  (stop [this]
    (let [thread (get this :thread)]
      (when (not (nil? thread))
        (at-at/stop thread))
      (dissoc this :thread))))

(defn create-atat-monitor
  "Create an ATATMonitor that sounds an alarm if the temperature readings from
   two or more sensors are above the threshold temperature for at least two
   seconds."
  [threshold max-exceeded duration log alarm sensors & {:keys [poll-fn]
                                                        :or {poll-fn sensor-loop}}]
  (let [m (create-peak-monitor threshold max-exceeded duration log alarm sensors)]
    (-> (->ATATMonitor m)
        (assoc :poll-fn poll-fn))))
