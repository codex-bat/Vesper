package dev.codexbat.vesper.api.util.scatter;

/**
 * Small marker for anything that can be paused by ScatterLogic.
 */
public interface ScatterPauseable {
    boolean vesper$isScatterPaused();
    void vesper$setScatterPaused(boolean paused);
}