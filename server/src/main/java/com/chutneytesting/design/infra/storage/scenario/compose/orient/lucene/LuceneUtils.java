package com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene;

public final class LuceneUtils {

    // Note : Special characters order should be change with extreme care...
    static final String LUCENE_SPECIAL_CHARACTERS = "\\ + - && || ! ( ) { } [ ] ^ \" ~ * ? : /";
    static final String[] LUCENE_SPECIAL_CHARACTERS_LIST = LUCENE_SPECIAL_CHARACTERS.split(" ");

    public static String escapeLuceneSearchQuery(String searchQuery) {
        for (String luceneSpecialChar : LUCENE_SPECIAL_CHARACTERS_LIST) {
            if (searchQuery.contains(luceneSpecialChar)) {
                searchQuery = searchQuery.replace(luceneSpecialChar, "\\" + luceneSpecialChar);
            }
        }
        return searchQuery;
    }

    public static String cleanFirstWildcardsCharacters(String searchQuery) {
        String wildcardsRegexp = "[\\" + LUCENE_SPECIAL_CHARACTERS_LIST[15] + "\\" + LUCENE_SPECIAL_CHARACTERS_LIST[16] + "]*";
        return searchQuery.replaceAll("^" + wildcardsRegexp, "")
            .replaceAll(" " + wildcardsRegexp, " ");
    }

    public static String forceAllRequiredTerm(String searchQuery) {
        StringBuilder result = new StringBuilder();
        for (String word : searchQuery.split("\\s")) {
            result.append(" ").append(LUCENE_SPECIAL_CHARACTERS_LIST[1]).append("\"").append(word).append("\"");
        }
        return result.toString().trim();
    }
}
