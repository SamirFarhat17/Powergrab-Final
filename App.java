package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// GeoJson imports for SDK
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

// APP runs here
public class App 
{
	
	// Initialise list of stations, drone, pseudo random number generator, and the list of coordinates that the drone will occupy
	public static List<Station> listOfStations = new ArrayList<Station>();
	private static List<Station> listOfPositiveStations = new ArrayList<Station>();
	public static List<Station> listOfNegativeStations = new ArrayList<Station>();
	private static Drone drone;
	private static Position startingPosition;
	public static Random pseudoRandom;
	public static List<Point> coordinatesTravelled = new ArrayList<Point>();
	public static String stateOfDrone;
	
	
	/**
	 * Application runs through the main
	 * @param args
	 * @throws IOException
	 */
    public static void main( String[] args ) throws IOException
    {
    	// Check inputs are correct
    	if(args.length != 7) {
    		System.out.println("Incorrect or missing run configurations");
    		System.exit(0);
    	}
    	
    	// Read inputs
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	double startingLatitude = Double.parseDouble(args[3]);
    	double startingLongitude = Double.parseDouble(args[4]);
    	int seed = Integer.parseInt(args[5]);
    	String state = args[6];
    	stateOfDrone = state;
    	
    	
    	// Assign seed to pseudo random number generator
    	pseudoRandom = new Random(seed);
    	
    	// Create instance of drone
    	startingPosition = new Position(startingLatitude, startingLongitude);
    	if(state.equals("stateless")) drone = new Stateless(startingPosition);
    	else drone = new Stateful(startingPosition);
    	
    	// Check position and state is valid
    	if(!state.equals("stateless") && !state.equals("stateful")) {
    		System.out.println("Invalid state selected");
    		System.exit(0);
    	}
    	if(!startingPosition.inPlayArea()) {
    		System.out.println("Invalid position selected");
    		System.exit(0);
    	}
    	
    	// get map URL and store features
    	String webpage = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/" + "powergrabmap.geojson";
    	String json = JsonReader.readJsonFromUrl(webpage);
    	FeatureCollection allStations = FeatureCollection.fromJson(json);
    	
    	
    	// Create and populate a feature list
    	List<Feature> listOfFeatures = allStations.features();
    	
    	// Use feature list to add all stations
    	for(Feature f: listOfFeatures) {
    		
    		// get Station ID
    		String currentID = f.getStringProperty("id");
    		
    		// Get station position
    		Geometry currentGeometry = f.geometry();
    		List<Double> currentCoordinates = ((Point) currentGeometry).coordinates();
    		Position current_position = new Position (currentCoordinates.get(1), currentCoordinates.get(0));
    		
    		// Get remaining station attributes
    		double currentCoins = Double.parseDouble(f.getStringProperty("coins"));
    		double currentPower = Double.parseDouble(f.getStringProperty("power"));
    		String currentMarkerSymbol = f.getStringProperty("marker_symbol");
    		String currentMarkerColor = f.getStringProperty("marker_color");
    		
    		// Instantiate each station and add to the respective lists
    		Station currentStation = new Station(currentID, current_position, currentCoins, currentPower, currentMarkerSymbol, currentMarkerColor);
    		listOfStations.add(currentStation);
    		if(currentStation.getCoins() > 0) listOfPositiveStations.add(currentStation);
    		else listOfNegativeStations.add(currentStation);
    	} 

    	// Create text file and geojson file
    	String textfile = state + "-" + day + "-" + month + "-" + year +".txt";
    	PrintWriter writer = new PrintWriter(textfile, "UTF-8");
    	String geofile = state + "-" + day + "-" + month + "-" + year +".geojson";
    	PrintWriter geoWriter = new PrintWriter(geofile, "UTF-8");
    	
    	// Go through 250 moves
    	int moves = 250;
    	while(moves > 0) {
    		System.out.printf("\nMove: %d -- Coins: %f -- Power: %f\n", 251-moves, drone.getCoins(), drone.getPower());
    		double coinsBefore = drone.getCoins();
    		// Stateless operation
    		if(state.equals("stateless") && drone.getPower() >= 1.25) {
    			String out = drone.makeMove();
    			writer.println(out);
    		}
    		// Stateful operation
    		else if(drone.getPower() >= 1.25) {
    			String out = drone.makeMove();
    			writer.println(out);
    		}
    		if(coinsBefore > drone.getCoins()) System.out.println("\n\n\n\n------------------------Went negative------------------------\n\n\n\n");
    		moves--;
    	}
    	
    	// add final destination of drone
    	coordinatesTravelled.add(Point.fromLngLat(drone.position.longitude, drone.position.latitude));
    	
    	// Add travel path to Json
    	LineString travelPath = LineString.fromLngLats(coordinatesTravelled);
    	Feature droneBlackbox = Feature.fromGeometry(travelPath);
    	listOfFeatures.add(droneBlackbox);
    	
    	// Create .geojson files from Feature Collection
    	FeatureCollection finalFeatureCollection = FeatureCollection.fromFeatures(listOfFeatures);
    	String dotGeojsonFile = (finalFeatureCollection).toJson();
    	geoWriter.println(dotGeojsonFile);
    	
    	// Close text file and geojson file
        writer.close();
        geoWriter.close();
        
    }
    
}