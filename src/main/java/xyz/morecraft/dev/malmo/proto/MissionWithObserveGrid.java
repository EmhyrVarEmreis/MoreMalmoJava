package xyz.morecraft.dev.malmo.proto;

import lombok.Getter;
import xyz.morecraft.dev.malmo.util.WorldObservation;

public abstract class MissionWithObserveGrid<Record> extends Mission<Record> {

    @Getter
    private final String defaultObserveGridName;
    @Getter
    private int defaultObserveGridRadius;
    @Getter
    private int defaultObserveGridWidth;
    @Getter
    private int defaultObserveGridSize;

    public MissionWithObserveGrid(String[] argv, int defaultObserveGridRadius) {
        super(argv);
        this.defaultObserveGridName = "og0";
        setDefaultObserveGridRadius(defaultObserveGridRadius);
    }

    public void setDefaultObserveGridRadius(int defaultObserveGridRadius) {
        if (this.isRunning()) {
            throw new UnsupportedOperationException("This method is not supported when mission is running!");
        }
        this.defaultObserveGridRadius = defaultObserveGridRadius;
        this.defaultObserveGridWidth = defaultObserveGridRadius * 2 + 1;
        this.defaultObserveGridSize = defaultObserveGridWidth * defaultObserveGridWidth;
    }

    public String[][][] getZeroGrid(WorldObservation worldObservation) {
        return worldObservation.getGrid(getDefaultObserveGridName(), getDefaultObserveGridWidth(), 1, getDefaultObserveGridWidth());
    }

}
