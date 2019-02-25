package GroupR;

import java.util.Collection;

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
 * Subject interface that notifies observers of any updates
 * @author goreckinj, hueblergw, jinq
 * @version 1.0
 * @created 05-Oct-2017 09:39:32
 */
public interface Subject {

	/**
	 * Adds an observer. Return true if successfully added
	 * 
	 * @param o
	 */
	boolean addObserver(Observer o);

	/**
	 * Deletes an observer. Returns the deleted observer
	 * 
	 * @param o
	 */
	Observer deleteObservers(Observer o);

	/**
	 * Notifies observers. Returns true if notified successfully
	 */
	void notifyObservers(String changedData);

}