package GroupR;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Time;
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
 * ElementController class that acts as a JavaFX GUI Data Manipulator to edit
 * information of stop, route, stop time and trip.
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:31
 */

public class ElementController implements Observer {

    /*******************************************************************************************
     * DATA OBJECTS
     *******************************************************************************************/

    private Controller mainController;
    private Collection<?> currentCollection;
    private Stage elementStage;
    private Stack<Object> prevData;
    private Stack<Object> nextData;
    private Object data;
    private Routes routes;
    private Trips trips;
    private Stops stops;

    /*******************************************************************************************
     * GUI OBJECTS
     *******************************************************************************************/

    @FXML
    private Label dataLabel1, dataLabel2, dataLabel3, dataLabel4, dataLabel5, dataLabel, elementsLabel;
    @FXML
    private TextField dataField1, dataField2, dataField3, dataField4, dataField5;
    @FXML
    private Button backButton, forwardButton, applyButton;
    @FXML
    private Circle indicator1, indicator2, indicator3, indicator4, indicator5;
    @FXML
    private Tooltip prevDataToolTip, nextDataToolTip, applyToolTip;
    @FXML
    private SplitPane dataPane1, dataPane2, dataPane3, dataPane4, dataPane5;
    @FXML
    private javafx.scene.control.ListView elementsList;

    /*******************************************************************************************
     * CONSTRUCTORS
     *******************************************************************************************/

    /**
     * Default constructor
     * @author goreckinj
     */
    public ElementController() {
        this.elementsList = new javafx.scene.control.ListView<>();
        this.elementsList.setOrientation(Orientation.VERTICAL);
        prevData = new Stack<>();
        nextData = new Stack<>();
    }

    /*******************************************************************************************
     * FUNCTIONS
     *******************************************************************************************/

    /**
     * Sets the main controller of this
     * @author goreckinj
     * @param mainController - The main controller that the GUI is called from
     * @return this
     */
    public ElementController setMainController(Controller mainController) {
        this.mainController = mainController;
        return this;
    }

    /**
     * Sets the data references for Object data to compare to
     *@author goreckinj
     * @param routes - Routes reference
     * @param trips - Trips reference
     * @param stops - Stops reference
     * @return
     */
    public ElementController setReferences(Routes routes, Trips trips, Stops stops) {
        this.routes = routes;
        this.trips = trips;
        this.stops = stops;
        return this;
    }

    /**
     * Initializes the controller. Must call method before any other first
     * @author goreckinj
     * @return this
     */
    public ElementController initialize() {
        indicator1.setFill(Color.LIME);
        indicator2.setFill(Color.LIME);
        indicator3.setFill(Color.LIME);
        indicator4.setFill(Color.LIME);
        indicator5.setFill(Color.LIME);
        dataChangeListeners();
        prevData.clear();
        nextData.clear();
        this.data = null;
        backButton.setDisable(true);
        forwardButton.setDisable(true);
        this.elementsList.setOnMouseClicked((MouseEvent e) -> {
            //initialize data in GUI
            if (elementsList.getItems().size() != 0) {
                String mode = elementsLabel.getText().toLowerCase().split(" ")[1].toLowerCase();
                String id = ((String) elementsList.getSelectionModel().getSelectedItem());
                if (id != null) {
                    id = id.split(" ")[0];
                    if (data instanceof Route) {
                        Route route = (Route) data;
                        Iterator<Stop> stops = route.getAssociatedStops().values().iterator();
                        while(stops.hasNext()) {
                            Stop stop = stops.next();
                            if(stop.getID().equals(id)) {
                                setData(stop);
                            }
                        }
                        fillData(true);
                    } else if (data instanceof Trip) {
                        Trip trip = (Trip) data;
                        LinkedList<StopTime> stopTimes = (LinkedList<StopTime>) trip.getAllStopTimes();
                        for (int i = 0; i < stopTimes.size(); i++) {
                            if (Integer.toString(stopTimes.get(i).getSequence()).equals(id)) {
                                setData(stopTimes.get(i));
                            }
                        }
                        fillData(true);
                    } else if (data instanceof Stop) {
                        Stop stop = (Stop) data;
                        Iterator<Trip> trips = stop.getAssociatedTrips().values().iterator();
                        while(trips.hasNext()) {
                            Trip trip = trips.next();
                            if(trip.getTripID().equals(id)) {
                                setData(trip);
                            }
                        }
                        fillData(true);
                    } else if (data instanceof StopTime) {
                        StopTime stopTime = (StopTime) data;
                        setData(stopTime.getStop());
                        fillData(true);
                    }
                }
            }
        });
        return this;
    }

    /**
     * Fills in the data based off Object data. Must call setData method before this one
     * @author goreckinj
     * @param mode - Either fill data in or clear data
     * @return this
     */
    public ElementController fillData(boolean mode) {
        if(data == null) {
            throw new NullPointerException("Must call setData(Object data) function first. Data reference is null.");
        }
        //set data
        if(mode) {
            if (data instanceof Route) {
                Route route = (Route) data;
                dataLabel.setText("Route: " + route.getID());
                setVisible(dataPane1, true);
                dataLabel1.setText("Route ID");
                dataField1.setText(route.getID());
                setVisible(dataPane2, true);
                dataLabel2.setText("Long Name");
                dataField2.setText(route.getLongName());
                setVisible(dataPane3, true);
                dataLabel3.setText("Short Name");
                dataField3.setText(route.getShortName());
                setVisible(dataPane4, true);
                dataLabel4.setText("Description");
                dataField4.setText(route.getDescription());
                setVisible(dataPane5, true);
                dataLabel5.setText("Color");
                dataField5.setText(route.getColor().toString().substring(2, 8));
                elementsLabel.setText("Associated Stops");
                /*ObservableList<String> writer = elementsList.getItems();
                writer.clear();
                Iterator<Stop> stops = route.getAssociatedStops().values().iterator();
                while(stops.hasNext()) {
                    Stop stop = stops.next();
                    writer.add(stop.getID());
                }*/
                listCollection(route.getAssociatedStops().values());
            } else if (data instanceof Trip) {
                Trip trip = (Trip) data;
                dataLabel.setText("Trip: " + trip.getTripID());
                setVisible(dataPane1, true);
                dataLabel1.setText("Trip ID");
                dataField1.setText(trip.getTripID());
                setVisible(dataPane2, true);
                dataLabel2.setText("Route ID");
                dataField2.setText(trip.getRoute().getID());
                setVisible(dataPane3, false);
                dataLabel3.setText("");
                dataField3.setText("");
                setVisible(dataPane4, false);
                dataLabel4.setText("");
                dataField4.setText("");
                setVisible(dataPane5, false);
                dataLabel5.setText("");
                dataField5.setText("");
                elementsLabel.setText("Associated StopTimes");
                ObservableList<String> writer = elementsList.getItems();
                writer.clear();
                Collection<?> stopTimes = trip.getAllStopTimes();
                /*while(stopTimes.hasNext()) {
                    StopTime stopTime = stopTimes.next();
                    writer.add(Integer.toString(stopTime.getSequence()) + " Stop: " + stopTime.getStop().getID()
                        + "\n\tArrival: " + stopTime.getArrivalTime() + "\tDepart: " + stopTime.getDepartTime());
                }*/
                listCollection(stopTimes);
            } else if (data instanceof Stop) {
                Stop stop = (Stop) data;
                dataLabel.setText("Stop: " + stop.getID());
                setVisible(dataPane1, true);
                dataLabel1.setText("Stop ID");
                dataField1.setText(stop.getID());
                setVisible(dataPane2, true);
                dataLabel2.setText("Name");
                dataField2.setText(stop.getName());
                setVisible(dataPane3, true);
                dataLabel3.setText("Description");
                dataField3.setText(stop.getDescription());
                setVisible(dataPane4, true);
                dataLabel4.setText("Longitude");
                dataField4.setText(Double.toString(stop.getLongitude()));
                setVisible(dataPane5, true);
                dataLabel5.setText("Latitude");
                dataField5.setText(Double.toString(stop.getLatitude()));
                elementsLabel.setText("Associated Trips");
                /*ObservableList<String> writer = elementsList.getItems();
                writer.clear();
                Iterator<Trip> trips = stop.getAssociatedTrips().values().iterator();
                while(trips.hasNext()) {
                    Trip trip = trips.next();
                    writer.add(trip.getTripID());
                }*/
                listCollection(stop.getAssociatedTrips().values());
            } else if(data instanceof StopTime) {
                StopTime stopTime = (StopTime) data;
                dataLabel.setText("StopTime: " + stopTime.getSequence());
                setVisible(dataPane1, true);
                dataLabel1.setText("Trip ID");
                dataField1.setText(stopTime.getTrip().getTripID());
                setVisible(dataPane2, true);
                dataLabel2.setText("Arrival Time");
                dataField2.setText(stopTime.getArrivalTime().toString());
                setVisible(dataPane3, true);
                dataLabel3.setText("Departure Time");
                dataField3.setText(stopTime.getDepartTime().toString());
                setVisible(dataPane4, true);
                dataLabel4.setText("Stop ID");
                dataField4.setText(stopTime.getStop().getID());
                setVisible(dataPane5, false);
                dataLabel5.setText("");
                dataField5.setText("");
                elementsLabel.setText("Associated Stops");
                /*ObservableList<String> writer = elementsList.getItems();
                writer.clear();
                writer.add(stopTime.getStop().getID());*/
                Collection<Stop> stops = new ArrayList<>();
                stops.add(stopTime.getStop());
                listCollection(stops);
            } else {
                throw new IllegalArgumentException("Data to load is invalid: Must be a route, trip, stop or stop time.");
            }
        //clear data
        } else {
            this.data = null;
        }
        return this;
    }

    /**
     * Sets the data reference that the ElementController is listing. Must call before fillData(mode)
     * @author goreckinj
     * @param data The data to be referenced to.
     * @return this
     */
    public ElementController setData(Object data) {
        if(this.data != null) {
            prevData.push(this.data);
        }
        this.data = data;
        if(backButton.isDisabled() && prevData.size() >= 1) {
            backButton.setDisable(false);
        }
        return this;
    }

    /**
     * Sets the stage reference of this
     * @param elementStage - The stage assigned for this to be displayed on
     * @return this
     */
    public ElementController setStage(Stage elementStage) {
        this.elementStage = elementStage;
        return this;
    }

    public void listCollection(Collection<?> currentCollection) {
        if(currentCollection != null && currentCollection.size() != 0) {
            this.currentCollection = currentCollection;
            ObservableList<String> writer = elementsList.getItems();
            writer.clear();
            Iterator<?> iter = currentCollection.iterator();
            while(iter.hasNext()) {
                Object o = iter.next();
                if(o instanceof Trip) {
                    Trip trip = (Trip) o;
                    writer.add(trip.getTripID());
                }else if(o instanceof Stop) {
                    Stop stop = (Stop) o;
                    writer.add(stop.getID());
                }else if(o instanceof StopTime) {
                    StopTime stopTime = (StopTime) o;
                    writer.add(Integer.toString(stopTime.getSequence()) + " Stop: " + stopTime.getStop().getID()
                            + "\n\tArrival: " + stopTime.getArrivalTime() + "\tDepart: " + stopTime.getDepartTime());
                }
            }
        }
    }

    /*******************************************************************************************
     * ON ACTIONS
     *******************************************************************************************/

    /**
     * Pushes to nextData and Pops from prevData stacks
     * @author goreckinj
     */
    public void prevData() {
        if(prevData.size() != 0) {
            Object data = prevData.pop();
            nextData.push(this.data);
            this.data = data;
            fillData(true);
        }
        if(!backButton.isDisabled() && prevData.size() == 0) {
            backButton.setDisable(true);
        }
        if(forwardButton.isDisabled()) {
            forwardButton.setDisable(false);
        }
    }

    /**
     * Pushes to prevData and Pops from nextData stacks
     * @author goreckinj
     */
    public void nextData() {
        if(nextData.size() != 0) {
            Object data = nextData.pop();
            prevData.push(this.data);
            this.data = data;
            fillData(true);
        }
        if(!forwardButton.isDisabled() && nextData.size() == 0) {
            forwardButton.setDisable(true);
        }
        backButton.setDisable(false);
    }

    /**
     * Initializes the dataChanged listeners
     * @author goreckinj
     */
    public void dataChangeListeners() {
        dataField1.textProperty().addListener(event -> dataChanged1());
        dataField2.textProperty().addListener(event -> dataChanged2());
        dataField3.textProperty().addListener(event -> dataChanged3());
        dataField4.textProperty().addListener(event -> dataChanged4());
        dataField5.textProperty().addListener(event -> dataChanged5());
        dataField1.setOnAction(event -> resetField(1));
        dataField2.setOnAction(event -> resetField(2));
        dataField3.setOnAction(event -> resetField(3));
        dataField4.setOnAction(event -> resetField(4));
        dataField5.setOnAction(event -> resetField(5));

    }

    /**
     * Checks if dataField1 is changed
     * @author goreckinj
     */
    public void dataChanged1() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateIndicator(indicator1, route.getID(), dataField1);
        } else if(data instanceof Trip) {
            Trip trip = (Trip) data;
            updateIndicator(indicator1, trip.getTripID(), dataField1);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateIndicator(indicator1, stop.getID(), dataField1);
        } else if(data instanceof StopTime) {
            StopTime stopTime = (StopTime) data;
            updateIndicator(indicator1, stopTime.getTrip().getTripID(), dataField1);
        }
        updateApplyButton();
    }

    /**
     * Checks if dataField2 is changed
     * @author goreckinj
     */
    public void dataChanged2() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateIndicator(indicator2, route.getLongName(), dataField2);
        } else if(data instanceof Trip) {
            Trip trip = (Trip) data;
            updateIndicator(indicator2, trip.getRoute().getID(), dataField2);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateIndicator(indicator2, stop.getName(), dataField2);
        } else if(data instanceof StopTime) {
            StopTime stopTime = (StopTime) data;
            updateIndicator(indicator2, stopTime.getArrivalTime().toString(), dataField2);
        }
        updateApplyButton();
    }

    /**
     * Checks if dataField3 is changed
     * @author goreckinj
     */
    public void dataChanged3() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateIndicator(indicator3, route.getShortName(), dataField3);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateIndicator(indicator3, stop.getDescription(), dataField3);
        } else if(data instanceof StopTime) {
            StopTime stopTime = (StopTime) data;
            updateIndicator(indicator3, stopTime.getDepartTime().toString(), dataField3);
        }
        updateApplyButton();
    }

    /**
     * Checks if dataField4 is changed
     * @author goreckinj
     */
    public void dataChanged4() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateIndicator(indicator4, route.getDescription(), dataField4);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateIndicator(indicator4, Double.toString(stop.getLongitude()), dataField4);
        } else if(data instanceof StopTime) {
            StopTime stopTime = (StopTime) data;
            updateIndicator(indicator4, stopTime.getStop().getID(), dataField4);
        }
        updateApplyButton();
    }

    /**
     * Checks if dataField5 is changed
     * @author goreckinj
     */
    public void dataChanged5() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateIndicator(indicator5, route.getColor().toString().substring(2, 8), dataField5);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateIndicator(indicator5, Double.toString(stop.getLatitude()), dataField5);
        }
        updateApplyButton();
    }

    /**
     * onAction for applyButton
     * @author goreckinj
     */
    public void apply() {
        if(data instanceof Route) {
            Route route = (Route) data;
            updateRoute(route);
        } else if(data instanceof Trip) {
            Trip trip = (Trip) data;
            updateTrip(trip);
        } else if(data instanceof Stop) {
            Stop stop = (Stop) data;
            updateStop(stop);
        } else if(data instanceof StopTime) {
            StopTime stopTime = (StopTime) data;
            updateStopTime(stopTime);
        }
        updateApplyButton();
    }

    /*******************************************************************************************
     * HELPER METHODS
     *******************************************************************************************/

    /**
     * Sets a dataPane visible based off if it is or not
     * @author goreckinj
     * @param dataPane - Data pane to set visible
     * @param mode
     */
    private void setVisible(SplitPane dataPane, boolean mode) {
        if (mode) {
            if(!dataPane.isVisible()) {
                dataPane.setVisible(true);
            }
        } else {
            if(dataPane.isVisible()) {
                dataPane.setVisible(false);
            }
        }
    }

    /**
     * Updates a specified indicator
     * @author goreckinj
     * @param indicator - The indicator to update
     * @param actual - Actual text
     * @param comparable - TextField to get compare text from
     */
    private void updateIndicator(Circle indicator, String actual, TextField comparable) {
        if (!actual.toUpperCase().equals(comparable.getText().toUpperCase())) {
            indicator.setFill(Color.DEEPSKYBLUE);
        } else {
            if (indicator.getFill() != Color.LIME) {
                indicator.setFill(Color.LIME);
            }
        }
    }

    /**
     * Resets a specified field
     * @param field - The field to reset
     * @author goreckinj
     */
    private void resetField(int field) {
        switch(field) {
            case 1:
                if(data instanceof Route) {
                    Route route = (Route) data;
                    resetData(indicator1, route.getID(), dataField1);
                } else if(data instanceof Trip) {
                    Trip trip = (Trip) data;
                    resetData(indicator1, trip.getTripID(), dataField1);
                } else if(data instanceof Stop) {
                    Stop stop = (Stop) data;
                    resetData(indicator1, stop.getID(), dataField1);
                } else if(data instanceof StopTime) {
                    StopTime stopTime = (StopTime) data;
                    resetData(indicator1, stopTime.getTrip().getTripID(), dataField1);
                }
                break;
            case 2:
                if(data instanceof Route) {
                    Route route = (Route) data;
                    resetData(indicator2, route.getLongName(), dataField2);
                } else if(data instanceof Trip) {
                    Trip trip = (Trip) data;
                    resetData(indicator2, trip.getRoute().getID(), dataField2);
                } else if(data instanceof Stop) {
                    Stop stop = (Stop) data;
                    resetData(indicator2, stop.getName(), dataField2);
                } else if(data instanceof StopTime) {
                    StopTime stopTime = (StopTime) data;
                    resetData(indicator2, stopTime.getArrivalTime().toString(), dataField2);
                }
                break;
            case 3:
                if(data instanceof Route) {
                    Route route = (Route) data;
                    resetData(indicator3, route.getShortName(), dataField3);
                } else if(data instanceof Stop) {
                    Stop stop = (Stop) data;
                    resetData(indicator3, stop.getDescription(), dataField3);
                } else if(data instanceof StopTime) {
                    StopTime stopTime = (StopTime) data;
                    resetData(indicator3, stopTime.getDepartTime().toString(), dataField3);
                }
                break;
            case 4:
                if(data instanceof Route) {
                    Route route = (Route) data;
                    resetData(indicator4, route.getDescription(), dataField4);
                } else if(data instanceof Stop) {
                    Stop stop = (Stop) data;
                    resetData(indicator4, Double.toString(stop.getLongitude()), dataField4);
                } else if(data instanceof StopTime) {
                    StopTime stopTime = (StopTime) data;
                    resetData(indicator4, stopTime.getStop().getID(), dataField4);
                }
                break;
            case 5:
                if(data instanceof Route) {
                    Route route = (Route) data;
                    resetData(indicator5, route.getColor().toString().substring(2, 8), dataField5);
                } else if(data instanceof Stop) {
                    Stop stop = (Stop) data;
                    resetData(indicator5, Double.toString(stop.getLatitude()), dataField5);
                }
                break;
            default:
                throw new IllegalArgumentException("Improper Field Entry.");

        }
    }

    /**
     * Resets the data specified
     * @author goreckinj
     * @param indicator - Indicator to reset
     * @param data - data to reset to
     * @param field - field to reset
     */
    private void resetData(Circle indicator, String data, TextField field) {
        if(field.getText().equals("")) {
            field.setText(data);
            if(indicator.getFill() != Color.LIME) {
                indicator.setFill(Color.LIME);
            }
        }
    }

    /**
     * Updates route information when apply is clicked
     * @author goreckinj
     * @param route - The route to update
     */
    private void updateRoute(Route route) {
        if(indicator1.getFill() != Color.LIME) {
            String id = dataField1.getText();
            if(id.equals("")) {
                dataField1.setText(route.getID());
                indicator1.setFill(Color.LIME);
            } else {
                if(routes.setRouteID(route.getID(), id.replaceAll("\\s+", "_"))) {
                    dataField1.setText(route.getID());
                    indicator1.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Route ID");
                    alert.setHeaderText("Route id could not be updated.");
                    alert.setContentText("Route id already exists.");
                    alert.showAndWait();
                }
            }
        }
        if(indicator2.getFill() != Color.LIME) {
            String longName = dataField2.getText();
            if(longName.equals("")) {
                dataField2.setText(route.getLongName());
            } else {
                route.setLongName(longName);
            }
            indicator2.setFill(Color.LIME);
        }
        if(indicator3.getFill() != Color.LIME) {
            String shortName = dataField3.getText();
            if(shortName.equals("")) {
                dataField3.setText(route.getShortName());
            } else {
                route.setShortName(shortName);
            }
            indicator3.setFill(Color.LIME);
        }
        if(indicator4.getFill() != Color.LIME) {
            String desc = dataField4.getText();
            if(desc .equals("")) {
                dataField4.setText(route.getDescription());
            } else {
                route.setDescription(desc);
            }
            indicator4.setFill(Color.LIME);
        }
        if(indicator5.getFill() != Color.LIME) {
            String color = dataField5.getText();
            if(color.equals("")) {
                dataField5.setText(route.getColor().toString().substring(2, 8));
                indicator5.setFill(Color.LIME);
            } else {
                String padding = "";
                for(int i = 0; i < 6 - color.length(); i++) {
                    padding += "0";
                }
                color = padding + color;
                if(routes.checkColor(color.toUpperCase())) {
                    route.setColor(color.toUpperCase());
                    dataField5.setText(route.getColor().toString().substring(2, 8));
                    indicator5.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Color");
                    alert.setHeaderText("Route color could not be updated.");
                    alert.setContentText("Color must match the constraints:\n\tCharacters [0-9][A-F]\n\tLength <= 6");
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Updates trip information when apply is clicked
     * @author goreckinj
     * @param trip - The trip to update
     */
    private void updateTrip(Trip trip) {
        if(indicator1.getFill() != Color.LIME) {
            String tripID = dataField1.getText();
            if(tripID.equals("")) {
                dataField1.setText(trip.getTripID());
                indicator1.setFill(Color.LIME);
            } else {
                if(trips.setTripID(trip.getTripID(), tripID.replaceAll("\\s+", "_"))) {
                    dataField1.setText(trip.getTripID());
                    indicator1.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Trip ID");
                    alert.setHeaderText("Trip id could not be updated.");
                    alert.setContentText("Trip id already exists.");
                    alert.showAndWait();
                }
            }
        }
        if(indicator2.getFill() != Color.LIME) {
            String routeID = dataField2.getText();
            if(routeID.equals("")) {
                dataField2.setText(trip.getRoute().getID());
                indicator2.setFill(Color.LIME);
            } else {
                if(routes.setRouteID(trip.getRoute().getID(), routeID.replaceAll("\\s+", "_"))) {
                    dataField2.setText(trip.getRoute().getID());
                    indicator2.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Route ID");
                    alert.setHeaderText("Route id could not be updated.");
                    alert.setContentText("Route id already exists.");
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Updates stop information when apply is clicked
     * @author goreckinj
     * @param stop - The stop to update
     */
    private void updateStop(Stop stop) {
        if(indicator1.getFill() != Color.LIME) {
            String id = dataField1.getText();
            if(id.equals("")) {
                dataField1.setText(stop.getID());
                indicator1.setFill(Color.LIME);
            } else {
                if(stops.setStopID(stop.getID(), id.replaceAll("\\s+", "_"))) {
                    dataField1.setText(stop.getID());
                    indicator1.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Stop ID");
                    alert.setHeaderText("Stop id could not be updated.");
                    alert.setContentText("Stop id already exists.");
                    alert.showAndWait();
                }
            }
        }
        if(indicator2.getFill() != Color.LIME) {
            String name = dataField2.getText();
            if(name.equals("")) {
                dataField2.setText(stop.getName());
            } else {
                stop.setName(name);
            }
            indicator2.setFill(Color.LIME);
        }
        if(indicator3.getFill() != Color.LIME) {
            String desc = dataField3.getText();
            if(desc.equals("")) {
                dataField3.setText(stop.getDescription());
            } else {
                stop.setDescription(desc);
            }
            indicator3.setFill(Color.LIME);
        }
        if(indicator4.getFill() != Color.LIME) {
            String longitude = dataField4.getText();
            if (longitude.equals("")) {
                dataField4.setText(Double.toString(stop.getLongitude()));
                indicator4.setFill(Color.LIME);
            } else {
                try {
                    Double lon = Double.parseDouble(longitude);
                    if(lon >= -180 && lon <= 180) {
                        stop.setLongitude(lon);
                        indicator4.setFill(Color.LIME);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Longitude");
                        alert.setHeaderText("Longitude could not be updated.");
                        alert.setContentText("Longitude must match the constraints:\n\tValues -180 <= x <= 180");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Longitude");
                    alert.setHeaderText("Longitude could not be updated.");
                    alert.setContentText("Longitude must be a valid number.");
                    alert.showAndWait();
                }
            }
        }
        if(indicator5.getFill() != Color.LIME) {
            String latitude = dataField5.getText();
            if (latitude.equals("")) {
                dataField5.setText(Double.toString(stop.getLongitude()));
                indicator5.setFill(Color.LIME);
            } else {
                try {
                    Double lat = Double.parseDouble(latitude);
                    if(lat >= -90 && lat <= 90) {
                        stop.setLongitude(lat);
                        indicator5.setFill(Color.LIME);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Latitude");
                        alert.setHeaderText("Latitude could not be updated.");
                        alert.setContentText("Latitude must match the constraints:\n\tValues -90 <= x <= 90");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Latitude");
                    alert.setHeaderText("Latitude could not be updated.");
                    alert.setContentText("Latitude must be a valid number.");
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Updates stopTime information when apply is clicked
     * @author goreckinj
     * @param stopTime - The stopTime to update
     */
    private void updateStopTime(StopTime stopTime) {
        if(indicator1.getFill() != Color.LIME) {
            String tripID = dataField1.getText();
            if(tripID.equals("")) {
                dataField1.setText(stopTime.getTrip().getTripID());
                indicator1.setFill(Color.LIME);
            } else {
                if(trips.setTripID(stopTime.getTrip().getTripID(), tripID.replaceAll("\\s+", "_"))) {
                    dataField1.setText(stopTime.getTrip().getTripID());
                    indicator1.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Trip ID");
                    alert.setHeaderText("Trip id could not be updated.");
                    alert.setContentText("Trip id already exists.");
                    alert.showAndWait();
                }
            }
        }
        if(indicator2.getFill() != Color.LIME) {
            String arrival = dataField2.getText();
            if(arrival.equals("")) {
                dataField2.setText(stopTime.getArrivalTime().toString());
                indicator2.setFill(Color.LIME);
            } else {
                if(trips.isValidTime(arrival)) {
                    String[] components = arrival.split(":");
                    int hr = Integer.parseInt(components[0]);
                    int min = Integer.parseInt(components[1]);
                    int sec = Integer.parseInt(components[2]);
                    stopTime.setArrival(new Time(hr, min, sec));
                    indicator2.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Arrival");
                    alert.setHeaderText("Arrival time could not be updated.");
                    alert.setContentText("Arrival time must be a valid military time:\n\tTime HH:MM:SS");
                    alert.showAndWait();
                }
            }
        }
        if(indicator3.getFill() != Color.LIME) {
            String depart = dataField3.getText();
            if(depart.equals("")) {
                dataField3.setText(stopTime.getDepartTime().toString());
                indicator3.setFill(Color.LIME);
            } else {
                if(trips.isValidTime(depart)) {
                    String[] components = depart.split(":");
                    int hr = Integer.parseInt(components[0]);
                    int min = Integer.parseInt(components[1]);
                    int sec = Integer.parseInt(components[2]);
                    stopTime.setDepart(new Time(hr, min, sec));
                    indicator3.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Departure");
                    alert.setHeaderText("Departure time could not be updated.");
                    alert.setContentText("Departure time must be a valid military time:\n\tTime HH:MM:SS");
                    alert.showAndWait();
                }
            }
        }
        if(indicator4.getFill() != Color.LIME) {
            String stopID = dataField4.getText();
            if(stopID.equals("")) {
                dataField4.setText(stopTime.getStop().getID());
                indicator4.setFill(Color.LIME);
            } else {
                if(stops.setStopID(stopTime.getStop().getID(), stopID.replaceAll("\\s+", "_"))) {
                    dataField4.setText(stopTime.getStop().getID());
                    indicator4.setFill(Color.LIME);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Stop ID");
                    alert.setHeaderText("Stop id could not be updated.");
                    alert.setContentText("Stop id already exists.");
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Updates the apply button based off the fill of the indicators
     * @author goreckinj
     */
    private void updateApplyButton() {
        if(indicator1.getFill() == Color.LIME && indicator2.getFill() == Color.LIME
                && indicator3.getFill() == Color.LIME && indicator4.getFill() == Color.LIME
                && indicator5.getFill() == Color.LIME) {
            applyButton.setDisable(true);
        } else {
            applyButton.setDisable(false);
        }
    }

    /**
     * Updates Observer based on data referenced.
     * @author hueblergw
     */
    public void update(String changedData) {
        if(currentCollection != null && changedData.matches("id")) {
            listCollection(currentCollection);
        }
    }
}
