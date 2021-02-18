package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 1.51
 */
public class TablePageController extends StandaloneWizardPageController<Optional<Void>> {

    @FXML
    private TableView<List<ReadOnlyStringProperty>> resultView;
    private final ObjectProperty<List<List<String>>> results = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanWrapper empty = new ReadOnlyBooleanWrapper();

    @FXML
    public void initialize() {
        empty.bind(Bindings.createBooleanBinding(
                () -> results.get() == null
                        || results.get().size() < 2
                        || results.get().stream().mapToLong(List::size).sum() <= 0,
                results));
        bindValidProperty(emptyProperty());

        results.addListener((obs, oldVal, newVal) -> {
            resultView.getItems().clear();
            resultView.getColumns().clear();

            if (newVal.size() > 0) {
                int numColumns = newVal.stream()
                        .mapToInt(List::size)
                        .max()
                        .orElse(0);
                List<String> headings = newVal.get(0);
                for (int i = 0; i < numColumns; i++) {
                    String heading = i >= headings.size() ? "" : headings.get(i);
                    TableColumn<List<ReadOnlyStringProperty>, String> column
                            = new TableColumn<>(heading); //NOPMD - Each iteration defines an unique column.
                    final int fixedI = i;
                    column.setCellValueFactory(param -> param.getValue().get(fixedI));
                    resultView.getColumns().add(column);
                }

                if (newVal.size() > 1) { //NOPMD - Check whether there are row entries besides the headings.
                    ObservableList<List<ReadOnlyStringProperty>> items = newVal.subList(1, newVal.size()).stream()
                            .map(givenRow -> {
                                List<ReadOnlyStringProperty> itemsRow = new ArrayList<>(numColumns);
                                for (int i = 0; i < numColumns; i++) {
                                    String cellValue = i >= givenRow.size() ? "" : givenRow.get(i);
                                    //Each iteration defines an observable cell entry.
                                    itemsRow.add(new SimpleStringProperty(cellValue)); //NOPMD
                                }
                                return itemsRow;
                            }).collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
                    resultView.setItems(items);
                }
            }
        });
        HBox.setHgrow(resultView, Priority.ALWAYS);
        VBox.setVgrow(resultView, Priority.ALWAYS);
    }

    @NotNull
    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }

    @NotNull
    public ObjectProperty<List<List<String>>> resultsProperty() {
        return results;
    }

    @NotNull
    public List<List<String>> getResults() {
        return resultsProperty().get();
    }

    public void setResults(@NotNull List<List<String>> results) {
        resultsProperty().set(Objects.requireNonNull(results));
    }

    @NotNull
    public ReadOnlyBooleanProperty emptyProperty() {
        return empty.getReadOnlyProperty();
    }

    public boolean isEmpty() {
        return emptyProperty().get();
    }
}
