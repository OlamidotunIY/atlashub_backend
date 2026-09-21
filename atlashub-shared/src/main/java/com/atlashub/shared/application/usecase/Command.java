package com.atlashub.shared.application.usecase;

/**
 * Marker interface for all application commands.
 * Commands mutate state and may or may not return a result.
 */
public abstract class Command<I, O> {
    /**
     * Executes the use case.
     * @param input The input command or query
     * @return The result of the use case
     */
    public abstract O execute(I command);
}
