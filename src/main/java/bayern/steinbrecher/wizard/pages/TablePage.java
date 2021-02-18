package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.StandaloneWizardPage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 1.51
 */
public class TablePage extends StandaloneWizardPage<Optional<Void>, TablePageController> {

    private final List<List<String>> results;

    /**
     * @since 1.52
     */
    public TablePage() {
        this(List.of());
    }

    /**
     * NOTE The constructor assumes that the results are in row-major order and the first row contains the headings of
     * the table.
     */
    public TablePage(@NotNull List<List<String>> results) {
        super("TablePage.fxml", null);
        this.results = results;
    }

    @Override
    protected void afterControllerInitialized() {
        setResults(results);
    }

    public void setResults(@NotNull List<List<String>> results) {
        getController().setResults(results);
    }
}
