(ns temperature_monitor.t-timestamp
  (:use midje.sweet)
  (:use [temperature_monitor.timestamp]))

(facts "about QueueTimestamp"
       (fact "getting a timestamp"
             (get-timestamp (create-queue-timestamp [..temp..])) => ..temp..)
       (fact "getting multiple temperature reading"
             (let [s (create-queue-timestamp [1 2 3])]
               (get-timestamp s) => 1
               (get-timestamp s) => 2
               (get-timestamp s) => 3
               (get-timestamp s) => 1)))
