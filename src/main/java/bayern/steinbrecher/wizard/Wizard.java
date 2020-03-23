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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
public class Wizard extends Application {

    private double xDragOffset;
    private double yDragOffset;
    private /* final */ WizardController controller;
    private final Map<String, WizardPage<?>> pages;

    /**
     * Constructs a wizard with showing {@code pages} and using default stylesheet.
     *
     * @param pages The pages to show.
     *
     */
    public Wizard(Map<String, WizardPage<?>> pages) {
        super();
        this.pages = pages;
    }

    /**
     * Initializes the wizard. The wizard is not shown yet. NOTE: Add your own stylesheets only after calling this
     * method.
     *
     * @param stage The stage to be used by the wizard.
     * @throws java.io.IOException May be thrown when loading the wizard.
     * @see FXMLLoader#load()
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Wizard.class.getResource("Wizard.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.wizard.bundles.Wizard"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.setOnMousePressed(mevt -> {
            xDragOffset = stage.getX() - mevt.getScreenX();
            yDragOffset = stage.getY() - mevt.getScreenY();
        });
        scene.setOnMouseDragged(mevt -> {
            stage.setX(mevt.getScreenX() + xDragOffset);
            stage.setY(mevt.getScreenY() + yDragOffset);
        });
        stage.setScene(scene);
        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setPages(pages);
    }

    /**
     * Adds the given page to the wizard and replaces pages with the same key but only if the page was not already
     * visited. This method can be used if a page of the wizard is depending on the result of a previous one. NOTE: The
     * size of {@code page} is not considered anymore after {@code start(...)} was called.
     *
     * @param key The key the page is associated with.
     * @param page The page to add to the wizard.
     */
    public void put(String key, WizardPage<?> page) {
        controller.put(key, page);
    }

    /**
     * Returns the property representing whether the wizard is finished. NOTE: It is not finished when it was closed
     * without using the "finish" button.
     *
     * @return The property representing whether the wizard is finished.
     */
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
     * @return {@code Optional.empty()} only if the wizard is not finished yet, otherwise the results of the visited
     * pages.
     * @throws IllegalCallableException Only thrown if thrown by one of the result functions of the visited pages.
     */
    public Optional<Map<String, ?>> getResults() {
        return controller.getResults();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return controller.atBeginningProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public boolean isAtBeginning() {
        return atBeginningProperty().get();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
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

    /**
     * Returns the property holding the currently shown page.
     *
     * @return The property holding the currently shown page.
     */
    public ReadOnlyProperty<WizardPage<?>> currentPageProperty() {
        return controller.currentPageProperty();
    }

    /**
     * Returns the currently shown page.
     *
     * @return The currently shown page.
     */
    public WizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
    }
}
