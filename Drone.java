package uk.ac.ed.inf.powergrab;



public abstract class Drone {
	// Attributes of drone
	public Position position;
	private double coins = 0;
	private double power = 250;
	
	// Drone constructor
	public Drone(Position position) {
		this.position = position;
	}
	
	// Getters and Setters
	public double getCoins() {
		return this.coins;
	}
	
	public void setCoins(Double coins) {
		this.coins = coins;
	}
	
	public double getPower() {
		return this.power;
	}
	
	public void setPower(Double power) {
		this.power = power;
	}
	
	
	/**
	 * Gets station that should exchange with drone(closest station)
	 * @return Station
	 */
	protected Station getExchangerStation() {
		double smallestDistance = 0.00025;
		Station exchanger = null;
		for(Station s: App.listOfStations) {
			Position stationPosition = s.position;
			double distance = getDistance(stationPosition);
			if(distance < smallestDistance) {
				smallestDistance = distance;
				exchanger = s;
			}
		}
		return exchanger;
	}
	
	
	// Process power and coins after move
	protected void exchangeCoins() {
		Station s = getExchangerStation();
		setPower(power - 1.25);
		if(s != null) {
			if((coins + s.getCoins()) < 0) {
				s.setCoins(s.getCoins() + coins);
				setCoins(0.0);
			}
			else {
				setCoins(coins + s.getCoins());
				s.setCoins(0.0);
			}
			if(power + s.getPower() < 0) {
				s.setPower(s.getPower() + power);
				setPower(0.0);
			}
			else {
				power = power + s.getPower();
				s.setPower(0.0);
			}
		}
	}
	
	
	/**
	 * Get euclidean distance between station and drone 
	 * @param station
	 * @return double
	 */
	protected double getDistance(Position station) {
		double yDistance = Math.pow((position.latitude - station.latitude), 2);
	    double xDistance = Math.pow((position.longitude - station.longitude), 2);
	    return Math.sqrt(xDistance + yDistance);
	}
	
	
	/**
	 * Checks if drone is in radius of negative station and that this station is the closest to it (meaning it will result in a negative exchange)
	 * @param next_pos
	 * @return Boolean
	 */
	protected Boolean notInNegative(Direction next_pos) {
		Drone futureLoc;
		if(App.stateOfDrone.equals("stateful")) futureLoc = new Stateful(position.nextPosition(next_pos));
		else futureLoc = new Stateless(position.nextPosition(next_pos));
		for(Station s: App.listOfNegativeStations) {
			if(futureLoc.getExchangerStation() == s) return false;
		}
		return true;
	}
	
	
	/**
	 * Check if the drone is unable to move without leaving the play area or hitting a negative station
	 * @return Boolean
	 */
	protected Boolean noOption() {
		int counts = 0;
		for(int i = 0; i < 16; i++) {
			if(!notInNegative((Direction.values()[i])) || !position.nextPosition(Direction.values()[i]).inPlayArea()) {
				counts++;
			}
		}
		if(counts==16) return true;
		else return false;
	}
	
	
	/**
	 * Should the drone be unable to move without going outside the play area or hitting a negative station, pick the direction that leads to the least loss of coins
	 * @return Direction
	 */
	protected Direction doLeastWorstMove() {
		double currentCoins = -10000;
		Direction wantedDirection = Direction.values()[App.pseudoRandom.nextInt(16)];
		Drone future;
		for(Station s: App.listOfNegativeStations) {
			for(int i = 0; i < 16; i++) {
				if(App.stateOfDrone.equals("stateful")) future= new Stateful(position.nextPosition(Direction.values()[i]));
				else future = new Stateless(position.nextPosition(Direction.values()[i]));
				if(s.getCoins() > currentCoins && future.getExchangerStation() == s && future.position.inPlayArea()) {
					currentCoins = s.getCoins();
					wantedDirection = Direction.values()[i];
				}
			}
		}
		return wantedDirection;
	}
	
	
	// Placeholder method which is overridden by both subclasses
	public String makeMove() {
		return "If you are seeing this, something has gone terribly wrong";
	}
	
}