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
