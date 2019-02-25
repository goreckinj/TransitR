package GroupR;

import javafx.scene.paint.*;

import java.util.*;

/**
 * Copyright [2017] [Qiuhao Jin, Nick Gorecki, Gunther Huebler]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Route class that acts holds the data of a singular route
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:31
 */
public class Route implements Subject {

	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private String description;
	private String longName;
	private Color routeColor;
	private String id;
	private String shortName;
	private Map<String, Stop> associatedStops;
	private Map<String, Trip> associatedTrips;
	Collection<Observer> observers;


	/*******************************************************************************************
	 * CONSTRUCTOR
	 *******************************************************************************************/

	public Route(String id, String shortName, String longName, String description, Color routeColor){

		associatedStops = new TreeMap<>();
		associatedTrips = new TreeMap<>();
		observers = new ArrayList<>();

		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		this.description = description;
		this.routeColor = routeColor;
	}

	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Map<String, Stop> getAssociatedStops() {
		return associatedStops;
	}
	public Map<String, Trip> getAssociatedTrips() {
		return associatedTrips;
	}
	public Color getColor(){
		return routeColor;
	}
	public String getDescription(){
		return description;
	}
	public String getID(){
		return id;
	}
	public String getLongName(){
		return longName;
	}
	public String getShortName(){
		return shortName;
	}


	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	/**
	 * Sets the color of the route
	 * 
	 * @param color - color to change to
	 * @return oldColor - the old color
	 */
	public Color setColor(String color){
		Color oldColor = this.routeColor;
		routeColor = Color.web(color);

		notifyObservers("color");

		return oldColor;
	}

	/**
	 * Adds a stop to associated stops
	 * @param stop
	 */
	public void addAssociatedStops(Stop stop) {
		String id = stop.getID();
		if(!associatedStops.containsKey(id)) {
			associatedStops.put(id, stop);
		}
	}

	/**
	 * Add a trip to associated trips
	 * @param trip
	 */
	public void addAssociatedTrip(Trip trip) {
		String id = trip.getTripID();
		if(!associatedTrips.containsKey(id)) {
			associatedTrips.put(id, trip);
		}
	}

	/**
	 * Sets the long name of the route
	 * @param longName - The long name to set
	 * @return String - oldLongName
	 */
	public String setLongName(String longName) {
		String oldLongName = this.longName;
		this.longName = longName;

		notifyObservers("name");

		return oldLongName;
	}

	/**
	 * Sets the short name of the route
	 * @param shortName - The short name to set
	 * @return String - oldShortName
	 */
	public String setShortName(String shortName) {
		String oldShortName = this.shortName;
		this.shortName = shortName;

		notifyObservers("sname");

		return oldShortName;
	}

	/**
	 * Sets the description of the route
	 * 
	 * @param description
	 * @return oldDescription The old description of the route
	 */
	public String setDescription(String description){
		String oldDescription = this.description;
		this.description = description;

		notifyObservers("desc");

		return oldDescription;
	}

	/**
	 * Sets the id of the route
	 * 
	 * @param id
	 * @return oldID - The old id of the route
	 */
	public String setID(String id){
		String oldID = this.id;
		this.id = id;

		notifyObservers("id");

		return oldID;
	}

	public String toString() {
		return this.id;
	}

	/*******************************************************************************************
	 * SUBJECT METHODS
	 *******************************************************************************************/

	@Override
	public boolean addObserver(Observer o) {
		observers.add(o);
		return false;
	}

	@Override
	public Observer deleteObservers(Observer o) {
		observers.remove(o);
		return null;
	}

	@Override
	public void notifyObservers(String changedData) {

		Iterator<Observer> iter = observers.iterator();

		while(iter.hasNext()) {
			iter.next().update(changedData);
		}
	}
}