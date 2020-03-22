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
 * Signals that calling a {@code Callable} of a page has thrown an exception.
 *
 * @author Stefan Huber
 * @since 1.0
 */
public class IllegalCallableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code IllegalCallableException} without detail message and no cause.
     */
    public IllegalCallableException() {
        super();
    }

    /**
     * Constructs an {@code IllegalCallableException} with detail message but no cause.
     *
     * @param message The detail message to show.
     */
    public IllegalCallableException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code IllegalCallableException} with given detail message and cause.
     *
     * @param message The detail message to show.
     * @param cause The cause of this exception.
     */
    public IllegalCallableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code IllegalCallableException} without detail message but with given cause.
     *
     * @param cause The cause of this exception.
     */
    public IllegalCallableException(Throwable cause) {
        super(cause);
    }
}
