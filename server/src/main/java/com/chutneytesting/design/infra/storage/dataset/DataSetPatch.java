package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.dataSetValues;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.unifiedDiff;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(refId, that.refId)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(creationDate, that.creationDate)) return false;
        if (!Objects.equals(tags, that.tags)) return false;
        if (!Objects.equals(unifiedDiffValues, that.unifiedDiffValues)) return false;
        return Objects.equals(version, that.version);
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
            if (!Optional.ofNullable(previousDataSet).isPresent()) {
                return fromDataSets(newDataSet);
            }

            DataSetMetaData newMetaData = newDataSet.metadata;
            DataSetMetaData previousMetadata = previousDataSet.metadata;
            if (!newMetaData.name.equals(previousMetadata.name)) {
                this.name = newMetaData.name;
            }
            if (!newMetaData.description.equals(previousMetadata.description)) {
                this.description = newMetaData.description;
            }
            this.creationDate = newDataSet.metadata.creationDate;
            if (!newMetaData.tags.equals(previousMetadata.tags)) {
                this.tags = newMetaData.tags;
            }
            if (!newDataSet.uniqueValues.equals(previousDataSet.uniqueValues) || !newDataSet.multipleValues.equals(previousDataSet.multipleValues)) {
                this.unifiedDiffValues = unifiedDiff(dataSetValues(newDataSet, false), dataSetValues(previousDataSet, false));
            }
            return this;
        }

        private DataSetPatchBuilder fromDataSets(DataSet newDataSet) {
            DataSetMetaData newMetaData = newDataSet.metadata;
            this.name = newMetaData.name;
            this.description = newMetaData.description;
            this.creationDate = newDataSet.metadata.creationDate;
            this.tags = newMetaData.tags;
            this.unifiedDiffValues = unifiedDiff(dataSetValues(newDataSet, false), "");
            return this;
        }
    }
}
