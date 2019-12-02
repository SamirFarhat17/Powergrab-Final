package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.Point;

// Subclass of Drone
public class Stateful extends Drone{
	
	// for going back and forth once done
	private static Direction back;
	// for mechanism when drone ends up in a place where no moves work
	private static Boolean emergency = false;
	private static Direction lastResort = null;
	private Random seed = App.pseudoRandom;
	
	/**
	 * Stateful constructor
	 * @param position
	 */
	public Stateful(Position position) {
		super(position);
	}

	
	/**
	 * Calculate optimal direction to desired destination 
	 * @param p
	 * @return Direction
	 */
	private Direction dirToDestination(Position p) {
		Direction direction = null;
		double yDistance = p.latitude - position.latitude;
		double xDistance = p.longitude - position.longitude;
		double angleRadians = Math.atan2(yDistance,xDistance);
		if(angleRadians < 0) angleRadians = angleRadians + 2*Math.PI;
		double angleDegrees = Math.toDegrees(angleRadians);
		for(int i = 0; i < 16; i++) {
			double angleCheck = i * 22.5;
			double angularDirection = Math.abs(angleCheck - angleDegrees);
			if(angularDirection < 11.25 || angularDirection > 348.75)  {
				direction = Direction.values()[i];
				break;
			}
		}
		
		return direction;
	}
	
	
	
	/**
	 * Gets position of closest positive station and checks if positive stations remain
	 * @return Position
	 */
	private Position getClosestPositive() {
		int numberOfPositives = 0;
		// placeholder since distances will naturally be shorter if any  found
		double distance = 100;
		Position destination = null;
		Station desired = null;
		// go through stations and determine if they are positive and closer than previous positive found
		for(Station s: App.listOfStations) {
			if(s.getCoins() > 0 && getDistance(s.position) < distance) {
				numberOfPositives ++;
				destination = s.position;
				distance = getDistance(s.position);
				desired = s;
			}
		}
		// if no positives are left drone simply goes to previous position meaning it goes back and forth till fuel or moves run out
		if(numberOfPositives == 0) {
			int m = 0;
			List<Direction> allDirections = new ArrayList<Direction>();
			while(m < 16) {
				allDirections.add(Direction.values()[m]);
				m++;
			}
			if(allDirections.indexOf(back) < 8) back = allDirections.get(allDirections.indexOf(back) + 8);
			else back = allDirections.get(allDirections.indexOf(back) - 8);
			return position.nextPosition(back);
		}
		
		// for efficiency purposes avoids for loop
		Drone droneIdeal= new Stateful(position.nextPosition(dirToDestination(destination)));
		if(droneIdeal.getExchangerStation() == desired && droneIdeal.getExchangerStation() != null && droneIdeal.position.inPlayArea()) {
			return droneIdeal.position;
		}
		
		// drone needs to avoid 0 power stations, but only if it results in a drone missing the right exchanger(it can path through traded stations)
		for(int i = 0; i < 16; i++) {
			Drone droneCollecting = new Stateful(position.nextPosition(Direction.values()[i]));
			if(droneCollecting.getExchangerStation() == desired && position.nextPosition(Direction.values()[i]).inPlayArea()) return position.nextPosition(Direction.values()[i]);
		}
		
		// come at the station from a different angle if caught in a clustering of stations
		if(getDistance(desired.position) < 0.0003 && recentlyRepeated(dirToDestination(destination))) {
			for(int i = 0; i < 16; i++) {
				Direction check = Direction.values()[i];
				if(!recentlyRepeated(check) && position.nextPosition(check).inPlayArea() && notInNegative(check)) {
					destination = position.nextPosition(check);
					break;
				}
			}
			
		}
		return destination;
	}
	
	
	/**
	 * Gets optimal direction of desired destination but calls bestRedirection to ensure move doesn't result in a loss of coins
	 * @return Direction
	 */
	private Direction handleRedirects() {
		if(noOption()) return doLeastWorstMove();
		Direction optimal = dirToDestination(getClosestPositive());
		for(Station s: App.listOfNegativeStations) {
			Position nextPosition = position.nextPosition(optimal);
			Drone nextPositionDrone =  new Stateful(nextPosition);
			if(nextPositionDrone.getExchangerStation() == s) {
				optimal = bestRedirection(position, s, optimal);
			}
			
		}
		back = optimal;
		return optimal;
	}
	
	
	/**
	 * If a negative station is in the way the drone is told to go in the direction most similar to desired that does not land it in negative
	 * @param drone
	 * @param in_the_way
	 * @param to_redirect
	 * @return Direction
	 */
	private Direction bestRedirection(Position drone, Station in_the_way, Direction to_redirect ) {
		// put directions in list to utilize array list methods
		Direction optimized = to_redirect;
		List<Direction> directionList = new ArrayList<Direction>();
		int m = 0;
		while(directionList.size() != 16) {
			directionList.add(Direction.values()[m]);
			m++;
		}
		
		int startingDirection = directionList.indexOf(to_redirect);
		// get angle between negative station and positive to pick optimal compass direction to avoid negative but still head to the desired station
		Direction toNegative = dirToDestination(in_the_way.position);
		double angleToNegative = directionList.indexOf(toNegative) * 22.5;
		double angleToPositive = directionList.indexOf(to_redirect) * 22.5;
		int i = startingDirection;
		recentlyRepeated(Direction.values()[i]);
		boolean first = false;
		boolean second = false;
		
		// if positive station at more positive angle then go counter clockwise in the compass is more efficient in most cases
		if(angleToNegative < angleToPositive && Math.abs(angleToPositive - angleToNegative) < 180) {
			while(i < 17) {
				first = true;
				if(position.nextPosition(directionList.get(i)).inPlayArea() && notInNegative(directionList.get(i))) {
					optimized = directionList.get(i);
					break;
				}
				i++;
				if(i ==16) i = 0;
			}
		}
		// if negative station at more positive angle go clockwise
		else {
			
			while(i >-2) {
				second = true;
				if(position.nextPosition(directionList.get(i)).inPlayArea() && notInNegative(directionList.get(i))) {
					optimized = directionList.get(i);
					break;
				}
				i--;
				if(i == -1) i = 15;
			}
			
		}
		
		if(emergency) {
			emergency = false;
			directionList.remove(directionList.indexOf(lastResort));
			directionList.add(directionList.get(0));
		}
		
		int j;
		if(directionList.indexOf(optimized) >= 0) j = directionList.indexOf(optimized);
		else j = 0; 
		// avoid infinite back and forth when multiple negative stations are together and in the way
		if(recentlyRepeated(optimized)) {
			int noOption = 0;
			
			if(first) {
				while(j > -2 ) {
					noOption ++;
					if(position.nextPosition(directionList.get(j)).inPlayArea() && notInNegative(directionList.get(j)) 
							&& !recentlyRepeated(directionList.get(j)) && noOption < 16) {
						optimized = directionList.get(j);
						break;
					}
					// should the drone be unable to proceed without hitting negative, go back to previous position 
					// and note direction which results in having to return so we can avoid it in the next move
					if(noOption >= 16) {
						if(directionList.indexOf(back) < 8) lastResort = directionList.get(directionList.indexOf(back)+ 8);
						else lastResort = directionList.get(directionList.indexOf(back) - 8);
						while(!position.nextPosition(directionList.get(directionList.indexOf(lastResort))).inPlayArea() 
						|| !notInNegative(directionList.get(directionList.indexOf(lastResort)))) {
							lastResort = Direction.values()[seed.nextInt(16)];
						}
						emergency = true;
						optimized = lastResort;
						break;
					}
					j--;
					if(j == -1) j = 15;
				}
			}
			if(second) {
				while(j < 17) {
					noOption ++;
					if(position.nextPosition(directionList.get(j)).inPlayArea() && notInNegative(directionList.get(j)) 
							&& !recentlyRepeated(directionList.get(j)) && noOption < 16) {
						
						optimized = directionList.get(j);
						break;
					}
					
					// should the drone be unable to proceed without hitting negative, go back to previous position 
					// and note direction which results in having to return so we can avoid it in the next move
					if(noOption >= 16) {
						if(directionList.indexOf(back) < 8) lastResort = directionList.get(directionList.indexOf(back)+ 8);
						else lastResort = directionList.get(directionList.indexOf(back) - 8);
						while(!position.nextPosition(directionList.get(directionList.indexOf(lastResort))).inPlayArea() 
						|| !notInNegative(directionList.get(directionList.indexOf(lastResort)))) {
							lastResort = Direction.values()[seed.nextInt(16)];
						}
						emergency = true;
						optimized = lastResort;
						break;
					}
					j++;
					if(j == 16) j = 0;
				}
			}
			
		}
		return optimized;
	}
	
	
	/**
	 * Check if the drone is returning to a recently visited point to avoid this 
	 * @param check_repeated
	 * @return Boolean
	 */
	private Boolean recentlyRepeated(Direction check_repeated) {
		List<Point> recentlyRepeated = new ArrayList<Point>();
		Point toVisit = Point.fromLngLat(position.nextPosition(check_repeated).longitude, position.nextPosition(check_repeated).latitude);
		int past = 0;
		if(App.coordinatesTravelled.size() < 11) {
			while(recentlyRepeated.size() < App.coordinatesTravelled.size()) {
				recentlyRepeated.add(App.coordinatesTravelled.get(App.coordinatesTravelled.size() - 1 - past));
				past++;
			}
		}
		else {
			while(recentlyRepeated.size() < 11) {
				recentlyRepeated.add(App.coordinatesTravelled.get(App.coordinatesTravelled.size() - 1 - past));
				past++;
			}
		}
		for(Point p: recentlyRepeated) {
			if(p.latitude() == toVisit.latitude() && p.longitude() == toVisit.longitude()) return true;
		}
		return false;
	}
	
	
	/**
	 * do calculations for next move, execute, and write records to text and geojson docs
	 */
	public String makeMove() {
		Position positionBefore = position;
		Direction direction = handleRedirects();
		position = position.nextPosition(direction); 
		exchangeCoins();
		DocWriter.newJsonLine(positionBefore);
		return DocWriter.newLine(positionBefore, direction, position, getCoins(), getPower());
	}
}