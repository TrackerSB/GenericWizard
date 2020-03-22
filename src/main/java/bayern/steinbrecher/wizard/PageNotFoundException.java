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
package bayern.steinbrecher.wizard;

/**
 * Signals that a wizard page having a certain key could not be found.
 *
 * @author Stefan Huber
 * @since 1.0
 */
public class PageNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code PageNotFoundException} without detail message and no cause.
     */
    public PageNotFoundException() {
        super();
    }

    /**
     * Constructs an {@code PageNotFoundException} with detail message but no cause.
     *
     * @param message The detail message to show.
     */
    public PageNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code PageNotFoundException} with given detail message and cause.
     *
     * @param message The detail message to show.
     * @param cause The cause of this exception.
     */
    public PageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code PageNotFoundException} without detail message but with given cause.
     *
     * @param cause The cause of this exception.
     */
    public PageNotFoundException(Throwable cause) {
        super(cause);
    }
}
