package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * Format decimals in a table view and show different styles if
 * falls under min or over max
 */
public class DecimalFormattedTableCellFactory<S, T>
        implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    private Format decimalFormat;

    private String format;
    private Double min;
    private Double max;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
        this.decimalFormat = new DecimalFormat(format);
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableCell<S, T> call(TableColumn<S, T> p) {
        TableCell<S, T> cell = new TableCell<S, T>() {
            @Override
            public void updateItem(Object item, boolean empty) {
                // CSS Styles
                String minNumber = "negativeNumberStyle";
                String maxNumber = "overMaxNumberStyle";
                String defaultTableStyle = "defaultTableStyle";
                String cssStyle;

                //Remove all previously assigned CSS styles from the cell.
                getStyleClass().remove(minNumber);
                getStyleClass().remove(defaultTableStyle);

                if (item == getItem()) {
                    return;
                }
                super.updateItem((T) item, empty);
                cssStyle = defaultTableStyle;
                if (item == null) {
                    super.setText(null);
                    super.setGraphic(null);
                } else if (format != null) {
                    if(item instanceof Double) {
                        Double value = (Double) item;
                        super.setText(decimalFormat.format(item));
                        if(min != null){
                            if(value < min) {
                                cssStyle = minNumber;
                            }
                        } else if(max != null) {
                            if(value > max) {
                                cssStyle = maxNumber;
                            }
                        } else {
                            cssStyle = defaultTableStyle;
                        }
                    }
                }

                //Set the CSS style on the cell and set the cell's text.
                getStyleClass().add(cssStyle);
            }
        };

        return cell;
    }
}
