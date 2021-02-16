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
