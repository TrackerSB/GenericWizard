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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.fxml.FXML;

/**
 * Represents a controller of a {@link WizardableView}.
 *
 * @author Stefan Huber
 * @param <T> The type of the result represented by this controller.
 * @since 1.2
 */
public abstract class WizardableController<T extends Optional<?>> {

    /**
     * A property indicating whether all input handled by this controller is valid.
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);
    /**
     * The stage the controller has to interact with.
     */
    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    /**
     * Only {@code true} when the user explicitly aborted his input. (E.g. pressing the X of the window.)
     */
    private boolean userAborted;
    @FXML
    private ResourceBundle resources;

    /**
     * Returns the value behind {@code key} of the resource bundle inserted params.
     *
     * @param key The key to search for.
     * @param params The params to insert.
     * @return The value with inserted params.
     * @since 1.12
     */
    public String getResourceValue(String key, Object... params) {
        return resources.containsKey(key) ? MessageFormat.format(resources.getString(key), params) : key;
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and with inserted params.
     *
     * @param key The key to search for.
     * @param params The list of params to insert each in the value behind {@code key}.
     * @return The list of values with inserted params.
     * @since 1.12
     */
    public List<String> getResourceValues(String key, List<Object[]> params) {
        List<String> values = new ArrayList<>(params.size());
        params.stream().forEachOrdered(p -> values.add(getResourceValue(key, p)));
        return values;
    }

    /**
     * Returns the current result that the content of the page represents.
     *
     * @return Iff the user aborted the wizard or the content of this page is invalid this method returns
     * {@link Optional#empty()}. Otherwise it returns the represented result.
     */
    @SuppressWarnings("unchecked")
    public T getResult() {
        T result;
        if (hasUserAbborted() || !isValid()) {
            result = (T) Optional.empty();
        } else {
            result = calculateResult();
        }
        return result;
    }

    /**
     * Calculates the actual result if any. It does not recognize any criteria about whether to return a result or not.
     * This is handled by {@link #getResult()}.
     *
     * @return The actual result if any.
     * @see #getResult()
     */
    protected abstract T calculateResult();

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public boolean isValid() {
        return valid.get();
    }

    protected void bindValidProperty(ObservableValue<? extends Boolean> binding) {
        valid.bind(binding);
    }

    /**
     * Returns the property holding the currently set {@link Stage}.
     *
     * @return The property holding the currently set {@link Stage}.
     */
    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    /**
     * Sets the stage the controller can refer to. (E.g. for closing the stage) NOTE: It overrides
     * {@link Stage#onCloseRequest}.
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        stageProperty().set(stage);
        stage.setOnCloseRequest(evt -> userAborted = true);
    }

    /**
     * Returns the currently set {@link Stage}.
     *
     * @return The currently set {@link Stage}.
     */
    public Stage getStage() {
        return stageProperty().get();
    }

    /**
     * Checks whether the user aborted his input.
     *
     * @return {@code true} only if the user aborted his input explicitly.
     */
    public boolean hasUserAbborted() {
        return userAborted;
    }
}
