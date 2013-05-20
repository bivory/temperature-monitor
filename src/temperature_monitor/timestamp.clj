(ns temperature_monitor.timestamp
  (:use [midje.open-protocols]))

(defprotocol Timestamp
  (get-timestamp [this] "Returns a timestamp"))


(defrecord-openly QueueTimestamp [offset timestamps])

(defn create-queue-timestamp
  "Creates a Queue Timestamp that doesn't actually return real timestamps.
   Instead it returns timestamps that are given to it at the time this function
   is called. This is useful behavior for test doubles."
  [timestamps]
  {:pre [(not (nil? timestamps))
         (coll? timestamps)
         (> (count timestamps) 0)]}
  (let [offset (atom 0)]
    (->QueueTimestamp offset timestamps)))

(extend-type QueueTimestamp Timestamp
  (get-timestamp [this]
    (let [offset (get this :offset)
          n @offset
          ts (get this :timestamps)]
      (swap! offset (fn [x] (mod (inc x) (count ts))))
      (nth ts n))))
