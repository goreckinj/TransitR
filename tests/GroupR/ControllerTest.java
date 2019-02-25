package GroupR;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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
 * Carries out tests for the controller class. Tests for the following are performed:
 * fileExist() - ensures an arbitrary number of fles exist
 * @author  hueblergw, jinq, goreckin
 */

class ControllerTest {

    Controller controller;
    File[] files;

    @BeforeEach
    public void startUp() {

        //intialize controller
        controller = new Controller();

        //definte 3 new files
        files = new File[3];

        //create the new files
        try {
            for(int i = 0; i < files.length; i++) {
                files[i] = new File(Integer.toString(i));
                files[i].createNewFile();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testFilesExist() {
        assertFalse(controller.filesExist(new File("fake file"), new File("not here")));
        assertTrue(controller.filesExist(files[0], files[1], files[2]));
    }

    @AfterEach
    void tearDown() {

        //delete all the files created
        for(int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

}