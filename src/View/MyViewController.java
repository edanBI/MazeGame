package View;
/*
Observe View Model
*/
import IO.MyDecompressorInputStream;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import com.sun.deploy.util.SystemUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.security.spec.ECField;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

public class MyViewController implements Observer, IView {

    @FXML
    private MyViewModel view_model;
    public MazeDisplayer mazeDisplayer;
    public javafx.scene.control.TextField txtfld_rowsNum;
    public javafx.scene.control.TextField txtfld_columnsNum;
    /*public javafx.scene.control.Label lbl_rowsNum;
    public javafx.scene.control.Label lbl_columnsNum;*/
    public javafx.scene.control.Label lbl_character_pos;
    public javafx.scene.control.Button btn_generateMaze;
    public javafx.scene.control.Button btn_solveMaze;

    //private boolean isGenerated = false;

    public void setViewModel(MyViewModel my_viewModel) {
        this.view_model = my_viewModel;
        mazeDisplayer.requestFocus();
        bindProperties(my_viewModel);
    }

    /*
     * binds the text label in the main menu to the my_viewModel in the background
     * */
    private void bindProperties(MyViewModel viewModel) {
        /*lbl_rowsNum.textProperty().bind(viewModel.characterPositionRow); // display row pos
        lbl_columnsNum.textProperty().bind(viewModel.characterPositionColumn); // display col pos*/
        lbl_character_pos.textProperty().bind(viewModel.character_Position);
    }

    public void update(Observable o, Object arg) {
        if (o == view_model) {
            if (arg.equals("maze generated")) {
                displayMaze(view_model.getMaze());
                btn_generateMaze.setDisable(false);
            }
            if (arg.equals("solution")) {
                mazeDisplayer.setSolution(view_model.getSolution());
                btn_solveMaze.setDisable(false);
            }
            if (arg.equals("player moved")) {
                int positionRow = view_model.getCharacterPositionRow();
                int positionColumn = view_model.getCharacterPositionColumn();
                mazeDisplayer.setCharacterPosition(positionRow, positionColumn); // display character on screen
                this.characterPositionRow.set(positionRow + "");
                this.characterPositionColumn.set(positionColumn + "");
            }
        }
    }

    @Override
    public void displayMaze(Maze maze) {
        mazeDisplayer.setMaze(maze);
        int character_pos_row = view_model.getCharacterPositionRow();
        int character_pos_col = view_model.getCharacterPositionColumn();
        //int goalPositionRow = view_model.getGoalPositionRowIndex();
        //int goalPositionColumn = view_model.getGoalPositionColumnIndex();
        mazeDisplayer.setCharacterPosition(character_pos_row, character_pos_col); // display character on screen
        //mazeDisplayer.setGoalPosition(goalPositionRow, goalPositionColumn);
        this.characterPositionRow.set(character_pos_row + "");
        this.characterPositionColumn.set(character_pos_col + "");
        //my_viewModel.btn_solveMaze();
        //mazeDisplayer.setSolution(sol);
        //mazeDisplayer.requestFocus();
    }

    public void generateMaze() {
        int height = Integer.valueOf(txtfld_rowsNum.getText());
        int width = Integer.valueOf(txtfld_columnsNum.getText());
        btn_generateMaze.setDisable(true);

        mazeDisplayer.setWidth(txtfld_rowsNum.getScene().getWidth() - 190);
        mazeDisplayer.setHeight(txtfld_rowsNum.getScene().getHeight() - 60);
        update(view_model, new Object());

        view_model.generateMaze(width, height);


    }

    public void solveMaze(ActionEvent actionEvent) {
        //showAlert("Solving maze..");
        if (view_model.getMaze() != null) {
            btn_solveMaze.setDisable(true);
            view_model.solveMaze();
        }
    }

    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    private void showFinishedGameAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText(alertMessage);
        alert.show();
    }

    public void KeyPressed(KeyEvent keyEvent) {
        if (view_model.moveCharacter(keyEvent.getCode())){
            EndGameDialog.show();
        }
        keyEvent.consume();
    }

    public void goal_reached(){

    }

    //region String Property for Binding
    public StringProperty characterPositionRow = new SimpleStringProperty();

    public StringProperty characterPositionColumn = new SimpleStringProperty();

    public String getCharacterPositionRow() {
        return characterPositionRow.get();
    }

    public StringProperty characterPositionRowProperty() {
        return characterPositionRow;
    }

    public String getCharacterPositionColumn() {
        return characterPositionColumn.get();
    }

    public StringProperty characterPositionColumnProperty() {
        return characterPositionColumn;
    }

    public void setResizeEvent(Scene scene) {
        long width = 0;
        long height = 0;
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                if (mazeDisplayer.getMaze() != null) {
                    mazeDisplayer.setWidth((double)newSceneWidth-190);
                    mazeDisplayer.redraw();
                }
            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                if (mazeDisplayer.getMaze() != null) {
                    mazeDisplayer.setHeight((double)newSceneWidth-60);
                    mazeDisplayer.redraw();
                }
            }
        });
    }

    public void properties() {

    }
    public void saveMaze() {
        if (view_model.getMaze() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You need to generate maze first..");
            alert.showAndWait();
            return;
        }
        String num_rows = Integer.valueOf(txtfld_rowsNum.getText()).toString();
        String num_cols = Integer.valueOf(txtfld_columnsNum.getText()).toString();

        FileChooser dialog = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("maze file (*.maze)", "*.maze");
        dialog.setTitle("Save Maze");
        dialog.setInitialFileName("Maze ["+num_rows+","+num_cols+"]: " + LocalDateTime.now(Clock.systemDefaultZone())+".maze");
        dialog.getExtensionFilters().add(filter);
        File file = dialog.showSaveDialog(new Stage());
        if (file != null) {
            try {
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(num_rows);
                bw.newLine();
                bw.write(num_cols);
                bw.newLine();
                bw.write(view_model.getMaze().getStartPosition().toString());
                bw.newLine();
                bw.write(view_model.getMaze().getGoalPosition().toString());
                bw.newLine();
                for (int i=0; i<view_model.getMaze().getM_arr().length; i++){
                    for (int j=0; j<view_model.getMaze().getM_arr()[i].length; j++) {
                        bw.write(Integer.toString(view_model.getMaze().getM_arr()[i][j]));
                    }
                    bw.newLine();
                }

                bw.close();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void About() {
        try {
            Stage window = new Stage();
            window.setTitle("Welcome");
            Parent layout = new FXMLLoader().load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(layout, 600, 232);
            window.setScene(scene);
            window.initModality(Modality.APPLICATION_MODAL);
            window.showAndWait();
        } catch (Exception e) {e.getStackTrace(); }
    }

    public void Help() {
        try {
            Stage window = new Stage();
            window.setTitle("Help");
            Parent layout = new FXMLLoader().load(getClass().getResource("Help.fxml").openStream());
            Scene scene = new Scene(layout, 600, 232);
            window.setScene(scene);
            //window.initModality(Modality.APPLICATION_MODAL);
            //window.showAndWait();
            window.show();
        } catch (Exception e) {e.getStackTrace(); }
    }

    public void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            view_model.stopServer();
            System.exit(0);
        }
    }
}
