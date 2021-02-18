package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.StandaloneWizardPage;
import com.google.common.collect.BiMap;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a {@link Selection} which allows to select and group items.
 *
 * @param <T> The type of the options to select.
 * @param <G> The type of the groups to associate items with.
 * @author Stefan Huber
 * @since 1.51
 */
public final class SelectionGroup<T extends Comparable<T>, G>
        extends StandaloneWizardPage<Optional<Map<T, G>>, SelectionGroupController<T, G>> {

    private final Supplier<Set<T>> options;
    private final Supplier<BiMap<G, Color>> groups;

    public SelectionGroup(Set<T> options, BiMap<G, Color> groups) {
        this(() -> options, () -> groups);
    }

    /**
     * @since 1.55
     */
    public SelectionGroup(Supplier<Set<T>> options, Supplier<BiMap<G, Color>> groups) {
        super("SelectionGroup.fxml", ResourceBundle.getBundle("bayern.steinbrecher.wizard.pages.Selection"));
        this.options = options;
        this.groups = groups;
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setGroups(groups.get());
        getController().setOptions(options.get());
    }
}
