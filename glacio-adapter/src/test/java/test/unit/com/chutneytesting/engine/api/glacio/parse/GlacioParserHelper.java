package test.unit.com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import com.github.fridujo.glacio.ast.TableRow;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;

public final class GlacioParserHelper {

    public static void loopOverRandomString(int stepTextMinLength, int stepTextMaxLength, int randomLoopMax, Consumer<String> assertToRun) {
        IntStream.range(stepTextMinLength, stepTextMaxLength)
            .forEach(length -> IntStream.range(1, randomLoopMax)
                .forEach(i ->
                    assertToRun.accept(RandomStringUtils.random(length, true, true))
                )
            );
    }

    public static Step buildSimpleStepWithText(String stepText) {
        return new Step(zeroPosition, stepText, emptyList(), empty(), empty());
    }

    public static Step buildDataTableStepWithText(String stepText, String dataTableString) {
        return new Step(zeroPosition, stepText, emptyList(), empty(), of(buildDataTableFromString(dataTableString)));
    }

    public static Step buildSubStepsStepWithText(String stepText, String subStepsString) {
        return new Step(zeroPosition, stepText, buildSimpleSubStepsFromString(subStepsString), empty(), empty());
    }

    public static DataTable buildDataTableFromString(String dataTableString) {
        List<TableRow> rows = new ArrayList<>();
        String[] lines = dataTableString.split("\n");
        for (String line : lines) {
            List<TableCell> cells = new ArrayList<>();
            for (String value : line.split("\\|")) {
                if (value.length() > 0) {
                    cells.add(new TableCell(value.trim()));
                }
            }
            rows.add(new TableRow(zeroPosition, cells));
        }
        return new DataTable(zeroPosition, rows);
    }

    public static List<Step> buildSimpleSubStepsFromString(String subStepsString) {
        List<Step> subSteps = new ArrayList<>();
        for (String line : subStepsString.split("\n")) {
            subSteps.add(buildSimpleStepWithText(line));
        }
        return subSteps;
    }

    public final static Position zeroPosition = new Position(0, 0);
}
