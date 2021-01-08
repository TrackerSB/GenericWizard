package bayern.steinbrecher.wizard;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a class which can be in a {@link Wizard}.
 *
 * @param <T> The type of the result of the {@link EmbeddedWizardPage}.
 * @param <C> The type of the controller used by the {@link WizardPage}.
 * @author Stefan Huber
 * @since 1.2
 */
public abstract class WizardPage<T extends Optional<?>, C extends WizardPageController<T>> {

    private final String fxmlPath;
    private final ResourceBundle bundle;
    private C controller;

    /**
     * @since 1.13
     */
    protected WizardPage(@NotNull String fxmlPath, @Nullable ResourceBundle bundle) {
        this.fxmlPath = Objects.requireNonNull(fxmlPath);
        this.bundle = bundle;
    }

    Parent loadFXML() throws LoadException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new LoadException(
                    new FileNotFoundException(
                            String.format("The class %s can not find the resource %s", getClass().getName(), fxmlPath)
                    )
            );
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(resource, bundle);
            Parent root;
            try {
                root = fxmlLoader.load();
            } catch (IOException ex) {
                throw new LoadException(ex);
            }
            controller = fxmlLoader.getController();
            afterControllerInitialized();
            return root;
        }
    }

    /**
     * This method is executed after the FXML is loaded and right after the corresponding controller is set. This
     * function represents an equivalent to a FXML controllers initialize method.
     *
     * @since 1.8
     */
    protected void afterControllerInitialized() {
        // No op
    }

    /**
     * Creates a {@link EmbeddedWizardPage}. The nextFunction returns always {@code null} and isFinish is set to
     * {@code true}.
     *
     * @return The newly created {@link EmbeddedWizardPage}.
     */
    @NotNull
    @Contract("-> new")
    public final EmbeddedWizardPage<T> generateEmbeddableWizardPage() throws LoadException {
        return new EmbeddedWizardPage<>(this, null, true);
    }

    public T getResult() {
        return getController()
                .getResult();
    }

    public ReadOnlyBooleanProperty validProperty() {
        return getController()
                .validProperty();
    }

    public boolean isValid() {
        return validProperty()
                .get();
    }

    protected C getController() {
        return controller;
    }
}
