[QueryItem="case-01_1"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y where {?x rdf:type :Author. ?x :name ?y}

[QueryItem="case-01_2"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y where {?x rdf:type :Book. ?x :title ?y}

[QueryItem="case-01_3"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y where {?x rdf:type :Editor. ?x :name ?y}

[QueryItem="case-02_1"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y where {?x rdf:type :AudioBook. ?x :title ?y}

[QueryItem="case-02_2"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y where {?x rdf:type :EmergingWriter. ?x :name ?y}

[QueryItem="case-02_3"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?x ?y ?z where 
{?x rdf:type :SpecialEdition. ?x :dateOfPublication ?y. ?x :editionNumber ?z}

[QueryItem="case-03_1"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
select ?title where 
{?x rdf:type :Book. ?x :title ?title. ?x :writtenBy ?y. 
?y rdf:type :Author. ?y :name "L.C. Higgs"^^xsd:string}

[QueryItem="case-03_2a"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?title ?author ?genre ?edition where 
{  ?x rdf:type :Book. ?x :title ?title. ?x :genre ?genre. ?x :writtenBy ?y. ?x :hasEdition ?z.  
 ?y rdf:type :Author. ?y :name ?author.   ?z rdf:type :Edition. ?z :editionNumber ?edition}

[QueryItem="case-03_2b"]
PREFIX : <http://meraka/moss/exampleBooks.owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
select ?title ?author ?genre ?edition where
 {  ?x a :Book; :title ?title; :genre ?genre; :writtenBy 
?y; :hasEdition ?z.   ?y a :Author; :name ?author.   ?z a :Edition; :editionNumber ?edition}
