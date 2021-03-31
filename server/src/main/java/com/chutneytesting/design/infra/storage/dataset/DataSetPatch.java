package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.dataSetValues;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.unifiedDiff;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.dataset.DataSet;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class DataSetPatch {

    public final String id;
    public final String refId;
    public final String name;
    public final String description;
    public final Instant creationDate;
    public final List<String> tags;
    public final String unifiedDiffValues;
    public final Integer version;

    private DataSetPatch(String id, String refId, String name, String description, Instant creationDate, List<String> tags, String unifiedDiffValues, Integer version) {
        this.id = id;
        this.refId = refId;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.tags = tags;
        this.unifiedDiffValues = unifiedDiffValues;
        this.version = version;
    }

    public boolean hasPatchedValues() {
        return name != null || description != null || tags != null || unifiedDiffValues != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSetPatch that = (DataSetPatch) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(refId, that.refId) &&
            Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(creationDate, that.creationDate) &&
            Objects.equals(tags, that.tags) &&
            Objects.equals(unifiedDiffValues, that.unifiedDiffValues) &&
            Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refId, name, description, creationDate, tags, unifiedDiffValues, version);
    }

    public static DataSetPatchBuilder builder() {
        return new DataSetPatchBuilder();
    }

    public static class DataSetPatchBuilder {
        private String id;
        private String refId;
        private String name;
        private String description;
        private Instant creationDate;
        private List<String> tags;
        private String unifiedDiffValues;
        private Integer version;

        private DataSetPatchBuilder() {
        }

        public DataSetPatch build() {
            return new DataSetPatch(
                id,
                refId,
                name,
                description,
                creationDate,
                tags,
                unifiedDiffValues,
                version
            );
        }

        public DataSetPatchBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataSetPatchBuilder withRefId(String refId) {
            this.refId = refId;
            return this;
        }

        public DataSetPatchBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DataSetPatchBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public DataSetPatchBuilder withCreationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public DataSetPatchBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public DataSetPatchBuilder withUnifiedDiffValues(String unifiedDiffValues) {
            this.unifiedDiffValues = unifiedDiffValues;
            return this;
        }

        public DataSetPatchBuilder withVersion(Integer version) {
            this.version = version;
            return this;
        }

        public DataSetPatchBuilder fromDataSets(DataSet newDataSet, DataSet previousDataSet) {
            if (!ofNullable(previousDataSet).isPresent()) {
                return fromDataSets(newDataSet);
            }

            if (!newDataSet.name.equals(previousDataSet.name)) {
                this.name = newDataSet.name;
            }
            if (!newDataSet.description.equals(previousDataSet.description)) {
                this.description = newDataSet.description;
            }
            this.creationDate = newDataSet.creationDate;
            if (!newDataSet.tags.equals(previousDataSet.tags)) {
                this.tags = newDataSet.tags;
            }
            if (!newDataSet.constants.equals(previousDataSet.constants) || !newDataSet.datatable.equals(previousDataSet.datatable)) {
                this.unifiedDiffValues = unifiedDiff(dataSetValues(newDataSet, false), dataSetValues(previousDataSet, false));
            }
            return this;
        }

        private DataSetPatchBuilder fromDataSets(DataSet newDataSet) {
            this.name = newDataSet.name;
            this.description = newDataSet.description;
            this.creationDate = newDataSet.creationDate;
            this.tags = newDataSet.tags;
            this.unifiedDiffValues = unifiedDiff(dataSetValues(newDataSet, false), "");
            return this;
        }
    }
}
