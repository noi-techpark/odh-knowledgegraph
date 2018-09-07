[QueryItem="All Stations"]
#
# Get all stations with their name and type 
#

prefix : <http://www.opendatahub.bz.it/ontologies/bdp#>

select * where {
	?station	a		:Station ;
		:stationName	?name ;
		:stationType	?type .
}

[QueryItem="Meteo Stations"]
#
# Get meteo stations with their name
#

prefix : <http://www.opendatahub.bz.it/ontologies/bdp#>

select * where {
	?station	a		:MeteoStation ;
		:stationName	?name .
}
