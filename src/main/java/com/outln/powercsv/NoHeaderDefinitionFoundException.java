package com.outln.powercsv;

public class NoHeaderDefinitionFoundException extends RuntimeException {
    public NoHeaderDefinitionFoundException() {
        super("No header definition found.");
    }
}
