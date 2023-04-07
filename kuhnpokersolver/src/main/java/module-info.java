module kuhnpoker {
    requires javafx.controls;
    requires javafx.fxml;

    opens kuhnpoker to javafx.fxml;
    exports kuhnpoker;
}
