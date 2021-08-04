package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.StandaloneWizardPage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 1.51
 */
public class TablePage extends StandaloneWizardPage<Optional<Void>, TablePageController> {

    /**
     * @since 1.52
     */
    public TablePage() {
        super("TablePage.fxml", ResourceBundle.getBundle("bayern.steinbrecher.wizard.pages.TablePage"));
    }

    /**
     * @param contents the data to display in row-major order where the first row is assumed to contain the table
     *                 headings.
     */
    public void setContents(@NotNull List<List<String>> contents) {
        applyToController(c -> c.setContents(contents));
    }
}
