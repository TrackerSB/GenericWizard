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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a controller of a {@link WizardPage}.
 *
 * @param <T> The type of the result represented by this controller.
 * @author Stefan Huber
 * @since 1.2
 */
public abstract class WizardPageController<T extends Optional<?>> {

    /**
     * A property indicating whether all input handled by this controller is valid.
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);
    @FXML
    private ResourceBundle resources;

    /**
     * Returns the value behind {@code key} of the resource bundle inserted params.
     *
     * @param key    The key to search for.
     * @param params The params to insert.
     * @return The value with inserted params.
     * @since 1.12
     */
    public String getResourceValue(@NotNull String key, @NotNull Object... params) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(params);
        return resources.containsKey(key) ? MessageFormat.format(resources.getString(key), params) : key;
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and with inserted params.
     *
     * @param key    The key to search for.
     * @param params The list of params to insert each in the value behind {@code key}.
     * @return The list of values with inserted params.
     * @since 1.12
     */
    public List<String> getResourceValues(@NotNull String key, @NotNull List<Object[]> params) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(params);

        List<String> values = new ArrayList<>(params.size());
        params.forEach(p -> values.add(getResourceValue(key, p)));
        return values;
    }

    /**
     * Returns the current result that the content of the page represents.
     *
     * @return If the content of this page is invalid this method returns {@link Optional#empty()}. Otherwise it returns
     * the represented result.
     */
    @SuppressWarnings("unchecked")
    public T getResult() {
        return isValid() ? calculateResult() : (T) Optional.empty();
    }

    /**
     * Calculates the actual result if any. It does not recognize any criteria about whether to return a result or not.
     * This is handled by {@link #getResult()}.
     *
     * @return The actual result if any.
     * @see #getResult()
     */
    protected abstract T calculateResult();

    @NotNull
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public boolean isValid() {
        return valid.get();
    }

    protected void bindValidProperty(ObservableValue<? extends Boolean> binding) {
        valid.bind(binding);
    }
}
