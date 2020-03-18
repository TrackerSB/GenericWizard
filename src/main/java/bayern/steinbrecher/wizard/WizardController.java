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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Contains the controller of the wizard.
 *
 * @author Stefan Huber
 * @since 1.0
 */
public class WizardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(WizardController.class.getName());
    /**
     * The percentage of height/width the wizard has to have initially.
     */
    private static final double MAX_SIZE_FACTOR = 0.8;
    private final StringProperty currentIndex = new SimpleStringProperty(this, "currentIndex");
    /**
     * NOTE The initial dummy page is needed since the current page may be already requested before there is a chance to
     * specify the pages of the wizard.
     */
    private final ReadOnlyObjectWrapper<WizardPage<?>> currentPage
            = new ReadOnlyObjectWrapper<>(this, "currentPage", new WizardPage<Void>(null, null, false, () -> null));
    private final MapProperty<String, WizardPage<?>> pages = new SimpleMapProperty<>();
    private final ReadOnlyBooleanWrapper atBeginning = new ReadOnlyBooleanWrapper(this, "atBeginning", true);
    private final ReadOnlyBooleanWrapper atFinish = new ReadOnlyBooleanWrapper(this, "atEnd");
    private final ReadOnlyBooleanWrapper finished = new ReadOnlyBooleanWrapper(this, "finished", false);
    private final ReadOnlyBooleanWrapper changingPage = new ReadOnlyBooleanWrapper(this, "swiping", false);
    private final Stack<String> history = new Stack<>();
    @FXML
    private ScrollPane scrollContent;
    @FXML
    private StackPane contents;
    @FXML
    private ButtonBar controls;
    private Stage stage;
    private double xDragOffset;
    private double yDragOffset;
    private static final String WIZARD_CONTENT_STYLECLASS = "wizard-content";
    private static final Duration SWIPE_DURATION = Duration.seconds(0.75);

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pages.addListener((obs, oldVal, newVal) -> {
            newVal.values().stream()
                    .map(WizardPage::getRoot)
                    .forEach(pane -> {
                        HBox.setHgrow(pane, Priority.ALWAYS);
                        VBox.setVgrow(pane, Priority.ALWAYS);
                    });
            currentIndex.addListener((obsI, oldValI, newValI) -> {
                WizardPage<?> newPage = pages.get(newValI);
                atFinish.set(newPage.isFinish());
                currentPage.setValue(newPage);
            });
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        scrollContent.setMaxHeight(screenSize.getHeight() * MAX_SIZE_FACTOR);
        scrollContent.setMaxWidth(screenSize.getWidth() * MAX_SIZE_FACTOR);
        contents.setOnMousePressed(mevt -> {
            xDragOffset = stage.getX() - mevt.getScreenX();
            yDragOffset = stage.getY() - mevt.getScreenY();
        });
        contents.setOnMouseDragged(mevt -> {
            stage.setX(mevt.getScreenX() + xDragOffset);
            stage.setY(mevt.getScreenY() + yDragOffset);
        });
        controls.setOnMousePressed(mevt -> {
            xDragOffset = stage.getX() - mevt.getScreenX();
            yDragOffset = stage.getY() - mevt.getScreenY();
        });
        controls.setOnMouseDragged(mevt -> {
            stage.setX(mevt.getScreenX() + xDragOffset);
            stage.setY(mevt.getScreenY() + yDragOffset);
        });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void showPrevious() {
        if (!atBeginning.get() && !changingPage.get()) {
            history.pop(); //Pop current index
            atBeginning.set(history.size() < 2);
            currentIndex.set(history.peek());
            updatePage(Optional.of(false));
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void showNext() {
        if (currentPage.getValue().isValid() && !changingPage.get()) {
            WizardPage<?> page = currentPage.getValue();
            Callable<String> nextFunction = page.getNextFunction();
            if (page.isHasNextFunction() && page.isValid()) {
                try {
                    String nextIndex = nextFunction.call();
                    if (!pages.containsKey(nextIndex)) {
                        throw new PageNotFoundException(
                                "Wizard contains no page with key \""
                                + nextIndex + "\".");
                    }
                    currentIndex.set(nextIndex);
                    history.push(currentIndex.get());
                    atBeginning.set(false);
                    updatePage(Optional.of(true));
                } catch (Exception ex) {
                    throw new IllegalCallableException(
                            "A valid function or a next function of page \""
                            + currentIndex.get() + "\" has thrown an exception",
                            ex);
                }
            }
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void finish() {
        if (currentPage.getValue().isValid() && atFinish.get()) {
            finished.set(true);
            stage.close();
        }
    }

    /**
     * Sets the {@link Stage} this controller has to use.
     *
     * @param stage The {@link Stage} to use.
     */
    void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Returns the property holding all pages visitable.
     *
     * @return The property holding all pages visitable.
     */
    public MapProperty<String, WizardPage<?>> pagesProperty() {
        return pages;
    }

    /**
     * Returns all visitable pages.
     *
     * @return All visitable pages.
     */
    public Map<String, WizardPage<?>> getPages() {
        return pages.get();
    }

    //Optional#empty() == dont swipe, just change
    //true == swipe to left
    //false == swipe to right
    private void updatePage(Optional<Boolean> swipeToLeft) {
        changingPage.set(true);
        ObservableList<Node> addedContents = contents.getChildren();
        Optional<Node> optCurrentPane = Optional.ofNullable(addedContents.isEmpty() ? null : addedContents.get(0));
        assert !optCurrentPane.isPresent()
                || optCurrentPane.get() instanceof Pane : "The current content of this wizard is not a pane.";
        Consumer<Node> removeCurrentPane = currentPane -> {
            currentPane.getStyleClass().remove(WIZARD_CONTENT_STYLECLASS);
            if (!contents.getChildren().remove(currentPane)) {
                LOGGER.log(Level.SEVERE, "The currently shown content of the wizard could not be removed.");
            }
        };

        Pane nextPane = pages.get(currentIndex.get()).getRoot();
        if (!optCurrentPane.isPresent() || optCurrentPane.get() != nextPane) {
            contents.getChildren().add(nextPane);
            nextPane.getStyleClass().add(WIZARD_CONTENT_STYLECLASS);
        }

        swipeToLeft.ifPresentOrElse(swipeLeft -> {
            double halfParentWidth = nextPane.getParent().getLayoutBounds().getWidth() / 2;
            double halfParentHeight = nextPane.getParent().getLayoutBounds().getHeight() / 2;
            double marginInScene = nextPane.getScene().getWidth() / 2 - halfParentWidth;
            //CHECKSTYLE.OFF: MagicNumber - The factor 3 is needed to make the initial x position outside the view.
            double xRightOuter = 3 * halfParentWidth + marginInScene;
            //CHECKSTYLE.ON: MagicNumber
            double xLeftOuter = -halfParentWidth - marginInScene;

            ParallelTransition overallTrans = new ParallelTransition();

            //Swipe new element in
            MoveTo initialMoveIn = new MoveTo(swipeLeft ? xRightOuter : xLeftOuter, halfParentHeight);
            HLineTo hlineIn = new HLineTo(halfParentWidth);
            Path pathIn = new Path(initialMoveIn, hlineIn);
            PathTransition pathTransIn = new PathTransition(SWIPE_DURATION, pathIn, nextPane);
            overallTrans.getChildren().add(pathTransIn);

            optCurrentPane.ifPresent(currentPane -> {
                //Swipe old element out
                MoveTo initialMoveOut = new MoveTo(halfParentWidth, halfParentHeight);
                HLineTo hlineOut = new HLineTo(swipeLeft ? xLeftOuter : xRightOuter);
                Path pathOut = new Path(initialMoveOut, hlineOut);
                PathTransition pathTransOut = new PathTransition(SWIPE_DURATION, pathOut, currentPane);
                pathTransOut.setOnFinished(aevt -> removeCurrentPane.accept(currentPane));
                overallTrans.getChildren().add(pathTransOut);
            });

            overallTrans.setOnFinished(aevt -> changingPage.set(false));
            overallTrans.playFromStart();
        }, () -> {
            optCurrentPane.ifPresent(currentPane -> removeCurrentPane.accept(currentPane));
            changingPage.set(false);
        });
    }

    /**
     * Sets a new map of visitable pages. NOTE: Calling this method causes the wizard to reset to the first page and
     * clear the history.
     *
     * @param pages The map of pages to set.
     */
    public void setPages(Map<String, WizardPage<?>> pages) {
        if (!pages.containsKey(WizardPage.FIRST_PAGE_KEY)) {
            throw new IllegalArgumentException("Map of pages must have a key WizardPage.FIRST_PAGE_KEY");
        }

        currentIndex.set(WizardPage.FIRST_PAGE_KEY);
        this.pages.set(FXCollections.observableMap(pages));
        currentPage.setValue(pages.get(WizardPage.FIRST_PAGE_KEY));
        history.clear();
        history.push(WizardPage.FIRST_PAGE_KEY);
        updatePage(Optional.empty());
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
        if (history.contains(key)) {
            throw new IllegalStateException("A page already visited can not be replaced");
        }
        pages.put(key, page);
    }

    /**
     * Returns the property representing whether the wizard is finished. NOTE: It is not finished when it was closed
     * without using the "finish" button.
     *
     * @return The property representing whether the wizard is finished.
     */
    public ReadOnlyBooleanProperty finishedProperty() {
        return finished.getReadOnlyProperty();
    }

    /**
     * Checks whether the wizard is finished. NOTE: It is not finished when it was closed without using the "finish"
     * button.
     *
     * @return {@code true} only if the wizard is finished.
     */
    public boolean isFinished() {
        return finishedProperty().getValue();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return atBeginning.getReadOnlyProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public boolean isAtBeginning() {
        return atBeginningProperty().getValue();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
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

    /**
     * Returns the property holding the currently shown page.
     *
     * @return The property holding the currently shown page.
     */
    public ReadOnlyObjectProperty<WizardPage<?>> currentPageProperty() {
        return currentPage.getReadOnlyProperty();
    }

    /**
     * Returns the currently shown page.
     *
     * @return The currently shown page.
     */
    public WizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
    }

    /**
     * Returns the property holding whether the current page is currently changed.
     *
     * @return The property holding whether the current page is currently changed.
     */
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

    /**
     * Returns the results of all pages visited in a sequence to an end.
     *
     * @return {@code Optional.empty()} only if the wizard is not finished yet, otherwise the results of the visited
     * pages.
     * @throws IllegalCallableException Only thrown if thrown by one of the result functions of the visited pages.
     */
    public Optional<Map<String, ?>> getResults() {
        Optional<Map<String, ?>> resultsResult;
        if (isFinished()) {
            Map<String, Object> results = new HashMap<>();
            history.forEach(key -> {
                try {
                    results.put(key, pages.get(key).getResultFunction().call());
                } catch (Exception ex) {
                    throw new IllegalCallableException(
                            "The result function of wizard page \""
                            + key
                            + "\" has thrown an exception", ex);
                }
            });
            resultsResult = Optional.of(results);
        } else {
            resultsResult = Optional.empty();
        }
        return resultsResult;
    }
}
