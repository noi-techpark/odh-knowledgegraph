[QueryItem="traffic_sensor_bolzano"]
PREFIX : <http://example.org/idm/hack/measurement#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?name ?townName {
 ?s a :TrafficSensor ; 
   :hasName ?name ;
   :hasMunicipality ?m .
 ?m :hasName ?townName .
}

[QueryItem="Municipalities"]
PREFIX : <http://example.org/idm/hack/measurement#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT DISTINCT ?mName {
 ?s :hasMunicipality ?mName .
}

[QueryItem="measurement"]
PREFIX : <http://example.org/idm/hack/measurement#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?sensorName ?v ?ts {
 ?s :hasName ?sensorName ; :hasMeasurement ?m .
 ?m :hasValue ?v ; :hasTimestamp ?ts ; :hasMeasurementType ?t .
 ?t a :MeanMeasurementType .
}
LIMIT 10

[QueryItem="AirQualityAlerts"]
PREFIX : <http://example.org/idm/hack/measurement#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT * WHERE {
  ?s :hasMeasurement ?m .
  ?m :hasAirQualityAlert true ; :hasTimestamp ?ts .
  OPTIONAL { ?s :hasMunicipality ?townName . }

}
