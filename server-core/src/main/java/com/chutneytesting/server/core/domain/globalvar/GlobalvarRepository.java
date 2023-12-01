/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.server.core.domain.globalvar;

import com.chutneytesting.server.core.domain.admin.Backupable;
import java.util.Map;
import java.util.Set;

public interface GlobalvarRepository extends Backupable {

    /**
     * @return list
     */
    Set<String> list();

    /**
     * @return Retrieve raw json
     */
    String getFileContent(String fileName);

    /**
     * @param content to persist
     */
    void saveFile(String fileName, String content);

    void deleteFile(String fileName);

    /**
     * For example:
     * {
     *     "key1": "value1",
     *     "key2": {
     *         "subKey1": "subValue1",
     *         "subKey2": "subValue2"
     *     }
     * }
     * will return a map of 3 object :
     *    - "key1" : "value1"
     *    - "key2.subKey1" : "subValue1"
     *    - "key2.subKey2" : "subValue2"
     * @return map with flatten key
     */
    Map<String, String> getFlatMap();

}
