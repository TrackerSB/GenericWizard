package bayern.steinbrecher.wizard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 1.23
 */
public abstract class StandaloneWizardPageController<T extends Optional<?>> extends WizardPageController<T> {
    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();

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

    @FXML
    private void close() {
        if (isValid()) {
            getStage()
                    .close();
        }
    }
}
