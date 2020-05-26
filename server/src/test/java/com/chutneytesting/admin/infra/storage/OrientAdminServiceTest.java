package com.chutneytesting.admin.infra.storage;

import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS;
import static org.assertj.core.api.Assertions.assertThat;

import com.orientechnologies.common.log.OLogManager;
import com.chutneytesting.admin.domain.DatabaseAdminService;
import com.chutneytesting.admin.domain.SqlResult;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepRepository;
import com.chutneytesting.design.infra.storage.compose.OrientFunctionalStepRepository;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.chutneytesting.tools.ImmutablePaginationRequestWrapperDto;
import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestWrapperDto;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ConstantConditions")
public class OrientAdminServiceTest extends AbstractOrientDatabaseTest {

    private static String DATABASE_NAME = "orient_admin_test";

    private static StepRepository orientRepository;
    private static DatabaseAdminService sut;

    @BeforeAll
    public static void setUp() {
        OrientAdminServiceTest.initComponentDB(DATABASE_NAME);

        orientRepository = new OrientFunctionalStepRepository(orientComponentDB);
        sut = new OrientAdminService(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
    }

    @AfterEach
    public void after() {
        truncateCollection(DATABASE_NAME, STEP_CLASS);
        truncateCollection(DATABASE_NAME, GE_STEP_CLASS);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        OrientAdminServiceTest.destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_limit_result_to_20_records_when_execute_select_query() {
        // Given
        ArrayList<FunctionalStep> steps = new ArrayList<>();
        IntStream.range(0, 25).forEach(
            value -> steps.add(
                FunctionalStep.builder()
                    .withName("func step " + value)
                    .build()
            ));

        steps.forEach(step -> orientRepository.save(step));

        // When
        SqlResult result = sut.execute("SELECT FROM " + STEP_CLASS);

        //Then
        assertThat(result.error).isEmpty();
        assertThat(result.updatedRows).isEmpty();
        assertThat(result.table.get().rows.size()).isEqualTo(20);
    }

    @Test
    public void should_not_limit_result_when_execute_already_limited_select_query() {
        // Given
        ArrayList<FunctionalStep> steps = new ArrayList<>();
        IntStream.range(0, 10).forEach(
            value -> steps.add(
                FunctionalStep.builder()
                    .withName("func step " + value)
                    .build()
            ));

        steps.forEach(step -> orientRepository.save(step));

        // When
        SqlResult result = sut.execute("SELECT FROM " + STEP_CLASS + "\nLIMIT 5");

        //Then
        assertThat(result.error).isEmpty();
        assertThat(result.updatedRows).isEmpty();
        assertThat(result.table.get().rows.size()).isEqualTo(5);
    }

    @Test
    public void should_set_update_count_when_execute_update_query() {
        // Given
        FunctionalStep step = FunctionalStep.builder()
            .withName("func step")
            .build();

        String firstSavedRcordId = orientRepository.save(step);

        // When
        SqlResult result = sut.execute("UPDATE" + firstSavedRcordId + " SET unit_test_field = 'test content'");

        //Then
        assertThat(result.error).isEmpty();
        assertThat(result.table).isEmpty();
        assertThat(result.updatedRows).hasValue(1);
    }

    @Test
    public void should_set_error_when_execute_not_executable_query() {

        // When
        SqlResult result = sut.execute("not a valid query");

        //Then
        assertThat(result.table).isEmpty();
        assertThat(result.updatedRows).isEmpty();
        assertThat(result.error.get()).contains("Unable to execute statement");
    }

    @Test
    public void should_get_pages_when_paginate_select_query() {
        // Given
        final Long TOTAL_FSTEPS = 25L;

        ArrayList<FunctionalStep> steps = new ArrayList<>();
        LongStream.range(0, TOTAL_FSTEPS).forEach(
            value -> steps.add(
                FunctionalStep.builder()
                    .withName("func step " + value)
                    .build()
            ));

        steps.forEach(step -> orientRepository.save(step));

        final Integer ITEM_PER_PAGE = 10;
        PaginationRequestWrapperDto<String> requestPage1 = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(1)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("SELECT FROM " + STEP_CLASS))
            .build();

        PaginationRequestWrapperDto<String> requestPage2 = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(2)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("SELECT FROM " + STEP_CLASS))
            .build();

        PaginationRequestWrapperDto<String> requestPage3 = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(3)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("SELECT FROM " + STEP_CLASS))
            .build();

        // When
        PaginatedDto<SqlResult> page1 = sut.paginate(requestPage1);
        PaginatedDto<SqlResult> page2 = sut.paginate(requestPage2);
        PaginatedDto<SqlResult> page3 = sut.paginate(requestPage3);

        //Then
        assertThat(page1.data().size()).isEqualTo(1);
        assertThat(page1.totalCount()).isEqualTo(TOTAL_FSTEPS);
        assertThat(page1.data().get(0).table.get().rows.size()).isEqualTo(ITEM_PER_PAGE);
        assertThat(page2.data().size()).isEqualTo(1);
        assertThat(page2.totalCount()).isEqualTo(TOTAL_FSTEPS);
        assertThat(page2.data().get(0).table.get().rows.size()).isEqualTo(ITEM_PER_PAGE);
        assertThat(page3.data().size()).isEqualTo(1);
        assertThat(page3.totalCount()).isEqualTo(TOTAL_FSTEPS);
        assertThat(page3.data().get(0).table.get().rows.size()).isEqualTo(5);
    }

    @Test
    public void should_set_update_count_when_paginate_update_query() {
        // Given
        FunctionalStep step = FunctionalStep.builder()
            .withName("func step")
            .build();

        String firstSavedRcordId = orientRepository.save(step);

        final Integer ITEM_PER_PAGE = 10;
        PaginationRequestWrapperDto<String> updateRequest = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(1)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("UPDATE " + firstSavedRcordId + " SET unit_test_field = 'test content'"))
            .build();

        // When
        PaginatedDto<SqlResult> updateResult = sut.paginate(updateRequest);

        //Then
        assertThat(updateResult.data().size()).isEqualTo(1);
        assertThat(updateResult.data().get(0).table).isEqualTo(Optional.empty());
        assertThat(updateResult.data().get(0).updatedRows).isEqualTo(Optional.of(1));
        assertThat(updateResult.data().get(0).error.isPresent()).isFalse();
    }

    @Test
    public void should_set_error_when_paginate_not_executable_query() {
        // Given
        final Integer ITEM_PER_PAGE = 10;
        PaginationRequestWrapperDto<String> unvalidQuery = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(1)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("not a valid query"))
            .build();

        // When
        PaginatedDto<SqlResult> updateResult = sut.paginate(unvalidQuery);

        //Then
        assertThat(updateResult.data().size()).isEqualTo(1);
        assertThat(updateResult.data().get(0).table).isEqualTo(Optional.empty());
        assertThat(updateResult.data().get(0).updatedRows).isEqualTo(Optional.empty());
        assertThat(updateResult.data().get(0).error.get()).contains("Unable to execute statement");
    }
}
