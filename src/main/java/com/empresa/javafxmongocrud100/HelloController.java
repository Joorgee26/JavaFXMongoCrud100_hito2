package com.empresa.javafxmongocrud100;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.bson.Document;
import org.bson.types.ObjectId;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TableView<Cliente> tablaDatos;
    @FXML
    private TableColumn<Cliente, String> columnaNombre;
    @FXML
    private TableColumn<Cliente, String> columnaCiudad;
    @FXML
    private TableColumn<Cliente, String> columnaFacturacion;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField ciudadField;
    @FXML
    private TextField facturacionField;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public HelloController() {
        String url = "mongodb+srv://administrador:Abc123456@cluster0.qhfs8na.mongodb.net/";
        mongoClient = MongoClients.create(url);
        database = mongoClient.getDatabase("Hito2_3T_Programacion");
        collection = database.getCollection("clientes");
    }

    @FXML
    public void initialize() {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        columnaFacturacion.setCellValueFactory(new PropertyValueFactory<>("facturacion"));
        mostrar();  // Mostrar los datos al inicio

        tablaDatos.setRowFactory(tv -> {
            TableRow<Cliente> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem("Editar");
                    MenuItem deleteItem = new MenuItem("Eliminar");

                    editItem.setOnAction(e -> editar(row.getItem()));
                    deleteItem.setOnAction(e -> eliminar(row.getItem()));

                    contextMenu.getItems().addAll(editItem, deleteItem);
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void mostrar() {
        try {
            ObservableList<Cliente> clientes = FXCollections.observableArrayList();
            for (Document doc : collection.find()) {
                Cliente cliente = new Cliente(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("ciudad"),
                        doc.getString("facturacion")
                );
                clientes.add(cliente);
            }
            tablaDatos.setItems(clientes);
        } catch (Exception e) {
            welcomeText.setText("Error al mostrar los datos: " + e.getMessage());
        }
    }

    @FXML
    protected void crear() {
        String nombre = nombreField.getText();
        String ciudad = ciudadField.getText();
        String facturacion = facturacionField.getText();

        if (nombre.isEmpty() || ciudad.isEmpty() || facturacion.isEmpty()) {
            welcomeText.setText("Todos los campos deben estar llenos");
            return;
        }

        try {
            Document nuevoCliente = new Document("nombre", nombre)
                    .append("ciudad", ciudad)
                    .append("facturacion", facturacion);
            collection.insertOne(nuevoCliente);
            mostrar();
        } catch (Exception e) {
            welcomeText.setText("Error al crear el cliente: " + e.getMessage());
        }
    }

    private void editar(Cliente cliente) {
        nombreField.setText(cliente.getNombre());
        ciudadField.setText(cliente.getCiudad());
        facturacionField.setText(cliente.getFacturacion());

        // Actualizar el cliente cuando se hace clic en el botón de actualizar
        actualizar(cliente.getId());
    }

    @FXML
    protected void actualizar() {
        // Este método solo actualiza el cliente actualmente seleccionado
        Cliente selectedItem = tablaDatos.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            actualizar(selectedItem.getId());
        } else {
            welcomeText.setText("Debe seleccionar un cliente para actualizar");
        }
    }

    private void actualizar(String id) {
        String nombre = nombreField.getText();
        String ciudad = ciudadField.getText();
        String facturacion = facturacionField.getText();

        if (nombre.isEmpty() || ciudad.isEmpty() || facturacion.isEmpty()) {
            welcomeText.setText("Todos los campos deben estar llenos");
            return;
        }

        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            Document update = new Document("$set", new Document("nombre", nombre)
                    .append("ciudad", ciudad)
                    .append("facturacion", facturacion));
            collection.updateOne(query, update);
            mostrar();
        } catch (Exception e) {
            welcomeText.setText("Error al actualizar el cliente: " + e.getMessage());
        }
    }

    @FXML
    protected void eliminar() {
        Cliente selectedItem = tablaDatos.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            eliminar(selectedItem);
        } else {
            welcomeText.setText("Debe seleccionar un cliente para eliminar");
        }
    }

    private void eliminar(Cliente cliente) {
        try {
            ObjectId id = new ObjectId(cliente.getId());
            Document query = new Document("_id", id);
            collection.deleteOne(query);
            mostrar();
        } catch (Exception e) {
            welcomeText.setText("Error al eliminar el cliente: " + e.getMessage());
        }
    }

    public static class Cliente {
        private String id;
        private String nombre;
        private String ciudad;
        private String facturacion;

        public Cliente(String id, String nombre, String ciudad, String facturacion) {
            this.id = id;
            this.nombre = nombre;
            this.ciudad = ciudad;
            this.facturacion = facturacion;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCiudad() {
            return ciudad;
        }

        public void setCiudad(String ciudad) {
            this.ciudad = ciudad;
        }

        public String getFacturacion() {
            return facturacion;
        }

        public void setFacturacion(String facturacion) {
            this.facturacion = facturacion;
        }
    }
}
