package com.chutneytesting.design.infra.storage.db.orient.lucene;

import static com.chutneytesting.design.infra.storage.db.orient.lucene.LuceneUtils.LUCENE_SPECIAL_CHARACTERS_LIST;
import static com.chutneytesting.design.infra.storage.db.orient.lucene.LuceneUtils.cleanFirstWildcardsCharacters;
import static com.chutneytesting.design.infra.storage.db.orient.lucene.LuceneUtils.escapeLuceneSearchQuery;
import static com.chutneytesting.design.infra.storage.db.orient.lucene.LuceneUtils.forceAllRequiredTerm;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LuceneUtilsTest {

    @Test
    public void should_escape_lucene_search_query() {
        StringBuilder query = new StringBuilder();
        StringBuilder expectedQuery = new StringBuilder();
        for (String luceneSpecialChar : LUCENE_SPECIAL_CHARACTERS_LIST) {
            query.append(" ").append(luceneSpecialChar);
            expectedQuery.append(" ").append("\\").append(luceneSpecialChar);
        }
        String cleanQuery = escapeLuceneSearchQuery(query.toString());
        assertThat(cleanQuery).isEqualTo(expectedQuery.toString());
    }

    @Test
    public void should_clean_first_wildcards_characters() {
        String query = "**??**test poa*zpa **??**poaz**iea??poz";
        String cleanQuery = cleanFirstWildcardsCharacters(query);
        assertThat(cleanQuery).isEqualTo("test poa*zpa poaz**iea??poz");
    }

    @Test
    public void should_force_all_required_term() {
        String query = "*ab*cd +efghij ?kl";
        String cleanQuery = forceAllRequiredTerm(query);
        assertThat(cleanQuery).isEqualTo("+\"*ab*cd\" +\"+efghij\" +\"?kl\"");
    }
}
