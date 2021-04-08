package com.chutneytesting.admin.domain.gitbackup;

import java.util.stream.Stream;

public interface ChutneyContentProvider {

    String provider();

    ChutneyContentCategory category();

    Stream<ChutneyContent> getContent();

}
