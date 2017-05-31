(defproject numspeak "1.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [overtone/at-at "1.2.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [overtone "0.8.1"]]
  :main  ^{:skip-aot true}  numspeak.core
  #_:aot #_[numspeak.core])
