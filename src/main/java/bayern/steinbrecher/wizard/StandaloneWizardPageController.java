package bayern.steinbrecher.wizard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 1.23
 */
public abstract class StandaloneWizardPageController<T extends Optional<?>> extends WizardPageController<T> {
    @FXML
    private ResourceBundle resources;
    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>(this, "stage", null);
    private final StringProperty closeText
            = new SimpleStringProperty(this, "closeText", null);

    @FXML
    private void initialize() {
        setCloseText(null);
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
                .set(closeText == null ? resources.getString("close") : closeText);
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
