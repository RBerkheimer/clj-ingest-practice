(ns ingest-test.utils.file-utils
    (:require   [clojure.java.io :as io]
                [clojure.string :refer [split-lines split replace]])
    (:gen-class))


;patient info regarding people in studies for the ANDROMEDA project
;Country,Site Number,Investigator Name,Subject ID,Screen Date,Screen Failure Date,Rescreen Date,Randomization Date,Randomization Number,Patient Type
(def enrollment-file (io/resource "inputs/andromeda_ivrs_enrollment_data_2018.csv"))

;non-unique csv of sites (duplicates exist)
;Columns - Site #,Site Status,Site Activation Date,State,Country
(def site-info-file (io/resource "inputs/andromeda_site_information_report_2018.csv"))

;output file
(def output-file (io/resource "outputs/output.csv"))


(defn load-test-files []
    "loads both test files into memory and returns them in map"
    (let [enrollments (split-lines (slurp enrollment-file))
          site-info  (split-lines (slurp site-info-file))
          enrollment-headers (map #(keyword %) (split (replace (first enrollments) " " "_") #","))
          site-info-headers (map #(keyword %) (split (replace (replace (first site-info) "#" "Number") " " "_") #","))
          enrollment-records (map #(split % #",") (rest enrollments))
          site-info-records (map #(split % #",") (rest site-info))]
          [site-info-headers site-info-records enrollment-headers enrollment-records]))


(defn write-to-file-one [headers records]
    (spit output-file (str "Site Number," "State," "Country\n"))
    (dorun (map #(spit output-file (str % "\n") :append true) records)))


(defn make-single-data-file []
    (let [[site-info-headers site-info-records & remaining] (load-test-files)
          site-info-maps (sort-by :Site_Number (into []
              (->> site-info-records
                  (map #(zipmap site-info-headers %)))))
            site-info-strings (into [] (for [m site-info-maps :let [{:keys [Site_Number State Country]} m]] (str Site_Number "," State "," Country)))]
    (write-to-file-one site-info-headers (vec (distinct site-info-strings)))))

    ;(println (first enrollment-maps))))
