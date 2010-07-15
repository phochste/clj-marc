(ns clj-marc.test
  (:use [clj-marc.parser] :reload-all)
  (:use [clojure.test]))

(deftest rug01-export-parse-test
  (let [mseq (marc-seq (parse "data/rug01.export"))
	rec  (first mseq)]
    (is (= "000000002" (rec "001")))
    (is (= "78307846" (rec "010")))
    (is (= "Jerrold J. Katz." (rec "245" :includes [:c])))
    (is (= "Jerrold J. Katz." (rec "245" :excludes [:a :b])))
    (is (= 5 (count (rec "650" :as_list true))))
    (is (= "1" (rec "245" :ind 1)))
    (is (= "1977" (rec "008" :pos [7 11])))
    ))
