[QueryItem="Enquiry Categories and their names"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT *
WHERE {
    ?c     a         :Category ;
           :name     ?n
}

[QueryItem="Number of children per category"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?n ?nc
WHERE {
    ?e     a                 :Enquiry ;
           :hasCategory      ?c  ;
           :noOfChildren     ?nc .

    ?c     :name             ?n .
}
ORDER BY ?n DESC(?nc)

[QueryItem="Districts and Categories"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?district
WHERE {
    ?e     a                 :Enquiry ;
           :hasCategory      ?c ;

            # Only category/1 (needs to be escaped)
           :hasCategory      :category\/1 ;
           :hasDestination   ?d .

    ?d     :belongsTo        ?district .
}
ORDER BY ?district

[QueryItem="Tourists from US heading to Ladin towns"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?nde ?nit ?nla
WHERE {
    ?e     a                  :Enquiry ;
           :submittedFrom     :country\/US ;
           :hasDestination    ?d ;
           :noOfChildren      ?nc ;
           :hasCategory       ?c .

    ?d     :name_it    ?nde ;
           :name_de    ?nit ;
           :name_lad   ?nla .

    ?d     a                  :LadinTown .

    FILTER (?nc > 0) . #Holiday with kids
}
ORDER BY ?nde ?nit ?nla

[QueryItem="HotelEnquiry from Asia"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

# All asian countries where tourists have submitted enquiries...

SELECT ?cn
WHERE {
    ?e     a                  :HotelEnquiry ;
           :submittedFrom     ?c .

    ?c     :isPartOf          ?cont .

    FILTER (STR(?cont) = 'AS')          # STR() needed in ontop

    ?c     :name        ?cn .
}
ORDER BY ?cn

[QueryItem="HotelEnquiry outside Asia (negation)"]
PREFIX : <http://www.semanticweb.org/pemoser/ontologies/2017/0/tourism#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

# All non-asian continents where tourists have submitted enquiries...

SELECT ?y
WHERE {
    ?e     a                  :HotelEnquiry .

    OPTIONAL {
          ?e     :submittedFrom     ?c .
          ?c     :isPartOf          ?cont .

          FILTER (STR(?cont) = 'AS')          # STR() needed in ontop

          ?c     :name        ?cn .
    }
    FILTER (!BOUND(?c))

    ?e :submittedFrom ?x .
    ?x :isPartOf ?y .
}
ORDER BY ?y

