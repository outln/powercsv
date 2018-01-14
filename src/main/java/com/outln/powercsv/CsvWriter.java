package com.outln.powercsv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.function.Supplier;

public class CsvWriter implements Closeable {
    BufferedWriter writer;
    private CsvHeader header = null;

    private char separator = ',';
    private char quote = '"';
    private char newLine = '\n';

    public CsvWriter(Writer writer) {
        this.writer = new BufferedWriter(writer);
    }

    public CsvWriter(String filename) throws IOException {
        this(new FileWriter(filename));
    }

    public CsvWriter setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public CsvWriter setQuote(char separator) {
        this.quote = quote;
        return this;
    }

    public CsvWriter setLineBreak(char lineBreak) {
        this.newLine = lineBreak;
        return this;
    }

    public void setHeader(String[] header) {
        this.header = new CsvHeader(header);
        write(header);
    }

    public CsvLine beginLine() {
        return new CsvLine(separator, quote, newLine, this, header);
    }

    public void write(String[] line) {
        write(beginLine().setLine(line));
    }

    public void write(CsvLine line) {
        while (header != null && line.values.size() < header.getHeaders().length) {
            line.values.add("");
        }
        try {
            writer.write(line.toString() + newLine);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(Writer writer, Iterator<CsvLine> iterator) {
        try (CsvWriter csv = new CsvWriter(writer)) {
            if (iterator.hasNext()) {
                CsvLine line = iterator.next();
                if (csv.header == null && line.getHeader() != null){
                    csv.setHeader(line.getHeader());
                }
                csv.write(line);
            }
            iterator.forEachRemaining(csv::write);
        }
    }

    public static void write(String filename, Iterator<CsvLine> iterator) {
        try {
            write(new FileWriter(filename), iterator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(Writer writer, Supplier<String[]> headers, Iterator<CsvLine> iterator) {
        try (CsvWriter csv = new CsvWriter(writer)) {
            if (headers != null) {
                csv.setHeader(headers.get());
            }
            iterator.forEachRemaining(csv::write);
        }
    }

    public static void write(String filename, Supplier<String[]> headers, Iterator<CsvLine> iterator) {
        try {
            write(new FileWriter(filename), headers, iterator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
