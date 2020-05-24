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
module bayern.steinbrecher.GenericWizard {
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires org.jetbrains.annotations;

    exports bayern.steinbrecher.wizard;
    exports bayern.steinbrecher.wizard.pages;

    opens bayern.steinbrecher.wizard to javafx.fxml;
    opens bayern.steinbrecher.wizard.pages to javafx.fxml;
}
