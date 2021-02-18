package bayern.steinbrecher.wizard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a class which can be in a {@link Wizard}.
 *
 * @param <T> The type of the result of the {@link EmbeddedWizardPage}.
 * @param <C> The type of the controller used by the {@link WizardPage}.
 * @author Stefan Huber
 * @since 1.2
 */
public abstract class WizardPage<T extends Optional<?>, C extends WizardPageController<T>> {

    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private static final Logger LOGGER = Logger.getLogger(WizardPage.class.getName());
    private final String fxmlPath;
    private final ResourceBundle bundle;
    private final ReadOnlyBooleanWrapper hasNextFunction = new ReadOnlyBooleanWrapper();
    private final ObjectProperty<Supplier<String>> nextFunction = new SimpleObjectProperty<>();
    private boolean finish = false;
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
    final EmbeddedWizardPage<T> generateEmbeddableWizardPage() throws LoadException {
        return new EmbeddedWizardPage<>(this);
    }

    public T getResult() {
        return getController()
                .getResult();
    }

    @NotNull
    public ReadOnlyProperty<Supplier<String>> nextFunctionProperty() {
        return nextFunction;
    }

    /**
     * Returns the function calculating the key of the next page.
     *
     * @return The function calculating the key of the next page. Returns {@code null} if this page has no next one.
     */
    @Nullable
    public Supplier<String> getNextFunction() {
        return nextFunctionProperty().getValue();
    }

    public boolean isFinish() {
        return finish;
    }

    /**
     * @param finish       {@code true} only if this page is a last one.
     * @param nextFunction The function calculating the name of the next page. In case {@code finish} is
     *                     {@code true} this value is allowed to be {@code null}.
     * @since 1.27
     */
    public void setFinishAndNext(boolean finish, @Nullable Supplier<String> nextFunction) {
        if (!finish) {
            Objects.requireNonNull(nextFunction,
                    "A non-last page must define a function which calculates the next page.");
        }
        this.finish = finish;
        this.nextFunction.setValue(nextFunction);
    }

    /**
     * This method allows to use the result of one {@link WizardPage} as the input for another {@link WizardPage}.
     * However, if there are multiple {@link WizardPage}s that depend on previous {@link WizardPage}s then using this
     * method results in heavily nested and thus hard to read source code. Instead if a {@link WizardPage} depends on
     * input of other {@link WizardPage}s it should either provide setters that can be called in the next-function or
     * provide a constructor that has parameters of type {@link Future} only. This allows to avoid this method entirely
     * clarify the structure of the {@link Wizard}.
     *
     * @return A {@link CompletableFuture} object which contains the dynamically generated next page as soon as the
     * {@link Wizard} this page belongs to tries to access its next page.
     * @see #setFinishAndNext(boolean, Supplier)
     * @since 1.27
     */
    @Deprecated(forRemoval = true, since = "1.52")
    public <R extends Optional<?>, C extends WizardPageController<R>> CompletableFuture<WizardPage<R, C>> setFinishAndDynamicNext(
            boolean finish, @Nullable Supplier<WizardPage<R, C>> dynamicNextFunction, @NotNull String pageID,
            @NotNull Wizard containingWizard) {
        CompletableFuture<WizardPage<R, C>> wizardPageCreation = new CompletableFuture<>();
        Supplier<String> nextFunction;
        if (dynamicNextFunction == null) {
            nextFunction = null;
            wizardPageCreation.completeExceptionally(
                    new NoSuchElementException(
                            "This wizard page has no next function which could have dynamically created the next "
                                    + "wizard page."));
        } else {
            nextFunction = () -> {
                WizardPage<R, C> wizardPage = dynamicNextFunction.get();
                containingWizard.putPage(pageID, wizardPage);
                wizardPageCreation.complete(wizardPage);
                if (wizardPageCreation.isCompletedExceptionally()) {
                    Throwable creationException;
                    try {
                        creationException = wizardPageCreation.handle((noResult, ex) -> ex)
                                .get();
                    } catch (InterruptedException | ExecutionException ex) {
                        creationException = ex;
                    }
                    LOGGER.log(Level.SEVERE, "The dynamic creation of a wizard page failed", creationException);
                }
                return pageID;
            };
        }
        setFinishAndNext(finish, nextFunction);
        return wizardPageCreation;
    }

    @NotNull
    public ReadOnlyBooleanProperty hasNextFunctionProperty() {
        return hasNextFunction.getReadOnlyProperty();
    }

    public boolean isHasNextFunction() {
        return hasNextFunctionProperty().get();
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
