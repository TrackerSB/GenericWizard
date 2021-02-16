package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.WizardPage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Stefan Huber
 * @since 1.51
 */
public class TablePage extends WizardPage<Optional<Void>, TablePageController> {

    private static final long NUM_TIMEOUT_TICKS = 3;
    private static final TimeUnit TIMEOUT_TICK_UNIT = TimeUnit.SECONDS;
    private final Future<List<List<String>>> results;

    /**
     * NOTE The constructor assumes that the results are in row-major order and the first row contains the headings of
     * the table.
     */
    public TablePage(@NotNull List<List<String>> results) {
        this(new FutureTask<>(() -> results));
    }

    public TablePage(@NotNull Future<List<List<String>>> results) {
        super("TablePage.fxml", null);
        this.results = results;
    }

    @Override
    protected void afterControllerInitialized() {
        try {
            setResults(results.get(NUM_TIMEOUT_TICKS, TIMEOUT_TICK_UNIT));
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            throw new IllegalStateException("The table content to show is not available");
        }
    }

    public void setResults(@NotNull List<List<String>> results) {
        getController().setResults(results);
    }
}
