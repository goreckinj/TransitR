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
 * Carries out tests for the stops class. Tests for the following are performed:
 * loadFile() - main filing loading method, tested for null case
 * emptyFile() - empty file check, checks if a file is empty
 * properFileFormat() - checks if a file uses the correct format
 * properNumberOfAttributes() - makes sure a line of the file has the right number of attributes
 * validCoordinates() - makes sure the coordinates in every line are valid
 * @author hueblergw, jinq, goreckin
 */

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class StopsTest {

    Stops stops;
    File emptyFile;

    @BeforeEach
    public void startUp() {

        //initialize stops
       stops = new Stops();

       //define test file
       emptyFile = new File("test.txt");

       //create the test file
       try {
           emptyFile.createNewFile();
       }catch (IOException e) {
           e.printStackTrace();
       }
    }

    @Test
    void testForNullFile() {
        assertFalse(stops.loadFile(null));
    }

    @Test
    void testForEmptyFile() {
        assertTrue(stops.emptyFile(emptyFile));
    }

    @Test
    void testForProperFileFormat() {
        assertFalse(stops.properFileFormat(""));
        assertFalse(stops.properFileFormat("stop_id,stop_name,stop_desc,stop_lat"));
        assertFalse(stops.properFileFormat(",,,,"));
        assertFalse(stops.properFileFormat("stop_id"));
        assertFalse(stops.properFileFormat("stop_id,stop_name,stop_desc,stop_lat,stop_lon,extra"));
    }

    @Test
    void testForProperNumberOfAttributes() {
        String fakeAttributes[];

        fakeAttributes = new String[0];
        assertFalse(stops.properAttributeLength(fakeAttributes));

        fakeAttributes = new String[4];
        assertFalse(stops.properAttributeLength(fakeAttributes));

        fakeAttributes = new String[6];
        assertFalse(stops.properAttributeLength(fakeAttributes));

    }

    @Test
    void testForValidCoordinates() {
        assertFalse(stops.validCoordinates("a", "a"));
        assertFalse(stops.validCoordinates("1.a", "1.a"));
        assertFalse(stops.validCoordinates(" ", " "));
        assertFalse(stops.validCoordinates("91", "0"));
        assertFalse(stops.validCoordinates("-91", "0"));
        assertFalse(stops.validCoordinates("0", "181"));
        assertFalse(stops.validCoordinates("0", "-181"));
    }

    @AfterEach
    public void tearDown() {
        //delete the test file
        emptyFile.delete();
    }

}