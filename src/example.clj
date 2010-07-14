;; (c) 2010 Patrick Hochstenbach <patrick.hochstenbach@gmail.com>
(use 'clj-meercat.marc)
(def records (parse "data/rug01.export"))
(def my-seq (marc-seq records))
(time (doseq [rec my-seq] (println (rec "008" :pos [7 11]))))
