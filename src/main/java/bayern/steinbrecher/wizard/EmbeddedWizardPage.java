package bayern.steinbrecher.wizard;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a page of the wizard.
 *
 * @param <T> The return type of the result represented by the page.
 * @author Stefan Huber
 * @since 1.0
 */
public final class EmbeddedWizardPage<T extends Optional<?>> {

    private final WizardPage<T, ?> page;
    private final Parent root;

    EmbeddedWizardPage(@NotNull WizardPage<T, ?> page) throws LoadException {
        this.page = Objects.requireNonNull(page);
        this.root = page.loadFXML();
    }

    @NotNull
    public Parent getRoot() {
        return root;
    }

    @NotNull
    public ReadOnlyProperty<Supplier<String>> nextFunctionProperty() {
        return page.nextFunctionProperty();
    }

    @Nullable
    public Supplier<String> getNextFunction() {
        return nextFunctionProperty().getValue();
    }

    public boolean isFinish() {
        return page.isFinish();
    }

    @NotNull
    public ReadOnlyBooleanProperty validProperty() {
        return page.validProperty();
    }

    public boolean isValid() {
        return validProperty().get();
    }
}
