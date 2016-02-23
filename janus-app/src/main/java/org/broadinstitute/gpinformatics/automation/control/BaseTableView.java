package org.broadinstitute.gpinformatics.automation.control;


import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

public class BaseTableView<T> extends TableView<T> {
    public void attachCellFactory(TableColumn column, final String property, boolean formatDecimal, boolean editable) {
        attachCellFactory(column, property, formatDecimal, editable, 100);
    }

    public void attachCellFactory(TableColumn column, final String property,
                                   boolean formatDecimal, boolean editable,
                                   double prefWidth) {
        getColumns().add(column);
        column.setPrefWidth(prefWidth);
        PropertyValueFactory propertyValueFactory = new PropertyValueFactory(property);
        column.setCellValueFactory(propertyValueFactory);
        if(editable) {
            Callback<TableColumn, TableCell> cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                            return new EditingCell();
                        }
                    };
            column.setCellFactory(cellFactory);
            column.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<T, Double>>() {
                        @Override public void handle(TableColumn.CellEditEvent<T, Double> t) {
                            T transfer = t.getTableView().getItems().get(
                                    t.getTablePosition().getRow());
                            PropertyAccessor propertyAccessor =
                                    PropertyAccessorFactory.forBeanPropertyAccess(transfer);
                            propertyAccessor.setPropertyValue(property, t.getNewValue());
                        }
                    });
        }else if(formatDecimal) {
            DecimalFormattedTableCellFactory decimalFormattedTableCellFactory =
                    new DecimalFormattedTableCellFactory();
            decimalFormattedTableCellFactory.setFormat("#0.00");
            decimalFormattedTableCellFactory.setMin(0.0);
            column.setCellFactory(decimalFormattedTableCellFactory);
        }
    }
}
