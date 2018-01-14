package com.outln.powercsv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

public class CsvLine implements Collection<String> {
    ArrayList<String> values;
    private final StringBuilder builder = new StringBuilder(4 * 1024);
    private final char separator;
    private final char quote;
    private final char newLine;
    private boolean inQuote = false;
    private CsvHeader header = null;
    private CsvWriter writer = null;

    public CsvLine() {
        this(',', '"', '\n');
    }

    public CsvLine(char separator) {
        this(separator, '"', '\n');
    }

    public CsvLine(char separator, char quote) {
        this(separator, quote, '\n');
    }

    public CsvLine(char separator, char quote, char newLine) {
        this.separator = separator;
        this.quote = quote;
        this.newLine = newLine;
        this.values = new ArrayList<>();
    }

    CsvLine(char separator, char quote, char newLine, CsvWriter writer, CsvHeader header) {
        this.separator = separator;
        this.quote = quote;
        this.newLine = newLine;
        this.header = header;
        this.writer = writer;
        if (header != null) {
            this.values = new ArrayList<>(header.getHeaders().length);
        } else {
            this.values = new ArrayList<>();
        }
    }

    public int getColumn(String columnName) {
        if (header == null) {
            throw new NoHeaderDefinitionFoundException();
        }
        return header.getIndex(columnName);
    }

    private int getColumnIndex(String columnName) {
        int column = getColumn(columnName);
        if (column == -1) {
            throw new ColumnNotExistException(columnName);
        }
        return column;
    }

    public String[] getHeader() {
        return header.getHeaders();
    }

    public CsvLine setLine(String[] values) {
        this.values.addAll(Arrays.asList(values));
        return this;
    }

    public static <T> CsvLine fromArray(T[] arr) {
        CsvLine line = new CsvLine();
        line.values = Arrays.stream(arr).map(Object::toString).collect(Collectors.toCollection(ArrayList::new));
        return line;
    }

    public static <T> CsvLine fromCollection(Collection<T> collection) {
        CsvLine line = new CsvLine();
        line.values = collection.stream().map(Object::toString).collect(Collectors.toCollection(ArrayList::new));
        return line;
    }

    public void setLine(String line) {
        for (int i = 0; i < line.length(); i++) {
            parseValue(line.charAt(i));
        }
        completeLine();
    }

    public void setHeader(CsvHeader header) {
        this.header = header;
    }

    public boolean parseValue(char c) {
        if (inQuote) {
            if (c == quote) {
                inQuote = false;
            } else {
                builder.append(c);
            }
        } else {
            if (c == quote) {
                inQuote = true;
                if (builder.length() != 0) {
                    builder.append(quote);
                }
            } else if (c == separator) {
                completeLine();
            } else if (c == newLine) {
                completeLine();
                return true;
            } else if (c != '\r') {
                builder.append(c);
            }
        }
        return false;
    }

    void completeLine() {
        values.add(builder.toString());
        builder.setLength(0);
    }

    public void end() {
        writer.write(this);
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public CsvLine append(Object value) {
        values.add(Objects.toString(value, ""));
        return this;
    }

    public CsvLine set(String key, Object value) {
        int index = getColumnIndex(key);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid header specified");
        }
        while (values.size() <= index) {
            values.add("");
        }
        values.set(index, Objects.toString(value, ""));
        return this;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }

    @Override
    public String[] toArray() {
        return values.toArray(new String[values.size()]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return values.toArray(a);
    }

    @Override
    public boolean add(String s) {
        return values.add(s);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Item remove is not supported.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return values.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return values.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Item remove is not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Item retain is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Clear is not supported.");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(separator) != -1 || value.indexOf(newLine) != -1 || value.indexOf(quote) != -1) {
            String quoteStr = String.valueOf(quote);
            value = quoteStr + value.replaceAll(quoteStr, quoteStr + quoteStr) + quoteStr;
        }
        return value;
    }

    /**
     * Gets the {@link String} for given index
     *
     * @param index the index
     * @return the {@link String} value
     */
    public String getString(int index) {
        if (index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    /**
     * Gets the {@link String} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link String} value
     */
    public String getString(int index, String defaultValue) {
        if (index >= values.size()) {
            return defaultValue;
        }
        return values.get(index);
    }

    /**
     * Gets the {@link String} for given column name
     *
     * @param column the column name
     * @return the {@link String} value
     */
    public String getString(String column) {
        return getString(getColumnIndex(column));
    }


    /**
     * Gets the {@link String} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link String} value
     */
    public String getString(String column, String defaultValue) {
        return getString(getColumnIndex(column), defaultValue);
    }

    /**
     * Gets the {@link Integer} for given index
     *
     * @param index the index
     * @return the {@link Integer} value
     */
    public Integer getInt(int index) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? null : Integer.parseInt(value.trim());
    }

    /**
     * Gets the {@link Integer} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link Integer} value
     */
    public int getInt(int index, int defaultValue) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? defaultValue : Integer.parseInt(value.trim());
    }

    /**
     * Gets the {@link Integer} for given column name
     *
     * @param column the column name
     * @return the {@link Integer} value
     */
    public Integer getInt(String column) {
        return getInt(getColumnIndex(column));
    }

    /**
     * Gets the {@link Integer} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link Integer} value
     */
    public int getInt(String column, int defaultValue) {
        return getInt(getColumnIndex(column), defaultValue);
    }

    /**
     * Gets the {@link Long} for given index
     *
     * @param index the index
     * @return the {@link Long} value
     */
    public Long getLong(int index) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? null : Long.parseLong(value.trim());
    }

    /**
     * Gets the {@link Long} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link Long} value
     */
    public long getLong(int index, long defaultValue) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? defaultValue : Long.parseLong(value.trim());
    }

    /**
     * Gets the {@link Long} for given column name
     *
     * @param column the column name
     * @return the {@link Long} value
     */
    public Long getLong(String column) {
        return getLong(getColumnIndex(column));
    }

    /**
     * Gets the {@link Long} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link Long} value
     */
    public long getLong(String column, long defaultValue) {
        return getLong(getColumnIndex(column), defaultValue);
    }

    /**
     * Gets the {@link BigDecimal} for given index
     *
     * @param index the index
     * @return the {@link BigDecimal} value
     */
    public BigDecimal getBigDecimal(int index) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? null : new BigDecimal(value.trim());
    }

    /**
     * Gets the {@link BigDecimal} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link BigDecimal} value
     */
    public BigDecimal getBigDecimal(int index, BigDecimal defaultValue) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? defaultValue : new BigDecimal(value.trim());
    }

    /**
     * Gets the {@link BigDecimal} for given column name
     *
     * @param column the column name
     * @return the {@link BigDecimal} value
     */
    public BigDecimal getBigDecimal(String column) {
        return getBigDecimal(getColumnIndex(column));
    }

    /**
     * Gets the {@link BigDecimal} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link BigDecimal} value
     */
    public BigDecimal getBigDecimal(String column, BigDecimal defaultValue) {
        return getBigDecimal(getColumnIndex(column), defaultValue);
    }

    /**
     * Gets the {@link Double} for given index
     *
     * @param index the index
     * @return the {@link Double} value
     */
    public Double getDouble(int index) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? null : Double.parseDouble(value.trim());
    }

    /**
     * Gets the {@link Double} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link Double} value
     */
    public double getDouble(int index, double defaultValue) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? defaultValue : Double.parseDouble(value.trim());
    }

    /**
     * Gets the {@link Double} for given column name
     *
     * @param column the column name
     * @return the {@link Double} value
     */
    public Double getDouble(String column) {
        return getDouble(getColumnIndex(column));
    }

    /**
     * Gets the {@link Double} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link Double} value
     */
    public double getDouble(String column, double defaultValue) {
        return getDouble(getColumnIndex(column), defaultValue);
    }

    /**
     * Gets the {@link Boolean} for given index
     *
     * @param index the index
     * @return the {@link Boolean} value
     */
    public Boolean getBoolean(int index) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? null : Boolean.getBoolean(value.trim());
    }

    /**
     * Gets the {@link Boolean} for given index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the {@link Boolean} value
     */
    public boolean getBoolean(int index, boolean defaultValue) {
        String value = getString(index);
        return (value == null || value.trim().isEmpty()) ? defaultValue : Boolean.getBoolean(value.trim());
    }

    /**
     * Gets the {@link Boolean} for given column name
     *
     * @param column the column name
     * @return the {@link Boolean} value
     */
    public Boolean getBoolean(String column) {
        return getBoolean(getColumnIndex(column));
    }

    /**
     * Gets the {@link Boolean} for given column name
     *
     * @param column       the column name
     * @param defaultValue the default value
     * @return the {@link Boolean} value
     */
    public boolean getBoolean(String column, boolean defaultValue) {
        return getBoolean(getColumnIndex(column), defaultValue);
    }

    @Override
    public String toString() {
        return values.stream().map(this::escape).collect(Collectors.joining(String.valueOf(separator)));
    }
}
