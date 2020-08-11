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
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.LoadException;
import javafx.scene.layout.Pane;
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

    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private final WizardPage<T, ?> page;
    private final Pane root;
    private final ReadOnlyBooleanWrapper hasNextFunction = new ReadOnlyBooleanWrapper(this, "hasNextFunction");
    private final Property<Supplier<String>> nextFunction = new SimpleObjectProperty<>(this, "nextFunction");
    private boolean finish;

    public EmbeddedWizardPage(@NotNull WizardPage<T, ?> page, @Nullable Supplier<String> nextFunction, boolean finish)
            throws LoadException {
        this.page = Objects.requireNonNull(page);
        this.root = page.loadFXML();
        this.nextFunction.addListener((obs, oldVal, newVal) -> hasNextFunction.set(newVal != null));
        makeFinishPage(finish, nextFunction);
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

    /*
     * @param finish         {@code true} only if this page is a last one.
     * @param nextFunction   The function calculating the name of the next page. In case {@code finish} is
     *                       {@code true} this value is allowed to be {@code null}.
     */
    public void makeFinishPage(boolean finish, @Nullable Supplier<String> nextFunction) {
        if (!finish) {
            Objects.requireNonNull(nextFunction,
                    "A non-last page must define a function which calculates the next page.");
        }
        this.finish = finish;
        this.nextFunction.setValue(nextFunction);
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
