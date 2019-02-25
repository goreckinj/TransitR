package GroupR;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.xml.bind.Element;
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
 * ListView class that acts as an JavaFX ScrollPane to display information on
 * stops, routes, trips, or stop times
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:31
 */
public class ListView implements Observer {

	private javafx.scene.control.ListView<String> list;
	private NavigableMap<String, Route> routes;
    private NavigableMap<String, Trip> trips;
    private NavigableMap<String, Stop> stops;
    private ElementController elementController;
    private Stage elementStage;
	private Collection<?> currentCollection;

	/**
	 * Default constructor for the listview class
	 *
	 * @author hueblergw
	 */
	public ListView(Label modeLabel, javafx.scene.control.ListView<String> list, Map<String, Route> routes, Map<String,
					Trip> trips, Map<String, Stop> stops, ElementController elementC, Stage elementS){
		list.getItems().clear();
		this.elementController = elementC;

		this.elementStage = elementS;
		elementController.setStage(elementStage);

		//initialize list features
		this.list = list;
		this.list.setOrientation(Orientation.VERTICAL);
		this.list.setOnMouseClicked((MouseEvent e) -> {

			if(list.getSelectionModel().getSelectedItem() != null && !list.getSelectionModel().getSelectedItem().equals("No items to display")) {
				//open GUI in correct location first
				elementStage.setX((ApplicationDriver.STAGE.getX() + (ApplicationDriver.SCENE_WIDTH)) + 14);
				elementStage.setY(ApplicationDriver.STAGE.getY());
				elementStage.setResizable(false);

				//initialize data in GUI
				if (list.getItems().size() != 0) {
					String mode = modeLabel.getText().toLowerCase().split(" ")[1].toLowerCase();
					String id = list.getSelectionModel().getSelectedItem();
					id = id.substring(0, id.indexOf("\n"));
					if (id != null) {
						elementInitialization(routes, trips, stops, mode, id);
					}
				}
			}
		});
	}

	/**
	 * Updates the observers display based on the data
	 * 
	 * @param data - The data to update the observer with
	 * @author goreckinj
	 */
	public void listCollection(Collection<?> data){
		list.getItems().clear();
		String text;
		Iterator<?> dataIter = data.iterator();
		Object obj;
		currentCollection = data;

		if(data.size() == 0) {
			list.getItems().add("No items to display");
		}else {
			while (dataIter.hasNext()) {
				obj = dataIter.next();
				if (obj instanceof Route) {
					Route route = (Route) obj;
					list.getItems().add(route.toString() + "\n\t" + route.getLongName()
							+ (route.getDescription() == "" ? "" : "\n\t") + route.getDescription());
				} else if (obj instanceof Stop) {
					Stop stop = (Stop) obj;
					list.getItems().add(stop.toString() + "\n\t" + stop.getName()
							+ (stop.getDescription().equals("") ? "" : "\n\t") + stop.getDescription());
				} else if (obj instanceof Trip) {
					Trip trip = (Trip) obj;
					list.getItems().add(trip.toString() + "\n\t"
							+ (trip.getStartTime() == null ? "Trip currently unavaible" : trip.getStartTime() + " - " + trip.getEndTime()));
				}
			}
		}
	}

	/*******************************************************************************************
	 * ELEMENT FUNCTIONALITY
	 *******************************************************************************************/

	/**
	 * Initializes the element selected to be displayed in Element.fxml
	 * @author goreckinj
	 * @param routes - The routes to compare data to
	 * @param trips - The trips to compare data to
	 * @param stops - The stops to compare data to
	 * @param mode - The mode the ListView is in
	 * @param id - The id selected
	 */
	private void elementInitialization(Map<String, Route> routes, Map<String, Trip> trips, Map<String, Stop> stops,
									   String mode, String id) {
		if (!elementStage.isShowing()) {
			elementStage.setTitle(id);
			elementStage.show();
		} else {
			if (id.equals(elementStage.getTitle())) {
				elementStage.hide();
				elementStage.setTitle(" ");
			} else {
				elementStage.setTitle(id);
			}
		}
		if (id != null) {
			switch (mode) {
				case "stops":
					Stop stop = stops.get(id);
					if (stop == null) {
						throw new NullPointerException("Stop " + id + " displayed in ListView could not be found");
					}
					elementController.initialize()
                            .setData(stop)
							.fillData(true);
					break;
				case "routes":
					Route route = routes.get(id);
					if (route == null) {
						throw new NullPointerException("Route " + id + " displayed in ListView could not be found");
					}
                    elementController.initialize()
                            .setData(route)
                            .fillData(true);
					break;
				case "trips":
					Trip trip = trips.get(id);
					if (trip == null) {
						throw new NullPointerException("Trip " + id + " displayed in ListView could not be found");
					}
                    elementController.initialize()
                            .setData(trip)
                            .fillData(true);
					break;
				default:
					throw new IllegalStateException("modeLabel for ApplicationDriver.fxml is improper. " +
							"Must have stops, trips or routes as second word in modeLabel.");
			}
		}
	}

	/**
	 * Updates Observer based on data referenced.
	 * @author hueblergw
	 */
	public void update(String changedData) {
		if(currentCollection != null && changedData.matches("id|name|desc|time")) {
			listCollection(currentCollection);
		}
	}
}