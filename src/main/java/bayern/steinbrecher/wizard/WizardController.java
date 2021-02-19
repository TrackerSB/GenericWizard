package bayern.steinbrecher.wizard;

import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.beans.binding.Bindings;
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
import javafx.scene.Parent;
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
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
    private static final String WIZARD_CONTENT_STYLECLASS = "wizard-content";
    private static final Duration SWIPE_DURATION = Duration.seconds(0.75);
    /**
     * The percentage of height/width the wizard has to have initially.
     */
    private static final double MAX_SIZE_FACTOR = 0.8;

    private final StringProperty currentIndex = new SimpleStringProperty();
    private final ReadOnlyObjectWrapper<EmbeddedWizardPage<?>> currentPage = new ReadOnlyObjectWrapper<>(null);

    private final MapProperty<String, WizardPage<?, ?>> visitablePages = new SimpleMapProperty<>();
    private final ReadOnlyBooleanWrapper atBeginning = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper atFinish = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper changingPage = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper currentPageValid = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper previousDisallowed = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper nextDisallowed = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper finishDisallowed = new ReadOnlyBooleanWrapper();

    private final ReadOnlyObjectWrapper<WizardState> state = new ReadOnlyObjectWrapper<>(WizardState.RUNNING);

    private final Stack<String> history = new Stack<>();
    @FXML
    private ScrollPane scrollContent;
    @FXML
    private StackPane contents;

    /**
     * {@code null} --> dont swipe, just change
     * {@code true} --> swipe to left
     * {@code false} --> swipe to right.
     */
    private Boolean swipeToLeft = null;

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        visitablePages.addListener((obs, previousVisitablePages, currentVisitablePages) -> {
            currentIndex.addListener((obsI, previousIndex, currentIndex) -> {
                changingPage.set(true);
                ObservableList<Node> addedContents = contents.getChildren();
                Optional<Node> optCurrentPane
                        = Optional.ofNullable(addedContents.isEmpty() ? null : addedContents.get(0));
                assert optCurrentPane.isEmpty()
                        || optCurrentPane.get() instanceof Pane : "The current content of this wizard is not a pane.";
                Consumer<Node> removeCurrentPane = currentPane -> {
                    currentPane.getStyleClass().remove(WIZARD_CONTENT_STYLECLASS);
                    if (!contents.getChildren().remove(currentPane)) {
                        LOGGER.log(Level.SEVERE, "The currently shown content of the wizard could not be removed.");
                    }
                };

                WizardPage<?, ?> nextPage = currentVisitablePages.get(currentIndex);
                EmbeddedWizardPage<?> nextEmbeddedPage;
                try {
                    nextEmbeddedPage = nextPage.generateEmbeddableWizardPage();
                } catch (LoadException ex) {
                    throw new IllegalStateException(
                            String.format("Could not create wizard page with index %s", currentIndex), ex);
                }
                Parent nextPane = nextEmbeddedPage.getRoot();
                HBox.setHgrow(nextPane, Priority.ALWAYS);
                VBox.setVgrow(nextPane, Priority.ALWAYS);
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
                    //CHECKSTYLE.OFF: MagicNumber - The factor 3 is needed to make the initial x position outside the
                    // view.
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

                atFinish.set(nextPage.isFinish());
                currentPage.setValue(nextEmbeddedPage);
            });
            history.clear();
            swipeToLeft = null;
            currentIndex.set(WizardPage.FIRST_PAGE_KEY);
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        scrollContent.setMaxHeight(screenSize.getHeight() * MAX_SIZE_FACTOR);
        scrollContent.setMaxWidth(screenSize.getWidth() * MAX_SIZE_FACTOR);
        currentPage.addListener((obs, previousPage, currentPage) -> {
            if (currentPage == null) {
                currentPageValid.unbind();
                currentPageValid.set(false);
            } else {
                currentPageValid.bind(currentPage.validProperty());
            }
        });
        previousDisallowed.bind(changingPage.or(atBeginningProperty()));
        nextDisallowed.bind(
                changingPage.or(currentPageProperty().isNull())
                        .or(currentPageValid.not())
                        .or(Bindings.createBooleanBinding(
                                // FIXME Does not recognize changing hasNextFunctionProperty()
                                () -> getCurrentPage() == null || !getCurrentPage()
                                        .isHasNextFunction(), currentPageProperty())));
        finishDisallowed.bind(
                changingPage.or(atFinishProperty().not())
                        .or(currentPageProperty().isNull())
                        .or(currentPageValid.not()));

        // Initialize all bindings
        currentIndex.set(WizardPage.FIRST_PAGE_KEY);
    }

    @FXML
    @SuppressWarnings("unused")
    private void showPrevious() {
        if (!isPreviousDisallowed()) {
            history.pop(); // Pop current index
            atBeginning.set(history.size() < 2);
            swipeToLeft = false;
            currentIndex.set(history.peek());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void showNext() {
        if (!isNextDisallowed()) {
            EmbeddedWizardPage<?> page = getCurrentPage();
            Supplier<String> nextFunction = page.getNextFunction();
            if (page.isHasNextFunction() && page.isValid()) {
                String nextIndex = nextFunction.get();
                if (nextIndex == null || !getVisitablePages().containsKey(nextIndex)) {
                    throw new PageNotFoundException(
                            String.format("Wizard contains no page with key \"%s\".", nextIndex));
                }
                history.push(nextIndex);
                atBeginning.set(false);
                swipeToLeft = true;
                currentIndex.set(nextIndex);
            }
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void finish() {
        if (getState() == WizardState.RUNNING && !isFinishDisallowed()) {
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

    /**
     * @since 1.52
     */
    @NotNull
    public MapProperty<String, WizardPage<?, ?>> visitablePagesProperty() {
        return visitablePages;
    }

    /**
     * @since 1.52
     */
    @NotNull
    public Map<String, WizardPage<?, ?>> getVisitablePages() {
        return visitablePagesProperty().get();
    }

    /**
     * Sets a new map of visitable pages. NOTE: Calling this method causes the wizard to reset to the first page and
     * clear the history.
     *
     * @param visitablePages The map of pages to set.
     * @since 1.52
     */
    public void setVisitablePages(@NotNull Map<String, WizardPage<?, ?>> visitablePages) {
        if (!visitablePages.containsKey(WizardPage.FIRST_PAGE_KEY)) {
            throw new IllegalArgumentException("Map of pages must have a key WizardPage.FIRST_PAGE_KEY");
        }

        this.visitablePages.set(FXCollections.observableMap(visitablePages));
    }

    /**
     * Adds the given page to the wizard and replaces pages with the same key but only if the page was not already
     * visited. This method can be used if a page of the wizard is depending on the result of a previous one. NOTE: The
     * size of {@code page} is not considered anymore after {@code start(...)} was called.
     *
     * @param key  The key the page is associated with.
     * @param page The page to add to the wizard.
     * @since 1.52
     */
    public void putPage(@NotNull String key, @NotNull WizardPage<?, ?> page) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(page);
        if (history.contains(key)) {
            throw new IllegalStateException("A page already visited can not be replaced");
        }
        visitablePages.put(key, page);
    }

    @NotNull
    public Optional<ArrayList<String>> getVisitedPages() {
        return Optional.ofNullable(getState() == WizardState.FINISHED ? Collections.list(history.elements()) : null);
    }

    @NotNull
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return atBeginning.getReadOnlyProperty();
    }

    public boolean isAtBeginning() {
        return atBeginningProperty().getValue();
    }

    @NotNull
    public ReadOnlyBooleanProperty atFinishProperty() {
        return atFinish.getReadOnlyProperty();
    }

    public boolean isAtFinish() {
        return atFinishProperty().getValue();
    }

    // Interface methods for Wizard

    @NotNull
    ReadOnlyObjectProperty<EmbeddedWizardPage<?>> currentPageProperty() {
        return currentPage.getReadOnlyProperty();
    }

    EmbeddedWizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
    }

    @NotNull
    ReadOnlyObjectProperty<WizardState> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @NotNull
    WizardState getState() {
        return stateProperty().get();
    }

    // Interface methods for FXML
    // NOTE Make private and annotate with @FXML as soon as supported

    @FXML
    public ReadOnlyBooleanProperty previousDisallowedProperty() {
        return previousDisallowed.getReadOnlyProperty();
    }

    public boolean isPreviousDisallowed() {
        return previousDisallowedProperty().getValue();
    }

    @NotNull
    public ReadOnlyBooleanProperty nextDisallowedProperty() {
        return nextDisallowed.getReadOnlyProperty();
    }

    public boolean isNextDisallowed() {
        return nextDisallowedProperty().getValue();
    }

    @NotNull
    public ReadOnlyBooleanProperty finishDisallowedProperty() {
        return finishDisallowed.getReadOnlyProperty();
    }

    public boolean isFinishDisallowed() {
        return finishDisallowedProperty().getValue();
    }
}
