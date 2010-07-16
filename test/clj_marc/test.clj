(ns clj-marc.test
  (:use [clj-marc.parser] :reload-all)
  (:use [clojure.test]))

(deftest rug01-export-parse-test
  (let [mseq (marc-seq (parse "data/rug01.export" :aleph))
	rec  (first mseq)]
    (is (= "000000002" (rec "001")))
    (is (= "78307846" (rec "010")))
    (is (= "Jerrold J. Katz." (rec "245" :includes [:c])))
    (is (= "Jerrold J. Katz." (rec "245" :excludes [:a :b])))
    (is (= 5 (count (rec "650" :as_list true))))
    (is (= "1" (rec "245" :ind 1)))
    (is (= "1977" (rec "008" :pos [7 11])))
    ))

(deftest loc-export-parse-test
  (let [mseq (marc-seq (parse "data/loc.export" :marc21))
	rec (first mseq)]
    (is (= "00000002 " (rec "001")))
    (is (= "00000002 " (rec "010")))
    (is (= "By S. H. Aurand." (rec "245" :includes [:c])))
    (is (= "By S. H. Aurand." (rec "245" :excludes [:a :b])))
    (is (= 2 (count (rec "650" :as_list true))))
    (is (= "1" (rec "245" :ind 1)))
    (is (= "1899" (rec "008" :pos [7 11])))
    ))

(deftest marcxml-export-parse-test
  (let [mseq (marc-seq (parse "data/rug01-xml.export" :marcxml))
	rec  (first mseq)]
    (is (= "000000002" (rec "001")))
    (is (= "78307846" (rec "010")))
    (is (= "Jerrold J. Katz." (rec "245" :includes [:c])))
    (is (= "Jerrold J. Katz." (rec "245" :excludes [:a :b])))
    (is (= 5 (count (rec "650" :as_list true))))
    (is (= "1" (rec "245" :ind 1)))
    (is (= "1977" (rec "008" :pos [7 11])))
    ))