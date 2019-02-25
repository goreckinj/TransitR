package GroupR;

import java.util.*;
import java.sql.Time;

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
 * Trips class that holds the data of a singular trip. It also holds a collection
 * of StopTimes representing the path of the trip over time
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public class Trip implements Subject  {

	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private Collection<Observer> observers;
	private Route route;
	private LinkedList<StopTime> stopTimes;
	private String tripID;

	/*******************************************************************************************
	 * CONSTRUCTOR
	 *******************************************************************************************/
	public Trip(Route route, String tripID){
		stopTimes = new LinkedList<StopTime>();

        observers = new LinkedList<>();

		this.route = route;
		this.tripID = tripID;
	}

	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Collection<StopTime> getAllStopTimes(){
		return stopTimes;
	}
	public Route getRoute(){
		return route;
	}
	public String getTripID(){
		return tripID;
	}

	/**
	 * Gets a stop time from the stopTimes collection
	 *
	 * @param sequence
	 */
	public StopTime getStopTime(int sequence){
		ArrayList<StopTime> stopTimes = new ArrayList<>(this.stopTimes);
		return stopTimes.get(sequence);
	}

	/**
	 * Determines if trip is happening in the future
	 * @return - true if the first stop of the trip is happening in the future
	 */
	public boolean isFutureTrip() {
		Date date = new Date();
		Time currentTime = new Time(date.getHours(), date.getMinutes(), 0);

		if(stopTimes.get(0).getArrivalTime().after(currentTime)) {
			return true;
		}else {
			return false;
		}
	}

	public Time getStartTime() {

		if(stopTimes == null || stopTimes.size() == 0) {
			return null;
		}

		return stopTimes.getFirst().getArrivalTime();
	}

	public Time getEndTime() {

		if(stopTimes == null || stopTimes.size() == 0) {
			return null;
		}

		return stopTimes.getLast().getDepartTime();
	}

	@Override
	public String toString() {
		return this.tripID;
	}


	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	/**
	 * Adds a stop time to the stopTimes collection by creating it from stop time variables
	 * @param arrive - the string representing the arrival time of the stop
	 * @param depart - the string representing the departure time of the stop
	 * @param sequence - the sequence the stop should be added at
	 * @return - true if the stop was added properly
	 */
	public boolean addStopTime(String arrive, String depart, Stop stop, int sequence){



		//if no stops yet, add
		if(stopTimes.size() == 0) {
			stopTimes.add(new StopTime(this,arrive, depart, stop, sequence));
			return true;
		}

		//if seqeunce at index 0 is less than sequence add at 0
		if(stopTimes.getFirst().getSequence() > sequence) {
			stopTimes.add(0, new StopTime(this, arrive, depart, stop, sequence));
			return true;
		}

		//if last sequnce is less than sequence add at end
		if(stopTimes.getLast().getSequence() < sequence) {
			stopTimes.add(new StopTime(this, arrive, depart, stop, sequence));
			return true;
		}

		ListIterator<StopTime> iter = stopTimes.listIterator(stopTimes.size());
		StopTime stopTime;

		while(iter.hasPrevious()) {
			stopTime = iter.previous();

			if(stopTime.getSequence() == sequence) {
				return false;
			}

			if(stopTime.getSequence() > sequence && iter.hasPrevious() && iter.previous().getSequence() < sequence) {
				iter.next();
				iter.add(new StopTime(this, arrive, depart, stop, sequence));
				return true;
			}else {
				iter.next();
			}
		}

		return false;
	}

	/**
	 * sets the id of the trip
	 *
	 * @param tripID - the new ID to set
	 * @return OldTripID - the old ID
	 */
	public String setID(String tripID){
		String oldTripID = tripID;
		this.tripID = tripID;

        notifyObservers("id");

		return oldTripID;
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