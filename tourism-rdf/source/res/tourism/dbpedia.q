[QueryItem="dbpedia"]
SELECT distinct *
WHERE {
    ?s dbo:province dbr:South_Tyrol .
    ?s foaf:name ?name .

	filter(?name = "Bozen"@en)

	?s dbo:postalCode ?postalCode .
	?s dbo:areaCode ?areaCode .
	?s dbo:elevation ?elevation .
	?s dbo:thumbnail ?thumbnail .

	?s dbo:abstract ?abstract .
	filter(langMatches(lang(?abstract), "EN")) .
}

[QueryItem="dbpedia: All literals from a town"]
SELECT distinct *
WHERE {
    ?s dbo:province dbr:South_Tyrol .
    ?s foaf:name ?name .
    ?s dbo:thumbnail ?thumbnail .

	#filter(?name = "Bozen"@en)  #Better use regex, to find also Bozen,_South_Tyrol or similar
	filter regex(?name, "^Bozen"@en) .
	
	?s ?x ?y .

	FILTER(isLiteral(?y) && (langMatches(lang(?y), "EN") || lang(?y) = ""))
}

[QueryItem="dbpedia-test-bz"]
SELECT distinct *
WHERE {
    ?s dbo:province dbr:South_Tyrol .
    ?s dbp:name ?n .

filter(?n = "Bozen"@en)

?s dbo:postalCode ?postalCode .
?s dbo:areaCode ?areaCode .
?s dbo:elevation ?elevation .
?s dbo:thumbnail ?thumbnail .

?s dbo:abstract ?abstract .
filter(langMatches(lang(?abstract), "EN")) .



} #LIMIT 50

[QueryItem="dbpedia+tourism"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbr: <http://dbpedia.org/resource/>
PREFIX dbp: <http://dbpedia.org/property/>

SELECT distinct ?s
WHERE {
    ?e     a                 :Enquiry ;
           :hasDestination   ?d .
    ?d     :name_de ?s .

    SERVICE <http://dbpedia.org/sparql> {
        ?s a dbo:PopulatedPlace .
        ?s dbp:name ?n .
        ?s dbo:province dbr:South_Tyrol .
    }
} LIMIT 50
