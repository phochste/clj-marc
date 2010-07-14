# clj-marc

Clj-marc is a Clojure library to parse Ex Libris ALEPH sequential MARC exports.

## Usage

	(use 'clj-marc.parser)
	(doseq [rec (marc-seq (parse "data/rug01.export"))] (println (rec "245")))

Other options:

	(rec "245")  
	=> "Propositional structure and illocutionary force : a study of the contribution of sentence meaning to speech acts / Jerrold J. Katz."
	(rec "245" :includes ["c"]) 
	=> "Jerrold J. Katz."
	(rec "245" :excludes ["a" "b"]) 
	=> "Jerrold J. Katz."
	(rec "852") 
	=> "LW01 L27 L27.18M201 Dept. LW01 L27; LW06 ....; LW11 ...; LW09 ..."
	(rec "852" :includes ["b"] :as_list true) 
	=> ("LW01" "LW06" "LW11" "LW09")
	(rec "008" :pos [7 11]) 
	=> "1977"
	(rec "245" :ind 1)
	=> 1

## License

Copyright (C) 2010 Patrick Hochstenbach <patrick.hochstenbach@ugent.be>

Distributed under the Eclipse Public License, the same as Clojure.n
