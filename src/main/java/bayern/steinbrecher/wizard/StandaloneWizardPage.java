package bayern.steinbrecher.wizard;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a {@link WizardPage} which can be shown without being embedded into a {@link Wizard}.
 *
 * @author Stefan Huber
 * @since 1.23
 */
public abstract class StandaloneWizardPage<T extends Optional<?>, C extends StandaloneWizardPageController<T>>
        extends WizardPage<T, C> {
    private final Stage stage;

    protected StandaloneWizardPage(@NotNull String fxmlPath, @Nullable ResourceBundle bundle, Stage stage) {
        super(fxmlPath, bundle);
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        getController()
                .setStage(stage);
    }
}
