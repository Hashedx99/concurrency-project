package com.ga.csvprocessor.interfaces;


public interface FileHandler {

    /**
     * Reads data from a file or path reference.
     *
     * @param source the file path, File object, or identifier to read from
     * @return the parsed object, or null on failure
     */
    Object readFromFile(Object source);

    /**
     * Writes content to a file identified by name and optional id.
     *
     * @param name    logical name (e.g. batch id, employee name)
     * @param id      unique identifier (e.g. UUID, timestamp)
     * @param content the content to persist
     * @return true if write succeeded, false otherwise
     */
    boolean writeToFile(String name, String id, Object content);
}
