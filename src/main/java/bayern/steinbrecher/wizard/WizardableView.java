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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a class which can be in a {@link bayern.steinbrecher.wizard.Wizard}.
 *
 * @param <T> The type of the result of the {@link WizardPage}.
 * @param <C> The type of the controller used by the {@link WizardableView}.
 * @author Stefan Huber
 * @since 1.2
 */
public abstract class WizardableView<T extends Optional<?>, C extends WizardableController<T>> {

    private final String fxmlPath;
    private final ResourceBundle bundle;
    private C controller;

    /**
     * @since 1.13
     */
    protected WizardableView(@NotNull String fxmlPath, @Nullable ResourceBundle bundle) {
        Objects.requireNonNull(fxmlPath);
        this.fxmlPath = fxmlPath;
        this.bundle = bundle;
    }

    private <P extends Parent> P loadFXML() throws LoadException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new LoadException(
                    new FileNotFoundException(
                            String.format("The class %s can not find the resource %s", getClass().getName(), fxmlPath)
                    )
            );
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(resource, bundle);
            P root;
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
    protected abstract void afterControllerInitialized();

    /**
     * Creates a {@link WizardPage}. The nextFunction returns always {@code null} and isFinish is set to {@code false}.
     *
     * @return The newly created {@link WizardPage}. Returns {@code null} if the {@link WizardPage} could not be
     * created.
     */
    @NotNull
    @Contract("-> new")
    public WizardPage<T> getWizardPage() throws LoadException {
        Pane root = loadFXML();
        return new WizardPage<>(
                root, () -> null, false, () -> getController().getResult(), getController().validProperty());
    }

    public C getController() {
        return controller;
    }
}
