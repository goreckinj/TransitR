package GroupR;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
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
 * MapView class that acts as a JavaFX GUI Canvas to display stops, routes and
 * trips visually to a user
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:31
 */
public class MapView implements Observer, DirectionsServiceCallback {

	private GoogleMap map;
	private GoogleMapView mapView;
	private MapOptions mapOptions;
	private MarkerOptions markerOptions;
	private Stops stops;
	private DirectionsService directionsService;
	private DirectionsPane directionsPane;
	private Collection<Stop> stopsDisplayed;
	private Stop selectedStop;

	/**
	 * Default constructor for the mapview class
	 */
	//public MapView(Pane displayPane, WebView webView){
	public MapView(GoogleMapView mapView, Stops stops, Label stopSelectInfoLabel, Label stopSelectedLabel){
		stopSelectInfoLabel.setVisible(true);
		stopSelectedLabel.setVisible(true);
	    this.mapView = mapView;
	    this.stops = stops;
	    selectedStop = null;
//	    ArrayList<Stop> stopList = (ArrayList<Stop>)(stops.getAllStops().values());
//	    Stop firstStop = stopList.get(0);
//	    double firstLat = firstStop.getLatitude();
//	    double firstLon = firstStop.getLongitude();
		Collection<Stop> stopCollection = stops.getAllStops().values();
		Stop firstStop = stopCollection.iterator().next();
		double firstLat = firstStop.getLatitude();
		double firstLon = firstStop.getLongitude();
		//System.out.println(stops.getAllStops().values());
		LatLong location1 = new LatLong(firstLat,firstLon);
		mapOptions = new MapOptions();
		markerOptions = new MarkerOptions();
		mapOptions.center(location1)
				.mapType(MapTypeIdEnum.ROADMAP)
				.overviewMapControl(false)
				.panControl(false)
				.rotateControl(false)
				.scaleControl(false)
				.streetViewControl(false)
				.zoomControl(false)
				.zoom(12);
		stopSelectInfoLabel.setText("Right Click A Stop To Select It.");
		stopSelectedLabel.setText("Stop: None Selected");
		map = mapView.createMap(mapOptions);
		map.addMouseEventHandler(UIEventType.rightclick, (GMapMouseEvent event) -> clickStop(event, stopSelectInfoLabel, stopSelectedLabel));
		map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> placeStop(event, stopSelectInfoLabel, stopSelectedLabel));
		directionsService = new DirectionsService();
		directionsPane = mapView.getDirec();
	}
	public void plotRoute(String departure, String destination, String transportOption){
		map.clearMarkers();
		switch (transportOption){
			case "Transit":
				DirectionsRequest transitRequest = new DirectionsRequest(departure, destination, TravelModes.TRANSIT);
				directionsService.getRoute(transitRequest, this, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
				break;
			case "Walking":
				DirectionsRequest walkingRequest = new DirectionsRequest(departure, destination, TravelModes.WALKING);
				directionsService.getRoute(walkingRequest, this, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
				break;
			case "Driving":
				DirectionsRequest drivingRequest = new DirectionsRequest(departure, destination, TravelModes.WALKING);
				directionsService.getRoute(drivingRequest, this, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
				break;
			case "Bicycling":
				DirectionsRequest bicyclingRequest = new DirectionsRequest(departure, destination, TravelModes.WALKING);
				directionsService.getRoute(bicyclingRequest, this, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
				break;
		}
	}
	public void plotRouteBetweenTwoStop(String departureCoor, String destinationCoor){
		DirectionsRequest transitRequest = new DirectionsRequest(departureCoor, destinationCoor, TravelModes.TRANSIT);
		directionsService.getRoute(transitRequest, this, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
	}
	public String convertCoordinate(double lat, double lon){
		String coordinate = Double.toString(lat) + "," + Double.toString(lon);
		return coordinate;
	}

	/**
	 * Plots the current routes. Must have stops plotted first. Returns true if
	 * plotted successfully
	 * 
	 * @param routes
	 * @param trips
	 */
	public boolean plotCurrentRoutes(Collection<Route> routes, Collection<Trip> trips){
		return false;
	}

	/**
	 * Plots the specified route. Must have stops plotted first. Returns true if
	 * plotted successfully
	 *
     * @author Nick
	 * @param route
	 */
	public boolean plotRoute(Route route){
        /*DirectionsService directionsService = new DirectionsService();
        DirectionsPane directionsPane = mapView.getDirec();
        DirectionsRequest request = new DirectionsRequest("Milwaukee", "Madison", TravelModes.DRIVING);
        directionsService.getRoute(request, new DirectionsServiceCallback() {
            @Override
            public void directionsReceived(DirectionsResult directionsResult, DirectionStatus directionStatus) {

            }
        }, new DirectionsRenderer(true, mapView.getMap(), directionsPane));
        */

	    return true;
	}
	public boolean plotRoutesAmongStops(Collection<Stop> stops){
		LinkedList<String> coordinates = new LinkedList<>();
		map.clearMarkers();
		Iterator<Stop> stopIter = stops.iterator();
		stopsDisplayed  = stops;
		while(stopIter.hasNext()){
			Stop stopObj = stopIter.next();
			LatLong location = new LatLong(stopObj.getLatitude(), stopObj.getLongitude());
			String curCoordinate = convertCoordinate(stopObj.getLatitude(),stopObj.getLongitude());
			coordinates.add(curCoordinate);
			markerOptions.position(location);
			Marker marker = new Marker(markerOptions);
			map.addMarker(marker);
		}
		//loop coordinate list and plot route
		for(int i= 0; i<coordinates.size()-1; i++){
			String departureCoor = coordinates.get(i);
			String destinationCoor = coordinates.get(i+1);
			plotRouteBetweenTwoStop(departureCoor,destinationCoor);
		}
		return true;
	}

	/**
	 * Plots the stops on the map. Returns true if plotted successfully
	 *
     * @author: Gavin, Nick
	 * @param stops
	 */
	public boolean plotStops(Collection<Stop> stops){
	    map.clearMarkers();
		Iterator<Stop> stopIter = stops.iterator();
		stopsDisplayed  = stops;
		while(stopIter.hasNext()){
			Stop stopObj = stopIter.next();
			LatLong location = new LatLong(stopObj.getLatitude(), stopObj.getLongitude());
			markerOptions.position(location);
			Marker marker = new Marker(markerOptions);
			map.addMarker(marker);
		}
		return true;
	}

	/**
	 * Updates the observers display based on the data
	 * @author goreckinj
	 */
	public void update(String changedData){
		if(stopsDisplayed != null && changedData.matches("loc")) {
			plotStops(stopsDisplayed);
		}
	}

	@Override
	public void directionsReceived(DirectionsResult results, DirectionStatus status) {

	}

	private void clickStop(GMapMouseEvent e, Label info, Label stinfo) {
		LatLong mouse = e.getLatLong();
		//Select Stop
		Stop stop = findSelectedStop(mouse, info, stinfo);
		if(stop != null) {
			selectedStop = stop;
			info.setText("To Place: Left Click, To Deselect: Right Click");
			stinfo.setText("Stop: " + stop.getID());
		} else if(selectedStop != null) {
			selectedStop = null;
			info.setText("Stop Deselected. Right Click A Stop To Select It.");
			stinfo.setText("Stop: " + selectedStop.getID());
		} else {
			info.setText("Right Click A Stop To Select It.");
			stinfo.setText("Stop: None Selected");
		}
	}

	private void placeStop(GMapMouseEvent e, Label info, Label stinfo) {
		if(selectedStop != null) {
			DecimalFormat decimal = new DecimalFormat("#.###");
			LatLong mouse = e.getLatLong();
			info.setText("Stop Moved To Lat: " + decimal.format(mouse.getLatitude()) + " Lon: " + decimal.format(mouse.getLongitude()));
			stinfo.setText("Stop: " + selectedStop.getID());
			selectedStop.setLongitude(mouse.getLongitude());
			selectedStop.setLatitude(mouse.getLatitude());
			selectedStop = null;
		} else {
			info.setText("Right Click A Stop To Select It.");
			stinfo.setText("Stop: None Selected");
		}
	}

	private Stop findSelectedStop(LatLong mouse, Label info, Label stinfo) {
		//check in bounds mouse +-0.0015
		double boundsLeft = mouse.getLongitude() - 0.0015;
		double boundsRight = mouse.getLongitude() + 0.0015;
		double boundsTop = mouse.getLatitude() + 0.0015;
		double boundsBottom = mouse.getLatitude() - 0.0015;
		if (stopsDisplayed != null && stopsDisplayed.size() != 0) {
			for (Stop stop : stopsDisplayed) {
				if (stop.getLongitude() >= boundsLeft && stop.getLongitude() <= boundsRight
						&& stop.getLatitude() >= boundsBottom && stop.getLatitude() <= boundsTop) {
					return stop;
				}
			}
		}
		return null;
	}
}