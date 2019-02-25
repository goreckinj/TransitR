package GroupR;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

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
 * StopTime class that holds the data of a singular stop time to be displayed in
 * the ListView
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public class StopTime implements Subject {


	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private Trip trip;
	private Time arrive;
	private Time depart;
	private int sequence;
	private Stop stop;
	private Collection<Observer> observers;


	/*******************************************************************************************
	 * CONSTRUCTOR
	 *******************************************************************************************/

	public StopTime(Trip trip, String arrive, String depart, Stop stop, int sequence){
		this.trip = trip;
		String timeComponents[];
		Time newTime;
		int hour;
		int minute;
		int second;

		observers = new LinkedList<Observer>();

		timeComponents = arrive.split(":");
		hour = Integer.parseInt(timeComponents[0]);
		minute = Integer.parseInt(timeComponents[1]);
		second = Integer.parseInt(timeComponents[2]);
		newTime = new Time(hour, minute, second);

		this.arrive = newTime;

		timeComponents = depart.split(":");
		hour = Integer.parseInt(timeComponents[0]);
		minute = Integer.parseInt(timeComponents[1]);
		second = Integer.parseInt(timeComponents[2]);
		newTime = new Time(hour, minute, second);

		this.depart = newTime;

		this.sequence = sequence;
		this.stop = stop;
	}

	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Time getArrivalTime(){
		return arrive;
	}
	public Time getDepartTime(){
		return depart;
	}
	public int getSequence(){
		return sequence;
	}
	public Stop getStop(){
		return stop;
	}
	public Trip getTrip() {
		return trip;
	}

	@Override
	public String toString() {
		return stop.toString();
	}


	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	/**
	 * Sets the arrival time for the stop times
	 * 
	 * @param arrive
	 * @return oldArrive
	 */
	public Time setArrival(Time arrive){
		Time oldArrive = this.arrive;
		this.arrive = arrive;

		notifyObservers("time");

		return oldArrive;
	}

	/**
	 * Sets the depart time for the stop id
	 * 
	 * @param depart
	 * @return oldDepart
	 */
	public Time setDepart(Time depart){
		Time oldDepart = this.depart;
		this.depart = depart;

		notifyObservers("time");

		return oldDepart;
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