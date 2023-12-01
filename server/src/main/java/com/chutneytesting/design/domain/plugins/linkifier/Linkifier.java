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

package com.chutneytesting.design.domain.plugins.linkifier;

import java.util.Objects;

public class Linkifier {

    public final String pattern;
    public final String link;
    public final String id;

    public Linkifier(String pattern, String link, String id) {
        this.pattern = pattern; // TODO - enforce pattern and link validation
        this.link = link;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Linkifier linkifier = (Linkifier) o;
        return Objects.equals(pattern, linkifier.pattern) &&
            Objects.equals(link, linkifier.link) &&
            Objects.equals(id, linkifier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, link, id);
    }
}
