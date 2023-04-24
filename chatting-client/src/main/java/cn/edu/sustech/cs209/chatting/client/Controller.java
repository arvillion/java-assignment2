package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.exceptions.SyncException;
import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;
import cn.edu.sustech.cs209.chatting.common.messages.MessageType;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.BasePacket;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.OfflineException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.DocFlavor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    @FXML
    ListView<BaseMessage> chatContentList;

    @FXML
    ListView<Room> chatList;


    String username;

    Client client;
    Logger logger = LoggerFactory.getLogger(getClass());


    String currentTargetName;
    LinkedHashMap<String, Room> chattedTargets = new LinkedHashMap<>();

    private List<String> individuals = new ArrayList<>();
    private List<String> groups = new ArrayList<>();
    private Map<String, List<BaseMessage>> allMessages = new HashMap<>();
    private BaseMessage tmpMessage;
    private String tmpGroupName;
    @FXML
    private Label currentOnlineCnt;

    @FXML
    private TextArea inputArea;
    @FXML
    private Button sendButton;
    @FXML
    private Label currentUsername;

    private void setUsername(String username) {
        this.username = username;
        currentUsername.setText("Current User: " + username);
    }
    public Controller() {
    }

    public void setIndividuals(List<String> individuals) {
        this.individuals = individuals;
        Platform.runLater(() -> {
            currentOnlineCnt.setText("Online: " + (individuals.size() + 1));
        });
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getIndividuals() {
        return individuals;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void onServerOffline() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Server is off");
            alert.showAndWait();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = new Client(this, "localhost", 23456);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Unable to connect to the server");
            alert.showAndWait();
            exit();
            return;
        }


        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");
        AtomicBoolean closed = new AtomicBoolean(false);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventHandler(EventType.ROOT, e -> {
            closed.set(false);
        });

        dialog.setOnCloseRequest(event -> {
            System.out.println("close");
            closed.set(true);
        });

        Hyperlink hyperlink = new Hyperlink("Click to register");
        hyperlink.setOnAction(e -> showRegisterDialog());
        dialog.getDialogPane().setGraphic(hyperlink);

        Optional<String> input = dialog.showAndWait();

        while (true) {

            if (input.isPresent()) {
                if (!input.get().isEmpty()) {
                    /*
                       TODO: Check if there is a user with the same name among the currently logged-in users,
                             if so, ask the user to change the username
                     */
                    String username = input.get();
                    try {
                        client.login(username, "no pwd");
                        setUsername(username);
                        break;
                    } catch (OfflineException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Unable to connect to the server");
                        alert.showAndWait();

                    } catch (SyncException e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText(e.getMessage());
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Invalid input");
                    alert.showAndWait();
                }

                input = dialog.showAndWait();
                if (closed.get() && !client.isLogined()) {
                    System.out.println("quit");
                    Platform.exit();
                    return;
                }
            } else {
                Platform.exit();
                return;
            }
        }
        new Thread(client).start();

        sendButton.addEventHandler(AckEvent.type, e -> {
//            logger.info("receive ack");
            storageMessage(tmpMessage, false);
            sendButton.setDisable(false);
            inputArea.clear();
        });

        chatContentList.setCellFactory(new MessageCellFactory());
        chatList.setCellFactory(new RoomCellFactory());


    }


    public void receiveMessage(BaseMessage message) {
        storageMessage(message, true);

    }

    /**
     * Storage message, update chat list and current chat content list, if necessary
     * @param message
     * @param isRecv
     */
    private void storageMessage(BaseMessage message, boolean isRecv) {
        String targetName = getTargetNameFromMessage(message, isRecv);
        allMessages.putIfAbsent(targetName, new ArrayList<>());
        allMessages.get(targetName).add(message);
        if (targetName.equals(currentTargetName)) {
            updateCurrentConversion();
            createOrLiftTarget(targetName, false);

        } else {
            createOrLiftTarget(targetName, true);
        }
    }

    private void createOrLiftTarget(String targetName, boolean unread) {
        Room target = chattedTargets.get(targetName);

        if (target == null) {
            target = new Room(targetName);
            chattedTargets.put(targetName, target);
        } else {
            target = chattedTargets.remove(targetName);
            target.setTimestamp(new Date().getTime());
            chattedTargets.put(targetName, target);
        }
        target.setRead(!unread);
        updateChatList();
    }

    private void updateChatList() {
        Platform.runLater(() -> {
            ObservableList<Room> rooms = FXCollections.observableArrayList();
            rooms.setAll(chattedTargets.values());
            Collections.reverse(rooms);
            logger.info(rooms.toString());
            chatList.setItems(rooms);
        });
    }

    private void updateCurrentConversion() {
        Platform.runLater(() -> {
            ObservableList<BaseMessage> messages = FXCollections.observableArrayList();
            allMessages.putIfAbsent(currentTargetName, new ArrayList<>());

            messages.setAll(allMessages.get(currentTargetName));
            chatContentList.setItems(messages);
            chatContentList.refresh();

        });

    }

    public void setCurrentTarget(String targetName) {
        String oldTargetName = currentTargetName;
        if (chattedTargets.get(targetName) == null) {
            createOrLiftTarget(targetName, false);
        } else {
            Room r = chattedTargets.get(targetName);
            if (!r.isRead()) {
                r.setRead(true);
                updateChatList();
            }
        }
        currentTargetName = targetName;
        if (oldTargetName == null || !oldTargetName.equals(currentTargetName)) {
            updateCurrentConversion();
        }
        Platform.runLater(() -> {
            chatList.getSelectionModel().select(chattedTargets.get(targetName));
        });

    }

    private void showRegisterDialog() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Register");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnCloseRequest(event -> {
            closed.set(true);
        });

        while (true) {
            if (input.isPresent()) {
                if (!input.get().isEmpty()) {
                    username = input.get();
                    try {
                        client.register(username, "no pwd");
                        break;
                    } catch (OfflineException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Unable to connect to the server");
                        alert.showAndWait();

                    } catch (SyncException e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText(e.getMessage());
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Invalid input");
                    alert.showAndWait();
                }

                input = dialog.showAndWait();
                if (closed.get()) {
                    dialog.close();
                    return;
                }
            } else {
                dialog.close();
                return;
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "success");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void exit() {
        Platform.exit();
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTargetNameFromMessage(BaseMessage message, boolean isRecv) {
        String targetName;
        if (isRecv) {
            String sendTo = message.getSendTo();
            if (sendTo.startsWith("G:")) {
                targetName = message.getSendTo();
            } else {
                targetName = message.getSentBy();
            }
        } else {
            targetName = message.getSendTo();
        }

        return targetName;
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll(getIndividuals());

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            String targetName = "U:" + user.get();
            setCurrentTarget(targetName);
            logger.info(currentTargetName);
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();


        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        stage.setTitle("New group");
        ListView<String> listView = new ListView<>();
        Button button = new Button("OK");
        List<String> list = new ArrayList<>();
        listView.getItems().addAll(individuals);

        TextField textField = new TextField();

        listView.setCellFactory(CheckBoxListCell.forListView(item -> {
            BooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    list.add(item);
                } else {
                    list.remove(item);
                }
            });
            return observable;
        }));

        button.setOnMouseClicked(event -> {
            String groupName = textField.getText();
            if (list.size() == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Plz select a least a user as group member");
                alert.showAndWait();
                return;
            }
            if (groupName.length() == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Plz enter group name");
                alert.showAndWait();
                return;
            }
            try {
                tmpGroupName = groupName;
                client.createGroup(button, groupName, list);
                button.setDisable(true);
            } catch (OfflineException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "server offline");
                alert.showAndWait();
            } catch (SyncException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage());
                alert.showAndWait();
            }

        });

        button.addEventHandler(OKEvent.type, event -> {
            setCurrentTarget("G:" + tmpGroupName);
            Platform.runLater(() -> {
                button.setDisable(false);
                stage.close();
            });
        });


        HBox box = new HBox(10);
        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(textField, listView);

        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(vBox, button);
        stage.setScene(new Scene(box));
        stage.showAndWait();

    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        if (currentTargetName == null || currentTargetName.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No target specified");
            alert.showAndWait();
            return;
        }
        String str = inputArea.getText();
        if (str.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Message can not be empty");
            alert.showAndWait();
            return;
        }
        try {
            tmpMessage = client.sendTextMessage(sendButton, "U:"+username, currentTargetName, str);
            sendButton.setDisable(true);
        } catch (OfflineException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Server offline");
            alert.showAndWait();
            return;
        } catch (SyncException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage());
            alert.showAndWait();
            return;
        }

    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<BaseMessage>, ListCell<BaseMessage>> {
        @Override
        public ListCell<BaseMessage> call(ListView<BaseMessage> param) {
            return new ListCell<>() {

                @Override
                public void updateItem(BaseMessage msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    String text = "";
                    if (msg.getMessageType() == MessageType.TEXT) {
                        text = ((TextMessage) msg).getText();
                    }
                    Label msgLabel = new Label(text);
                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");


                    if (("U:" + username).equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    private class RoomCellFactory implements Callback<ListView<Room>, ListCell<Room>> {
        @Override
        public ListCell<Room> call(ListView<Room> param) {
            ListCell<Room> cell =  new ListCell<>() {

                @Override
                public void updateItem(Room room, boolean empty) {
                    super.updateItem(room, empty);
                    if (empty || Objects.isNull(room)) {
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(room.getName());
                    Label msgLabel = new Label(new Date(room.getTimestamp()).toString());
                    nameLabel.setPrefSize(80, 20);
                    nameLabel.setWrapText(true);
                    if (room.isRead()) {
                        nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
                    } else {
                        nameLabel.setStyle("-fx-border-color: #0052ff; -fx-border-width: 2px;");
                    }

                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel, msgLabel);
                    msgLabel.setPadding(new Insets(0, 0, 0, 20));

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);


                }
            };
            cell.setOnMouseClicked((e) -> {
                if (cell.getItem() != null) {
                    setCurrentTarget(cell.getItem().getName());
                }
            });
            return cell;

        }
    }
}
