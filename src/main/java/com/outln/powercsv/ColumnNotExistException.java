package com.outln.powercsv;

public class ColumnNotExistException extends RuntimeException{
    private final String column;

    public ColumnNotExistException(String column) {
        super("Column '" + column + "' not exist");
        this.column = column;
    }

    public String getColumn() {
        return column;
    }
}
