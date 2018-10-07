package com.polytech;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        // Réglages généraux
        primaryStage.setWidth(400);
        primaryStage.setHeight(300);
        primaryStage.setTitle("TFTP");

        // Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        // LocalFile TextField
        final TextField fichierLocal = new TextField();
        fichierLocal.setPromptText("Entrez le nom du fichier local.");
        fichierLocal.setPrefColumnCount(10);
        GridPane.setConstraints(fichierLocal, 0, 0);
        grid.getChildren().add(fichierLocal);

        // DistanteFile TextField
        final TextField fichierDistant = new TextField();
        fichierDistant.setPromptText("Entrez le nom du fichier distant.");
        GridPane.setConstraints(fichierDistant, 0, 1);
        grid.getChildren().add(fichierDistant);

        //Defining the Submit button
        Button submit = new Button("Envoyer");
        GridPane.setConstraints(submit, 0, 5);
        grid.getChildren().add(submit);

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Client monClient= new Client();
                monClient.runClient(fichierLocal.getText(), fichierDistant.getText());
            }
        });

        // Defining the Receive button
        Button receive = new Button("Reçevoir");
        GridPane.setConstraints(receive, 0, 6);
        grid.getChildren().add(receive);

        receive.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Client monClient= new Client();
                monClient.runClient(fichierLocal.getText(), fichierDistant.getText());
            }
        });

        // Groupe des éléments de la scène
        Group group = new Group();
        group.getChildren().add(grid);

        // Affichage de la scène
        primaryStage.setScene(new Scene(group));
        primaryStage.show();
    }
}
