package GroupR;

/**
 *  * Copyright [2017] [Qiuhao Jin, Nick Gorecki, Gunther Huebler]
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
 * TripsTest that tests if trips import is properly checked
 * @author goreckinj, jinq, hueblerg
 * @version 2.0
 * @created 17-Oct-2017 17:38:00
 */

import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class TripsTest {
    File emptyT;
    File emptyST;
    Trips trips;

    /**
     * Initializes variables
     * @author: goreckinj
     */
    @BeforeEach
    void startUp() {
        emptyT = new File("trips.txt");
        emptyST = new File("stop_times.txt");
        try {
            emptyT.createNewFile();
            emptyST.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }
        trips = new Trips();
    }

    /**
     * Tests if files are properly checked when loading
     * @author: goreckinj
     */
    @Test
    void loadFile() {
        //load an null file
        assertFalse(trips.loadFile(null, emptyST, null, null));
        assertFalse(trips.loadFile(emptyT, null, null, null));

        //load empty files
        assertTrue(trips.emptyFile(emptyT));
        assertTrue(trips.emptyFile(emptyST));
    }

    /**
     * Checks if headers are properly compared
     * @author: goreckinj
     */
    @Test
    void isValidHeader() {
        String tripsHeader = "route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id";
        String stopTimesHeader = "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type";
        //check trips header comparison
        assertTrue(trips.isValidHeader(tripsHeader, "route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id"));
        //check stoptimes header comparison
        assertTrue(trips.isValidHeader(stopTimesHeader, "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type"));
        //check comparing two different headers
        assertFalse(trips.isValidHeader(tripsHeader, stopTimesHeader));
    }

    /**
     * Checks if ids are properly compared
     * @author: goreckinj
     */
    @Test
    void isValidID() {
        //check if ID's can be compared and found
        Map<String, String> ids = new TreeMap<>();
        for(int i = 1; i <= 10; i++) {
            ids.put("route_" + i, "route_" + i);
        }
        //Check if valid ID can be found
        assertTrue(trips.isValidID("route_3", ids));
        //check if invalid route id cannot be found
        assertFalse(trips.isValidID("route_15", ids));
    }

    /**
     * Checks if the number of elements are properly compared
     * @author: goreckinj
     */
    @Test
    void numElementsValid() {
        String s = "1,2,3,4,5,6,7";
        String attributes[] = s.split(",");
        //check if num elements agree
        assertTrue(trips.numElementsValid(attributes.length, 7));
        //check if num elements do not agree
        assertFalse(trips.numElementsValid(attributes.length, 6));
    }

    /**
     * Tests if times are properly checked for validation
     * @author: goreckinj
     */
    @Test
    void isValidTime() {
        //check if time is valid military time
        assertTrue(trips.isValidTime("23:09:01"));
        //check if time is not valid military time
        assertFalse(trips.isValidTime("ABCDE"));
        assertFalse(trips.isValidTime("255:09:01"));
        assertFalse(trips.isValidTime("25:095:01"));
        assertFalse(trips.isValidTime("25:09:015"));
        assertFalse(trips.isValidTime("14:100:01"));
    }

    /**
     * Checks the sequences and makes sure the stoptimes are in order
     * @author goreckinj
     */
    @Test
    void checkSequences() {
        Trip trip1 = new Trip(new Route("route_1", "","", "", Color.RED), "trip_1");
        Trip trip2 = new Trip(new Route("route_1", "","", "", Color.RED), "trip_2");

        for(int i = 0; i <= 10; i++) {

            trip1.addStopTime("10:10:10", "10:10:10", new Stop("stop_" + i,"","",0,0), i);
        }
        for(int i = 0; i <= 10; i += 2) {
            trip2.addStopTime("10:10:10", "10:10:10", new Stop("stop_" + i,"","",0,0), i);
        }

        //check for proper sequences
        //assertTrue(trips.validSequence(trip1, null));
        //check for improper sequences
        //assertFalse(trips.validSequence(trip2, null));
    }

    /**
     * Tears down the files when done
     * @author: goreckinj
     */
    @AfterEach
    void tearDown() {
        emptyT.delete();
        emptyST.delete();
    }

}