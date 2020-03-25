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

import bayern.steinbrecher.wizard.WizardableController;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Represents controller for Selection.fxml.
 *
 * @param <T> The type of the objects being able to select.
 * @author Stefan Huber
 * @since 1.4
 */
public class SelectionController<T extends Comparable<T>> extends WizardableController<Optional<Set<T>>> {

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

        optionsListView.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            optionsProperty.entrySet().stream()
                    .filter(entry -> !entry.getValue().isPresent())
                    .forEach(entry -> {
                        CheckBox newItem = new CheckBox(entry.getKey().toString());
                        newItem.selectedProperty().addListener(selectionChange);
                        entry.setValue(Optional.of(newItem));
                    });
            List<CheckBox> listItems = optionsProperty.values()
                    .stream()
                    .map(Optional::get)
                    .sorted((c, d) -> c.getText().compareToIgnoreCase(d.getText()))
                    .collect(Collectors.toList());
            return new FilteredList<>(
                    FXCollections.observableList(listItems),
                    i -> {
                        String searchCriterion = listSearch.getText();
                        return searchCriterion == null
                        || searchCriterion.isBlank()
                        || i.getText().toLowerCase(Locale.ROOT)
                                .contains(searchCriterion.toLowerCase(Locale.ROOT));
                    }
            );
        }, optionsProperty));

        HBox.setHgrow(optionsListView, Priority.ALWAYS);
    }

    /**
     * Removes all options and replaces them with the new list of options.
     *
     * @param options The list of new options.
     */
    public void setOptions(Set<T> options) {
        optionsProperty.set(FXCollections.observableMap(
                options.stream().collect(Collectors.toMap(op -> op, op -> Optional.empty()))));
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectAllOptions() {
        optionsListView.getItems()
                .stream()
                .forEach(cb -> Platform.runLater(() -> cb.setSelected(true)));
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectNoOption() {
        optionsListView.getItems()
                .stream()
                .forEach(cb -> Platform.runLater(() -> cb.setSelected(false)));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns the property representing the number of currently selected fields.
     *
     * @return The property representing the number of currently selected fields.
     */
    public ReadOnlyIntegerProperty selectedCountProperty() {
        return selectedCount.getReadOnlyProperty();
    }

    /**
     * Returns the number of currently selected fields.
     *
     * @return The number of currently selected fields.
     */
    public int getSelectedCount() {
        return selectedCount.get();
    }

    /**
     * Returns the property representing the total number of entries to select.
     *
     * @return The property representing the total number of entries to select.
     */
    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    /**
     * The total number of entries to select.
     *
     * @return The total number of entries to select.
     */
    public int getTotalCount() {
        return totalCount.get();
    }

    /**
     * Returns the property representing a boolean indicating whether no entry is currently selected.
     *
     * @return The property representing a boolean indicating whether no entry is currently selected.
     */
    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected.getReadOnlyProperty();
    }

    /**
     * Checks whether currently no entry is selected.
     *
     * @return {@code true} only if no entry is selected.
     */
    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    /**
     * Returns the property representing a boolean indicating whether every entry is currently selected.
     *
     * @return The property representing a boolean indicating whether every entry is currently selected.
     */
    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected.getReadOnlyProperty();
    }

    /**
     * Checks whether currently every entry is selected.
     *
     * @return {@code true} only if every entry is selected.
     */
    public boolean isAllSelected() {
        return allSelected.get();
    }
}
