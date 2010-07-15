;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugent.be>
(ns 
    clj-marc.marc21
  (:use [clojure.contrib.str-utils])
  (:use [clojure.contrib.duck-streams :only (reader)]))

(defstruct marc-record-field :field :ind1 :ind2 :subfields)

(defstruct marc-directory :tag :start :end)

(defn- parse-integer
  "Returns an Integer given a String"
  [^String str]
  (try (Integer/parseInt str)
       (catch NumberFormatException nfe 0)))

(defn raw-marc-seq
  "Returns the raw marc records from rdr as a lazy sequence of strings.
  rdr must implement java.io.BufferedReader."
  [^java.io.BufferedReader rdr]
  (let [buffer (char-array 5)]
    (when-let [x (= 5 (.read rdr buffer 0 5))]
      (let [reclen (parse-integer (String. buffer))
	    record (char-array (- reclen 5))
	    _ (.read rdr record 0 (- reclen 5))]
	(cons (String. record) (lazy-seq (raw-marc-seq rdr)))))))

(defn- parse-directory
  [^String record]
  (when (not (= \u001e (.charAt record 0)))
    (let [tag    (.substring record 0 3)
	  length (parse-integer (.substring record 3 7))
	  start  (parse-integer (.substring record 7 12))]
      (cons (struct marc-directory tag start (+ start (dec length)))
	    (lazy-seq (parse-directory (.substring record 12)))))))

(defn- marc-subfields
  [^String line] 
  (let [parts  (cons "\u001f_" (re-partition #"\u001f." line)) 
        subfs  (map #(if (.startsWith % "\u001f") (keyword (.substring % 1)) %) parts)]
    (partition 2 subfs)))

(defn contenthandler
  [^String record]
  (let [leader (str "0000" (.substring record 0 18))
	directory (parse-directory (.substring record 19))
	data (.substring record (+ 20 (* 12 (count directory))))]
   (cons (struct marc-record-field "LDR" " " " " (list (list :_ leader)))
    (for [elm directory]
      (let [fdata       (.substring data (:start elm) (:end elm))
	    field      (:tag elm)
	    ctrl?      (re-matches #"^00." field)
	    ind1       (.substring fdata 0 1)
	    ind2       (.substring fdata 1 2)
	    subfields  (marc-subfields (if ctrl? fdata (.substring fdata 2)))]
       (struct marc-record-field field ind1 ind2 subfields))))))

(defn startparse
  [s]
  (let [records (raw-marc-seq (reader s))]
    (for [record records] (contenthandler record))))

(defn parse
  "Parses and loads the source s which is a File. Returns a Lazy Sequence
  of records which are vectors of clj-marc/marc-record-field with keys :field,
  :ind1, :ind2 and :subfields."
  [s]
  (startparse s))