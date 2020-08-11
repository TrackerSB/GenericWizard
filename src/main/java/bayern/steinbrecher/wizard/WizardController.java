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

import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 1.0
 */
public final class WizardController {

    private static final Logger LOGGER = Logger.getLogger(WizardController.class.getName());
    /**
     * The percentage of height/width the wizard has to have initially.
     */
    private static final double MAX_SIZE_FACTOR = 0.8;
    private final StringProperty currentIndex = new SimpleStringProperty(this, "currentIndex");
    /* FIXME The initial dummy page is needed since the current page may be already requested before there is a chance
     * to specify the pages of the wizard.
     */
    private final ReadOnlyObjectWrapper<EmbeddedWizardPage<?>> currentPage = new ReadOnlyObjectWrapper<>(
            this, "currentPage", null);
    private final MapProperty<String, EmbeddedWizardPage<?>> visitablePages = new SimpleMapProperty<>();
    private final ReadOnlyBooleanWrapper atBeginning = new ReadOnlyBooleanWrapper(this, "atBeginning", true);
    private final ReadOnlyBooleanWrapper atFinish = new ReadOnlyBooleanWrapper(this, "atEnd");
    private final ReadOnlyObjectWrapper<WizardState> state
            = new ReadOnlyObjectWrapper<>(this, "state", WizardState.RUNNING);
    private final ReadOnlyBooleanWrapper changingPage = new ReadOnlyBooleanWrapper(this, "swiping", false);
    private final Stack<String> history = new Stack<>();
    @FXML
    private ScrollPane scrollContent;
    @FXML
    private StackPane contents;
    private static final String WIZARD_CONTENT_STYLECLASS = "wizard-content";
    private static final Duration SWIPE_DURATION = Duration.seconds(0.75);

    public WizardController() throws LoadException {
        // Make sure to not access null
        currentPage.set(
                new EmbeddedWizardPage<Optional<Void>>(
                        new WizardPage<>("nonExistent", null) {
                            @Override
                            protected void afterControllerInitialized() {
                            }
                        }, () -> null, false
                )
        );
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        visitablePages.addListener((obs, oldVal, newVal) -> {
            newVal.values().stream()
                    .map(EmbeddedWizardPage::getRoot)
                    .forEach(pane -> {
                        HBox.setHgrow(pane, Priority.ALWAYS);
                        VBox.setVgrow(pane, Priority.ALWAYS);
                    });
            currentIndex.addListener((obsI, oldValI, newValI) -> {
                EmbeddedWizardPage<?> newPage = visitablePages.get(newValI);
                atFinish.set(newPage.isFinish());
                currentPage.setValue(newPage);
            });
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        scrollContent.setMaxHeight(screenSize.getHeight() * MAX_SIZE_FACTOR);
        scrollContent.setMaxWidth(screenSize.getWidth() * MAX_SIZE_FACTOR);
    }

    @FXML
    @SuppressWarnings("unused")
    private void showPrevious() {
        if (!isAtBeginning() && !isChangingPage()) {
            history.pop(); //Pop current index
            atBeginning.set(history.size() < 2);
            currentIndex.set(history.peek());
            updatePage(false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void showNext() {
        if (getCurrentPage().isValid() && !isChangingPage()) {
            EmbeddedWizardPage<?> page = getCurrentPage();
            Supplier<String> nextFunction = page.getNextFunction();
            if (page.isHasNextFunction() && page.isValid()) {
                String nextIndex = nextFunction.get();
                if (nextIndex == null || !getVisitablePages().containsKey(nextIndex)) {
                    throw new PageNotFoundException(
                            String.format("Wizard contains no page with key \"%s\".", nextIndex));
                }
                currentIndex.set(nextIndex);
                history.push(nextIndex);
                atBeginning.set(false);
                updatePage(true);
            }
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void finish() {
        if (getState() == WizardState.RUNNING && currentPage.getValue().isValid() && atFinish.get()) {
            state.set(WizardState.FINISHED);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void cancel() {
        if (getState() == WizardState.RUNNING) {
            state.set(WizardState.ABORTED);
        }
    }

    @NotNull
    public MapProperty<String, EmbeddedWizardPage<?>> visitablePagesProperty() {
        return visitablePages;
    }

    @NotNull
    public Map<String, EmbeddedWizardPage<?>> getVisitablePages() {
        return visitablePages.get();
    }

    /**
     * @param swipeToLeft {@code null} == dont swipe, just change; {@code true} == swipe to left; {@code false} == swipe
     *                    to right
     */
    private void updatePage(@Nullable Boolean swipeToLeft) {
        changingPage.set(true);
        ObservableList<Node> addedContents = contents.getChildren();
        Optional<Node> optCurrentPane = Optional.ofNullable(addedContents.isEmpty() ? null : addedContents.get(0));
        assert optCurrentPane.isEmpty()
                || optCurrentPane.get() instanceof Pane : "The current content of this wizard is not a pane.";
        Consumer<Node> removeCurrentPane = currentPane -> {
            currentPane.getStyleClass().remove(WIZARD_CONTENT_STYLECLASS);
            if (!contents.getChildren().remove(currentPane)) {
                LOGGER.log(Level.SEVERE, "The currently shown content of the wizard could not be removed.");
            }
        };

        Pane nextPane = getVisitablePages()
                .get(currentIndex.get())
                .getRoot();
        if (optCurrentPane.isEmpty() || optCurrentPane.get() != nextPane) {
            contents.getChildren().add(nextPane);
            nextPane.getStyleClass().add(WIZARD_CONTENT_STYLECLASS);
        }

        if (swipeToLeft == null) {
            optCurrentPane.ifPresent(removeCurrentPane);
            changingPage.set(false);
        } else {
            double halfParentWidth = nextPane.getParent().getLayoutBounds().getWidth() / 2;
            double halfParentHeight = nextPane.getParent().getLayoutBounds().getHeight() / 2;
            double marginInScene = nextPane.getScene().getWidth() / 2 - halfParentWidth;
            //CHECKSTYLE.OFF: MagicNumber - The factor 3 is needed to make the initial x position outside the view.
            double xRightOuter = 3 * halfParentWidth + marginInScene;
            //CHECKSTYLE.ON: MagicNumber
            double xLeftOuter = -halfParentWidth - marginInScene;

            ParallelTransition overallTrans = new ParallelTransition();

            //Swipe new element in
            MoveTo initialMoveIn = new MoveTo(swipeToLeft ? xRightOuter : xLeftOuter, halfParentHeight);
            HLineTo hlineIn = new HLineTo(halfParentWidth);
            Path pathIn = new Path(initialMoveIn, hlineIn);
            PathTransition pathTransIn = new PathTransition(SWIPE_DURATION, pathIn, nextPane);
            overallTrans.getChildren().add(pathTransIn);

            optCurrentPane.ifPresent(currentPane -> {
                //Swipe old element out
                MoveTo initialMoveOut = new MoveTo(halfParentWidth, halfParentHeight);
                HLineTo hlineOut = new HLineTo(swipeToLeft ? xLeftOuter : xRightOuter);
                Path pathOut = new Path(initialMoveOut, hlineOut);
                PathTransition pathTransOut = new PathTransition(SWIPE_DURATION, pathOut, currentPane);
                pathTransOut.setOnFinished(aevt -> removeCurrentPane.accept(currentPane));
                overallTrans.getChildren().add(pathTransOut);
            });

            overallTrans.setOnFinished(aevt -> changingPage.set(false));
            overallTrans.playFromStart();
        }
    }

    /**
     * Sets a new map of visitable pages. NOTE: Calling this method causes the wizard to reset to the first page and
     * clear the history.
     *
     * @param visitablePages The map of pages to set.
     */
    public void setVisitablePages(@NotNull Map<String, EmbeddedWizardPage<?>> visitablePages) {
        if (!visitablePages.containsKey(EmbeddedWizardPage.FIRST_PAGE_KEY)) {
            throw new IllegalArgumentException("Map of pages must have a key WizardPage.FIRST_PAGE_KEY");
        }

        currentIndex.set(EmbeddedWizardPage.FIRST_PAGE_KEY);
        this.visitablePages.set(FXCollections.observableMap(visitablePages));
        currentPage.setValue(visitablePages.get(EmbeddedWizardPage.FIRST_PAGE_KEY));
        history.clear();
        history.push(EmbeddedWizardPage.FIRST_PAGE_KEY);
        updatePage(null);
    }

    /**
     * Adds the given page to the wizard and replaces pages with the same key but only if the page was not already
     * visited. This method can be used if a page of the wizard is depending on the result of a previous one. NOTE: The
     * size of {@code page} is not considered anymore after {@code start(...)} was called.
     *
     * @param key  The key the page is associated with.
     * @param page The page to add to the wizard.
     */
    public void putPage(@NotNull String key, @NotNull EmbeddedWizardPage<?> page) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(page);
        if (history.contains(key)) {
            throw new IllegalStateException("A page already visited can not be replaced");
        }
        visitablePages.put(key, page);
    }

    @NotNull
    public EmbeddedWizardPage<?> getPage(@NotNull String key) {
        Objects.requireNonNull(key);
        if (visitablePages.containsKey(key)) {
            return visitablePages.get(key);
        } else {
            throw new NoSuchElementException("Could not find wizard page with id \"" + key + "\"");
        }
    }

    @NotNull
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return atBeginning.getReadOnlyProperty();
    }

    public boolean isAtBeginning() {
        return atBeginningProperty().getValue();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    @NotNull
    public ReadOnlyBooleanProperty atFinishProperty() {
        return atFinish.getReadOnlyProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public boolean isAtFinish() {
        return atFinishProperty().getValue();
    }

    @NotNull
    public ReadOnlyObjectProperty<EmbeddedWizardPage<?>> currentPageProperty() {
        return currentPage.getReadOnlyProperty();
    }

    @NotNull
    public EmbeddedWizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
    }

    /**
     * Returns the property holding whether the current page is currently changed.
     *
     * @return The property holding whether the current page is currently changed.
     */
    @NotNull
    public ReadOnlyBooleanProperty changingPageProperty() {
        return changingPage.getReadOnlyProperty();
    }

    /**
     * Checks whether this wizard is currently changing its page.
     *
     * @return {@code true} only if this wizard is currently changing its page.
     */
    public boolean isChangingPage() {
        return changingPageProperty().getValue();
    }

    @NotNull
    public ReadOnlyObjectProperty<WizardState> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @NotNull
    public WizardState getState() {
        return stateProperty().get();
    }

    /**
     * Returns a list of all visited pages if the wizard finished.
     */
    @NotNull
    public Optional<ArrayList<String>> getVisitedPages() {
        return Optional.ofNullable(getState() == WizardState.FINISHED ? Collections.list(history.elements()) : null);
    }
}
