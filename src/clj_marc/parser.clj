;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugent.be>
(ns 
    clj-marc.parser
  (:require [clj-marc aleph marc4j])
  (:use [clojure.contrib.str-utils])
  (:use [clojure.contrib.duck-streams :only (reader)]))

(defn parse
  "Parses and loads the source s which is a File. Returns a Lazy Sequence
  of records which are vectors of clj-marc/marc-record-field with keys :field,
  :ind1, :ind2 and :subfields."
  ([s] (parse s :aleph))
  ([s type & args]
     (cond
      (= :aleph type) (clj-marc.aleph/parse s)
      (= :marc21 type) (clj-marc.marc4j/parse s :marc21)
      (= :marcxml type) (clj-marc.marc4j/parse s :marcxml)
      true (clj-marc.aleph/parse s))))

;; ACCESSORS
(defn- marc-include-filter [includes subfields]
  (if (or (nil? includes) (empty? includes))
    subfields
    (filter (fn [sf] (some #(= (first sf) %) includes)) subfields)))

(defn- marc-exclude-filter [excludes subfields]
  (if (or (nil? excludes) (empty? excludes))
    subfields
    (filter (fn [sf] (not-any? #(= (first sf) %) excludes)) subfields)))

(defn- marc-data [field includes excludes]
  (->> field
       :subfields
       (marc-include-filter includes)
       (marc-exclude-filter excludes)
       (map fnext)
       (str-join " ")
       (re-sub #"(^\s+|\s+$)" "")))

(defn marc [rec field & args]
  (let [{:keys [ind includes excludes as_list as_string pos]} (apply hash-map args)
	res (filter #(= (:field %) field) rec)
	values  (map #(marc-data % includes excludes) res)]
    (cond
     (= ind 1) (:ind1 (first res))
     (= ind 2) (:ind2 (first res))
     (not (nil? pos)) (.substring (str-join "; " values) (get pos 0) (get pos 1))
     (or as_string (not as_list)) (str-join "; " values)
     true (apply vector values))))

(defn marc-seq [records]
  (for [rec records] (partial marc rec)))

;; Example
;; (use 'clj-marc.parser)
;; (def records (parse "data/rug01.export" :aleph))
;; (def records (parse "data/loc.export" :marc21))
;; (def my-seq (marc-seq records))
;; (doseq [rec my-seq] (println (rec "245")))

;; (rec "245")  -> "Data2 Data3 Data4"
;; (rec "008" :pos [7 11]) -> "1977"
;; (rec "245" :includes [:b :c]) -> "Data3 Data4"
;; (rec "245" :excludes  [:b]) -> "Data2 Data4 ..." 
;; (rec "245" :ind 1) -> "5"
;; (rec "245" :as_list true) -> ["Data2" "Data3" "Data4" ...]
;; (rec "245" :as_string true) -> "Data2 Data3 Data4 ..."