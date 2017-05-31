(ns numspeak.core
  (:use overtone.live)
  (:require [overtone.at-at :refer [mk-pool every]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [join]])
  (:gen-class :main true))

(def cli-options
  [["-p" "--pause SECONDS" "pause between rounds"
    :id :pause
    :default 5
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %) "Must be a number greater than 0"]]
   ["-s" "--sample-set" "Sample Set"
    :id :sample-set
    :default "default"]
   ["-x" "-ext" "--extention" "File Extention"
    :id :ext
    :default "aiff"
    :validate [#(contains? {"wav" "aiff"} %) "only wav or aiff are supported"]]
   ["-h" "--help"]])

(defn cart [colls]
  (if (empty? colls)
    '(())
    (for [x (first colls)
          more (cart (rest colls))]
      (cons x more))))
(defn powerset [s] (cart [s s]))

(defn random-number [length]
  (keyword (join (take length (repeatedly #(rand-int 9))))))
(defn random-pao []
  (take 3 (repeatedly #(random-number 2))))

(defn -main [& args]
  (def options ((parse-opts args cli-options) :options))
  (def numbers (->> (map str (range 10))
                    powerset
                    (map join)
                    (map #(vec [(keyword %) (load-sample (join ["resources/" (options :sample-set) "/" % "." (options :ext)]))]))
                    (into {})))

  (defn synth-params [number]
    (let [dry (play-buf :num-channels 1 :bufnum (numbers number) :rate 0.5)]
      (out 0 [dry dry])))

  (defn set-person! [number]
    (defsynth play-person []
      (synth-params number)))

  (defn set-action! [number]
    (defsynth play-action []
      (synth-params number)))

  (defn set-object! [number]
    (defsynth play-object []
      (synth-params number)))

  (defn set-pao [[p a o]]
    (set-person! p)
    (set-action! a)
    (set-object! o))

  (defn play-pao [nome]
    (let [beat (nome)]
      (at (nome beat) (play-person))
      (apply-at (nome (inc beat)) play-action)
      (apply-at (nome (inc (inc beat))) play-object)))

  (defn pao-round []
    (do (set-pao (random-pao))
      (play-pao (metronome 60))))

  (def my-pool (mk-pool))

  (every (* (options :pause) 1000) pao-round my-pool))
