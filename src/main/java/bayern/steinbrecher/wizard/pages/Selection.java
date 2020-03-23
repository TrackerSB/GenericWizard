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

import bayern.steinbrecher.wizard.WizardableView;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a selection dialog.
 *
 * @param <T> The type of the attributes being able to select.
 * @author Stefan Huber
 * @since 1.4
 */
public class Selection<T extends Comparable<T>> extends WizardableView<Optional<Set<T>>, SelectionController<T>> {

    private final Set<T> options;

    /**
     * Creates a new Frame representing the given options as selectable {@link javafx.scene.control.CheckBox} es and
     * representing a {@link javafx.scene.control.TextField} for entering a number.
     *
     * @param options The options the user is allowed to select.
     */
    public Selection(Set<T> options) {
        super();
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterControllerInitialized() {
        getController().setOptions(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWizardFxmlPath() {
        return "Selection.fxml";
    }
}
