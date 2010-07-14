# clj-marc

Clj-marc is a Clojure library to parse Ex Libris ALEPH seqential MARC exports.

## Usage

	(use 'clj-marc.parser)
	(doseq [rec (marc-seq (parse "data/rug01.export"))] (println (rec "245")))

Other options:

	(rec "245")  -> "Data2 Data3 Data4"
	(rec "008" :pos [7 11]) -> "1977"
	(rec "245" :includes ["b" "c"]) -> "Data3 Data4"
	(rec "245" :excludes  ["b"]) -> "Data2 Data4 ..." 
	(rec "245" :ind 1) -> "5"
	(rec "245" :as_list true) -> ["Data2" "Data3" "Data4" ...]
	(rec "245" :as_string true) -> "Data2 Data3 Data4 ...

## License

Copyright (C) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugent.be>

Distributed under the Eclipse Public License, the same as Clojure.n
