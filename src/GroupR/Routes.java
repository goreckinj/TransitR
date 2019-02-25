package GroupR;

import javafx.scene.control.Alert;
import javafx.scene.paint.Color;

import java.io.*;
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
 * Routes class that stores a collection of the routes to be displayed on the
 * MapView
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public class Routes {

	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private Map<String, Route> routes;


	/*******************************************************************************************
	 * CONSTRUCTOR
	 ******************************************************************************************/
	public Routes(){
		routes = new TreeMap<>();
	}


	/*******************************************************************************************
	 * LOAD FILE
	 *******************************************************************************************/

	/**
	 * Load file into data structure for stop
	 * @param file - file to be loaded
	 * @return - true if load successful, false if not
	 */
	public boolean loadFile(File file) {
		//might wanna change this to hashmap or something, reuse stopAttributes[0] as key when adding

		String line;
		String fileFormatDescription = "route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color";
		String routeAttributes[];

		try(Scanner fileIn = new Scanner(file)) {

			//make sure file has stuff in it
			if(checkEmptyFile(file)) {
				line = fileIn.nextLine();
			}else {
				throw new IllegalArgumentException("routes.txt is an empty file");
			}

			//make sure file format is correct, based on first line
			if(!checkFormat(fileFormatDescription)) {
				throw new IllegalArgumentException("Improper File Header Format for routes.txt");
			}

			//keep count and go through entire file
			for(int i = 2; fileIn.hasNextLine(); i++) {

				//get line
				line = fileIn.nextLine();

				//split line by commas, ignoring commas surrounded by quotes
				routeAttributes = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

				//get rid of unwanted spaces and quotes
				routeAttributes[0] = routeAttributes[0].replaceAll("\\s+", "");
				routeAttributes[2] = routeAttributes[2].replaceAll("\"", "");
				routeAttributes[3] = routeAttributes[3].replaceAll("\"", "");
				routeAttributes[4] = routeAttributes[4].replaceAll("\"", "");
				routeAttributes[7] = routeAttributes[7].replaceAll("\\s+", "");

				//if the line had more or less attributes than needed/wanted throw exception
				if(!checkAttributeLength(routeAttributes.length)) {
					throw new IllegalArgumentException("Improper number of attributes at line " + i);
				}

				//if color is empty set to black
				if(routeAttributes[7].equals("")) {
					routeAttributes[7] = "FFFFFF";
				}
				//ensure the color contains only 6 hex characters
				if(!checkColor(routeAttributes[7])) {
					throw new IllegalArgumentException("Improper color format at line " + i);
				}

				//add the new route
				routes.put(routeAttributes[0], new Route(routeAttributes[0], routeAttributes[2], routeAttributes[3], routeAttributes[4], Color.web(routeAttributes[7])));
			}

			fileIn.close(); //using try with actually automatically creates the finally block for the file, of which closes it as well.

		}catch(IllegalArgumentException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("routes.txt is corrupt");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			return false;

		}catch(FileNotFoundException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("File Not Found");
			alert.setContentText("The file routes.txt could not be found.");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	/*******************************************************************************************
	 * EXPORT AND EXPORT CHECK
	 *******************************************************************************************/

	/**
	 * Exports routes.txt files to directory
	 * @param directory
	 * @return boolean
	 * @author hueblergw
	 */
	public boolean exportFiles(File directory) {
		if(directory == null) {
			return false;
		}

		try {
			File routesFile = new File(directory + "\\routes.txt");

			if(routesFile.exists()) {
				routesFile.createNewFile();
			}

			FileWriter out = new FileWriter(routesFile);
			PrintWriter writer = new PrintWriter(out);

			writer.println("route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color");

			Iterator<Route> iter = routes.values().iterator();
			Route route;

			while(iter.hasNext()) {

				route = iter.next();

				writer.println(route.getID() + ",,\"" + route.getShortName() + "\",\"" + route.getLongName()
						+ "\",\"" + route.getDescription() + "\",,," + route.getColor().toString().substring(2,8).toUpperCase() + ',');
			}

			writer.close();

		}catch(IOException e) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("routes.txt export failed");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			return false;
		}

		return true;
	}

	public boolean isReadyToExport() {

		Iterator<Route> iter = routes.values().iterator();
		Route route;

		while(iter.hasNext()) {
			route = iter.next();

			if(route.getID() == null || route.getShortName() == null || route.getDescription() == null || route.getLongName() == null || route.getColor() == null) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Routes data structure invalid");
				alert.setContentText("Problem with ID, Shor tName, Long Name, or Description for route: " + route.toString());
				alert.showAndWait();
				return false;
			}

			if(!checkColor(route.getColor().toString().substring(2,8).toUpperCase())) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Routes data structure invalid");
				alert.setContentText("Invalid color for route: " + route.toString());
				alert.showAndWait();
				return false;
			}

		}

		return true;
	}

	/*******************************************************************************************
	 * GETTERS
	 *******************************************************************************************/
	public Map<String, Route> getAllRoutes(){
		return routes;
	}
	public Route getRoute(String id){
        return routes.get(id);
	}

	/*******************************************************************************************
	 * SETTERS
	 *******************************************************************************************/

	public boolean setRouteID(String oldID, String newID) {
		Route route = routes.get(oldID);
		Route route2 = routes.get(newID);
		if(route != null && route2 == null) {
			route.setID(newID);
			routes.put(newID, routes.remove(oldID));
			return true;
		}else {
			return false;
		}
	}

	public boolean setRouteColor(String ID, String newColor) {
		Route route = routes.get(ID);

		if(route != null) {
			route.setColor(newColor);
			return true;
		}else {
			return false;
		}
	}

	public boolean setRouteDescription(String ID, String newDescription) {
		Route route = routes.get(ID);

		if(route != null) {
			route.setDescription(newDescription);
			return true;
		}else {
			return false;
		}
	}

	/*******************************************************************************************
	 * VALIDITY CHECKERS
	 *******************************************************************************************/

	public boolean checkFormat(String header){
		String fileFormatDescription = "route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color";
		if(header.equals(fileFormatDescription)){
			return true;
		}
		return false;
	}
	public boolean checkAttributeLength(int attributeLength){
		int correctAttributeLength = 9;
		if(attributeLength == correctAttributeLength){
			return true;
		}
		return false;
	}
	public static boolean checkColor(String color){
		if(color.matches("[0-9A-F]+") && color.length() <= 6) {
			return true;
		}
		return false;
	}
	public boolean checkEmptyFile(File fileIn){
		if(fileIn.length()>0) {
			return true;
		}
		return false;
	}
}