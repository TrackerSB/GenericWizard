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
package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.wizard.WizardPageController;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;

/**
 * Represents controller for Selection.fxml.
 *
 * @param <T> The type of the objects being able to select.
 * @author Stefan Huber
 * @since 1.4
 */
public class SelectionController<T extends Comparable<? extends T>> extends WizardPageController<Optional<Set<T>>> {

    private final MapProperty<T, Optional<CheckBox>> optionsProperty
            = new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final ReadOnlyIntegerWrapper selectedCount = new ReadOnlyIntegerWrapper(this, "selectedCount");
    private final ReadOnlyIntegerProperty totalCount = optionsProperty.sizeProperty();
    private final ReadOnlyBooleanWrapper nothingSelected = new ReadOnlyBooleanWrapper(this, "nothingSelected");
    private final ReadOnlyBooleanWrapper allSelected = new ReadOnlyBooleanWrapper(this, "allSelected");
    @FXML
    private Label outOf;
    @FXML
    private ListView<CheckBox> optionsListView; //TODO Use ListView<T>
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> selectedCount.set(selectedCount.get() + (newVal ? 1 : -1));
    @FXML
    private TextField listSearch;

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        outOf.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> getResourceValue("chosenOutOf", getSelectedCount(), getTotalCount()),
                        selectedCount,
                        totalCount)
                );

        nothingSelected.bind(selectedCount.lessThanOrEqualTo(0));
        allSelected.bind(selectedCount.greaterThanOrEqualTo(totalCount));
        bindValidProperty(nothingSelected.not());

        ObservableList<CheckBox> listItems = FXCollections.observableArrayList();
        FilteredList<CheckBox> filteredListItems = listItems.filtered(cb -> true);
        optionsListView.itemsProperty().bind(new SimpleObjectProperty<>(filteredListItems));
        optionsProperty.addListener((obs, oldVal, newVal) -> {
            newVal.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .forEach(entry -> {
                        CheckBox newItem = new CheckBox(entry.getKey().toString());
                        newItem.selectedProperty().addListener(selectionChange);
                        entry.setValue(Optional.of(newItem));
                    });
            listItems.setAll(newVal.values()
                    .stream()
                    .map(Optional::get)
                    .sorted((c, d) -> c.getText().compareToIgnoreCase(d.getText()))
                    .collect(Collectors.toList()));
        });
        listSearch.textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    filteredListItems.setPredicate(cb -> {
                        String item = cb.getText();
                        return item == null
                                || item.isBlank()
                                || item.toLowerCase(Locale.ROOT)
                                .contains(newVal.toLowerCase(Locale.ROOT));
                    });
                });

        HBox.setHgrow(optionsListView, Priority.ALWAYS);
    }

    /**
     * Removes all options and replaces them with the new list of options.
     *
     * @param options The list of new options.
     */
    public void setOptions(@NotNull Set<T> options) {
        optionsProperty.set(FXCollections.observableMap(
                options.stream().collect(Collectors.toMap(op -> op, op -> Optional.empty()))));
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectAllOptions() {
        optionsListView.getItems()
                .forEach(cb -> Platform.runLater(() -> cb.setSelected(true)));
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectNoOption() {
        optionsListView.getItems()
                .forEach(cb -> Platform.runLater(() -> cb.setSelected(false)));
    }

    @Override
    protected Optional<Set<T>> calculateResult() {
        Set<T> selection = new HashSet<>();
        optionsProperty.forEach((option, checkbox) -> {
            checkbox.ifPresent(c -> {
                if (c.isSelected()) {
                    selection.add(option);
                }
            });
        });
        return Optional.of(selection);
    }

    @NotNull
    public ReadOnlyIntegerProperty selectedCountProperty() {
        return selectedCount.getReadOnlyProperty();
    }

    public int getSelectedCount() {
        return selectedCount.get();
    }

    @NotNull
    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    @NotNull
    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected.getReadOnlyProperty();
    }

    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    @NotNull
    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected.getReadOnlyProperty();
    }

    public boolean isAllSelected() {
        return allSelected.get();
    }
}
