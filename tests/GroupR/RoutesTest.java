package GroupR;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
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
 * RouteTest test import file, color format and empty file
 * @author  hueblergw, jinq, goreckin
 */
class RoutesTest {
    File routesFile = Paths.get("routes.txt").toFile();
    File emptyFile;
    Routes routes = new Routes();
    @BeforeEach
    public void startUp() {

        emptyFile = new File("test.txt");

        //create the test file
        try {
            emptyFile.createNewFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadFile() {

    }
    @Test
    public void testFormat(){
        String errorFormat1 = "route_id";
        assertFalse(routes.checkFormat(errorFormat1),"the Header doens't match");
        String emptyFormat = "";
        assertFalse(routes.checkFormat(emptyFormat),"the format is empty");
        String errorFormat2 = "123qdddasdasdasdas";
        assertFalse(routes.checkFormat(errorFormat2), "the Header format is wrong");
        String correctFormat = "route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color";
        assertTrue(routes.checkFormat(correctFormat));
    }
    @Test
    public void testLength(){
        int correctLength = 9;
        assertTrue(routes.checkAttributeLength(correctLength));
        int wrongLength = 0;
        assertFalse(routes.checkAttributeLength(wrongLength));
        int maxValue = Integer.MAX_VALUE;
        assertFalse(routes.checkAttributeLength(maxValue));
    }
    @Test
    public void testColorFormat(){
        String colorHex = "FFFFFF";
        assertTrue(routes.checkColor(colorHex));
        String errorFormat = "fffffg";
        assertFalse(routes.checkColor(errorFormat));
        String errorFormat2 = " FFFFFFFFFF";
        assertFalse(routes.checkColor(errorFormat2));
    }
    @Test
    public void testEmptyFile(){
        assertFalse(routes.checkEmptyFile(emptyFile));
    }


    @AfterEach
    public void tearDown() {
        emptyFile.delete();
    }

}