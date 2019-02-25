package GroupR;

import java.text.DecimalFormat;
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
 * SleepTimer class that benchmark how fast each process takes such as load, export.
 * MapView
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public class SimpleTimer {

    private final DecimalFormat time = new DecimalFormat("#.###");
    private long start;

    public SimpleTimer() {
        start = -1;
    }

    public void start() {
        if(start != -1) {
            throw new IllegalArgumentException("Stop timer before starting.");
        }
        start = System.nanoTime();
    }


    public String stop() {
        if(this.start == -1) {
            throw new IllegalArgumentException("Timer must be started before stopping.");
        }
        long end = System.nanoTime();
        long start = this.start;
        this.start = -1;
        return "Execution Time: " + time.format(((end - start) / 1000000000.0)) + "s";
    }
}
