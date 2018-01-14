package com.outln.powercsv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvReader implements Iterable<CsvLine>, Closeable {
    private BufferedReader reader;

    private CsvHeader header = null;

    private char separator = ',';
    private char quote = '"';
    private char newLine = '\n';

    public CsvReader(String filename) throws FileNotFoundException {
        this(new FileReader(filename));
    }

    public CsvReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public CsvReader setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public CsvReader setQuote(char separator) {
        this.quote = quote;
        return this;
    }

    public CsvReader setLineBreak(char lineBreak) {
        this.newLine = lineBreak;
        return this;
    }

    public String[] getHeader() {
        return header == null ? null : header.getHeaders();
    }

    public CsvReader skipLine() {
        try {
            int c;
            CsvLine nextLine = new CsvLine(separator, quote, newLine);
            while ((c = reader.read()) != -1) {
                if (nextLine.parseValue((char) c)) {
                    return this;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public CsvReader skipLines(int n) {
        for (int i = 0; i < n; i++) {
            skipLine();
        }
        return this;
    }

    public CsvReader readHeaders() {
        CsvLine nextLine = new CsvLine(separator, quote, newLine);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                if (nextLine.parseValue((char) c)) {
                    this.header = new CsvHeader(nextLine.toArray());
                    return this;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.header = new CsvHeader(nextLine.toArray());
        return this;
    }

    public CsvReader setHeaders(String[] header) {
        this.header = new CsvHeader(header);
        return this;
    }

    @Override
    public Iterator<CsvLine> iterator() {
        return new Iterator<CsvLine>() {
            CsvLine nextLine = null;

            @Override
            public boolean hasNext() {
                if (nextLine != null) {
                    return true;
                } else {
                    try {
                        int c = reader.read();
                        if (c == -1) {
                            return false;
                        }
                        nextLine = new CsvLine(separator, quote, newLine);
                        if (nextLine.parseValue((char) c)) {
                            return true;
                        }
                        while ((c = reader.read()) != -1) {
                            if (nextLine.parseValue((char) c)) {
                                return true;
                            }
                        }
                        nextLine.completeLine();
                        return true;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public CsvLine next() {
                if (nextLine != null || hasNext()) {
                    CsvLine line = nextLine;
                    line.setHeader(header);
                    nextLine = null;
                    return line;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public Stream<CsvLine> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator(), Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    public static Stream<CsvLine> lines(String fileName) {
        try {
            CsvReader reader = new CsvReader(fileName);
            return reader.stream().onClose(uncheckedClose(reader));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<CsvLine> lines(String fileName, boolean readHeaders) {
        try {
            CsvReader reader = new CsvReader(fileName);
            if (readHeaders) {
                reader.readHeaders();
            }
            return reader.stream().onClose(uncheckedClose(reader));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Runnable uncheckedClose(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
