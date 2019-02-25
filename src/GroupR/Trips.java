package GroupR;

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
 * Description: Trips Class that stores a collection of the trips to be displayed on the MapView
 * corresponding information
 * @author goreckinj, hueblergw, jinq
 * @version 2.0
 * @created 17-Oct-2017 17:50:00
 */


import javafx.scene.control.Alert;

import java.io.*;
import java.sql.Time;
import java.util.*;

public class Trips {

	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private Map<String, Trip> trips;


	/*******************************************************************************************
	 * CONSTRUCTOR
	 *******************************************************************************************/
	public Trips(){
		trips = new TreeMap<>();
	}


	/*******************************************************************************************
	 * LOAD FILE
	 *******************************************************************************************/

	/**
	 * @param tripsFile - trips file to be loaded
	 * @param stopTimesFile - stop file to be loaded
	 * @return - true if load successful, false if unsuccessful
	 * @author hueblergw, goreckinj, jinq
	 */
	public boolean loadFile(File tripsFile, File stopTimesFile, Map<String, Route> routes, Map<String, Stop> stops) {

		if(tripsFile == null || stopTimesFile == null) {
			return false;
		}

		if(emptyFile(tripsFile)) {
			throw new IllegalArgumentException("trips.txt is an empty file");
		}

		if(emptyFile(stopTimesFile)) {
			throw new IllegalArgumentException("stop_times.txt is an empty file");
		}

		String line;
		String tripsFileFormatDiscription = "route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id";
		String tripAttributes[];

		//for trips file
		try(Scanner tripsFileIn = new Scanner(tripsFile)) {

			line = getNextLine(tripsFileIn);

			//make sure file format is correct, based on first line
			if(!isValidHeader(line, tripsFileFormatDiscription)) {
				throw new IllegalArgumentException("Improper File Header Format for trips.txt");
			}

			//keep count and go through entire file, all specific routes start at line 2
			for(int i = 2; tripsFileIn.hasNextLine(); i++) {
				//get line
				line = tripsFileIn.nextLine();

				//get rid of all spaces unless in parentheses
				line = line.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)","");

				//split line by commas, ignoring commas surrounded by quotes
				tripAttributes = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

				//if the line had more or less attributes than needed/wanted throw exception
				if(!numElementsValid(tripAttributes.length, 7)) {
					throw new IllegalArgumentException("Improper number of attributes at line " + i);
				}

				//check if the route the trip is associated to exists
				if(!isValidID(tripAttributes[0], routes)) {
					throw new IllegalArgumentException("Route specified in line " + i + " not found.");
				}

				//add the new trip
				trips.put(tripAttributes[2], new Trip(routes.get(tripAttributes[0]), tripAttributes[2]));

				routes.get(tripAttributes[0]).addAssociatedTrip(trips.get(tripAttributes[2]));
			}
		}catch(IllegalArgumentException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("trips.txt is corrupt");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			return false;

		}catch(FileNotFoundException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("File Not Found");
			alert.setContentText("The file trips.txt could not be found");
			alert.showAndWait();
			return false;
		}

		String stopTimesFileFormatDiscription = "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type";
		String stopTimesAttributes[];
		int sequence;
		Trip trip;

		//for stop_times file
		try(Scanner StopTimesFileIn = new Scanner(stopTimesFile)) {

			//check if stop time file is empty
			line = getNextLine(StopTimesFileIn);

			//make sure file format is correct, based on first line
			if(!isValidHeader(stopTimesFileFormatDiscription, line)) {
				throw new IllegalArgumentException("Improper File Header Format for stop_times.txt");
			}
			//keep count and go through entire file, the specific data starts @ line 2
			for(int i = 2; StopTimesFileIn.hasNextLine(); i++) {

				//get line
				line = StopTimesFileIn.nextLine();

				//get rid of all spaces unless in parentheses
				line = line.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)","");

				//split line by commas, ignoring commas surrounded by quotes
				stopTimesAttributes = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

				//if the line had more or less attributes than needed/wanted throw exception
				if(!numElementsValid(stopTimesAttributes.length, 8)) {
					throw new IllegalArgumentException("Improper number of attributes at line " + i);
				}

				//get the trip
				trip = getTrip(stopTimesAttributes[0]);

				//if trip came back as null complain
				if(trip == null) {
					throw new IllegalArgumentException("Trip specified in line " + i + " not found.");
				}

				//check if arrival and depart time are in valid format
				if(!(isValidTime(stopTimesAttributes[1]) && isValidTime(stopTimesAttributes[2]))) {
					throw new IllegalArgumentException("Times for StopTime at line " + i + " are invalid.");
				}
				if(!isValidID(stopTimesAttributes[3], stops)) {
					throw new IllegalArgumentException("Stop specified in line " + i + " not found.");
				}

				//parse sequence number and throw exception if theres a problem
				if(!validSequence(trip, stopTimesAttributes[4])) {
					throw new IllegalArgumentException("Stop sequence specified in line " + i + " is invalid");
				}

				sequence = Integer.parseInt(stopTimesAttributes[4]);

				//add stopTime to trip
				trip.addStopTime(stopTimesAttributes[1], stopTimesAttributes[2], stops.get(stopTimesAttributes[3]), sequence);

				stops.get(stopTimesAttributes[3]).addAssociatedRoute(trip.getRoute());

				stops.get(stopTimesAttributes[3]).addAssociatedTrip(trip);

				trip.getRoute().addAssociatedStops(stops.get(stopTimesAttributes[3]));
			}
		}catch(IllegalArgumentException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("stop_times.txt is corrupt");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			return false;

		}catch(FileNotFoundException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("File Not Found");
			alert.setContentText("The file stop_times.txt could not be found");
			alert.showAndWait();
			return false;
		}

		return true;
	}


	/*******************************************************************************************
	 * EXPORT AND EXPORT CHECK
	 *******************************************************************************************/

	/**
	 * Exports the trips and stop times into .txt files
	 * @param directory
	 * @return boolean
	 */
	public boolean exportFile(File directory) {
		if(directory != null) {
			String tripsData = "";
			String stopTimesData = "";
			File tripsFile = new File(directory, "trips.txt");
			File stopTimesFile = new File(directory, "stop_times.txt");

			try (BufferedWriter tripsWriter = new BufferedWriter(new FileWriter(tripsFile))) {
				try (BufferedWriter stopTimesWriter = new BufferedWriter(new FileWriter(stopTimesFile))) {
					//Write headers for stop_times.txt and trips.txt
					tripsWriter.write("route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id");
					stopTimesWriter.write("trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type");

					//Setup large data buffer size (improve writing efficiency)
					for (Trip trip : trips.values()) {

						tripsData += "\n" + trip.getRoute().getID() + ",," + trip.getTripID() + ",,,," + trip.getRoute().getID() + "_shape";

						for (StopTime stopTime : trip.getAllStopTimes()) {
							stopTimesData += "\n" + trip.getTripID() + "," + stopTime.getArrivalTime() + "," + stopTime.getDepartTime()
									+ "," + stopTime.getStop().getID() + "," + stopTime.getSequence() + ",,,";
						}

					}
					//Write buffer to stop_times.txt
					stopTimesWriter.write(stopTimesData);
				}
				//write buffer to trips.txt
				tripsWriter.write(tripsData);
			} catch (IOException e) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("File Error");
				alert.setContentText("Error creating trips.txt and stop_times.txt");
				alert.showAndWait();
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks if all trips are ready to be exported
	 * @author: goreckinj
	 * @return boolean
	 */
	public boolean isReadyToExport() {

		//for all trips
		for(Trip trip : trips.values()) {

			//if has invalid trip id or invalid route id alert and return false
			if(trip.getRoute().getID() == null || trip.getTripID() == null) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Trips data structure invalid");
				alert.setContentText("Problem with ids for trip: " + trip.toString());
				alert.showAndWait();
				return false;
			}

			//if trip has no stop times, alert and return false
			if(trip.getAllStopTimes() == null) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Trips data structure invalid");
				alert.setContentText("StopTime data structure in trip " + trip.getTripID() + " is empty");
				alert.showAndWait();
				return false;
			}

			Time currentArrivalTime = new Time(0,0,0);
			Time previousDepartureTime = new Time(0,0,0);

			//for each stop time in this trip
			for(StopTime stopTime : trip.getAllStopTimes()) {

				//if the stop time has a null id alert and return false
				if(stopTime.getStop().getID() == null) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("StopTime data structure invalid");
					alert.setContentText("Problem with ids for stop time " + stopTime.toString());
					alert.showAndWait();
					return false;
				}

				//if the times arent valid, alert and return false
				if(!isValidTime(stopTime.getArrivalTime().toString()) || !isValidTime(stopTime.getDepartTime().toString())) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("StopTime data structure invalid");
					alert.setContentText("Problem with arrival and depart time for stop time " + stopTime.toString());
					alert.showAndWait();
					return false;
				}

				currentArrivalTime = stopTime.getArrivalTime();

				if(currentArrivalTime.before(previousDepartureTime) || stopTime.getArrivalTime().after(stopTime.getDepartTime())) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("StopTime data structure invalid");
					alert.setContentText("Time order for stops in trip " + trip + " is invalid"
						+ "\nCheck Stop " + stopTime.getStop().getID());
					alert.showAndWait();
					return false;
				}

				previousDepartureTime = stopTime.getDepartTime();
			}
		}
		return true;
	}


	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Map<String, Trip> getAllTrips() {
		return trips;
	}
	public Trip getTrip(String tripID){
		return trips.get(tripID);
	}

	/**
	 * Gets the trip based off the route id
	 * @param routeID
	 * @return Trip
	 */
	public Trip getTripByRouteId(String routeID){
		Iterator<Trip> iter = trips.values().iterator();
		Trip trip;
		while(iter.hasNext()) {
			trip = iter.next();
			if(trip.getRoute().toString().equals(routeID)) {
				return trip;
			}
		}
		return null;
	}

	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	public boolean setTripID(String oldID, String newID) {
		Trip trip = trips.get(oldID);
		Trip trip2 = trips.get(newID);
		if(trip != null && trip2 == null) {
			trip.setID(newID);
			trips.put(newID, trips.remove(oldID));
			return true;
		}else {
			return false;
		}
	}

	public boolean addStopTime(String ID, String arrive, String depart, Stop stop, int sequence) {
		Trip trip = trips.get(ID);

		if(trip != null) {
			trip.addStopTime(arrive, depart, stop, sequence);
			return true;
		}else {
			return false;
		}
	}

	/*******************************************************************************************
	 * VALIDITY CHECKERS
	 *******************************************************************************************/

	public static boolean isValidID(String id, Map<String, ?> map) {
		if(map.get(id) == null) {
			return false;
		}
		return true;
	}

	public String getNextLine(Scanner in) {
		if(!in.hasNextLine()) {
			return null;
		}
		return in.nextLine();
	}

	public boolean isValidHeader(String header, String comparator) {
		if(header.compareTo(comparator) != 0) {
			return false;
		}
		return true;
	}

	public boolean numElementsValid(int numElements, int prefNum) {
		if(numElements != prefNum) {
			return false;
		}
		return true;
	}

	public boolean isValidTime(String time) {
		if(time == null) {
			return false;
		}
		if(time.matches("[0-9][0-9]:[0-5][0-9]:[0-5][0-9]|[0-9]:[0-5][0-9]:[0-5][0-9]")) {
			return true;
		}
		return false;
	}

	public boolean emptyFile(File file) {
		if (file.length() > 0) {
			return false;
		}
		return true;
	}

	public static boolean validSequence(Trip trip, String sequenceString) {

		int sequence;

		try {
			sequence = Integer.parseInt(sequenceString);

			if(sequence < 0) {
				return false;
			}

		}catch(Exception e) {
			return false;
		}

		return true;
	}
}