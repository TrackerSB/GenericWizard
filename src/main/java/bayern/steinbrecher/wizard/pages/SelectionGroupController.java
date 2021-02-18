package bayern.steinbrecher.wizard.pages;

import bayern.steinbrecher.javaUtility.BindingUtility;
import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import com.google.common.collect.BiMap;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The controller of the SelectionGroup.
 *
 * @param <T> The type of the options to select.
 * @param <G> The type of the groups to associate items with.
 * @author Stefan Huber
 * @since 1.51
 */
public class SelectionGroupController<T extends Comparable<T>, G>
        extends StandaloneWizardPageController<Optional<Map<T, G>>> {

    private static final Logger LOGGER = Logger.getLogger(SelectionGroupController.class.getName());
    private final ObservableValue<ObservableList<AssociatedItem>> options
            = new SimpleObjectProperty<>(
                    FXCollections.observableArrayList(i -> new Observable[]{i.itemProperty(), i.groupProperty()}));
    private final ReadOnlyIntegerWrapper totalCount = new ReadOnlyIntegerWrapper();
    //FIXME Since the value should also be unique it should be something like a BiMapProperty.
    private final MapProperty<G, Color> groups
            = new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final ObjectProperty<Optional<G>> currentGroup = new SimpleObjectProperty<>(Optional.empty());
    private final ReadOnlyBooleanWrapper currentGroupSelected = new ReadOnlyBooleanWrapper();
    private final ReadOnlyMapWrapper<G, IntegerProperty> selectedPerGroup
            = new ReadOnlyMapWrapper<>(FXCollections.observableHashMap());
    private final ReadOnlyIntegerWrapper selectedCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyBooleanWrapper nothingSelected = new ReadOnlyBooleanWrapper(true);
    private final ReadOnlyBooleanWrapper allSelected = new ReadOnlyBooleanWrapper();
    @FXML
    private ListView<AssociatedItem> optionsListView;
    @FXML
    private ToggleGroup groupsToggleGroup;
    @FXML
    private VBox groupsBox;
    private RadioButton unselectGroup;
    private final Callback<ListView<AssociatedItem>, ListCell<AssociatedItem>> optionsCellFactory
            = listview -> new ListCell<>() {

        @Override
        protected void updateItem(AssociatedItem item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText(item.getItem().toString());
                Optional<G> newGroup = item.getGroup();

                //TODO Spare creations of CheckBoxes
                CheckBox groupGraphic = new CheckBox();
                groupGraphic.setSelected(newGroup.isPresent());
                Consumer<Boolean> updateGroupGraphic = selected -> {
                    if (selected) {
                        // Got selected
                        item.setGroup(currentGroup.get());
                    } else {
                        if (currentGroup.get().equals(newGroup) || currentGroup.get().isEmpty()) {
                            // Got deselected
                            item.setGroup(Optional.empty());
                        } else {
                            /* Got deselected but has to reselected since the items current group differs from its
                             * previous group
                             */
                            item.setGroup(currentGroup.get());
                        }
                    }
                };
                groupGraphic.selectedProperty()
                        .addListener((obs, oldVal, newVal) -> {
                            updateGroupGraphic.accept(newVal);
                            updateItem(item, empty);
                        });

                if (newGroup.isPresent()) {
                    Color fill = groups.get(newGroup.get());
                    List<Region> boxes = groupGraphic.lookupAll(".box")
                            .stream()
                            // There should be only boxes (which are regions)
                            .filter(node -> node instanceof Region)
                            .map(node -> (Region) node)
                            .collect(Collectors.toList());
                    boxes.forEach(region -> region.setBackground(
                            new Background(new BackgroundFill(fill, CornerRadii.EMPTY, Insets.EMPTY))));
                    groupGraphic.getChildrenUnmodifiable()
                            .addListener((ListChangeListener.Change<? extends Node> change) -> {
                                while (change.next()) {
                                    change.getAddedSubList()
                                            .stream()
                                            .filter(node -> node.getStyleClass().contains("box"))
                                            .filter(node -> node instanceof Region)
                                            .map(node -> (Region) node)
                                            //CHECKSTYLE.OFF: MagicNumber - The range of RGB goes from 0 to 255
                                            .forEach(region -> region.setStyle(
                                                    String.format("-fx-background-color: rgba(%s, %s, %s, %s)",
                                                            255 * fill.getRed(), 255 * fill.getGreen(),
                                                            255 * fill.getBlue(), fill.getOpacity())));
                                    //CHECKSTYLE.ON: MagicNumber
                                }
                            });
                }
                setGraphic(groupGraphic);
                setOnMouseClicked(mevt -> updateGroupGraphic.accept(!groupGraphic.isSelected()));
            }
        }
    };

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        bindValidProperty(nothingSelected.not());
        nothingSelected.bind(selectedCount.lessThanOrEqualTo(0));
        allSelected.bind(selectedCount.greaterThanOrEqualTo(totalCount));
        currentGroup.addListener((obs, oldVal, newVal) -> currentGroupSelected.set(newVal.isPresent()));

        optionsListView.itemsProperty().bind(options);
        optionsListView.setCellFactory(optionsCellFactory);

        unselectGroup = addGroupRadioButton(getResourceValue("deselect"), Optional.empty(), false);
        selectedPerGroup.addListener((MapChangeListener.Change<? extends G, ? extends IntegerProperty> change)
                -> selectedCount.bind(BindingUtility.reduceSum(selectedPerGroup.values().stream())));

        groups.addListener((MapChangeListener.Change<? extends G, ? extends Color> change) -> {
            G group = change.getKey();
            if (change.wasAdded()) {
                selectedPerGroup.put(group, new SimpleIntegerProperty(0));
                RadioButton groupRadioButton = addGroupRadioButton(
                        change.getKey().toString(), Optional.of(group),
                        groups.size() == 1); //NOPMD - Select the first added group
            }
            if (change.wasRemoved()) {
                selectedPerGroup.remove(change.getKey());
                // Remove RadioButton representing the group
                Color removedColor = groups.get(group);
                groupsToggleGroup.getToggles().stream()
                        // Per construction Toggles are always Radiobuttons
                        .filter(toggle -> toggle instanceof RadioButton)
                        .map(toggle -> (RadioButton) toggle)
                        .filter(radiobutton -> {
                            Node graphic = radiobutton.getGraphic();
                            // Per construction graphics are always Rectangles
                            return graphic instanceof Rectangle && ((Rectangle) graphic).getFill().equals(removedColor);
                        })
                        // FIXME Without BiMapProperty the number of found radio buttons is not limited to 1.
                        .findAny()
                        .ifPresentOrElse(
                                radiobutton -> {
                                    if (radiobutton.isSelected()) {
                                        unselectGroup.setSelected(true);
                                    }
                                    groupsToggleGroup.getToggles().remove(radiobutton);
                                },
                                () -> LOGGER.log(Level.WARNING, "Could not find the group radiobutton to remove."));

                // Remove all associations of items with this group.
                options.getValue().stream()
                        .filter(ai -> ai.getGroup().equals(group))
                        .forEach(ai -> ai.setGroup(Optional.empty()));
            }
        });

        options.getValue().addListener((ListChangeListener.Change<? extends AssociatedItem> change) -> {
            while (change.next()) {
                totalCount.set(totalCount.get() + change.getAddedSize() - change.getRemovedSize());
                change.getAddedSubList()
                        .forEach(item ->
                                item.groupProperty().addListener((obs, oldVal, newVal) -> {
                                    oldVal.ifPresent(oldGroup -> {
                                        IntegerProperty countProperty = selectedPerGroup.get(oldGroup);
                                        countProperty.set(countProperty.get() - 1);
                                    });
                                    newVal.ifPresent(newGroup -> {
                                        IntegerProperty countProperty = selectedPerGroup.get(newGroup);
                                        countProperty.set(countProperty.get() + 1);
                                    });
                                }));
            }
        });
        totalCount.set(options.getValue().size());

        HBox.setHgrow(optionsListView, Priority.ALWAYS);
    }

    private RadioButton addGroupRadioButton(String text, Optional<G> group, boolean setSelected) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.textProperty().bind(new SimpleStringProperty(text).concat(" (")
                .concat(group.isPresent() ? selectedPerGroup.get(group.get()) : totalCount.subtract(selectedCount))
                .concat(")"));
        double fontSize = radioButton.getFont().getSize();
        radioButton.setGraphic(new Rectangle(
                fontSize, fontSize, group.isPresent() ? groups.get(group.get()) : Color.TRANSPARENT));
        radioButton.setToggleGroup(groupsToggleGroup);
        radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                currentGroup.set(group);
            }
        });
        radioButton.setSelected(setSelected);
        groupsBox.getChildren().add(radioButton);
        return radioButton;
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectAllOptions() {
        options.getValue()
                .parallelStream()
                .forEach(option ->
                        Platform.runLater(() -> {
                            if (!option.isAssociated()) {
                                option.setGroup(currentGroup.get());
                            }
                        }));
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectNoOption() {
        options.getValue()
                .parallelStream()
                .forEach(option -> Platform.runLater(() -> option.setGroup(Optional.empty())));
    }

    @Override
    protected Optional<Map<T, G>> calculateResult() {
        Map<T, G> selection = options.getValue().stream()
                .filter(entry -> entry.getGroup().isPresent())
                .collect(Collectors.toMap(AssociatedItem::getItem, entry -> entry.getGroup().get()));
        return Optional.of(selection);
    }

    public void setOptions(Set<T> options) {
        this.options.getValue().clear();
        options.stream().sorted().forEach(o -> this.options.getValue().add(new AssociatedItem(o)));
    }

    public Set<T> getOptions() {
        return options.getValue().stream()
                .map(AssociatedItem::getItem)
                .collect(Collectors.toSet());
    }

    public void setGroups(BiMap<G, Color> groups) {
        this.groups.clear();
        this.groups.putAll(groups);
    }

    public Set<G> getGroups() {
        return groups.keySet();
    }

    public ReadOnlyBooleanProperty currentGroupSelectedProperty() {
        return currentGroupSelected.getReadOnlyProperty();
    }

    public boolean isCurrentGroupSelected() {
        return currentGroupSelectedProperty().get();
    }

    public ReadOnlyMapProperty<G, ? extends ReadOnlyIntegerProperty> selectedPerGroupProperty() {
        return selectedPerGroup.getReadOnlyProperty();
    }

    public ObservableMap<G, ? extends ReadOnlyIntegerProperty> getSelectedPerGroup() {
        return selectedPerGroupProperty().getValue();
    }

    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount.getReadOnlyProperty();
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    public ReadOnlyIntegerProperty selectedCountProperty() {
        return selectedCount.getReadOnlyProperty();
    }

    public int getSelectedCount() {
        return selectedCountProperty().getValue();
    }

    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected.getReadOnlyProperty();
    }

    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected.getReadOnlyProperty();
    }

    public boolean isAllSelected() {
        return allSelected.get();
    }

    /**
     * Represents a relation between an option to select and an group it is associated with.
     */
    private class AssociatedItem {

        private final ObjectProperty<T> item = new SimpleObjectProperty<>();
        private final ObjectProperty<Optional<G>> group = new SimpleObjectProperty<>(Optional.empty());

        AssociatedItem(T item) {
            this.item.set(item);
        }

        public ReadOnlyObjectProperty<T> itemProperty() {
            return item;
        }

        public T getItem() {
            return itemProperty().get();
        }

        public ObjectProperty<Optional<G>> groupProperty() {
            return group;
        }

        public Optional<G> getGroup() {
            return groupProperty().get();
        }

        /**
         * Changes the group this item is currently associated with.
         *
         * @param group The group this item is currently associated with. {@link Optional#empty()} signals that this
         *              item not associated with any group.
         */
        public void setGroup(Optional<G> group) {
            if (group.isEmpty() || getGroups().contains(group.get())) {
                this.group.set(group);
            }
        }

        public boolean isAssociated() {
            return group.get().isPresent();
        }
    }
}
