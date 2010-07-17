;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugent.be>
(ns ^{:doc "A wrapper around Marc4j for parsing MARC21/MARCXML documents" :author "Patrick Hochstenbach"}
    clj-marc.marc4j
  (:import (java.io FileInputStream))
  (:import (org.marc4j MarcStreamReader MarcXmlReader MarcReader))
  (:import (org.marc4j.marc Record Leader DataField ControlField))
  (:use [clojure.contrib.str-utils])
  (:use [clojure.contrib.duck-streams :only (reader)]))

(defstruct marc-record-field :field :ind1 :ind2 :subfields)

(defn- marc4j-seq
  [^MarcReader reader]
  (when (.hasNext reader)
    (cons (.next reader) (lazy-seq (marc4j-seq reader)))))

(defmulti #^{:private true} parse-field class)

(defmethod #^{:private true} parse-field Leader [x]
  (let [subfields (list (list :_ (.marshal x)))]
    (struct marc-record-field "LDR" " " " " subfields)))    	     

(defmethod #^{:private true} parse-field ControlField [x]
  (let [tag (.getTag x)
	subfields (list (list :_ (.getData x)))]
    (struct marc-record-field tag " " " " subfields)))

(defmethod #^{:private true} parse-field DataField [x]
   (let [tag       (.getTag x)
	 ind1      (str (.getIndicator1 x))
	 ind2      (str (.getIndicator2 x))
	 subfields (map #(vector (keyword (str (.getCode %))) (.getData %)) (.getSubfields x))]
    (struct marc-record-field tag ind1 ind2 subfields))) 

(defn- contenthandler
  [^Record record]
  (let [leader (.getLeader record)
	controlfields (.getControlFields record)
	datafields (.getDataFields record)]
    (for [field (concat [leader] controlfields datafields)]
      (parse-field field))))

(defn- startparse
  [s format]
  (let [in (FileInputStream. s)
	reader (cond (= :marc21 format) (MarcStreamReader. in)
		     (= :marcxml format) (MarcXmlReader. in)
		     true (MarcStreamReader. in))
	records (marc4j-seq reader)]
    (for [record records] (contenthandler record))))

(defn parse
  "Parses and loads the source s which is a File. The second argument should be
  the file format (:marc21 or :marcxml). Returns a Lazy Sequence
  of records which are vectors of clj-marc/marc-record-field with keys :field,
  :ind1, :ind2 and :subfields."
  [s & args]
  (let [format (first args)]
    (startparse s format)))