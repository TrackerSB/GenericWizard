package bayern.steinbrecher.wizard;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
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

    /**
     * @since 1.26
     */
    protected StandaloneWizardPage(@NotNull String fxmlPath, @Nullable ResourceBundle bundle) {
        super(fxmlPath, bundle);
    }

    /**
     * @since 1.26
     */
    public Pane generateStandalonePage(@NotNull Stage stage, @Nullable String closeText) throws LoadException {
        FXMLLoader fxmlLoader;
        try {
            fxmlLoader = FXMLLoader.load(
                    StandaloneWizardPage.class.getResource("StandaloneWizardPage.fxml"),
                    ResourceBundle.getBundle("bayern.steinbrecher.wizard.StandaloneWizardPage"));
        } catch (IOException ex) {
            throw new LoadException("Could not load the standalone wizard page wrapper description", ex);
        }

        // Setup controller
        StandaloneWizardPageController<?> standaloneController = fxmlLoader.getController();
        standaloneController.setCloseText(closeText);
        standaloneController.setStage(
                Objects.requireNonNull(stage, "For being used as a standalone window a stage is required"));

        // Embed wizard page content
        Pane standaloneRoot = fxmlLoader.getRoot();
        standaloneRoot.getChildren()
                .stream()
                .filter(n -> n.getId().equals("contentHolder"))
                .filter(n -> n instanceof Pane)
                .map(n -> (Pane) n)
                .findFirst()
                .orElseThrow(() -> new Error(
                        "Could not insert wizard page into standalone wrapper",
                        new NoSuchElementException(
                                "Could not find the element to hold the wizard page content "
                                        + "(Either no element with the required name is found or no children can "
                                        + "be added)")))
                .getChildren()
                .add(generateEmbeddableWizardPage().getRoot());
        return standaloneRoot;
    }
}
