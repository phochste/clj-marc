;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugen.be>
(ns 
    clj-marc.aleph
  (:use [clojure.contrib.str-utils])
  (:use [clojure.contrib.duck-streams :only (reader)]))\

(defstruct marc-record-field :field :ind1 :ind2 :subfields)

(defn- marc-boundary?
  [line]
  (not (nil? (re-matches #"^[0-9]{9} FMT.*" line))))

(defn- marc-subfields
  [line] ; "$$aData2$$bData3..." or "Data1"
  (let [parts  (cons "$$_" (re-partition #"\$\$." line)) ; ("$$_" "Data1" "$$a" "Data2" "$$b" "Data3"...)
        subfs  (map #(if (.startsWith % "$$") (keyword (.substring % 2)) %) parts)] ; (:_ "Data1" :a "Data2" :b "Data3" ...)
   (partition 2 subfs))) ; ([:_ "Data1"] [:a "Data2"] [:b "Data3"])

(defn- marc-line
  [line]
  (let [id        (.substring line 0 9)
	field     (.substring line 10 13)
	ind1      (.substring line 13 14)
	ind2      (.substring line 14 15)
	subfields (marc-subfields (.substring line 18))]
    (struct marc-record-field field ind1 ind2 subfields)))

(defn- contenthandler
  [record]
  (for [line record] (marc-line line)))               ; Map record into marc-records-fields

(defn- startparse
  [s]
  (let [lst (line-seq (reader s))                     ; Read all lines
	parts (partition-by marc-boundary? lst)       ; Split lines into records (header + body) 
	records (map flatten (partition 2 parts))]    ; Join the head + body into a record
    (for [record records] (contenthandler record))))

(defn parse
  "Parses and loads the source s which is a File. Returns a Lazy Sequence
  of records which are vectors of clj-marc/marc-record-field with keys :field,
  :ind1, :ind2 and :subfields."
  [s]
  (startparse s))