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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a wizard for showing a sequence of {@code Pane}s. You can step back and forward on these {@code Panes} and
 * only can close is on the last page.<br>
 * You also can style it using CSS. Following CSS classes are available:<br>
 * <ul>
 * <li>wizard</li>
 * <li>wizard-content</li>
 * <li>wizard-controls</li>
 * </ul>
 *
 * @author Stefan Huber
 * @since 1.0
 */
public class Wizard {

    private final WizardController controller;
    private final Parent root;

    private Wizard(WizardController controller, Parent root) {
        super();
        this.controller = controller;
        this.root = root;
    }

    @Contract("_ -> new")
    @NotNull
    public static Wizard create(@NotNull Map<String, WizardPage<?>> pages) {
        Objects.requireNonNull(pages);
        FXMLLoader fxmlLoader = new FXMLLoader(Wizard.class.getResource("Wizard.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.wizard.Wizard"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            throw new Error("The internal implementation is erroneous", ex);
        }
        WizardController controller = fxmlLoader.getController();
        controller.setVisitablePages(pages);
        return new Wizard(controller, root);
    }

    /**
     * Adds the given page to the wizard and replaces pages with the same key but only if the page was not already
     * visited. This method can be used if a page of the wizard is depending on the result of a previous one.
     *
     * @param key  The key the page is associated with.
     * @param page The page to add to the wizard.
     */
    public void put(@NotNull String key, @NotNull WizardPage<?> page) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(page);
        controller.put(key, page);
    }

    @NotNull
    public Parent getRoot() {
        return root;
    }

    /**
     * Returns the property representing whether the wizard is finished. NOTE: It is not finished when it was closed
     * without using the "finish" button.
     *
     * @return The property representing whether the wizard is finished.
     */
    @NotNull
    public ReadOnlyBooleanProperty finishedProperty() {
        return controller.finishedProperty();
    }

    /**
     * Checks whether the wizard is finished. NOTE: It is not finished when it was closed without using the "finish"
     * button.
     *
     * @return {@code true} only if the wizard is finished.
     */
    public boolean isFinished() {
        return controller.isFinished();
    }

    /**
     * Returns the results of all pages visited in a sequence to an end.
     *
     * @return {@link Optional#empty} only if the wizard is not finished yet, otherwise the results of the visited
     * pages.
     */
    @NotNull
    public Optional<Map<String, ?>> getResults() {
        return controller.getResults();
    }

    @NotNull
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return controller.atBeginningProperty();
    }

    public boolean isAtBeginning() {
        return atBeginningProperty().get();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    @NotNull
    public ReadOnlyBooleanProperty atFinishProperty() {
        return controller.atFinishProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public boolean isAtFinish() {
        return atFinishProperty().get();
    }

    @NotNull
    public ReadOnlyProperty<WizardPage<?>> currentPageProperty() {
        return controller.currentPageProperty();
    }

    @NotNull
    public WizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
    }
}
