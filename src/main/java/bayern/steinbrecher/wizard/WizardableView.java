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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * Represents a class which can be in a {@link bayern.steinbrecher.wizard.Wizard}.
 *
 * @param <T> The type of the result of the {@link WizardPage}.
 * @param <C> The type of the controller used by the {@link WizardableView}.
 * @author Stefan Huber
 * @since 1.2
 */
public abstract class WizardableView<T extends Optional<?>, C extends WizardableController<T>> {

    private C controller;

    protected final <P extends Parent> P loadFXML(String fxmlPath, Optional<ResourceBundle> resourceBundle)
            throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new FileNotFoundException(
                    "The class " + getClass().getName() + " can not find the resource " + fxmlPath);
        } else {
            FXMLLoader fxmlLoader = resourceBundle.isPresent()
                    ? new FXMLLoader(resource, resourceBundle.get())
                    : new FXMLLoader(resource);
            P root = fxmlLoader.load();
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
    protected abstract void afterControllerInitialized();

    /**
     * Returns the path of the FXML file to load to be used by a wizard.
     *
     * @return The path of the FXML file to load to be used by a wizard.
     * @see #getWizardPage()
     */
    protected abstract String getWizardFxmlPath();

    /**
     * Returns the resource bundle which contains the resources that the content of the {@link WizardPage} requires.
     *
     * @return The resource bundle which contains the resources that the content of the {@link WizardPage} requires.
     * @see #getWizardPage()
     */
    protected abstract Optional<ResourceBundle> getResourceBundle();

    /**
     * Creates a {@link WizardPage}.The nextFunction is set to {@code null} and isFinish is set to {@code false}.
     *
     * @return The newly created {@link WizardPage}. Returns {@code null} if the {@link WizardPage} could not be
     * created.
     * @throws IOException {@link #loadFXML(java.lang.String, java.util.Optional)}
     */
    public WizardPage<T> getWizardPage() throws IOException {
        WizardPage<T> page;
        Pane root = loadFXML(getWizardFxmlPath(), getResourceBundle());
        page = new WizardPage<>(
                root, null, false, () -> getController().getResult(), getController().validProperty());
        return page;
    }

    public C getController() {
        return controller;
    }
}
