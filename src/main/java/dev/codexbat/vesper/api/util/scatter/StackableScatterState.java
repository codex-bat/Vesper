package dev.codexbat.vesper.api.util.scatter;

public interface StackableScatterState {
    boolean vesper$isScatterPaused();
    void vesper$setScatterPaused(boolean paused);

    boolean vesper$isFragile();
    void vesper$setFragile(boolean fragile);

    int vesper$getScatterCooldown();
    void vesper$setScatterCooldown(int ticks);
}