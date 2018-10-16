package com.polytech;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private String fichierLocalChooser = "";


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        Client monClient = new Client();

        // Réglages généraux
        primaryStage.setWidth(650);
        primaryStage.setHeight(300);
        primaryStage.setTitle("TFTP");

        // Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        // Label sorties
        TextArea textAreaSorties = new TextArea();
        textAreaSorties.setDisable(true);
        textAreaSorties.setPrefHeight(100);
        textAreaSorties.setPrefWidth(400);
        grid.add(textAreaSorties, 0, 7, 7, 3);

        /****** ENVOYER *****/
        // Adresse IP Label
        Label labelAdresseIP = new Label("Adresse IP : ");
        GridPane.setConstraints(labelAdresseIP, 0, 10);
        grid.getChildren().add(labelAdresseIP);

        // Adresse IP TextField Envoyer
        final TextField adresseIP = new TextField();
        GridPane.setConstraints(adresseIP, 1, 10);
        grid.getChildren().add(adresseIP);

        // Label Envoyer
        Label labelEnvoyer = new Label("ENVOYER UN FICHIER");
        GridPane.setConstraints(labelEnvoyer, 1, 0);
        grid.getChildren().add(labelEnvoyer);

        // Localfile Label
        Label labelFichierLocalEnvoyer = new Label("Fichier local : ");
        GridPane.setConstraints(labelFichierLocalEnvoyer, 0, 1);
        grid.getChildren().add(labelFichierLocalEnvoyer);

        // LocalFile TextField
        final FileChooser fileChooser = new FileChooser();

        final Button fichierLocalEnvoyerButton = new Button("Choisir un fichier");

        fichierLocalEnvoyerButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            fichierLocalChooser = file.getName();
                            fichierLocalEnvoyerButton.setText(fichierLocalChooser);
                        }
                    }
                });
        GridPane.setConstraints(fichierLocalEnvoyerButton, 1, 1);
        grid.getChildren().add(fichierLocalEnvoyerButton);

        // DistantFile Label Envoyer
        Label labelFichierDistantEnvoyer = new Label("Fichier distant : ");
        GridPane.setConstraints(labelFichierDistantEnvoyer, 0, 2);
        grid.getChildren().add(labelFichierDistantEnvoyer);

        // DistanteFile TextField Envoyer
        final TextField fichierDistant = new TextField();
        GridPane.setConstraints(fichierDistant, 1, 2);
        grid.getChildren().add(fichierDistant);

        //Defining the Submit button
        Button submit = new Button("Envoyer");
        GridPane.setConstraints(submit, 1, 3);
        grid.getChildren().add(submit);

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(fichierLocalChooser);
                if (!fichierLocalChooser.equals("") && !fichierDistant.getText().equals("")) {

                    textAreaSorties.setText( monClient.runClient(fichierLocalChooser, fichierDistant.getText(),adresseIP.getText(),false));
                } else {
                    textAreaSorties.setText("Les fichiers ne sont pas renseignés");
                }

            }
        });

        /***RECEVOIR***/
        // Label Recevoir
        Label labelRecevoir = new Label("RECEVOIR UN FICHIER");
        GridPane.setConstraints(labelRecevoir, 6, 0);
        grid.getChildren().add(labelRecevoir);

        // Localfile Label Recevoir
        Label labelFichierLocalRecevoir = new Label("Fichier local : ");
        GridPane.setConstraints(labelFichierLocalRecevoir, 5, 1);
        grid.getChildren().add(labelFichierLocalRecevoir);

        // LocalFile TextField Recevoir
        final TextField fichierLocalRecevoir = new TextField();
        fichierLocalRecevoir.setPrefWidth(200);
        fichierLocalRecevoir.setPrefColumnCount(10);
        GridPane.setConstraints(fichierLocalRecevoir, 6, 1);
        grid.getChildren().add(fichierLocalRecevoir);

        // DistantFile Label Recevoir
        Label labelFichierDistantRecevoir = new Label("Fichier distant : ");
        GridPane.setConstraints(labelFichierDistantRecevoir, 5, 2);
        grid.getChildren().add(labelFichierDistantRecevoir);

        // DistanteFile TextField Recevoir
        final TextField fichierDistantRecevoir = new TextField();
        GridPane.setConstraints(fichierDistantRecevoir, 6, 2);
        grid.getChildren().add(fichierDistantRecevoir);

        // Defining the Receive button
        Button receive = new Button("Reçevoir");
        GridPane.setConstraints(receive, 6, 3);
        grid.getChildren().add(receive);

        receive.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!fichierLocalRecevoir.getText().equals("") && !fichierDistantRecevoir.getText().equals("")) {
                    textAreaSorties.setText(monClient.runClient(fichierLocalRecevoir.getText(), fichierDistantRecevoir.getText(),adresseIP.getText(),true));
                } else {
                    textAreaSorties.setText("Les fichiers ne sont pas renseignés");
                }
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
