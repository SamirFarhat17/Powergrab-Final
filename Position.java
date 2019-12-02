package uk.ac.ed.inf.powergrab;

public class Position {
	
	public double latitude; 
	public double longitude; 

	/**
	 * Retrieve current latitude and longitude 
	 * @param latitude
	 * @param longitude
	 */
	public Position(double latitude, double longitude) {
		this.latitude = latitude; 
		this.longitude = longitude; 
	}
	
	
	/**
	 * Get next position
	 * @param direction
	 * @return Position
	 */
	public Position nextPosition(Direction direction) {
		int compass = direction.ordinal(); // Starting from north and going clockwise increment index till matching direction found
		double angle = compass * 22.5; // Each direction is a multiple off 22.5 so simply retrieve index
		double angleRad = Math.toRadians(angle); // Convert to radians
		
		// Compute angle and multiply by magnitude(0.0003) to get change in position
		double deltaLat = Math.sin(angleRad) * 0.0003;
		double deltaLon = Math.cos(angleRad) * 0.0003;
		
		// Compute final position and return
		double newLatitude = latitude + deltaLat;
		double newLongitude = longitude + deltaLon;
		Position nextPos = new Position(newLatitude, newLongitude);
		return nextPos;
	}
	
	/**
	 * Checks if drone is within the play area
	 * @return boolean
	 */
	public boolean inPlayArea() {
		// Define limits of where drone can go(non inclusive)
		boolean goodLat = latitude > 55.942617 && latitude < 55.946233;
		boolean goodLon = longitude < -3.184319 && longitude > -3.192473;
		boolean inPlay = goodLat && goodLon;
		return inPlay;
 	}
	
}