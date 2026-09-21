package com.atlashub.shared.application.usecase;

/**
 * Marker interface for all application queries.
 * Queries read state and do not mutate it.
 */
public abstract class Query<I, O> {

    /**
     * Executes the use case.
     * @param input The input command or query
     * @return The result of the use case
     */
    public abstract O execute(I input);
}
