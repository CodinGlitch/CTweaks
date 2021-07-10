package com.codinglitch.ctweaks.registry.capabilities;

public class DeathFear implements IDeathFear {
    private String fear = "";
    private int fearCounter = 0;
    private int maxFearCounter = 0;
    private long tickWhenTraumatized = 0;

    @Override
    public String getFear() {
        return fear;
    }

    @Override
    public void setFear(String fear) {
        this.fear = fear;
    }

    @Override
    public int getFearCounter() {
        return fearCounter;
    }

    @Override
    public void setFearCounter(int counter) {
        this.fearCounter = counter;
    }

    @Override
    public int getMaxFearCounter() {
        return maxFearCounter;
    }

    @Override
    public void setMaxFearCounter(int maxcounter) {
        this.maxFearCounter = maxcounter;
    }

    @Override
    public long getTickWhenTraumatized() {
        return tickWhenTraumatized;
    }

    @Override
    public void setTickWhenTraumatized(long tickWhenTraumatized) {
        this.tickWhenTraumatized = tickWhenTraumatized;
    }
}
