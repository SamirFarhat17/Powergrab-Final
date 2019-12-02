package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Subclass of Drone
public class Stateless extends Drone {

	
	// constructors for Stateless drone
	private Random seed = App.pseudoRandom;
	public Stateless(Position position) {
		super(position);
	}
	
	
	/**
	 * Get direction of next move based on several calculations
	 * @return Direction
	 */
	private Direction findNextMove() {
		Direction finalDirection = Direction.values()[seed.nextInt(16)];;
		if(noOption()) return doLeastWorstMove();
		List<Direction> avoid = new ArrayList<Direction>();
		// Avoid directions which lead to negative stations	and out of play area
		for(int i = 0; i <= 15; i++ ) {
			Direction d = Direction.values()[i];
			if(!position.nextPosition(d).inPlayArea() || !notInNegative(d)) {
				avoid.add(d);
			}
		}
		
		double maxCoins = 0;
		double currentCoins = 0;
		boolean randomMove = true;
		// Find positive stations within range of 1 move
		for(Station s: App.listOfStations) {
			Position stationPosition = s.position;
			double distance = getDistance(stationPosition);
			currentCoins = s.getCoins();
			if(distance < 0.00055 && currentCoins > maxCoins) {
				// Go to positions which get the maximum coins
				for(int j = 0; j <= 15; j++) {
					Direction goTo = Direction.values()[j];
					Position future = position.nextPosition(goTo);
					Drone futureDrone = new Stateless(future);
					if(futureDrone.getExchangerStation() == s && futureDrone.position.inPlayArea()) {
						randomMove = false;
						finalDirection = goTo;
						maxCoins = currentCoins;
					}
				}
				 
			}
		}
		// go to positive station
		if(!randomMove) {
			return finalDirection;
		}
		// avoid negative stations and stay in play area when random move is being made
		else {
			while(avoid.contains(finalDirection)) {
				finalDirection = Direction.values()[seed.nextInt(16)];
			}
			return finalDirection;
		}
	}
	
	
	/**
	 * Perform move and return record(text and json) of move
	 */
	public String makeMove() {
		Position positionBefore = position;
		Direction direction = findNextMove();
		position = position.nextPosition(direction); 
		exchangeCoins();
		DocWriter.newJsonLine(positionBefore);
		return DocWriter.newLine(positionBefore, direction, position, getCoins(), getPower());
		}
}