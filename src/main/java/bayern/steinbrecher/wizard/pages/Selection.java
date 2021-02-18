package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.StandaloneWizardPage;
import javafx.scene.control.CheckBox;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @param <T> The type of the attributes being able to select.
 * @author Stefan Huber
 * @since 1.4
 */
public class Selection<T extends Comparable<? extends T>>
        extends StandaloneWizardPage<Optional<Set<T>>, SelectionController<T>> {

    private static final long NUM_TIMEOUT_TICKS = 3;
    private static final TimeUnit TIMEOUT_TICK_UNIT = TimeUnit.SECONDS;
    private final Supplier<Set<T>> options;

    /**
     * Creates a new page which represents each given option as selectable {@link CheckBox} and adds a search box which
     * allows to filter the visible options.
     */
    public Selection(@NotNull Set<T> options) {
        this(() -> options);
    }

    /**
     * @since 1.51
     */
    public Selection(@NotNull Supplier<Set<T>> options) {
        super("Selection.fxml", ResourceBundle.getBundle("bayern.steinbrecher.wizard.pages.Selection"));
        this.options = Objects.requireNonNull(options);
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setOptions(options.get());
    }
}
