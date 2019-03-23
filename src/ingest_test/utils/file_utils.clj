(ns ingest-test.utils.file-utils
    (:require   [clojure.java.io :as io]
                [clojure.string :refer [split-lines split replace]]
                [ingest-test.utils.date-utils :refer [valid-date]])
    (:gen-class))


;patient info regarding people in studies for the ANDROMEDA project
;Country,Site Number,Investigator Name,Subject ID,Screen Date,Screen Failure Date,Rescreen Date,Randomization Date,Randomization Number,Patient Type
(def patient-file (io/resource "inputs/andromeda_ivrs_enrollment_data_2018.csv"))

;non-unique csv of sites (duplicates exist)
;Columns - Site #,Site Status,Site Activation Date,State,Country
(def site-file (io/resource "inputs/andromeda_site_information_report_2018.csv"))

;output file
(def output-file (io/resource "outputs/output.csv"))

(defn add-total-patients [site-maps]
    "Computes the total number of distinct patients for a site,
    and adds the total to a new key on the map"
    (into []
        (for [site-map site-maps]
            (assoc site-map
                :Total_Patients
                (count (distinct (into [] (map #(:Subject_ID %) (:Patients site-map)))))))))

(defn add-randomized-patients [site-maps]
    "adds the total number of distinct randomized patients as a new key to each map
    in the passed site-map vector"
    (into []
        (for [site-map site-maps]
            (assoc site-map
                :Total_Randomized_Patients
                (count (distinct (into [] (map #(:Subject_ID %)
                    (filter #(= "Randomized" (:Patient_Type %)) (:Patients site-map))))))))))

(defn add-randomized-patients-jan-to-jul [site-maps]
    "adds the total number of distinct randomized patients as a new key to each map
    in the passed site-map vector"
    (into []
        (for [site-map site-maps]
            (assoc site-map
                :Total_Randomized_Patients_Jan_To_Jul
                (count (distinct (into [] (map #(:Subject_ID %)
                    (filter #(valid-date (:Randomization_Date %)) (filter #(= "Randomized" (:Patient_Type %)) (:Patients site-map)))))))))))

;(defn add-randomized-patients [site-maps]
;    "adds the total number of distinct randomized patients as a new key to each map
;    in the passed site-map vector"
;    (into []
;        (for [site-map site-maps]
;            (assoc site-map
;                :Avg_Date_Difference
;                (count (distinct (into [] (map #(:Subject_ID %)
;                    (filter #(= "Randomized" (:Patient_Type %)) (:Patients site-map))))))))))

(defn add-computations [site-maps]
    "Runs through a site-map vector and adds several computatios"
    (->> site-maps
        (add-total-patients)
        (add-randomized-patients)
        (add-randomized-patients-jan-to-jul)))

(defn assoc-patients [site-maps patient-maps]
    "associates two vectors, one of site-maps, and one of patient maps.
    adds patient maps to the proper site map as a new vector."
    (into []
        (for [site-map site-maps
            :let [assoc-patient-maps (filter #(= (:Site_Number site-map) (:Site_Number %)) patient-maps)]]
                (assoc site-map :Patients (vec assoc-patient-maps)))))

(defn make-maps []
    "loads both test files into memory and returns them in a vector of fully associated maps"
    (let [patients (split-lines (slurp patient-file))
          sites  (split-lines (slurp site-file))
          patient-headers (map #(keyword %) (split (replace (first patients) " " "_") #","))
          site-headers (map #(keyword %) (split (replace (replace (first sites) "#" "Number") " " "_") #","))
          patient-records (map #(split % #",") (rest patients))
          site-records (map #(split % #",") (rest sites))
          site-maps (sort-by :Site_Number (into []
              (->> site-records
                  (map #(zipmap site-headers %)))))
          patient-maps (sort-by :Site_Number (into []
              (->> patient-records
                  (map #(zipmap patient-headers %)))))]
           (add-computations (assoc-patients site-maps patient-maps))))


(defn write-output-file [site-maps]
    (let [records (into [] (for [m site-maps
        :let [{:keys [Site_Number State Country Total_Patients Total_Randomized_Patients Total_Randomized_Patients_Jan_To_Jul]} m]]
        (str Site_Number "," State "," Country "," Total_Patients "," Total_Randomized_Patients "," Total_Randomized_Patients_Jan_To_Jul)))]
    (spit output-file (str "Site Number," "State," "Country," "Total_Patients," "Total_Randomized_Patients," "Total_Randomized_Patients_Jan_To_Jul\n"))
    (dorun (map #(spit output-file (str % "\n") :append true) (vec (distinct records))))))


;(defn make-data-file []
;    (let [[site-maps patient-maps] (load-test-files)
;            site-strings (into [] (for [m site-maps :let [{:keys [Site_Number State Country]} m]] (str Site_Number "," State "," Country)))]
;    (write-to-file-one site-headers (vec (distinct site-strings)))))
