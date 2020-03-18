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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javax.swing.text.View;

/**
 * Represents a class which can be in a {@link bayern.steinbrecher.wizard.Wizard}.
 *
 * @param <T> The type of the result of the {@link WizardPage}.
 * @param <C> The type of the controller used by the {@link View}.
 * @author Stefan Huber
 */
public abstract class WizardableView<T extends Optional<?>, C extends WizardableController<T>> {

    private C controller;

    protected final <P extends Parent> P loadFXML(String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new FileNotFoundException(
                    "The class " + getClass().getName() + " can not find the resource " + fxmlPath);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            P root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            return root;
        }
    }

    /**
     * Returns the path of the FXML file to load to be used by a wizard.
     *
     * @return The path of the FXML file to load to be used by a wizard.
     * @see #getWizardPage()
     */
    protected abstract String getWizardFxmlPath();

    /**
     * Creates a {@link WizardPage}.The nextFunction is set to {@code null} and isFinish is set to {@code false}.
     *
     * @return The newly created {@link WizardPage}. Returns {@code null} if the {@link WizardPage} could not be
     * created.
     * @throws IOException {@link #loadFXML(java.lang.String)}
     */
    public WizardPage<T> getWizardPage() throws IOException {
        WizardPage<T> page;
        Pane root = loadFXML(getWizardFxmlPath());
        page = new WizardPage<>(
                root, null, false, () -> getController().getResult(), getController().validProperty());
        return page;
    }

    public C getController() {
        return controller;
    }
}
