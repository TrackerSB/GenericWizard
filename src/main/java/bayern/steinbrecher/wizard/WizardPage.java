/*
 * Copyright (C) 2020 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.wizard;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a page of the wizard.
 *
 * @param <T> The return type of the result represented by the page.
 * @author Stefan Huber
 * @since 1.0
 */
public final class WizardPage<T> {

    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");
    private final ReadOnlyBooleanWrapper hasNextFunction = new ReadOnlyBooleanWrapper(this, "hasNextFunction");
    private final Pane root;
    private final Property<Supplier<String>> nextFunction = new SimpleObjectProperty<>(this, "nextFunction");
    private boolean finish;
    private final Supplier<T> resultFunction;

    /**
     * Creates a new page with given params.
     *
     * @param root           The root pane containing all controls.
     * @param nextFunction   The function calculating the name of th next page.
     * @param finish         {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page represents.
     * @param valid          A binding to bind this pages {@code valid} property to.
     */
    public WizardPage(@NotNull Pane root, @NotNull Supplier<String> nextFunction, boolean finish,
                      @NotNull Supplier<T> resultFunction, @NotNull ObservableValue<? extends Boolean> valid) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(nextFunction);
        Objects.requireNonNull(resultFunction);
        Objects.requireNonNull(valid);

        this.root = root;
        this.nextFunction.addListener((obs, oldVal, newVal) -> {
            hasNextFunction.set(newVal != null);
        });
        this.nextFunction.setValue(nextFunction);
        this.finish = finish;
        this.resultFunction = Objects.requireNonNull(resultFunction, "The resultFunction must not be zero.");
        this.valid.bind(valid);
    }

    /**
     * Creates a new page with given params. The {@code valid} property always contains {@code true}.
     *
     * @param root           The root pane containing all controls.
     * @param nextFunction   The function calculating the name of the next page.
     * @param finish         {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page represents.
     */
    public WizardPage(@NotNull Pane root, @NotNull Supplier<String> nextFunction, boolean finish,
                      @NotNull Supplier<T> resultFunction) {
        this(root, nextFunction, finish, resultFunction, new SimpleBooleanProperty(true));
    }

    /**
     * Returns the pane containing all controls.
     *
     * @return The pane containing all controls.
     */
    @NotNull
    public Pane getRoot() {
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
    @NotNull
    public Supplier<String> getNextFunction() {
        return nextFunctionProperty().getValue();
    }

    /**
     * Sets a new function calculating the key of the next page.
     *
     * @param nextFunction The function calculating the key of the next page.
     */
    public void setNextFunction(@NotNull Supplier<String> nextFunction) {
        Objects.requireNonNull(nextFunction);
        this.nextFunction.setValue(nextFunction);
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
     * Changes whether this page is a last one.
     *
     * @param finish {@code true} if this page has to be a last one.
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    /**
     * Returns the function calculating the result this page represents.
     *
     * @return The function calculating the result this page represents.
     */
    @NotNull
    public Supplier<T> getResultFunction() {
        return resultFunction;
    }

    /**
     * Sets a new binding to bind this pages {@link #valid} property to.
     *
     * @param validBinding A new binding to bind this pages {@link #valid} property to.
     */
    public void setValidBinding(@NotNull ObservableValue<? extends Boolean> validBinding) {
        this.valid.bind(validBinding);
    }

    /**
     * Returns the property representing whether this page has valid input.
     *
     * @return The property representing whether this page has valid input.
     */
    @NotNull
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
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
