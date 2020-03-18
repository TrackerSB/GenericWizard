/*
 * The MIT License
 *
 * Copyright 2020 Stefan Huber.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
