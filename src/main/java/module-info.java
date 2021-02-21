module bayern.steinbrecher.GenericWizard {
    requires bayern.steinbrecher.Utility;
    requires com.google.common;
    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jetbrains.annotations;

    exports bayern.steinbrecher.wizard;
    exports bayern.steinbrecher.wizard.pages;

    // NOTE 2021-02-21: java.base is required due to usage of Bindings#select(...)
    opens bayern.steinbrecher.wizard to javafx.fxml, java.base;
    opens bayern.steinbrecher.wizard.pages to javafx.fxml;
}
