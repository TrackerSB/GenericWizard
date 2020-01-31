/* 
 * The MIT License
 *
 * Copyright 2020 Stefan Huber.
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

import java.util.Objects;
import java.util.concurrent.Callable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;

/**
 * Represents a page of the wizard.
 *
 * @author Stefan Huber
 * @param <T> The return type of the result represented by the page.
 */
public final class WizardPage<T> {

    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");
    private final ReadOnlyBooleanWrapper hasNextFunction = new ReadOnlyBooleanWrapper(this, "hasNextFunction");
    private final Pane root;
    private final Property<Callable<String>> nextFunction = new SimpleObjectProperty<>(this, "nextFunction");
    private boolean finish;
    private final Callable<T> resultFunction;

    /**
     * Creates a new page with given params.
     *
     * @param root The root pane containing all controls.
     * @param nextFunction The function calculating the name of th enext page.
     * @param finish {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page represents.
     * @param valid A binding to bind this pages {@code valid} property to.
     */
    public WizardPage(Pane root, Callable<String> nextFunction, boolean finish,
            Callable<T> resultFunction,
            ObservableValue<? extends Boolean> valid) {
        this(root, nextFunction, finish, resultFunction);
        this.valid.bind(valid);
    }

    /**
     * Creates a new page with given params. The {@code valid} property always contains {@code true}.
     *
     * @param root The root pane containing all controls.
     * @param nextFunction The function calculating the name of the next page.
     * @param finish {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page represents.
     */
    public WizardPage(Pane root, Callable<String> nextFunction, boolean finish,
            Callable<T> resultFunction) {
        this.root = root;
        this.nextFunction.addListener((obs, oldVal, newVal) -> {
            hasNextFunction.set(newVal != null);
        });
        this.nextFunction.setValue(nextFunction);
        this.finish = finish;
        this.resultFunction = Objects.requireNonNull(resultFunction, "The resultFunction must not be zero.");
        valid.unbind();
        valid.set(true);
    }

    /**
     * Returns the pane containing all controls.
     *
     * @return The pane containing all controls.
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * The property containing the function calculating which page to show next.
     *
     * @return The property containing the function calculating which page to show next.
     */
    public Property<Callable<String>> nextFunctionProperty() {
        return nextFunction;
    }

    /**
     * Returns the function calculating the key of the next page.
     *
     * @return The function calculating the key of the next page. Returns {@code null} if this page has no next one.
     */
    public Callable<String> getNextFunction() {
        return nextFunction.getValue();
    }

    /**
     * Sets a new function calculating the key of the next page.
     *
     * @param nextFunction The function calculating the key of the next page.
     */
    public void setNextFunction(Callable<String> nextFunction) {
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
    public Callable<T> getResultFunction() {
        return resultFunction;
    }

    /**
     * Sets a new binding to bind this pages {@link #valid} property to.
     *
     * @param validBinding A new binding to bind this pages {@link #valid} property to.
     */
    public void setValidBinding(ObservableValue<? extends Boolean> validBinding) {
        this.valid.bind(validBinding);
    }

    /**
     * Returns the property representing whether this page has valid input.
     *
     * @return The property representing whether this page has valid input.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    /**
     * Returns whether the current input of this page is valid.
     *
     * @return {@code true} only if the current input of this page is valid.
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Returns the property holding {@code true} only if this page has a {@code nextFunction}.
     *
     * @return The property holding {@code true} only if this page has a {@code nextFunction}.
     * @see #nextFunctionProperty()
     */
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
        return hasNextFunction.get();
    }
}
