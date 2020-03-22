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
package bayern.steinbrecher.wizard.utility;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides comfort methods for accessing the wizards resource bundles.
 *
 * @author Stefan Huber
 * @since 1.4
 */
public final class ResourceBundleHandler {

    public static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle("bayern.steinbrecher.wizard.bundles.Wizard");
    private static final Logger LOGGER = Logger.getLogger(ResourceBundleHandler.class.getName());

    private ResourceBundleHandler() {
    }

    /**
     * Returns the value behind {@code key} of the resource bundle inserted params.
     *
     * @param key The key to search for.
     * @param params The params to insert.
     * @return The value with inserted params.
     */
    public static String getResourceValue(String key, Object... params) {
        String resourceValue;
        if (RESOURCE_BUNDLE.containsKey(key)) {
            resourceValue = MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } else {
            LOGGER.log(Level.INFO, "No resource for \"{0}\" found.", key);
            resourceValue = key;
        }
        return resourceValue;
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and with inserted params.
     *
     * @param key The key to search for.
     * @param params The list of params to insert each in the value behind {@code key}.
     * @return The list of values with inserted params.
     */
    public static List<String> getResourceValues(String key, List<Object[]> params) {
        List<String> values = new ArrayList<>(params.size());
        params.stream().forEachOrdered(p -> values.add(getResourceValue(key, p)));
        return values;
    }
}
