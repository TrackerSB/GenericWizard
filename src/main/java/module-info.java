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

    opens bayern.steinbrecher.wizard to javafx.fxml;
    opens bayern.steinbrecher.wizard.pages to javafx.fxml;
}
