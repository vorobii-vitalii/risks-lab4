package com.example.riskmgmtlab4;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class NamedTable<T> {
    private final Node node;
    private final TableView<T> tableView;

    public NamedTable(TableView<T> tableView, String tableName) {
        this.tableView = tableView;
        node = create(tableView, tableName);
    }

    public TableView<T> getTableView() {
        return tableView;
    }

    public void clearItems() {
        tableView.getItems().clear();
    }

    public ObservableList<TableColumn<T,?>> getColumns() {
        return tableView.getColumns();
    }

    public ObservableList<T> getItems() {
        return tableView.getItems();
    }

    private Node create(TableView<T> tableView, String tableName) {
        var vBox = new VBox();
        var tableNameLabel = createCenteredLabel(tableName);
        vBox.getChildren().add(tableNameLabel);
        vBox.getChildren().add(tableView);
        return vBox;
    }

    public void enable() {
        node.setVisible(true);
    }

    public void disable() {
        node.setVisible(false);
    }

    public Node asNode() {
        return node;
    }

    private static Label createCenteredLabel(String tableName) {
        var tableNameLabel = new Label(tableName);
        tableNameLabel.setPadding(new Insets(10));
        tableNameLabel.setFont(Font.font("Arial", 20));
        tableNameLabel.setMaxWidth(Double.MAX_VALUE);
        AnchorPane.setLeftAnchor(tableNameLabel, 0.0);
        AnchorPane.setRightAnchor(tableNameLabel, 0.0);
        tableNameLabel.setAlignment(Pos.CENTER);
        return tableNameLabel;
    }

}
