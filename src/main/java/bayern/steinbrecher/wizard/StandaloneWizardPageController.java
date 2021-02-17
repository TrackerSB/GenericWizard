package bayern.steinbrecher.wizard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 1.23
 */
public class StandaloneWizardPageController<T extends Optional<?>> extends WizardPageController<T> {
    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>(this, "stage", null);
    private final StringProperty closeText
            = new SimpleStringProperty(this, "closeText", null);
    @FXML
    private Pane contentHolder;

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        setCloseText(null);
    }

    /**
     * @since 1.35
     */
    void setContent(Node content){
        contentHolder.getChildren()
                .add(content);
    }

    /**
     * @since 1.35
     */
    @Override
    protected T calculateResult() {
        /* NOTE To be able to use this class as a controller in a FXML file this class has to allow the creation of
         * instances, i.e. it must not be abstract.
         */
        throw new UnsupportedOperationException("The default controller implementation does not yield any result");
    }

    @NotNull
    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    public void setStage(Stage stage) {
        stageProperty()
                .set(stage);
    }

    public Stage getStage() {
        return stageProperty()
                .get();
    }

    /**
     * @since 1.26
     */
    @NotNull
    public StringProperty closeTextProperty() {
        return closeText;
    }

    /**
     * @since 1.26
     */
    public void setCloseText(@Nullable String closeText) {
        closeTextProperty()
                .set(closeText == null ? getResourceValue("close") : closeText);
    }

    /**
     * @since 1.26
     */
    @NotNull
    public String getCloseText() {
        return closeTextProperty()
                .get();
    }

    @FXML
    @SuppressWarnings("unused")
    private void close() {
        if (getStage() == null) {
            throw new IllegalStateException("Can not close stage since there is no stage set");
        } else {
            if (isValid()) {
                getStage()
                        .close();
            }
        }
    }
}
