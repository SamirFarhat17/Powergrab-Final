package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Point;

// Class meant for writing to text file and Json file
public class DocWriter {
	
	/**
	 * Text file entry 
	 * @param before
	 * @param move
	 * @param after
	 * @param coins
	 * @param power
	 * @return String
	 */
	public static String newLine(Position before, Direction move, Position after, double coins, double power) {
		String direction = "";
		String latBefore = Double.toString(before.latitude);
		String longBefore = Double.toString(before.longitude);
		String latAfter = Double.toString(after.latitude);
		String longAfter = Double.toString(after.longitude);
		String coinage = Double.toString(coins);
		String powers = Double.toString(power);
		String m = ", ";
		
		int j = 0;
		for(int i = 0; i < 16; i++) {
			if(move == Direction.values()[i]) j = i;
		}
		
		if(j == 0) direction = "E";
		if(j == 1) direction = "ENE";
		if(j == 2) direction = "NE";
		if(j == 3) direction = "NNE";
		if(j == 4) direction = "N";
		if(j == 5) direction = "NNW";
		if(j == 6) direction = "NW";
		if(j == 7) direction = "WNW";
		if(j == 8) direction = "W";
		if(j == 9) direction = "WSW";
		if(j == 10) direction = "SW";
		if(j == 11) direction = "SSW";
		if(j == 12) direction = "S";
		if(j == 13) direction = "SSE";
		if(j == 14) direction = "SE";
		if(j == 15) direction = "ESE";
		
		String output = latBefore + m + longBefore + m + direction + m + latAfter + m + longAfter + m + coinage + m + powers;
		return output;
	}
	
	
	/**
	 * JSON Entry
	 * @param drone_position
	 */
	public static void newJsonLine(Position drone_position) {
		Point dronePoint = Point.fromLngLat(drone_position.longitude, drone_position.latitude);
		App.coordinatesTravelled.add(dronePoint);
	}
}