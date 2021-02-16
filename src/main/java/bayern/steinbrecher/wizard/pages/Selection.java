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

import bayern.steinbrecher.wizard.WizardPage;
import javafx.scene.control.CheckBox;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @param <T> The type of the attributes being able to select.
 * @author Stefan Huber
 * @since 1.4
 */
public class Selection<T extends Comparable<? extends T>>
        extends WizardPage<Optional<Set<T>>, SelectionController<T>> {

    private static final long NUM_TIMEOUT_TICKS = 3;
    private static final TimeUnit TIMEOUT_TICK_UNIT = TimeUnit.SECONDS;
    private final Future<Set<T>> options;

    /**
     * Creates a new page which represents each given option as selectable {@link CheckBox} and adds a search box which
     * allows to filter the visible options.
     */
    public Selection(@NotNull Set<T> options) {
        this(new FutureTask<>(() -> options));
    }

    /**
     * @since 1.51
     */
    public Selection(@NotNull Future<Set<T>> options) {
        super("Selection.fxml", ResourceBundle.getBundle("bayern.steinbrecher.wizard.pages.Selection"));
        this.options = Objects.requireNonNull(options);
    }

    @Override
    protected void afterControllerInitialized() {
        try {
            getController().setOptions(options.get(NUM_TIMEOUT_TICKS, TIMEOUT_TICK_UNIT));
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("The options to select are not available");
        }
    }
}
