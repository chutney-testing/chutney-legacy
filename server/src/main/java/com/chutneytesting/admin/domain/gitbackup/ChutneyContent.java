package com.chutneytesting.admin.domain.gitbackup;

import static java.util.Optional.ofNullable;

public class ChutneyContent {

    public final String name;
    public final String content;
    public final String format;

    public final ChutneyContentCategory category;
    public final String provider;

    private ChutneyContent(String name, String content, String format, ChutneyContentCategory category, String provider) {
        this.name = name;
        this.content = content;
        this.format = format;
        this.category = category;
        this.provider = provider;
    }

    public static ChutneyContentBuilder builder() {
        return new ChutneyContentBuilder();
    }

    public static class ChutneyContentBuilder {

        private String name;
        private String content;
        private String format;
        private ChutneyContentCategory category;
        private String provider;

        public ChutneyContent build() {
            return new ChutneyContent(
                ofNullable(name).orElseThrow(() -> new IllegalArgumentException("name required")),
                ofNullable(content).orElse(""),
                ofNullable(format).orElse("json"),
                ofNullable(category).orElseThrow(() -> new IllegalArgumentException("category required")),
                ofNullable(provider).orElseThrow(() -> new IllegalArgumentException("provider required"))
            );
        }

        public ChutneyContentBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ChutneyContentBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        public ChutneyContentBuilder withFormat(String format) {
            this.format = format;
            return this;
        }

        public ChutneyContentBuilder withCategory(ChutneyContentCategory category) {
            this.category = category;
            return this;
        }

        public ChutneyContentBuilder withProvider(String provider) {
            this.provider = provider;
            return this;
        }
    }
}
