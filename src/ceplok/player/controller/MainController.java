/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ceplok.player.controller;

import ceplok.player.CeplokPlayer;
import ceplok.player.Media.Visualizer.Visualizer;
import ceplok.player.Media.Visualizer.impl.LaplapVisualizer;
import ceplok.player.Media.Visualizer.impl.NdogVisualizer;
import ceplok.player.Media.Visualizer.impl.StandardVisualizer;
import ceplok.player.model.DataProp;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author RIZAL
 */
public class MainController implements Initializable {

    @FXML
    private AnchorPane parent;
//    private Pane ceplok;
    private ObservableList<File> fileList;
    private ObservableList<Media> mediaList;
    private ObservableList<DataProp> dataProps;
    private boolean repeat;
    private boolean shuffle;
    private Media media;
    private int visualState;
    private MediaPlayer mediaPlayer;
    private List<Visualizer> visualizers;
    private Visualizer currentVisualizer;
    private int numBands =120;
    private final Double updateInterval =0.05;
    @FXML
    private HBox visualBox;
    @FXML
    private ImageView trackImg;
    @FXML
    private Label trackTitle;
    @FXML
    private Label trackArtist;
    @FXML
    private JFXButton bPrev;
    @FXML
    private JFXButton bPause;
    @FXML
    private JFXButton bPlay;
    @FXML
    private JFXButton bStop;
    @FXML
    private JFXButton bNext;
    @FXML
    private JFXSlider slider;
    @FXML
    private Label ltCurrent;
    @FXML
    private Label ltDuration;
    @FXML
    private JFXButton bShuffle;
    @FXML
    private JFXButton bList;
    @FXML
    private HBox boxTrackTitle;
    @FXML
    private HBox boxMain;
    @FXML
    private JFXButton bExit;
    @FXML
    private JFXButton bMinimize;
    @FXML
    private JFXButton bVisual;
    @FXML
    private VBox boxList;
    @FXML
    private JFXButton bListClose;
    @FXML
    private JFXButton bListFolder;
    @FXML
    private JFXButton bListFile;
    @FXML
    private JFXButton bRepeat;
    @FXML
    private TableView<DataProp> tableMusic;
    @FXML
    private TableColumn<DataProp, String> colTitle;
    @FXML
    private TableColumn<DataProp, String> colDuration;
    @FXML
    private FontAwesomeIconView viewRepeat;
    @FXML
    private FontAwesomeIconView viewShuffle;
    @FXML
    private HBox visualBoxParent;
    @FXML
    private JFXButton bGithub;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        mediaList = FXCollections.observableArrayList();
        fileList = FXCollections.observableArrayList();
        currentVisualizer = new StandardVisualizer();
        visualState= 0;
        repeat = false;
        shuffle = false;
        parent.setBackground(Background.EMPTY);
        boxMainProperties();
        boxListProperties();
        Platform.runLater(() -> {
            boxMain.setLayoutX(400);boxMain.setLayoutY(200);
            boxList.setLayoutX(400);boxList.setLayoutY(415);
            //check files
            checkStoredList();
        });
    }   
    private void checkStoredList(){
        File file = new File("list.txt");
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String read;
                while((read = br.readLine()) != null){
                    int i = read.lastIndexOf(".");
                    String ext = read.substring(i+1);
                    if (ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wav")
                            ||ext.equalsIgnoreCase("mp4")) {
                        addFile(new File(read), false);
                    }
                }
                br.close();
            } catch (Exception e) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    private void saveMediaData(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String paths = "";
                    for (File f : fileList) {
                        paths +=f.getAbsolutePath()+"\n";
                    }
                    File file = new File("list.txt");
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(paths);
                    bw.flush();
                    bw.close();
                } catch (Exception e) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }, 0);
    }
    double xOffset = 0;
    double yOffset = 0;
    private void boxMainProperties(){
        boxMain.setLayoutX(400);
        boxMain.setLayoutY(200);
        boxMain.setOnMousePressed((e) -> {
            boxMain.setOpacity(0.8);
            xOffset = boxMain.getLayoutX()- e.getSceneX();
            yOffset = boxMain.getLayoutY()- e.getSceneY();
        });
        boxMain.setOnMouseDragged((e) -> {
            boxMain.setLayoutX(e.getSceneX()+ xOffset);
            boxMain.setLayoutY(e.getSceneY()+ yOffset);
        });
        boxMain.setOnMouseReleased((e) -> {
            boxMain.setOpacity(1);
        });
        trackProperties();
        playerProperties();
        windowProperties();
        bVisual.setVisible(false);
        visualBoxParent.setVisible(false);
        visualBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue()>50) {
                bVisual.setVisible(true);
                visualBoxParent.setVisible(true);
            }
        });
    }
    private void trackProperties(){
        trackTitle.textProperty().addListener((obs, oldVal, newVal) -> {
            trackTitle.setTooltip(new Tooltip(newVal));
        });
        trackArtist.textProperty().addListener((obs, oldVal, newVal) -> {
            trackArtist.setTooltip(new Tooltip(newVal));
        });
        Rectangle r = new Rectangle(77, 68);
        r.setArcWidth(10);
        r.setArcHeight(10);
        trackImg.setClip(r);
        Rectangle rect = new Rectangle(135, 27);
        boxTrackTitle.setClip(rect);
    }
    private void playerProperties(){
        bPlayProperties();
        bPauseProperties();
        bStopProperties();
        bNextPrevProperties();
        sliderProperties();
    }
    private void windowProperties(){
        bMinimize.setOnAction((event) -> {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setIconified(true);
        });
        bExit.setOnAction((event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            System.exit(0);
        });
        bVisual.setOnAction((event) -> {
            if (currentVisualizer != null) {
                currentVisualizer.end();
            }
            switch(visualState){
                case 0: currentVisualizer= new NdogVisualizer();
                        visualState = 2;break;
                case 1: currentVisualizer= new LaplapVisualizer();//takeout laplap. not cool
                        visualState++;break;
                default:currentVisualizer= new StandardVisualizer();
                        visualState =0;break;
            }
            numBands = (visualState==99)?16:120;
            currentVisualizer.start(numBands, visualBox);
        });
        bList.setOnAction((e) -> {
            boxList.setVisible(!boxList.isVisible());
        });
        bRepeat.setTooltip(new Tooltip((repeat)?"repeat is active":"repeat disabled"));
        bRepeat.setOnAction((event) -> {
            repeat = !repeat;
            viewRepeat.setFill(Paint.valueOf((repeat)? "#ffee00":"#ffffff"));
            bRepeat.setTooltip(new Tooltip((repeat)?"repeat is active":"repeat disabled"));
        });
        bShuffle.setTooltip(new Tooltip((shuffle)?"shuffle is active":"shuffle disabled"));
        bShuffle.setOnAction((event) -> {
            shuffle = !shuffle;
            viewShuffle.setFill(Paint.valueOf((shuffle)? "#ffee00":"#ffffff"));
            bShuffle.setTooltip(new Tooltip((shuffle)?"shuffle is active":"shuffle disabled"));
            if (shuffle) {
                repeat = true;
                viewRepeat.setFill(Paint.valueOf((repeat)? "#ffee00":"#ffffff"));
                bRepeat.setTooltip(new Tooltip((repeat)?"repeat is active":"repeat disabled"));
            }
        });
        bGithub.setTooltip(new Tooltip("Check out my other prejects here :D"));
        bGithub.setOnAction((event) -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://github.com/rizalmf"));
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    private void bPlayProperties(){
        bPlay.setOnAction((event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }else{
                if (!mediaList.isEmpty()) {
                    trackTitle.setText(fileList.get(0).getName());
                    media = mediaList.get(0);
                    tableMusic.getSelectionModel().select(0);
                    openMedia(media);
                }
            }
        });
    }
    private void bPauseProperties(){
        bPause.setOnAction((event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause(); 
            }
        });
    }
    private void bStopProperties(){
        bStop.setOnAction((event) -> {
            if (mediaPlayer != null) {
                slider.setValue(0);
                mediaPlayer.stop(); 
            }
        });
    }
    private void bNextPrevProperties(){
        bNext.setOnAction((event) -> {
            if (mediaPlayer != null) {
                handleEndOfMedia(false);
            }else{
                if (!mediaList.isEmpty()) {
                    trackTitle.setText(fileList.get(0).getName());
                    tableMusic.getSelectionModel().select(0);
                    media = mediaList.get(0);
                    openMedia(media);
                }
            }
        });
        bPrev.setOnAction((event) -> {
            if (mediaPlayer != null) {
                handleEndOfMedia(true); 
            }
        });
    }
    private void sliderProperties(){
        slider.setOnMouseReleased((event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(new Duration(slider.getValue()));

                currentVisualizer.start(numBands, visualBox);
                mediaPlayer.play();
            }  
        });
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ltCurrent.setText(convertMs(newVal.doubleValue()));
        });
    }
    private boolean addFile(File file, boolean isPlay){
        Media m = new Media(file.toURI().toString());
        mediaList.add(m);
        fileList.add(file);
        updateTable(m, file);
//        mediaList.add(new Media("file:/F:/fold/videoplayback%20(4).mp4"));
//        fileList.add(new File("file:/F:/fold/videoplayback%20(4).mp4"));
        if (isPlay) {
            trackTitle.setText(file.getName());
            media = new Media(file.toURI().toString());
            openMedia(media);
        }
        //set always true
        return true;
    }
    private void updateTable(Media m, File f){
        if (dataProps == null) {
            dataProps = FXCollections.observableArrayList();
        }
        DataProp dp = new DataProp();
        dp.setFileData(f);
        dp.setMediaData(m);
        dp.setTitle(f.getName());
        dataProps.add(dp);
        m.durationProperty().addListener((obs, oldVal, newVal) -> {
            dp.setDuration(convertMs(newVal.toMillis()));
        });
        tableMusic.setItems(dataProps);
    }
    private void openMedia(Media m) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        try {
            mediaPlayer = new MediaPlayer(m);
            trackArtist.setText("Unknown Artist");
            Image image = new Image(CeplokPlayer.class.getResourceAsStream("views/images/ceplok2.png"));
            trackImg.setImage(image);
            m.getMetadata().addListener((MapChangeListener<String, Object>) c -> {
                if (c.wasAdded()) {
                    if (c.getKey().equals("title")) {
                        trackTitle.setText(c.getValueAdded().toString());
                    }else if (c.getKey().equals("artist")) {
                        trackArtist.setText(c.getValueAdded().toString());
                    }else if (c.getKey().equals("image")) {
                        Image img =(Image) c.getValueAdded();
                        trackImg.setImage(img);
                    }
                }
            });
            mediaPlayer.setOnReady(() -> {
                if (m.getMetadata().containsKey("title")) {
                    trackTitle.setText(m.getMetadata().get("title").toString());
                }
                if (m.getMetadata().containsKey("artist")) {
                    trackArtist.setText(m.getMetadata().get("artist").toString());
                }
                if (m.getMetadata().containsKey("image")) {
                    Image img =(Image) m.getMetadata().get("image");
                    trackImg.setImage(img);
                }
                handleReady();
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                handleEndOfMedia(false);
            });
            mediaPlayer.setAudioSpectrumNumBands(numBands);
            mediaPlayer.setAudioSpectrumInterval(updateInterval);
            
            mediaPlayer.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
                
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
           //seek on table and add point that she playing
            mediaPlayer.setAutoPlay(true);
        } catch (Exception e) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    private void handleUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        Duration ct = mediaPlayer.getCurrentTime();
        double ms = ct.toMillis();
        if (!slider.isPressed()) {
            slider.setValue(ms);
        }
       currentVisualizer.update(timestamp, duration, magnitudes, phases);
    }
    // set timeSlider value
    private void handleReady() {
        Duration duration = mediaPlayer.getTotalDuration();
        ltDuration.setText(convertMs(duration.toMillis()));
        Duration ct = mediaPlayer.getCurrentTime();
        ltCurrent.setText(convertMs(ct.toMillis()));
        currentVisualizer.start(numBands, visualBox);
        slider.setMin(0);
        slider.setMax(duration.toMillis());
    }
    private void handleEndOfMedia(boolean isPrev) {
        mediaPlayer.stop();
        mediaPlayer.seek(Duration.ZERO);
        slider.setValue(0);
        boolean found = false;
        if (isPrev) {
            for (int i = mediaList.size()-1; i >= 0; i--) {
                if (i > 0) {
                    if (media.getDuration() == mediaList.get(i).getDuration()) {
                        found = true;
                        media = mediaList.get(i-1);
                        tableMusic.getSelectionModel().select(i-1);
                        trackTitle.setText(fileList.get(i-1).getName());
                        openMedia(media);
                        break;
                    }
                }
            }
        }else{
            if (!shuffle) {
                for (int i = 0; i < mediaList.size(); i++) {
                    if (i > 0) {
                        if (media.getDuration() == mediaList.get(i-1).getDuration()) {
                            found = true;
                            media = mediaList.get(i);
                            tableMusic.getSelectionModel().select(i);
                            trackTitle.setText(fileList.get(i).getName());
                            openMedia(media);
                            break;
                        }
                    }
                }
            }else{
                found = true;
                int i = new Random().nextInt(mediaList.size());
                media = mediaList.get(i);
                tableMusic.getSelectionModel().select(i);
                trackTitle.setText(fileList.get(i).getName());
                openMedia(media);
            }
        }
        
        if (!found && repeat) {
            if (!mediaList.isEmpty()) {
                trackTitle.setText((isPrev)?fileList.get(fileList.size()-1).getName()
                        :fileList.get(0).getName());
                media = mediaList.get((isPrev)?mediaList.size()-1:0);
                tableMusic.getSelectionModel().select((isPrev)?mediaList.size()-1:0);
                openMedia(media);
            }
        }
    }
    private String convertMs(double ms){
        int hour =(int) ((ms /(1000*60*60)) %24);
        int minute =(int) ((ms /(1000*60)) %60);
        int second =(int) (ms /(1000) %60);
        String sSecond = (second < 10)? "0"+second :second+"";
        return (hour==0) ?minute+":"+sSecond :hour+":"+minute+":"+sSecond;
    }
    private void boxListProperties(){
        boxList.setVisible(false);
        boxMain.setLayoutX(400);
        boxMain.setLayoutY(415);
        bListClose.setOnAction((event) -> {
            boxList.setVisible(false);
        });
        boxList.setOnMousePressed((e) -> {
            boxList.setOpacity(0.8);
            xOffset = boxList.getLayoutX()- e.getSceneX();
            yOffset = boxList.getLayoutY()- e.getSceneY();
        });
        boxList.setOnMouseDragged((e) -> {
            boxList.setLayoutX(e.getSceneX()+ xOffset);
            boxList.setLayoutY(e.getSceneY()+ yOffset);
        });
        boxList.setOnMouseReleased((e) -> {
            boxList.setOpacity(1);
        });
        bFileProperties();
        tableMusicProperties();
    }
    private void bFileProperties(){
        bListFile.setOnAction((event) -> {
            Stage primaryStage = (Stage)parent.getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            List<String> ext = new ArrayList<>(
                    Arrays.asList(
                            "*.mp3", 
                            "*.wav",
                            "*.mp4"
                    )
            );
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio format", ext);
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Give ceplok some music files");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                boolean exist = false;
                for (File f : fileList) {
                    if (f.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
                        exist = true;
                        System.out.println("Was added before");
                        break;
                    }
                }
                if (!exist) {
                    if (addFile(file, true)) {
                        saveMediaData();
                    }
                }
            }
        });
        bListFolder.setOnAction((event) -> {
            Stage primaryStage = (Stage)parent.getScene().getWindow();
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Give ceplok some music folder");
            File files = dirChooser.showDialog(primaryStage);
            if (files != null) {
                boolean saving = false;
                for (File f : files.listFiles()) {
                    if (f.isFile()) {
                        int i = f.getAbsolutePath().lastIndexOf('.');
                        String s = f.getAbsolutePath().substring(i+1);
                        if (s.equalsIgnoreCase("mp3")||s.equalsIgnoreCase("wav")
                                || s.equalsIgnoreCase("mp4")) {
                            saving = addFile(f, false);
                        }
                    }
                }
                if(saving){ saveMediaData(); }
            }
        });
    }
    private void tableMusicProperties(){
        colTitle.setCellValueFactory((param) -> {
            return param.getValue().titleProperty();
        });
        colDuration.setCellValueFactory((param) -> {
            return param.getValue().durationProperty();
        });
        tableMusic.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );
        tableMusic.setOnMouseClicked((event) -> {
            if (event.getClickCount() ==2) {
                DataProp data = tableMusic.getSelectionModel().getSelectedItem();
                slider.setValue(0);
                trackTitle.setText(data.getFileData().getName());
                media = data.getMediaData();
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.seek(Duration.ZERO);
                }
                openMedia(media);
            }
        });
        tableMusic.setOnKeyReleased((event) -> {
            if (event.getCode() == KeyCode.DELETE) {
                //bug selectedItems() at jdk 1.8.0_191
                ObservableList<DataProp> datas = tableMusic.getSelectionModel().getSelectedItems();
                boolean saving = false;
                for (DataProp data : datas) {
//                    System.out.println(data.getTitle());
                    for (int i = 0; i < fileList.size(); i++) {
                        if (fileList.get(i).getName().equalsIgnoreCase(data.getTitle())) {
                            fileList.remove(i);mediaList.remove(i);
                            dataProps.remove(i);
                            saving = true;
                            break;
                        }
                    }
                }
                if(saving){ saveMediaData();}
                tableMusic.setItems(dataProps);
            }else if(event.getCode() == KeyCode.ENTER){
                DataProp data = tableMusic.getSelectionModel().getSelectedItems().get(0);
                slider.setValue(0);
                trackTitle.setText(data.getFileData().getName());
                media = data.getMediaData();
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.seek(Duration.ZERO);
                }
                openMedia(media);
            }
        });
        tableMusic.setOnDragOver((event) -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }
        });
        tableMusic.setOnDragDropped((event) -> {
                List<File> files = event.getDragboard().getFiles();
                boolean saving = false;
                for (File file : files) {
                    if (file.isDirectory()) {
                        for (File f : file.listFiles()) {
                            if (f.isFile()) {
                                int i = f.getAbsolutePath().lastIndexOf('.');
                                String s = f.getAbsolutePath().substring(i+1);
                                if (s.equalsIgnoreCase("mp3")||s.equalsIgnoreCase("wav")
                                        ||s.equalsIgnoreCase("mp4")) {
                                    saving = addFile(f, false);
                                }
                            }
                        }
                    }else{
                        int i = file.getAbsolutePath().lastIndexOf('.');
                        String s = file.getAbsolutePath().substring(i+1);
                        if (s.equalsIgnoreCase("mp3")||s.equalsIgnoreCase("wav")
                                ||s.equalsIgnoreCase("mp4")) {
                            saving = addFile(file, false);
                        }
                    }
                }
                if (saving) {saveMediaData();}
        });
    }
}
