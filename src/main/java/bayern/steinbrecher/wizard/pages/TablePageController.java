package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.javaUtility.CSVFormat;
import bayern.steinbrecher.javaUtility.IOUtility;
import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 1.51
 */
public class TablePageController extends StandaloneWizardPageController<Optional<Void>> {

    private static final FileChooser CSV_SAVE_PATH = new FileChooser();
    @FXML
    private TableView<List<String>> resultView;
    private final ReadOnlyObjectWrapper<List<String>> headings = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<SortedList<List<String>>> results = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper empty = new ReadOnlyBooleanWrapper();

    public TablePageController() {
        CSV_SAVE_PATH.getExtensionFilters()
                .add(new ExtensionFilter("CSV", "*.csv"));
    }

    @FXML
    public void initialize() {
        empty.bind(Bindings.createBooleanBinding(
                () -> results.get() == null // Has no contents
                        || results.get().isEmpty() // Has no lines
                        || results.get().stream().mapToLong(List::size).sum() <= 0, // Has no columns
                results));
        bindValidProperty(emptyProperty().not());

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
                    TableColumn<List<String>, String> column
                            = new TableColumn<>(heading); //NOPMD - Each iteration defines an unique column.
                    final int fixedI = i;
                    column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(fixedI)));
                    resultView.getColumns().add(column);
                }

                if (newVal.size() > 1) { //NOPMD - Check whether there are row entries besides the headings.
                    ObservableList<List<String>> items
                            = newVal.stream()
                            .skip(1)
                            .map(givenRow -> {
                                List<String> itemsRow = new ArrayList<>(numColumns);
                                for (int i = 0; i < numColumns; i++) {
                                    String cellValue = i >= givenRow.size() ? "" : givenRow.get(i);
                                    //Each iteration defines an observable cell entry.
                                    itemsRow.add(cellValue);
                                }
                                return itemsRow;
                            }).collect(
                                    FXCollections::observableArrayList,
                                    ObservableList::add,
                                    ObservableList::addAll);
                    resultView.setItems(items);
                }
            }
        });
        HBox.setHgrow(resultView, Priority.ALWAYS);
        VBox.setVgrow(resultView, Priority.ALWAYS);
    }

    @FXML
    private void export() throws IOException {
        final File savePath = CSV_SAVE_PATH.showSaveDialog(getStage());
        if (savePath != null) {
            IOUtility.writeCSV(Path.of(savePath.toURI()), getResults(), CSVFormat.EXCEL);
        }
    }

    @NotNull
    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }

    @NotNull
    public ReadOnlyObjectProperty<List<String>> headingsProperty(){
        return headings.getReadOnlyProperty();
    }

    @NotNull
    public List<String> getHeadings(){
        return headingsProperty().get();
    }

    @NotNull
    public ReadOnlyObjectProperty<SortedList<List<String>>> resultsProperty() {
        return results.getReadOnlyProperty();
    }

    /**
     * @return This content of the table sorted as the content is currently sorted in the table view.
     */
    @NotNull
    public SortedList<List<String>> getResults() {
        return resultsProperty().get();
    }

    /**
     * @param contents The first entry is assumed to represent the headings of the table columns.
     */
    public void setContents(@NotNull List<List<String>> contents) {
        headings.set(Objects.requireNonNull(contents).get(0));
        results.set(
                contents.stream()
                        .skip(1)
                        .collect(() -> new SortedList<>(
                                FXCollections.observableArrayList()),
                                ObservableList::add,
                                ObservableList::addAll
                        )
        );
        getResults()
                .comparatorProperty()
                .bind(resultView.comparatorProperty());
    }

    @NotNull
    public ReadOnlyBooleanProperty emptyProperty() {
        return empty.getReadOnlyProperty();
    }

    public boolean isEmpty() {
        return emptyProperty().get();
    }
}
