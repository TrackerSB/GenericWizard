package bayern.steinbrecher.wizard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a page of the wizard.
 *
 * @param <T> The return type of the result represented by the page.
 * @author Stefan Huber
 * @since 1.0
 */
public final class EmbeddedWizardPage<T extends Optional<?>> {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedWizardPage.class.getName());
    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private final WizardPage<T, ?> page;
    private final Parent root;
    private final ReadOnlyBooleanWrapper hasNextFunction = new ReadOnlyBooleanWrapper(this, "hasNextFunction");
    private final ObjectProperty<Supplier<String>> nextFunction = new SimpleObjectProperty<>(this, "nextFunction");
    private boolean finish;
    private Wizard containingWizard;

    EmbeddedWizardPage(@NotNull WizardPage<T, ?> page, @Nullable Supplier<String> nextFunction, boolean finish)
            throws LoadException {
        this.page = Objects.requireNonNull(page);
        this.root = page.loadFXML();
        hasNextFunction.bind(this.nextFunction.isNotNull());
        setFinishAndNext(finish, nextFunction);
    }

    /**
     * Returns the pane containing all controls.
     *
     * @return The pane containing all controls.
     */
    @NotNull
    public Parent getRoot() {
        return root;
    }

    /**
     * The property containing the function calculating which page to show next.
     *
     * @return The property containing the function calculating which page to show next.
     */
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

    /**
     * Returns whether this page is a last one.
     *
     * @return {@code true} only if this page is a last one.
     */
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
     * This method must be called by a {@link Wizard} if it registers this page as one of its visitable pages.
     * NOTE Currently (2020-09-01) the reference to the containing wizard is solely needed in case this pages next
     * function dynamically creates a {@link WizardPage}.
     */
    void setContainingWizard(@NotNull Wizard wizard) {
        if (containingWizard != null) {
            throw new IllegalStateException("This page is already registered to a wizard");
        }
        containingWizard = Objects.requireNonNull(wizard);
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
    public <R extends Optional<?>, C extends WizardPageController<R>> CompletableFuture<EmbeddedWizardPage<R>> setFinishAndDynamicNext(
            boolean finish, @Nullable Supplier<WizardPage<R, C>> dynamicNextFunction, @NotNull String pageID) {
        CompletableFuture<EmbeddedWizardPage<R>> wizardPageCreation = new CompletableFuture<>();
        Supplier<String> nextFunction;
        if (dynamicNextFunction == null) {
            nextFunction = null;
            wizardPageCreation.completeExceptionally(
                    new NoSuchElementException(
                            "This wizard page has no next function which could have dynamically created the next "
                                    + "wizard page."));
        } else {
            nextFunction = () -> {
                if (containingWizard == null) {
                    wizardPageCreation.completeExceptionally(
                            new IllegalStateException(
                                    "This pages next function can not register a dynamically created next page since "
                                            + "no wizard registered for containing this page"));
                } else {
                    try {
                        EmbeddedWizardPage<R> wizardPage = dynamicNextFunction.get()
                                .generateEmbeddableWizardPage();
                        containingWizard.putPage(pageID, wizardPage);
                        wizardPageCreation.complete(wizardPage);
                    } catch (LoadException ex) {
                        wizardPageCreation.completeExceptionally(ex);
                    }
                }
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

    public T getResult() {
        return page.getResult();
    }

    /**
     * Returns the property representing whether this page has valid input.
     *
     * @return The property representing whether this page has valid input.
     */
    @NotNull
    public ReadOnlyBooleanProperty validProperty() {
        return page.validProperty();
    }

    /**
     * Returns whether the current input of this page is valid.
     *
     * @return {@code true} only if the current input of this page is valid.
     */
    public boolean isValid() {
        return validProperty().get();
    }

    /**
     * Returns the property holding {@code true} only if this page has a {@code nextFunction}.
     *
     * @return The property holding {@code true} only if this page has a {@code nextFunction}.
     * @see #nextFunctionProperty()
     */
    @NotNull
    public ReadOnlyBooleanProperty hasNextFunctionProperty() {
        return hasNextFunction.getReadOnlyProperty();
    }

    /**
     * Checks whether this page has a {@code nextFunction}.
     *
     * @return Returns {@code true} only if this page has a {@code nextFunction}.
     * @see #nextFunctionProperty()
     */
    public boolean isHasNextFunction() {
        return hasNextFunctionProperty().get();
    }
}
