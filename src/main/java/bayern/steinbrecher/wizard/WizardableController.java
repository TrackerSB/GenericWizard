/*
 * The MIT License
 *
 * Copyright 2020 Steinbrecher.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package bayern.steinbrecher.wizard;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;

/**
 * Represents a controller of a {@link WizardableView}.
 *
 * @author Stefan Huber
 * @param <T> The type of the result represented by this controller.
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
