package com.example.showSequencerJavafx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class MainController implements Initializable {

    public double RUNSCREEN_FADE_TIME = 1;
    public double PLAYLIST_FADE_TIME = 1;
    public double MIN_FADE_TIME = 0.1;
    public final Map<COMMAND, Color> commandColorMap = new HashMap<>();


    private File projectFile = null;
    private Preferences preferences;
    private final Map<String, ExponentialFade> exponentialFadeMap = new HashMap<>();
    private final ArrayList<Tab> tempTabs = new ArrayList<>();
    private final ArrayList<Cue> displayedCues = new ArrayList<>();
    private final Node[][] displayedCuesNodes = new Node[7][4];
    private final Map<ReadOnlyObjectProperty<Duration>, ChangeListener<Duration>> displayListeners = new HashMap<>(){};
    private boolean playlistControlPanelDisabled = true;
    private final PlaylistManager playlistManager = new PlaylistManager(this);
    private final CuesManager cuesManager = new CuesManager(this);
    private final FaderManager faderManager = new FaderManager();

    public enum COMMAND{ NONE, PLAY, STOP, VOLUME, STOP_ALL, PLAYLIST_START, PLAYLIST_CONT, PLAYLIST_FADE }

    @FXML
    private TextField textCurrentCueNum, textCurrentCueName, textCurrentCueTime, textNextCueNum, textNextCueAuto, textNextCueName, textNextCueCommand, textNextCueAudioFile, textNextCueVol, textNextCueTime;
    @FXML
    private ProgressBar currentCueProgress;
    @FXML
    private Slider runScreenCueVolumeSlider, runScreenPlaylistVolumeSlider;
    @FXML
    private ComboBox<Cue> comboBoxCueJump;
    @FXML
    private ComboBox<Cue> comboBoxCueJumpCopy;
    @FXML
    private VBox playlistControlPanel;

    @FXML
    private MenuItem menuItemSaveShow, menuItemPreferences;

    @FXML
    private TextField textDisplayCueTrack1, textDisplayCueTrack2, textDisplayCueTrack3, textDisplayCueTrack4, textDisplayCueVol1, textDisplayCueVol2, textDisplayCueVol3, textDisplayCueVol4;
    @FXML
    private Button buttonCueDisplayFade1, buttonCueDisplayFade2, buttonCueDisplayFade3, buttonCueDisplayFade4, buttonCueDisplayVolUp1, buttonCueDisplayVolUp2, buttonCueDisplayVolUp3, buttonCueDisplayVolUp4, buttonCueDisplayVolDn1, buttonCueDisplayVolDn2, buttonCueDisplayVolDn3, buttonCueDisplayVolDn4;
    @FXML
    private Label lblRunScreenCurrentTime1, lblRunScreenCurrentTime2, lblRunScreenCurrentTime3, lblRunScreenCurrentTime4, lblRunScreenTotalTime1, lblRunScreenTotalTime2, lblRunScreenTotalTime3, lblRunScreenTotalTime4;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabRunScreen, tabCueList, tabPlaylist;
    @FXML
    private CheckMenuItem checkRunScreen, checkCueList, checkPlaylist, checkShowMode;
    @FXML
    private MenuBar menuBar;
    @FXML
    Slider playlistVolumeSlider;


    @FXML
    private TableViewClean<Cue> cueListTableAudio;
    @FXML
    private TableColumn<Cue, String> cueNum;
    @FXML
    private TableColumn<Cue, String> cueName;
    @FXML
    private TableColumn<Cue, Double> cueAuto;
    @FXML
    private TableColumn<Cue, COMMAND> cueCommand;
    @FXML
    private TableColumn<Cue, PlaylistFile> cueFile;
    @FXML
    private TableColumn<Cue, SimpleDoubleProperty> cueVol;
    @FXML
    private TableColumn<Cue, Double> cueTime;
    @FXML
    private TableColumn<Cue, Double> fader1, fader2, fader3, fader4, fader5, fader6, fader7, fader8,
            fader9, fader10, fader11, fader12, fader13, fader14, fader15, fader16, fader17, fader18,
            fader19, fader20, fader21, fader22, fader23, fader24, fader25, fader26, fader27, fader28,
            fader29, fader30, fader31, fader32;

    private ArrayList<TableColumn<Cue, Double>> faderColumns;


    @FXML
    private TableView<Cue> cueListTableFaders;

    @FXML
    Slider cueListVolumeSlider;

    @FXML
    private TableView<PlaylistFile> playlistTable;
    @FXML
    private TableColumn<PlaylistFile, String> fileName;
    @FXML
    private TableColumn<PlaylistFile, Boolean> excluded;
    @FXML
    private TableColumn<PlaylistFile, Integer> playlistOrder;
    @FXML
    ProgressBar playlistProgressBar;
    @FXML
    Label playlistTotalDuration;
    @FXML
    Label playlistCurrentDuration;


    private final MenuItem cueInsert = new MenuItem("Add Cue");
    private final MenuItem cueCopy = new MenuItem("Copy");
    private final MenuItem cuePaste = new MenuItem("Paste Into");
    private final MenuItem cuePasteAsNew = new MenuItem("Paste As New");
    private final MenuItem cueDelete = new MenuItem("Delete");

    public TableView<Cue> getCueAudioTable() {
        return cueListTableAudio;
    }

    public TableView<PlaylistFile> getPlaylistTable() {
        return playlistTable;
    }

    public Map<String, ExponentialFade> getFades(){
        return exponentialFadeMap;
    }


    public void setPlaylistControlPanelDisabled(Boolean bool){
        playlistControlPanelDisabled = bool;
        playlistControlPanel.setDisable(bool);
        if(!bool){
            playlistControlPanel.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
        }else{
            playlistControlPanel.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }


    public CuesManager getCuesManager() {
        return cuesManager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public FaderManager getFaderManager() {return faderManager;}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        refreshTables();
        refreshRunScreen();

        faderColumns = new ArrayList<>(Arrays.asList(fader1, fader2, fader3, fader4, fader5, fader6, fader7, fader8,
                fader9, fader10, fader11, fader12, fader13, fader14, fader15, fader16, fader17, fader18,
                fader19, fader20, fader21, fader22, fader23, fader24, fader25, fader26, fader27, fader28,
                fader29, fader30, fader31, fader32));


        for(COMMAND command : COMMAND.values()){
            commandColorMap.put(command, Color.WHITE);
        }

        comboBoxCueJump.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cue cue) {
                if(cue!=null){
                    return cue.getCueNum() + ": " + cue.getCueName();
                }
                return "";
            }

            @Override
            public Cue fromString(String s) {
                return cuesManager.getCues().stream().filter(cue->(cue.getCueNum() + ": " + cue.getCueName()).equals(s)).findFirst().orElse(null);
            }
        });

        comboBoxCueJumpCopy.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cue cue) {
                if(cue!=null){
                    return cue.getCueNum() + ": " + cue.getCueName();
                }
                return "";
            }

            @Override
            public Cue fromString(String s) {
                return cuesManager.getCues().stream().filter(cue->(cue.getCueNum() + ": " + cue.getCueName()).equals(s)).findFirst().orElse(null);
            }
        });


        menuItemSaveShow.setDisable(true);
        menuItemPreferences.setDisable(true);


        displayedCuesNodes[0][0] = textDisplayCueTrack1;
        displayedCuesNodes[0][1] = textDisplayCueTrack2;
        displayedCuesNodes[0][2] = textDisplayCueTrack3;
        displayedCuesNodes[0][3] = textDisplayCueTrack4;

        displayedCuesNodes[1][0] = textDisplayCueVol1;
        displayedCuesNodes[1][1] = textDisplayCueVol2;
        displayedCuesNodes[1][2] = textDisplayCueVol3;
        displayedCuesNodes[1][3] = textDisplayCueVol4;

        displayedCuesNodes[2][0] = buttonCueDisplayVolUp1;
        displayedCuesNodes[2][1] = buttonCueDisplayVolUp2;
        displayedCuesNodes[2][2] = buttonCueDisplayVolUp3;
        displayedCuesNodes[2][3] = buttonCueDisplayVolUp4;

        displayedCuesNodes[3][0] = buttonCueDisplayVolDn1;
        displayedCuesNodes[3][1] = buttonCueDisplayVolDn2;
        displayedCuesNodes[3][2] = buttonCueDisplayVolDn3;
        displayedCuesNodes[3][3] = buttonCueDisplayVolDn4;

        displayedCuesNodes[4][0] = buttonCueDisplayFade1;
        displayedCuesNodes[4][1] = buttonCueDisplayFade2;
        displayedCuesNodes[4][2] = buttonCueDisplayFade3;
        displayedCuesNodes[4][3] = buttonCueDisplayFade4;

        displayedCuesNodes[5][0] = lblRunScreenCurrentTime1;
        displayedCuesNodes[5][1] = lblRunScreenCurrentTime2;
        displayedCuesNodes[5][2] = lblRunScreenCurrentTime3;
        displayedCuesNodes[5][3] = lblRunScreenCurrentTime4;

        displayedCuesNodes[6][0] = lblRunScreenTotalTime1;
        displayedCuesNodes[6][1] = lblRunScreenTotalTime2;
        displayedCuesNodes[6][2] = lblRunScreenTotalTime3;
        displayedCuesNodes[6][3] = lblRunScreenTotalTime4;


        runScreenCueVolumeSlider.valueProperty().bindBidirectional(cueListVolumeSlider.valueProperty());
        runScreenPlaylistVolumeSlider.valueProperty().bindBidirectional(playlistVolumeSlider.valueProperty());

        cueListTableAudio.setRowFactory(factory -> new StatusRow<>());
        cueListTableAudio.setStyle("-fx-selection-bar: lightGrey; -fx-focus-color: transparent;");
        cueListTableFaders.setRowFactory(factory -> new StatusRow<>());
        cueListTableFaders.setStyle("-fx-selection-bar: lightGrey; -fx-focus-color: transparent;");


        cueListTableAudio.selectionModelProperty().bindBidirectional(cueListTableFaders.selectionModelProperty());

        cueListTableFaders.onScrollToProperty().bindBidirectional(cueListTableAudio.onScrollToProperty());

        cueNum.setCellValueFactory(new PropertyValueFactory<>("cueNum"));
        cueNum.setCellFactory(col -> EditCell.createStringEditCell("cueNum"));
        cueNum.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueNum(event.getNewValue());
        });
        cueName.setCellValueFactory(new PropertyValueFactory<>("cueName"));
        cueName.setCellFactory(col -> EditCell.createStringEditCell("cueName"));
        cueName.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueName(event.getNewValue());
        });

        cueAuto.setCellValueFactory(new PropertyValueFactory<>("cueAuto"));
        cueAuto.setCellFactory(col -> new EditCell<>("cueAuto", new DoubleStringConverter(){
            @Override
            public String toString(Double aDouble) {
                if(aDouble<0) return "";
                return super.toString(aDouble);
            }

            @Override
            public Double fromString(String s) {
                if(s.isEmpty()) return (double) -1;
                return super.fromString(s);
            }
        }));
        cueAuto.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueAuto(event.getNewValue());
        });


        cueCommand.setCellValueFactory(new PropertyValueFactory<>("cueCommand"));
        cueCommand.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(COMMAND command) {
                if (command==null||command.equals(COMMAND.NONE)) return "--None--";
                return command.name();
            }

            @Override
            public COMMAND fromString(String s) {
                for (COMMAND command : COMMAND.values()) {
                    if (command.name().equals(s)) {
                        return command;
                    }
                }
                return COMMAND.NONE;
            }
        }, COMMAND.values()));
        cueCommand.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueFile(null);
            Cue newCue = new Cue(cue.getCueNum(), cue.getCueName(), cue.getCueAuto(), event.getNewValue(), null, 75, 0, cueListTableAudio, this);
            cuesManager.getCues().get(cuesManager.getCues().indexOf(cue)).stop();
            cuesManager.getCues().set(cuesManager.getCues().indexOf(cue), newCue);
            refreshTables();
        });

        cueFile.setCellValueFactory(new PropertyValueFactory<>("cueFile"));

        ObservableList<PlaylistFile> choiceList = cuesManager.getSFXFiles();
        choiceList.add(null);

        cueFile.setCellFactory(col -> new ComboBoxTableCell<>(new StringConverter<PlaylistFile>() {
            @Override
            public String toString(PlaylistFile playlistFile) {
                if (playlistFile == null) return "--None--";
                else return playlistFile.getFileName();
            }

            @Override
            public PlaylistFile fromString(String s) {
                for (PlaylistFile file : cuesManager.getSFXFiles()) {
                    if (file.getFileName().equals(s)) {
                        return file;
                    }
                }
                return null;
            }
        }, choiceList) {
            @Override
            public void updateItem(PlaylistFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setDisable(false);
                    setText(null);
                    return;
                }
                Cue cue = getTableView().getItems().get(getIndex());
                setVisible(cue == null || cue.getCueCommand() == COMMAND.PLAY || cue.getCueCommand() == COMMAND.STOP);
            }
        });


        cueFile.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueFile(event.getNewValue());

        });
        cueVol.setCellValueFactory(new PropertyValueFactory<>("cueVol"));
        cueVol.setCellFactory(col -> new EditCell<>("cueVol", new StringConverter<>() {
            @Override
            public String toString(SimpleDoubleProperty aDouble) {
                return Double.toString(aDouble.get());
            }

            @Override
            public SimpleDoubleProperty fromString(String s) {
                return new SimpleDoubleProperty(Double.parseDouble(s));
            }
        }));
        cueVol.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueVol(event.getNewValue().get());
        });
        cueTime.setCellValueFactory(new PropertyValueFactory<>("cueTime"));
        cueTime.setCellFactory(col -> new EditCell<>("cueTime", new DoubleStringConverter()));
        cueTime.setOnEditCommit(event-> {
            Cue cue = event.getRowValue();
            cue.setCueTime(event.getNewValue());
        });

        ObservableList<Double> doubleList = FXCollections.observableArrayList(Arrays.asList((double) -41, (double) -40, (double) -30, (double) -20, (double) -15, (double) -10,
                (double) -7.5, (double) -5, (double) -3, (double) 0, (double) 3,
                (double) 0, (double) 3, (double) 5, (double) 10));


        for(TableColumn<Cue, Double> col : faderColumns){
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getFaderValues().get(faderColumns.indexOf(col))));
            col.setCellFactory(x-> new ComboBoxTableCell<Cue, Double>(new StringConverter<Double>() {
                @Override
                public String toString(Double aDouble) {
                    if(aDouble<-40) return "-∞";
                    else return aDouble.toString();
                }

                @Override
                public Double fromString(String s) {
                    if(s.equals("-∞")) return (double) -41;
                    else return Double.parseDouble(s);
                }
            }, doubleList));
            col.setOnEditCommit(event-> {
                Cue cue = event.getRowValue();
                cue.getFaderValues().set(faderColumns.indexOf(col), event.getNewValue());
            });
        }

        playlistTable.setRowFactory(factory -> new StatusRow<>());
        playlistTable.setStyle("-fx-selection-bar: lightGrey");

        Button playlistPlaceHolder = new Button("Set Playlist Directory");
        playlistPlaceHolder.setOnAction(e->getPlaylistDirectory());
        playlistTable.setPlaceholder(playlistPlaceHolder);

        fileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        playlistOrder.setCellValueFactory(new PropertyValueFactory<>("playlistOrder"));
        excluded.setCellValueFactory(new PropertyValueFactory<>("excluded"));
        excluded.setCellFactory(factory ->  new ExclusionCell());

        playlistVolumeSlider.valueProperty().addListener((Observable, oldValue, newValue) -> playlistManager.setCurrentVolume(playlistVolumeSlider.getValue()));

        cueListVolumeSlider.valueProperty().addListener((Observable, oldValue, newValue) -> cuesManager.setCurrentCueVolume((double)newValue));

        ShowSequencer.getStage().fullScreenProperty().addListener((Observable, oldValue, newValue)->{
            if(!newValue){
                checkShowMode.setSelected(false);
                showMode();
            }});



        cueInsert.setOnAction(event -> cueListAddCue());
        cueDelete.setOnAction(event -> cueListRemoveCue());
        cueCopy.setOnAction(event -> cueListCopyCue());
        cuePaste.setOnAction(event -> cueListPasteCue());
        cuePasteAsNew.setOnAction(event -> cueListPasteCueAsNew());
        cuePaste.setDisable(true);
        cuePasteAsNew.setDisable(true);

        ContextMenu cueMenu = new ContextMenu(cueInsert, cueCopy, cuePaste, cuePasteAsNew, cueDelete);

        cueListTableAudio.setContextMenu(cueMenu);
        cueListTableFaders.setContextMenu(cueMenu);
        cueListTableAudio.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cueListTableFaders.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Button cueListPlaceHolder = new Button("Add Cue");
        cueListPlaceHolder.setOnAction(e-> cueListAddCue());
        cueListTableAudio.setPlaceholder(cueListPlaceHolder);
        cueListTableFaders.setPlaceholder(new Label(""));

        preferences = Preferences.userRoot().node(this.getClass().getName());
        String currentProjectDir = preferences.get("ProjectFile", null);
        if(currentProjectDir!=null) {
            Platform.runLater(()->openShow(new File(currentProjectDir)));
        }

    }

    private String colorToString(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
    }

    private Color stringToColor(String colorString) {
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }

        int red = Integer.parseInt(colorString.substring(0, 2), 16);
        int green = Integer.parseInt(colorString.substring(2, 4), 16);
        int blue = Integer.parseInt(colorString.substring(4, 6), 16);
        int alpha = Integer.parseInt(colorString.substring(6, 8), 16);

        return Color.rgb(red, green, blue, alpha / 255.0);
    }

    @FXML
    protected void launchPreferences(){
        new PreferencesScreen().open(this);
    }

    public void setPreferences(PreferencesController preferences){
        MIN_FADE_TIME = preferences.minFadeTime;
        RUNSCREEN_FADE_TIME = preferences.runScreenFadeTime;
        PLAYLIST_FADE_TIME = preferences.playlistFadeTime;

        faderManager.setDevice(preferences.device);

        commandColorMap.put(COMMAND.NONE, preferences.colorNone);
        commandColorMap.put(COMMAND.PLAY, preferences.colorPLAY);
        commandColorMap.put(COMMAND.STOP, preferences.colorSTOP);
        commandColorMap.put(COMMAND.VOLUME, preferences.colorVOLUME);
        commandColorMap.put(COMMAND.STOP_ALL, preferences.colorSTOP_ALL);
        commandColorMap.put(COMMAND.PLAYLIST_START, preferences.colorPLAYLIST_START);
        commandColorMap.put(COMMAND.PLAYLIST_CONT, preferences.colorPLAYLIST_CONT);
        commandColorMap.put(COMMAND.PLAYLIST_FADE, preferences.colorPLAYLIST_FADE);

        savePreferences();

    }

    public void savePreferences(){

        try{
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Create a new Document
            Document document = builder.newDocument();

            // Create root element
            Element root = document.createElement("preferences");
            document.appendChild(root);

            // Create book elements and add text content
            Element book1 = document.createElement("MIN_FADE_TIME");
            book1.appendChild(document.createTextNode(String.valueOf(MIN_FADE_TIME)));
            Element book2 = document.createElement("RUNSCREEN_FADE_TIME");
            book2.appendChild(document.createTextNode(String.valueOf(RUNSCREEN_FADE_TIME)));
            Element book3 = document.createElement("PLAYLIST_FADE_TIME");
            book3.appendChild(document.createTextNode(String.valueOf(PLAYLIST_FADE_TIME)));

            Element book4 = document.createElement("COMMAND_COLOURS");
            int index = 0;
            for(Color color : commandColorMap.values()){
                Element command = document.createElement(commandColorMap.keySet().toArray()[index].toString());
                command.appendChild(document.createTextNode(colorToString(color)));
                book4.appendChild(command);
                index++;
            }

            Element book5 = document.createElement("Faders");
            for(Fader fader : faderManager.getFaderList()){
                Element faderElement = document.createElement("fader");

                Element faderNum = document.createElement("Num");
                faderNum.appendChild(document.createTextNode(Integer.toString(fader.getFaderNum())));
                faderElement.appendChild(faderNum);

                Element faderName = document.createElement("Name");
                faderName.appendChild(document.createTextNode(fader.getName()));
                faderElement.appendChild(faderName);

                Element faderIsMix = document.createElement("isMix");
                faderIsMix.appendChild(document.createTextNode(Boolean.toString(fader.getIsMix())));
                faderElement.appendChild(faderIsMix);

                Element faderValue = document.createElement("value");
                faderValue.appendChild(document.createTextNode(Integer.toString(fader.getValue())));
                faderElement.appendChild(faderValue);

                Element faderVisible = document.createElement("isVisible");
                faderVisible.appendChild(document.createTextNode(Boolean.toString(fader.getIsVisible().get())));
                faderElement.appendChild(faderVisible);

                book5.appendChild(faderElement);
            }

            Element book6 = document.createElement("MidiDevice");
            if(faderManager.getDevice()!=null){
                book6.appendChild(document.createTextNode(faderManager.getDevice().getDeviceInfo().toString()));
            }else{
                book6.appendChild(document.createTextNode("null"));
            }

            root.appendChild(book1);
            root.appendChild(book2);
            root.appendChild(book3);
            root.appendChild(book4);
            root.appendChild(book5);
            root.appendChild(book6);

            // Write to XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if(!projectFile.exists()) {
                throw new Exception("Save Error");
            }

            // Specify your local file path
            StreamResult result = new StreamResult(projectFile + "/preferences");
            transformer.transform(source, result);

            refreshRunScreen();

        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Save Failed");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        }
    }

    private void loadPreferences(){
        try{
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(projectFile+"/preferences");

            org.w3c.dom.Node minFadeNode = document.getElementsByTagName("MIN_FADE_TIME").item(0);
            MIN_FADE_TIME = Double.parseDouble(minFadeNode.getTextContent());

            org.w3c.dom.Node runScreenFadeNode = document.getElementsByTagName("RUNSCREEN_FADE_TIME").item(0);
            RUNSCREEN_FADE_TIME = Double.parseDouble(runScreenFadeNode.getTextContent());

            org.w3c.dom.Node playlistFadeNode = document.getElementsByTagName("PLAYLIST_FADE_TIME").item(0);
            PLAYLIST_FADE_TIME = Double.parseDouble(playlistFadeNode.getTextContent());

            NodeList nodeList = document.getElementsByTagName("COMMAND_COLOURS").item(0).getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node command = nodeList.item(i);
                commandColorMap.put(COMMAND.valueOf(command.getNodeName()), stringToColor(command.getTextContent()));
            }

            NodeList faderList = document.getElementsByTagName("Faders").item(0).getChildNodes();
            ArrayList<Fader> addList = new ArrayList<>();
            for(int i=0; i<faderList.getLength(); i++){
                NodeList faderChildren = faderList.item(i).getChildNodes();

                Fader fader = new Fader(Integer.parseInt(faderChildren.item(0).getTextContent()), faderChildren.item(1).getTextContent(), Boolean.parseBoolean(faderChildren.item(2).getTextContent()),
                        Integer.parseInt(faderChildren.item(3).getTextContent()), Boolean.parseBoolean(faderChildren.item(4).getTextContent()));
                addList.add(fader);
            }
            faderManager.setFaderList(addList);

            for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()){
                if(info.toString().equals(document.getElementsByTagName("MidiDevice").item(0).getTextContent())){
                    faderManager.setDevice(MidiSystem.getMidiDevice(info));
                }
            }


        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Load Failed");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            newShow();

        }
    }


    @FXML
    protected void changeView(ActionEvent event) {
        CheckMenuItem chk = (CheckMenuItem) event.getSource();
        switch (chk.getId()) {
            case "checkRunScreen":
                if (!checkRunScreen.isSelected()) tabPane.getTabs().remove(tabRunScreen);
                else tabPane.getTabs().add(0, tabRunScreen);
                break;
            case "checkCueList":
                if (!checkCueList.isSelected()) tabPane.getTabs().remove(tabCueList);
                else tabPane.getTabs().add(1, tabCueList);
                break;
            case "checkPlaylist":
                if (!checkPlaylist.isSelected()) tabPane.getTabs().remove(tabPlaylist);
                else tabPane.getTabs().add(2, tabPlaylist);
                break;
        }
    }

    public void assertValidFiles(){
        boolean assertionPass = true;

        if(cuesManager.getSFXDirectory()!=null){
            for(Cue cue : cuesManager.getCues()){
                if(cue.getCueFile()!=null){
                    File file = new File(cuesManager.getSFXDirectory().getAbsolutePath() + "/"  + cue.getCueFile().getFileName());
                    if(!file.exists() || file.isDirectory()){
                        assertionPass = false;
                    }
                }
            }
        }

        if(playlistManager.getPlaylistDirectory()!=null){
            for(PlaylistFile playlistFile : playlistManager.getPlaylistFiles()){
                File file = new File(playlistManager.getPlaylistDirectory().getAbsolutePath() + "/" + playlistFile.getFileName());
                if(!file.exists() || file.isDirectory()){
                    assertionPass = false;
                }

            }
        }

        if(!assertionPass){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("FILES NOT FOUND!");
            alert.setContentText("Please check your SFX/Playlist directory's are valid then click \"Ok\" to restart");
            alert.showAndWait();

            saveShow();

            if(projectFile!=null){
                openShow(projectFile);
            }else{
                saveShow();
                newShow();
            }

        }

    }

    @FXML
    protected void close(){
        saveShow();
        ShowSequencer.getStage().close();
    }

    @FXML
    protected void newShowPressed() {
        saveShow();
        newShow();
    }


    private void newShow(){

        preferences.remove("ProjectFile");

        ShowSequencer.getStage().close();

        Platform.runLater( () -> {
            try{
                new ShowSequencer().start(new Stage());
            }catch (Exception e){
                close();
            }
        } );

    }

    @FXML
    protected void openShowPressed(){
        cueListReset();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFile = directoryChooser.showDialog(ShowSequencer.getStage());
        if (selectedFile != null) {

            openShow(selectedFile);
        }
    }


    private void openShow(File selectedFile){


        try{
            ShowSequencer.getStage().setTitle(selectedFile.getName());

            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(selectedFile+"/data");

            org.w3c.dom.Node sfxNode = document.getElementsByTagName("SFXDirectory").item(0);
            if(!sfxNode.getTextContent().equals("--None--")) cuesManager.setSFXDirectory(new File(sfxNode.getTextContent()));


            org.w3c.dom.Node playlistNode = document.getElementsByTagName("PlaylistDirectory").item(0);
            if(!playlistNode.getTextContent().equals("--None--")) playlistManager.setDirectory(new File(playlistNode.getTextContent()));

            cuesManager.getCues().clear();
            // Access elements by tag name
            NodeList nodeList = document.getElementsByTagName("Cue");
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node cue = nodeList.item(i);
                NodeList cueChildren = cue.getChildNodes();

                PlaylistFile playlistFile = null;
                if (!cueChildren.item(4).getTextContent().equals("--None--")){
                    playlistFile = cuesManager.getSFXFiles().stream().filter(x->x.getFileName().equals(cueChildren.item(4).getTextContent())).findFirst().orElse(null);
                }

                cuesManager.getCues().add(new Cue(cueChildren.item(0).getTextContent(),
                        cueChildren.item(1).getTextContent(),
                        Double.parseDouble(cueChildren.item(2).getTextContent()),
                        COMMAND.valueOf(cueChildren.item(3).getTextContent()),
                        null,
                        Double.parseDouble(cueChildren.item(5).getTextContent()),
                        Double.parseDouble(cueChildren.item(6).getTextContent()),
                        cueListTableAudio, this));
                cuesManager.getCues().get(i).setCueFile(playlistFile);

                NodeList faderNodeList = cueChildren.item(7).getChildNodes();
                for (int j = 0; j < faderNodeList.getLength(); j++) {
                    org.w3c.dom.Node faderNode = faderNodeList.item(j);
                    cuesManager.getCues().get(i).getFaderValues().set(j, Double.parseDouble(faderNode.getTextContent()));

                }
            }



            cueListTableAudio.setItems(cuesManager.getCues());
            cueListTableFaders.setItems(cuesManager.getCues());



            preferences.put("ProjectFile", selectedFile.getAbsolutePath());
            projectFile = selectedFile;
            menuItemSaveShow.setDisable(false);
            menuItemPreferences.setDisable(false);

            loadPreferences();

            refreshRunScreen();
            refreshTables();
            playlistTable.refresh();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Project Loaded");
            alert.showAndWait();

        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Load Failed");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            newShow();
        }
    }

    @FXML
    protected void saveAsShow(){
        cueListReset();
        FileChooser fileChooser = new FileChooser();
        File selectedDirectory = fileChooser.showSaveDialog(ShowSequencer.getStage());
        if (selectedDirectory != null) {
            save(selectedDirectory);
        }
    }

    @FXML
    protected void saveShow(){
        cueListReset();
        if(projectFile!=null){
            save(projectFile);
        }
    }

    private void save(File saveDestination) {

        try{
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Create a new Document
            Document document = builder.newDocument();

            // Create root element
            Element root = document.createElement("data");
            document.appendChild(root);

            // Create book elements and add text content
            Element book1 = document.createElement("PlaylistDirectory");
            if(playlistManager.getPlaylistDirectory()!=null) book1.appendChild(document.createTextNode(playlistManager.getPlaylistDirectory().getAbsolutePath()));
            else book1.appendChild(document.createTextNode("--None--"));
            Element book2 = document.createElement("SFXDirectory");
            if(cuesManager.getSFXDirectory()!=null) book2.appendChild(document.createTextNode(cuesManager.getSFXDirectory().getAbsolutePath()));
            else book2.appendChild(document.createTextNode("--None--"));

            Element book3 = document.createElement("Cues");
            for(Cue cue : cuesManager.getCues()){
                Element num = document.createElement("CueNumber");
                num.appendChild(document.createTextNode(cue.getCueNum()));
                Element name = document.createElement("CueName");
                name.appendChild(document.createTextNode(cue.getCueName()));
                Element auto = document.createElement("CueAuto");
                auto.appendChild(document.createTextNode(Double.toString(cue.getCueAuto())));
                Element command = document.createElement("CueCommand");
                command.appendChild(document.createTextNode(cue.getCueCommand().name()));
                Element file = document.createElement("CueFile");
                if(cue.getCueFile()!=null)  file.appendChild(document.createTextNode(cue.getCueFile().getFileName()));
                else file.appendChild(document.createTextNode("--None--"));
                Element vol = document.createElement("CueVolume");
                vol.appendChild(document.createTextNode(Double.toString(cue.getCueVol().get())));
                Element time = document.createElement("CueTime");
                time.appendChild(document.createTextNode(Double.toString(cue.getCueTime())));

                Element faders = document.createElement("faders");

                int i = 1;
                for(double value : cue.getFaderValues()){
                    Element faderNum = document.createElement("Fader" + i);
                    faderNum.appendChild(document.createTextNode(String.valueOf(value)));
                    faders.appendChild(faderNum);
                    i++;
                }

                Element cueEntry = document.createElement("Cue");
                cueEntry.appendChild(num);
                cueEntry.appendChild(name);
                cueEntry.appendChild(auto);
                cueEntry.appendChild(command);
                cueEntry.appendChild(file);
                cueEntry.appendChild(vol);
                cueEntry.appendChild(time);
                cueEntry.appendChild(faders);

                book3.appendChild(cueEntry);
            }

            root.appendChild(book1);
            root.appendChild(book2);
            root.appendChild(book3);

            // Write to XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if(!saveDestination.exists()) {
                if(!saveDestination.mkdir()){
                    throw new Exception("Save Error");
                }
            }

            // Specify your local file path
            StreamResult result = new StreamResult(saveDestination + "/data");
            transformer.transform(source, result);

            preferences.put("ProjectFile", saveDestination.getAbsolutePath());
            menuItemSaveShow.setDisable(false);
            menuItemPreferences.setDisable(false);
            projectFile = saveDestination;
            ShowSequencer.getStage().setTitle(saveDestination.getName());

            savePreferences();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Project Saved");
            alert.showAndWait();

        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Save Failed");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        }

    }



    @FXML
    protected void showMode() {
        if (checkShowMode.isSelected()){

            assertValidFiles();

            ShowSequencer.getStage().setFullScreenExitHint("");
            ShowSequencer.getStage().setFullScreen(true);

            tempTabs.addAll(tabPane.getTabs());
            tabPane.getTabs().clear();
            tabPane.getTabs().add(tabRunScreen);

            tabPane.lookup(".tab-header-area").setVisible(false);

            menuBar.setVisible(false);
            ShowSequencer.fontSizeBinding.invalidate();

        }else{
            ShowSequencer.getStage().setFullScreen(false);

            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(tempTabs);
            tempTabs.clear();

            tabPane.lookup(".tab-header-area").setVisible(true);

            menuBar.setVisible(true);
        }
    }

    //Run Screen Functions

    @FXML
    protected void displayCue1Seek(MouseEvent event){
        if(displayedCues.isEmpty()) return;
        double seekValue = event.getX()/((TextField)displayedCuesNodes[0][0]).getWidth();
        Duration total = displayedCues.get(0).getCueFile().getPlayer().getTotalDuration();
        Duration seekTime = total.multiply(seekValue);
        displayedCues.get(0).getCueFile().getPlayer().seek(seekTime);
        ((TextField) displayedCuesNodes[0][0]).selectRange(0,0);
    }

    @FXML
    protected void displayCue1SeekStart(){
        if(displayedCues.isEmpty()) return;
        displayedCues.get(0).getCueFile().getPlayer().pauseFaded(displayedCues.get(0).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue1SeekEnd(){
        if(displayedCues.isEmpty()) return;
        displayedCues.get(0).getCueFile().getPlayer().playFaded(displayedCues.get(0).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue2Seek(MouseEvent event){
        if(displayedCues.size()<=1) return;
        double seekValue = event.getX()/((TextField)displayedCuesNodes[0][1]).getWidth();
        Duration total = displayedCues.get(1).getCueFile().getPlayer().getTotalDuration();
        Duration seekTime = total.multiply(seekValue);
        displayedCues.get(1).getCueFile().getPlayer().seek(seekTime);
        ((TextField) displayedCuesNodes[0][1]).selectRange(0,0);
    }

    @FXML
    protected void displayCue2SeekStart(){
        if(displayedCues.size()<=1) return;
        displayedCues.get(1).getCueFile().getPlayer().pauseFaded(displayedCues.get(1).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue2SeekEnd(){
        if(displayedCues.size()<=1) return;
        displayedCues.get(1).getCueFile().getPlayer().playFaded(displayedCues.get(1).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue3Seek(MouseEvent event){
        if(displayedCues.size()<=2) return;
        double seekValue = event.getX()/((TextField)displayedCuesNodes[0][2]).getWidth();
        Duration total = displayedCues.get(2).getCueFile().getPlayer().getTotalDuration();
        Duration seekTime = total.multiply(seekValue);
        displayedCues.get(2).getCueFile().getPlayer().seek(seekTime);
        ((TextField) displayedCuesNodes[0][2]).selectRange(0,0);
    }

    @FXML
    protected void displayCue3SeekStart(){
        if(displayedCues.size()<=2) return;
        displayedCues.get(2).getCueFile().getPlayer().pauseFaded(displayedCues.get(2).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue3SeekEnd(){
        if(displayedCues.size()<=2) return;
        displayedCues.get(2).getCueFile().getPlayer().playFaded(displayedCues.get(2).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue4Seek(MouseEvent event){
        if(displayedCues.size()<=3) return;
        double seekValue = event.getX()/((TextField)displayedCuesNodes[0][3]).getWidth();
        Duration total = displayedCues.get(3).getCueFile().getPlayer().getTotalDuration();
        Duration seekTime = total.multiply(seekValue);
        displayedCues.get(3).getCueFile().getPlayer().seek(seekTime);
        ((TextField) displayedCuesNodes[0][3]).selectRange(0,0);
    }

    @FXML
    protected void displayCue4SeekStart(){
        if(displayedCues.size()<=3) return;
        displayedCues.get(3).getCueFile().getPlayer().pauseFaded(displayedCues.get(0).getCueVol().get(), MIN_FADE_TIME);
    }

    @FXML
    protected void displayCue4SeekEnd(){
        if(displayedCues.size()<=3) return;
        displayedCues.get(3).getCueFile().getPlayer().playFaded(displayedCues.get(0).getCueVol().get(), MIN_FADE_TIME);
    }



    @FXML
    protected void refreshRunScreen() {

        setPlaylistControlPanelDisabled(playlistControlPanelDisabled);

        comboBoxCueJump.setItems(null);
        comboBoxCueJump.setItems(FXCollections.observableArrayList(cuesManager.getCues()));

        displayedCues.clear();
        for(Cue cue : cuesManager.getCues()){
            if(cue.getState().equals(States.STATE.PLAYING) && cue.getCueCommand().equals(COMMAND.PLAY) && cue.getCueFile()!=null) {
                displayedCues.add(cue);
            }
        }


        Collections.reverse(displayedCues);

        for (ReadOnlyObjectProperty<Duration> obs : displayListeners.keySet()){
            obs.removeListener(displayListeners.get(obs));
        }


        for (int i = 0; i < 4; i++) {
            if(displayedCuesNodes[0][i]!=null && displayedCuesNodes[1][i]!=null){
                ((TextField) displayedCuesNodes[0][i]).textProperty().unbind();
                ((TextField) displayedCuesNodes[1][i]).textProperty().unbind();
                ((TextField) displayedCuesNodes[0][i]).setText("");
                displayedCuesNodes[0][i].setStyle("-fx-font-size: 1em;");
                ((TextField) displayedCuesNodes[1][i]).setText("");
                ((Label) displayedCuesNodes[5][i]).setText("0:00");
                ((Label) displayedCuesNodes[6][i]).setText("0:00");
            }
        }

        if(!displayedCues.isEmpty()){
            for (int i = 0; i < Math.min(4, displayedCues.size()); i++) {
                if(displayedCuesNodes[0][i]!=null && displayedCuesNodes[1][i]!=null) {
                    ((TextField) displayedCuesNodes[0][i]).setText(displayedCues.get(i).getCueFile().getFileName());
                    ((Label) displayedCuesNodes[6][i]).setText(String.format("%2d",(int)Math.floor(displayedCues.get(i).getCueFile().getPlayer().getTotalDuration().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(displayedCues.get(i).getCueFile().getPlayer().getMediaPlayer().getTotalDuration().toSeconds()%60)));
                    ((TextField) displayedCuesNodes[1][i]).textProperty().bind(displayedCues.get(i).getCueVol().asString());
                    ChangeListener<Duration> listener = getDurationChangeListener(i);
                    displayedCues.get(i).getCueFile().getPlayer().currentTimeProperty().addListener(listener);
                    displayListeners.put(displayedCues.get(i).getCueFile().getPlayer().currentTimeProperty(), listener);
                }
            }
        }



        if (cuesManager.getCurrentCueNum() >= 0) {
            Cue currentCue = cuesManager.getCues().get(cuesManager.getCurrentCueNum());
            textCurrentCueNum.setText(currentCue.getCueNum());
            textCurrentCueName.setText(currentCue.getCueName());
            textCurrentCueTime.setText(Double.toString(currentCue.getCueTime()));

            if(currentCue.getCueCommand().equals(COMMAND.STOP)){
                List<Cue> prevCues = cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum());
                for(Cue cue : prevCues){
                    if(cue.getCueFile() !=null && cue.getCueFile().equals(currentCue.getCueFile()) && cue.getState().equals(States.STATE.PLAYING)) {
                        currentCueProgress.progressProperty().bind(cue.getProgress());
                    }
                }
            }else if(currentCue.getCueCommand().equals(COMMAND.STOP_ALL)){
                List<Cue> prevCues = new ArrayList<>(List.copyOf(cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum())));
                List<Cue> cuesToStop = new ArrayList<>();
                for(Cue cue : prevCues){
                    if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.PLAY)) {
                        cuesToStop.add(cue);
                    }else if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.STOP)) {
                        cuesToStop.stream().filter(x -> x.getCueFile().equals(cue.getCueFile())).findFirst().ifPresent(cuesToStop::remove);
                    }else if(cue.getCueCommand().equals(MainController.COMMAND.STOP_ALL)){
                        cuesToStop.clear();
                    }
                }
                if(!cuesToStop.isEmpty()) currentCueProgress.progressProperty().bind(cuesToStop.stream().findAny().get().getProgress());
            } else if(currentCue.getCueCommand().equals(COMMAND.PLAYLIST_FADE)){
                currentCueProgress.progressProperty().bind(playlistManager.progress);
            } else{
                currentCueProgress.progressProperty().bind(currentCue.getProgress());
            }


        }else{
            textCurrentCueNum.setText("-1");
            textCurrentCueName.setText("PreShow");
            textCurrentCueTime.setText("");
        }

        if (cuesManager.getCurrentCueNum() < cuesManager.getCues().size() - 1) {
            Cue nextCue = cuesManager.getCues().get(cuesManager.getCurrentCueNum() + 1);
            textNextCueNum.setText(nextCue.getCueNum());
            textNextCueName.setText(nextCue.getCueName());
            if(nextCue.getCueAuto()>=0){
                textNextCueAuto.setText(Double.toString(nextCue.getCueAuto()));
            }
            else{
                textNextCueAuto.setText("N/A");
            }
            if(nextCue.getCueCommand()!=COMMAND.NONE){
                textNextCueCommand.setText(nextCue.getCueCommand().toString());
            }
            else{
                textNextCueCommand.setText("--None--");
            }
            textNextCueCommand.setBackground(new Background(new BackgroundFill(commandColorMap.get(nextCue.getCueCommand()), new CornerRadii(3), Insets.EMPTY)));
            textNextCueCommand.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.SOLID, new CornerRadii(3), BorderWidths.DEFAULT)));
            if(nextCue.getCueFile()!=null){
                textNextCueAudioFile.setText(nextCue.getCueFile().getFileName());
            }
            else{
                textNextCueAudioFile.setText("N/A");
            }
            if( nextCue.getCueCommand() == COMMAND.PLAY
                    || nextCue.getCueCommand() == COMMAND.VOLUME
                    || nextCue.getCueCommand() == COMMAND.PLAYLIST_START
                    || nextCue.getCueCommand() == COMMAND.PLAYLIST_CONT){
                textNextCueVol.setText(Double.toString(nextCue.getCueVol().get()));
            }else{
                textNextCueVol.setText("N/A");
            }
            if (nextCue.getCueCommand() != COMMAND.NONE){
                textNextCueTime.setText(Double.toString(nextCue.getCueTime()));
            }else{
                textNextCueTime.setText("N/A");
            }
        }else{
            textNextCueNum.setText("");
            textNextCueName.setText("");
            textNextCueAuto.setText("");
            textNextCueCommand.setText("");
            textNextCueAudioFile.setText("");
            textNextCueVol.setText("");
            textNextCueTime.setText("");

        }
    }

    private ChangeListener<Duration> getDurationChangeListener(int i) {
        return (Obs, oldValue, newValue) ->{
            double progressValue = newValue.toSeconds()/displayedCues.get(i).getCueFile().getPlayer().getTotalDuration().toSeconds();
            String style = String.format("-fx-background-color: linear-gradient(to right, green %.2f%%, white %.2f%%); -fx-font-size: 1em;",
                    progressValue * 100, progressValue * 100);
            displayedCuesNodes[0][i].setStyle(style);
            ((Label) displayedCuesNodes[5][i]).setText(String.format("%2d",(int)Math.floor(displayedCues.get(i).getCueFile().getPlayer().getCurrentTime().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(displayedCues.get(i).getCueFile().getPlayer().getCurrentTime().toSeconds()%60)));
        };
    }

    @FXML
    protected void runScreenAllOff(){
        cuesManager.stop(RUNSCREEN_FADE_TIME);
    }

    @FXML
    protected void cueJumpTo(){


        if(comboBoxCueJump.getSelectionModel().getSelectedIndex()>=0){
            int selectedIndex = comboBoxCueJump.getSelectionModel().getSelectedIndex();
            cuesManager.jumpTo(selectedIndex);
        }
        if(comboBoxCueJumpCopy.getSelectionModel().getSelectedIndex()>=0){
            int selectedIndex = comboBoxCueJumpCopy.getSelectionModel().getSelectedIndex();
            cuesManager.jumpTo(selectedIndex);
        }

        refreshRunScreen();
    }

    @FXML
    protected void cueDisplayVolUp(ActionEvent event){
        int index = findNodeRow(displayedCuesNodes, (Node) event.getSource());
        if(index<=displayedCues.size()-1){
            displayedCues.get(index).setCueVol(Math.min(100, displayedCues.get(index).getCueVol().get()+5));
        }
        refreshTables();
    }

    @FXML
    protected void cueDisplayVolDn(ActionEvent event){
        int index = findNodeRow(displayedCuesNodes, (Node) event.getSource());
        if(index<=displayedCues.size()-1){
            displayedCues.get(index).setCueVol(Math.max(0, displayedCues.get(index).getCueVol().get()-5));
        }
        refreshTables();
    }

    @FXML
    protected void cueDisplayFade(ActionEvent event){
        int index = findNodeRow(displayedCuesNodes, (Node) event.getSource());
        if(index<=displayedCues.size()-1){
            displayedCues.get(index).pausePlay(RUNSCREEN_FADE_TIME);
        }
        refreshTables();
    }

    public static int findNodeRow(Node[][] nodes, Node target) {
        for (Node[] node : nodes) {
            for (int col = 0; col < node.length; col++) {
                if (node[col] == target) {
                    return col;
                }
            }
        }
        return -1;
    }

    //Cue Tab Functions

    public void refreshTables(){
        cueListTableFaders.refresh();
        cueListTableAudio.refresh();

        ScrollBar scrollBar1 = getScrollBar(cueListTableAudio);
        ScrollBar scrollBar2 = getScrollBar(cueListTableFaders);
        if(scrollBar1!=null && scrollBar2!=null){
            scrollBar1.valueProperty().bindBidirectional(scrollBar2.valueProperty());
        }

        comboBoxCueJumpCopy.setItems(null);
        comboBoxCueJumpCopy.setItems(FXCollections.observableArrayList(cuesManager.getCues()));

    }

    private ScrollBar getScrollBar(TableView<?> tableView) {
        for (Node node : tableView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation().toString().equals("VERTICAL")) {
                    return scrollBar;
                }
            }
        }
        return null;
    }


    @FXML
    protected void cueListPasteCue(){
        if (cueListTableAudio.getSelectionModel().getSelectedItem()==null || cuesManager.getCueClipboard().size()!=1) return;
        int selectedIndex = cueListTableAudio.getSelectionModel().getSelectedIndex();
        cuesManager.pasteCue(selectedIndex);
        cueListTableAudio.getSelectionModel().select(selectedIndex);
    }

    @FXML
    protected void cueListPasteCueAsNew(){
        if (cueListTableAudio.getSelectionModel().getSelectedItem()==null || cuesManager.getCueClipboard().isEmpty()) return;

        int selectionStart = cueListTableAudio.getSelectionModel().getSelectedIndex()+1;

        cuesManager.pasteCueAsNew(selectionStart);

        cueListTableAudio.getSelectionModel().clearSelection();
        cueListTableAudio.getSelectionModel().selectRange(selectionStart, selectionStart + cuesManager.getCueClipboard().size() - 1);

    }

    @FXML
    protected void cueListCopyCue(){
        if (cueListTableAudio.getSelectionModel().getSelectedItems().isEmpty()) return;
        cuesManager.copyCues(cueListTableAudio.getSelectionModel().getSelectedItems());
        cuePaste.setDisable(cueListTableAudio.getSelectionModel().getSelectedItems().size() != 1);
        cuePasteAsNew.setDisable(false);
    }


    @FXML
    protected void cueListAddCue(){
        cuesManager.addCue();

        cueListTableAudio.setItems(cuesManager.getCues());
        cueListTableFaders.setItems(cuesManager.getCues());
    }

    @FXML
    protected void cueListRemoveCue(){
        if (!cueListTableAudio.getSelectionModel().getSelectedItems().isEmpty()) {
            // Create a copy of the selected items
            List<Cue> selectedCues = new ArrayList<>(cueListTableAudio.getSelectionModel().getSelectedItems());
            // Iterate over the copy and remove each item from the cues list
            cuesManager.removeCue(selectedCues);
            //Clear the selection after removal
            cueListTableAudio.getSelectionModel().clearSelection();
        }
        refreshTables();
    }

    @FXML
    protected void getSFXDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(ShowSequencer.getStage());
        if (selectedDirectory != null) {
            cuesManager.setSFXDirectory(selectedDirectory);
        }
    }

    @FXML
    protected void cueListNext(){
        assertValidFiles();

        cuesManager.next();
        refreshRunScreen();

    }

    @FXML
    protected void cueListPrevious(){
        assertValidFiles();

        cuesManager.previous();
        refreshRunScreen();
    }


    @FXML
    protected void cueListReset(){

        ExponentialFade volFade = exponentialFadeMap.remove("||CUELIST||");
        if(volFade!=null) volFade.remove();

        cuesManager.reset();
        playlistManager.stop(MIN_FADE_TIME);
        setPlaylistControlPanelDisabled(true);
        refreshRunScreen();
    }



    //Playlist Tab Functions

    @FXML
    protected void changeExcludedKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.SPACE)){
            PlaylistFile file = playlistTable.getSelectionModel().getSelectedItem();
            if (file != null) {
                file.setExcluded(!file.isExcluded());
                playlistTable.refresh();
            }
        }
    }

    @FXML
    protected void playlistSeekTrack(MouseEvent event){
        playlistManager.seek(event.getX()/playlistProgressBar.getWidth());
    }
    
    @FXML
    protected void playlistSeekStart(){
        playlistManager.seekStart();
    }

    @FXML
    protected void playlistSeekEnd(){
        playlistManager.seekEnd();
    }

    @FXML
    protected void playlistNextTrack(){
        playlistManager.nextTrack(PLAYLIST_FADE_TIME);
    }

    @FXML
    protected void playlistPrevTrack(){
        playlistManager.prevTrack(PLAYLIST_FADE_TIME);
    }


    @FXML
    protected void pausePlayPlaylist(){
        playlistManager.pausePlay(PLAYLIST_FADE_TIME);
    }


    @FXML
    protected void stopPlaylistPressed(){
        playlistManager.stop(PLAYLIST_FADE_TIME);
    }

    @FXML
    protected void shufflePlaylist(){
        playlistManager.shuffle();
    }


    private static boolean isNodeContained(Node parent, Node child) {
        Node current = child;
        while (current != null) {
            if (current == parent) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @FXML
    protected void clickPlaylistTable(MouseEvent event){
        if (event.getTarget().getClass().equals(ExclusionCell.class)){
            playlistTable.getSelectionModel().getSelectedItem().setExcluded(!playlistTable.getSelectionModel().getSelectedItem().isExcluded());
        }

        if (isNodeContained(playlistTable ,event.getPickResult().getIntersectedNode())
                && event.getClickCount()==2
                && playlistTable.getSelectionModel().getSelectedItem()!=null
                && (event.getPickResult().getIntersectedNode().getParent().getClass().toString().endsWith("StatusRow") || event.getPickResult().getIntersectedNode().getClass().getName().endsWith("LabeledText"))){
            playlistManager.jumpTo(playlistTable.getSelectionModel().getSelectedItem(), PLAYLIST_FADE_TIME);
        }
        playlistTable.refresh();
    }


    @FXML
    protected void getPlaylistDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(ShowSequencer.getStage());
        if (selectedDirectory != null) {
            playlistManager.setDirectory(selectedDirectory);
        }
    }
}

