;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@gmail.com>
(ns 
    clj-marc.parser
  (:use [clojure.contrib.str-utils])
  (:use [clojure.contrib.duck-streams :only (reader)]))

(defstruct marc-record-field :field :ind1 :ind2 :subfields)

(defn- marc-boundary?
  [line]
  (not (nil? (re-matches #"^[0-9]{9} FMT.*" line))))

(defn- marc-subfields
  [line] ; "$$aData1$$bData2..." or "Data1"
  (let [parts  (re-partition #"\$\$." line) ; ("Data1" "$$a" "Data2" "$$b" "Data3"...)
        subfs  (concat (vector "_" (first parts)) (map #(re-sub #"\$\$" "" %) (rest parts)))] ; ("_" "Data1" "a" "Data3" "b" "Data3" ...)
   (partition 2 subfs))) ; (["_" "Data1"] ["a" "Data2"] ["b" "Data3"])

(defn- marc-line
  [line]
  (let [id        (.substring line 0 9)
	field     (.substring line 10 13)
	ind1      (.substring line 13 14)
	ind2      (.substring line 14 15)
	subfields (marc-subfields (.substring line 18))]
    (struct marc-record-field field ind1 ind2 subfields)))

(defn contenthandler
  [records]
  (map #(for [line %] (marc-line line)) records))     ; Map record into marc-records-fields

(defn startparse
  [s]
  (let [lst (line-seq (reader s))                     ; Read all lines
	parts (partition-by marc-boundary? lst)       ; Split lines into records (header + body) 
	records (map flatten (partition 2 parts))]    ; Join the head + body into a record
    (contenthandler records)))

(defn parse
  "Parses and loads the source s which is a File. Returns a Lazy Sequence
  of records which are vectors of clj-marc/marc-record-field with keys :field,
  :ind1, :ind2 and :data."
  [s]
  (startparse s))

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
     (= ind 1) (:ind1 res)
     (= ind 2) (:ind2 res)
     (not (nil? pos)) (.substring (str-join "; " values) (get pos 0) (get pos 1))
     (or as_string (not as_list)) (str-join "; " values)
     true values)))

(defn marc-seq [records]
  (for [rec records] (partial marc rec)))

;; Example
;; (use 'clj-marc.parser)
;; (def records (parse "data/rug01.export"))
;; (def my-seq (marc-seq records))
;; (doseq [rec my-seq] (println (rec "245")))

;; (rec "245")  -> "Data2 Data3 Data4"
;; (rec "008" :pos [7 11]) -> "1977"
;; (rec "245" :includes ["b" "c"]) -> "Data3 Data4"
;; (rec "245" :excludes  ["b"]) -> "Data2 Data4 ..." 
;; (rec "245" :ind 1) -> "5"
;; (rec "245" :as_list true) -> ["Data2" "Data3" "Data4" ...]
;; (rec "245" :as_string true) -> "Data2 Data3 Data4 ..."