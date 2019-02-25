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
 * Controller class that holds the JavaFX GUI elements to be displayed, along with
 * corresponding information
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:31
 */

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.*;

import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.textfield.TextFields;
import com.lynden.gmapsfx.GoogleMapView;


public class Controller{

	/*******************************************************************************************
	 * GUI OBJECTS
	 *******************************************************************************************/

	private ListView listModel;
	private MapView mapModel;
	private ElementController elementController;
	private Stage elementStage;
	private String transport_option = "Transit";

	@FXML
	private TextField stopText;
	@FXML
	private TextField routeText;
	@FXML
	private Label leftStatus, stopSelectInfoLabel, stopSelectedLabel;
	@FXML
	private Label rightStatus;
	@FXML
	private javafx.scene.control.ListView<String> itemList;
	@FXML
	private Label listMode;
	@FXML
	private GoogleMapView mapView;
	@FXML
	private TextField fromTextField;
	@FXML
	private TextField toTextField;

	/*******************************************************************************************
	 * DATA OBJECTS
	 *******************************************************************************************/
	private Routes routes;
	private Stops stops;
	private Trips trips;

	//indicator that files are loaded, starts off false
	private boolean filesLoaded = false;
	private final SimpleTimer timer = new SimpleTimer();
	SuggestionProvider stopSuggestions;
	SuggestionProvider routeSuggestions;


	/*******************************************************************************************
	 * INITIALIZATION
	 *******************************************************************************************/

	public void initialize() {
		ApplicationDriver.STAGE.setOnShown(event -> {
			try {
				importFiles();
			} catch( IOException e) {
				System.out.println("Error loading Element GUI");
			}
		});
	}

	/**
	 * Initialize the MapView and ListView to be ready to display things. Initializes the
	 * objects by sending them the panes in which to display things.
	 *
	 * @author hueblergw
	 */
	private void initializeViews() throws IOException {

		//clear list display description and text fields
		listMode.setText("");
		stopText.setText("");
		routeText.setText("");

		if(elementStage != null && elementStage.isShowing()) {
			elementStage.close();
		}
		//create element stage and controller
		elementStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Element.fxml"));
		Parent root;
		root = loader.load();
		elementController = loader.getController();
		elementController.setMainController(this)
						 .setReferences(routes, trips, stops);
		elementStage.setTitle("Element");
		elementStage.setScene(new Scene(root, ApplicationDriver.SCENE_WIDTH/4,
				ApplicationDriver.SCENE_HEIGHT));

		//create new models
		//mapModel = new MapView(mapPane, webView);

		mapModel = new MapView(mapView, stops, stopSelectInfoLabel, stopSelectedLabel);
		listModel = new ListView(listMode, itemList, routes.getAllRoutes(), trips.getAllTrips(), stops.getAllStops(),
				elementController, elementStage);

		//add observers to all subjects
		Iterator<Stop> stopIter = stops.getAllStops().values().iterator();
		Stop stop;

		while(stopIter.hasNext()) {
			stop = stopIter.next();
			stop.addObserver(listModel);
			stop.addObserver(mapModel);
			stop.addObserver(elementController);
		}

		Iterator<Route> routeIter = routes.getAllRoutes().values().iterator();
		Route route;

		while(routeIter.hasNext()) {
			route = routeIter.next();
			route.addObserver(listModel);
			route.addObserver(mapModel);
		}

		Iterator<Trip> tripIter = trips.getAllTrips().values().iterator();
		Iterator<StopTime> stopTimeIter;
		StopTime stopTime;
		Trip trip;

		while(tripIter.hasNext()) {
			trip = tripIter.next();
			trip.addObserver(listModel);
			trip.addObserver(mapModel);
			trip.addObserver(elementController);

			stopTimeIter = trip.getAllStopTimes().iterator();

			while(stopTimeIter.hasNext()) {
				stopTime = stopTimeIter.next();
				stopTime.addObserver(listModel);
				stopTime.addObserver(mapModel);
				stopTime.addObserver(elementController);
			}
		}
	}
	/*******************************************************************************************
	 * SET TRANSPORT OPTION
	 *******************************************************************************************/
	public String transport_transit(){
		return transport_option = "Transit";
	}
	public String transport_driving(){
		return transport_option = "Driving";
	}
	public String transport_bicycling(){
		return transport_option = "Bicycling";
	}
	public String transport_walking(){
		return transport_option = "walking";
	}
	/*******************************************************************************************
	 * DISPLAY METHODS
	 *******************************************************************************************/

	/**
	 * Checks for when the MapView runs displayCurrentRoutes()
	 *
	 * @param routeID
	 * @author goreckinj
	 */
	public void displayCurrentRoutesListener(String routeID) {
		timer.start();

		leftStatus.setText(timer.stop());
		rightStatus.setText("Displaying current routes");
	}

	/**
	 * Checks for when routes are displayed on the MapView
	 *
	 * @param routeID
	 * @author goreckinj
	 */
	public void displayStopsOnRouteListener(String routeID) {
		timer.start();

		leftStatus.setText(timer.stop());
		rightStatus.setText("Displaying all stops on route " + routeID);
	}

	/**
	 * Display all stops on Google map
	 *
	 * @author goreckinj
	 */
	public void displayAllStops() {
		timer.start();
        LatLong stop1 = new LatLong(44.822390,-91.478580);
        MapOptions mapOption = new MapOptions();

		leftStatus.setText(timer.stop());
		rightStatus.setText("Displaying all stops");
	}

	public void displayRoute(){
		String departure = fromTextField.getText();
		String destination = toTextField.getText();
		if(filesLoaded) {
			switch (transport_option) {
				case "Transit":
					mapModel.plotRoute(departure, destination, "Transit");
					break;
				case "Driving":
					mapModel.plotRoute(departure, destination, "Driving");
					break;
				case "Walking":
					mapModel.plotRoute(departure, destination, "Walking");
					break;
				case "Bicycling":
					mapModel.plotRoute(departure, destination, "Bicycling");
					break;
				default:
					mapModel.plotRoute(departure, destination, "Transit");
			}
		}
	}

	/*******************************************************************************************
	 * LIST METHODS
	 *******************************************************************************************/

	/**
	 * List all stops in listModel
	 */
	public void listAllStops() {
		if (filesLoaded) {
			rightStatus.setText("Listing all stops . . .");
			timer.start();
			listMode.setText("ALL STOPS");
			Collection<Stop> allStops = stops.getAllStops().values();
			listModel.listCollection(allStops);
			mapModel.plotStops(stops.getAllStops().values());
			leftStatus.setText(timer.stop());
			rightStatus.setText("All stops listed successfully");
		} else {
			rightStatus.setText("No files loaded");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("No files loaded. Nothing to list.");
			alert.showAndWait();
		}
	}

	/**
	 * List all trips in listModel
	 */
	public void listAllTrips() {
		if (filesLoaded) {
			rightStatus.setText("Listing all trips . . .");
			timer.start();
			listMode.setText("ALL TRIPS");
			listModel.listCollection(trips.getAllTrips().values());
			leftStatus.setText(timer.stop());
			rightStatus.setText("All trips listed successfully");
		} else {
			rightStatus.setText("No files loaded");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("No files loaded. Nothing to list.");
			alert.showAndWait();
		}
	}

	/**
	 * List all routes in listModel
	 */
	public void listAllRoutes() {
		if (filesLoaded) {
			rightStatus.setText("Listing all routes . . .");
			timer.start();
			listMode.setText("ALL ROUTES");
			listModel.listCollection(routes.getAllRoutes().values());
			leftStatus.setText(timer.stop());
			rightStatus.setText("All routes listed successfully");
		} else {
			rightStatus.setText("No files loaded");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("No files loaded. Nothing to list.");
			alert.showAndWait();
		}
	}

	/**
	 * List all stops on an entered route in listModel
	 */
	public void listStopsOnRoute() {
		//if not text enter do nothing
		if (!routeText.getText().equals("")) {

			//if files not loaded alert
			if (filesLoaded) {

				Route route = routes.getRoute(routeText.getText());
				rightStatus.setText("Listing all stops on route " + route.getID());
				//if stop is not found alert user
				if (route != null) {
					timer.start();
					listMode.setText("ALL STOPS ON ROUTE: " + route.getID());
					listModel.listCollection(route.getAssociatedStops().values());
					mapModel.plotRoutesAmongStops(route.getAssociatedStops().values());
					leftStatus.setText(timer.stop());
					rightStatus.setText("All stops on route " + route.getID() + " listed successfully");
				} else {
					rightStatus.setText("Stops could not be listed");
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("That route ID is invalid");
					alert.showAndWait();
				}

			} else {
				rightStatus.setText("No files loaded");
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("No files loaded. Nothing to list.");
				alert.showAndWait();
			}
		}
	}

	/**
	 * List all trips of on certain route that are happening in the future in listModel
	 */
	public void listFutureTrips() {

		//if not text enter do nothing
		if (!routeText.getText().equals("")) {

			//if files not loaded alert
			if (filesLoaded) {

				Route route = routes.getRoute(routeText.getText());

				//if stop is not found alert user
				if (route != null) {
					timer.start();

					Collection<Trip> futureTrips = new LinkedList<>();
					for(Trip trip : route.getAssociatedTrips().values()) {
						if(trip.isFutureTrip()) {
							futureTrips.add(trip);
						}
					}

					listMode.setText("FUTURE TRIPS ON ROUTE: " + route.getID());
					listModel.listCollection(futureTrips);

					rightStatus.setText("Listing all future trips on route " + route.getID());
					leftStatus.setText(timer.stop());
				} else {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("That route ID is invalid");
					alert.showAndWait();
				}

			} else {
				rightStatus.setText("No files loaded");
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("No files loaded. Nothing to list.");
				alert.showAndWait();
			}
		}
	}

	/**
	 * Lists routes that contain certain stop in listModel
	 */
	public void listRoutesContainingStop() {
		//if not text enter do nothing
		if (!stopText.getText().equals("")) {

			//if files not loaded alert
			if (filesLoaded) {

				Stop stop = stops.getStop(stopText.getText());

				//if stop is not found alert user
				if (stop != null) {
					timer.start();

					listMode.setText("ALL ROUTES WITH STOP: " + stop.getID());
					listModel.listCollection(stop.getAssociatedRoutes().values());

					rightStatus.setText("Listing all routes that contain stop " + stop.getID());
					leftStatus.setText(timer.stop());
				} else {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("That stop ID is invalid");
					alert.showAndWait();
				}

			} else {
				rightStatus.setText("No files loaded");
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("No files loaded. Nothing to list.");
				alert.showAndWait();
			}
		}
	}

	public void listFutureTripsOnStop() {
		//if not text enter do nothing
		if (!stopText.getText().equals("")) {

			//if files not loaded alert
			if (filesLoaded) {

				Stop stop = stops.getStop(stopText.getText());

				//if stop is not found alert user
				if (stop != null) {
					timer.start();

					Iterator<Trip> iter = stop.getAssociatedTrips().values().iterator();
					Map<Time, Trip> sortedTrips = new TreeMap<Time, Trip>();
					Trip trip;

					while(iter.hasNext()) {
						trip = iter.next();

						if(trip.isFutureTrip()) {
							sortedTrips.put(trip.getStartTime(), trip);
						}
					}

					listMode.setText("FUTURE TRIPS ON STOP: " + stop.getID());
					listModel.listCollection(sortedTrips.values());

					rightStatus.setText("Listing all routes that contain stop " + stop.getID());
					leftStatus.setText(timer.stop());
				} else {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("That stop ID is invalid");
					alert.showAndWait();
				}

			} else {
				rightStatus.setText("No files loaded");
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("No files loaded. Nothing to list.");
				alert.showAndWait();
			}
		}
	}

	/*******************************************************************************************
	 * EXPORT
	 *******************************************************************************************/

	/**
	 * Exports the files to a specified directory
	 *
	 * @author goreckinj, hueblergw
	 */
	public void exportFiles() {
		if (filesLoaded) {
			rightStatus.setText("Exporting files . . .");
			//choose the directory for GTFS files
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("GTFS File Selector");
			File directory = directoryChooser.showDialog(new Stage());
			//ensure all exports are possible then export
			if (directory != null) {
				if (stops.isReadyToExport() && trips.isReadyToExport() && routes.isReadyToExport()) {
					timer.start();
					stops.exportFile(directory);
					trips.exportFile(directory);
					routes.exportFiles(directory);

					//set status
					rightStatus.setText("Successful export to directory " + directory);
					leftStatus.setText(timer.stop());
				} else {
					//set status, no alert needed, export check should alert
					rightStatus.setText("Unsuccessful export");
				}
			} else {
				rightStatus.setText("Export cancelled");
			}
		} else {
			rightStatus.setText("No files loaded. Nothing to export");
		}
	}

	/*******************************************************************************************
	 * IMPORT
	 *******************************************************************************************/

	/**
	 * Called on import click from menuBar. Prompts for directory selection then finds the needed files
	 * in the directory. If the files exist then create the trip, route, and stop objects from those files.
	 * If the files aren't found then print a list of the files that aren't found.
	 */
	public void importFiles() throws IOException {
		rightStatus.setText("Importing files . . .");
		//choose the directory for GTFS files
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("GTFS File Selector");
		Stage dirStage = new Stage();
        dirStage.setX(ApplicationDriver.STAGE.getX());
        dirStage.setY(ApplicationDriver.STAGE.getY());
		File directory = directoryChooser.showDialog(dirStage);

		timer.start();
		if (directory != null) {
			//add the file references for the needed files to the directory location
			File stopsFile = new File(directory, "stops.txt");
			File stop_timesFile = new File(directory, "stop_times.txt");
			File tripsFile = new File(directory, "trips.txt");
			File routesFile = new File(directory, "routes.txt");

			//if all the files exist in the director create the objects
			if (filesExist(stopsFile, stop_timesFile, tripsFile, routesFile)) {

				//save subjects for now
				Trips tripsSave = trips;
				Stops stopsSave = stops;
				Routes routesSave = routes;

				trips = new Trips();
				stops = new Stops();
				routes = new Routes();

				//if all files loaded properly then set status and initialize views
				if (routes.loadFile(routesFile) && stops.loadFile(stopsFile) &&
						trips.loadFile(tripsFile, stop_timesFile, routes.getAllRoutes(), stops.getAllStops())) {

					//update right status to indicate success
					rightStatus.setText("Files loaded successfully");

					//initalize the views (possibly otherthings later on)
					initializeViews();

					//set files loaded to true;
					filesLoaded = true;

					//set stops text field auto completion to stop ids
					if(stopSuggestions == null) {
						stopSuggestions = SuggestionProvider.create(stops.getAllStops().values());
					} else {
						stopSuggestions.clearSuggestions();
						stopSuggestions = SuggestionProvider.create(stops.getAllStops().values());
					}
					new AutoCompletionTextFieldBinding(stopText, stopSuggestions);

					//set routes text field auto completion to route ids
					if(routeSuggestions == null) {
						routeSuggestions = SuggestionProvider.create(routes.getAllRoutes().values());
					} else {
						routeSuggestions.clearSuggestions();
						routeSuggestions = SuggestionProvider.create(routes.getAllRoutes().values());
					}
					new AutoCompletionTextFieldBinding(routeText, routeSuggestions);

				}
				//if they didnt load properly then restore subjects from saves
				else {
					trips = tripsSave;
					stops = stopsSave;
					routes = routesSave;
					rightStatus.setText("File import unsuccessful. Reverting to previous state");
				}
			}
			//if any of the files dont exist then print which ones are missing as alert
			else {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("The following necessary files were not found");

				if (!stopsFile.exists()) {
					alert.setContentText("stops.txt\n");
				}

				if (!stop_timesFile.exists()) {
					alert.setContentText(alert.getContentText() + "stop_times.txt\n");
				}

				if (!tripsFile.exists()) {
					alert.setContentText(alert.getContentText() + "trips.txt\n");
				}

				if (!routesFile.exists()) {
					alert.setContentText(alert.getContentText() + "routes.txt\n");
				}

				alert.showAndWait();

				//update right status to indicate error
				rightStatus.setText("Unsuccessful file load: files not found in directory " + directory);
			}
		} else {
			rightStatus.setText("Import cancelled");
		}
		leftStatus.setText(timer.stop());
	}

	/*******************************************************************************************
	 * VALIDITY CHECKERS
	 *******************************************************************************************/
	/**
	 * Check if a route with a route id exists
	 * @param routeID - route id to check
	 * @return - true if the route exists
	 */
	public boolean validateRouteID(String routeID){
		return routes.getRoute(routeID) != null;
	}

	/**
	 * Check if a stop with a stop id exists
	 * @param stringID - stop id to check
	 * @return - true if the stop exists
	 */
	public boolean validateStopID(String stringID){
		return stops.getStop(stringID) != null;
	}

	/**
	 * Ensure all files exist
	 * @param files - arbitrary array to check
	 * @return - returns true if all files exist
	 */
	public boolean filesExist(File... files) {
		for (File file : files) {
			if (!file.exists()) {
				return false;
			}
		}

		return true;
	}

	/*******************************************************************************************
	 * EXIT
	 *******************************************************************************************/
	public void exit() {
		Platform.exit();
	}

}