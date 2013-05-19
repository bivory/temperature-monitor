(ns temperature_monitor.monitor
  (:use [midje.open-protocols]))

(defprotocol Monitor
  "Monitor a list of sensors for a threshold value."
  (start [this] "Start monitoring the sensors.")
  (stop [this] "Stop monitoring the sensors."))


(defrecord-openly ThresholdMonitor [threshhold-fn duration log alarm sensors])

(extend-type ThresholdMonitor Monitor
  (start [this] :undefined)
  (stop [this] :undefined))

