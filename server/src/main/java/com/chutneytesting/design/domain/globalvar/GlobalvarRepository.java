package com.chutneytesting.design.domain.globalvar;

import com.chutneytesting.admin.domain.Backupable;
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
    String getFile(String fileName);

    /**
     * @param json to persist
     */
    void saveFile(String fileName, String json);

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
     *    - "key1" => "value1"
     *    - "key2.subKey1" => "subValue1"
     *    - "key2.subKey2" => "subValue2"
     * @return map with flatten key
     */
    Map<String, String> getFlatMap();

}
