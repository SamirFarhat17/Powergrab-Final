package uk.ac.ed.inf.powergrab;

public class Station {
	// Station constructors
	public String id;
	public Position position;
	private double coins;
	private double power;
	public String markerSymbol;
	public String markerColor;
	
	/**
	 * Create instance of Station
	 * @param id
	 * @param position
	 * @param coins
	 * @param power
	 * @param marker_symbol
	 * @param marker_color
	 */
	public Station(String id, Position position, double coins, double power, String marker_symbol, String marker_color) {
		this.id = id;
		this.position = position;
		this.coins = coins;
		this.power = power;
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
	
}