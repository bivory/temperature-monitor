(ns temperature_monitor.t-alarm
  (:use midje.sweet)
  (:use [temperature_monitor.alarm]))

(facts "about temperature alarm"
       (against-background [(around :checks (let [alarm (->ConsoleAlarm)] ?form))]
                           (fact "Sounding the console alarm will beep"
                             (sound-alarm alarm) => true)))

