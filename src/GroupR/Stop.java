package GroupR;

import com.lynden.gmapsfx.javascript.object.Marker;

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
 * Stop class that holds the data of a singular stop
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public class Stop implements Subject {


	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private String description;
	private String id;
	private double latitude;
	private double longitude;
	private String name;
	private Map<String, Trip> associatedTrips;
	private Map<String, Route> associatedRoutes;
	private Collection<Observer> observers;



	/*******************************************************************************************
	 * CONSTRUCTOR
	 *******************************************************************************************/
	public Stop(String id, String name, String description, double latitude, double longitude){

		associatedTrips = new TreeMap<>();
		associatedRoutes = new TreeMap<>();
		observers = new ArrayList<>();

		this.id = id;
		this.name = name;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
	}


	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Map<String, Route> getAssociatedRoutes() {
		return associatedRoutes;
	}
	public Map<String, Trip> getAssociatedTrips() {
		return associatedTrips;
	}
	public String getDescription(){
		return description;
	}
	public String getID(){
		return id;
	}
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	public String getName(){
		return name;
	}
	public String toString() {
		return this.getID();
	}


	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	public void addAssociatedRoute(Route route) {
		String id = route.getID();
		if(!associatedRoutes.containsKey(id)) {
			associatedRoutes.put(id, route);
		}
	}

	public void addAssociatedTrip(Trip trip) {
		String id = trip.getTripID();
		if(!associatedTrips.containsKey(id)) {
			associatedTrips.put(id, trip);
		}
	}

	public String setDescription(String description){
		String oldDescription = this.description;
		this.description = description;

		notifyObservers("desc");

		return oldDescription;
	}

	public String setID(String id){
		String oldid = this.id;
		this.id = id;

		notifyObservers("id");

		return oldid;
	}

	public double setLatitude(double latitude){
		double oldLatitude = this.latitude;
		this.latitude = latitude;

		notifyObservers("loc");

		return oldLatitude;
	}

	public double setLongitude(double longitude){
		double oldLongitude = this.longitude;
		this.longitude = longitude;

		notifyObservers("loc");

		return oldLongitude;
	}

	public String setName(String name){
		String oldName = this.name;
		this.name = name;

		notifyObservers("name");

		return oldName;
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