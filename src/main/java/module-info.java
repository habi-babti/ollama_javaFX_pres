module com.ollama.olama {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.ollama.olama to javafx.fxml;
    opens com.ollama.olama.controller to javafx.fxml;
    opens com.ollama.olama.model to com.fasterxml.jackson.databind;
    opens com.ollama.olama.manager to com.fasterxml.jackson.databind;
    opens com.ollama.olama.service to com.fasterxml.jackson.databind;
    
    exports com.ollama.olama;
    exports com.ollama.olama.model;
    exports com.ollama.olama.manager;
    exports com.ollama.olama.service;
    exports com.ollama.olama.controller;
}