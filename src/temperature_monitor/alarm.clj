(ns temperature_monitor.alarm
  (:use [midje.open-protocols]))

(defprotocol Alarm
  (sound-alarm [this] "Triggers an alarm."))

(defrecord-openly ConsoleAlarm []
  Alarm
  (sound-alarm [this] :unfinished))
