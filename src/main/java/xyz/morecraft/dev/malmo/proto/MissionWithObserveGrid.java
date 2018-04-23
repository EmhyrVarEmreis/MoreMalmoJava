package xyz.morecraft.dev.malmo.proto;

import lombok.Getter;
import xyz.morecraft.dev.malmo.util.WorldObservation;

public abstract class MissionWithObserveGrid<Record> extends Mission<Record> {

    @Getter
    private final String defaultObserveGridName;
    @Getter
    private final int defaultObserveGridRadius;
    @Getter
    private final int defaultObserveGridWidth;
    @Getter
    private final int defaultObserveGridSize;

    public MissionWithObserveGrid(String[] argv, int defaultObserveGridRadius) {
        super(argv);
        this.defaultObserveGridName = "og0";
        this.defaultObserveGridRadius = defaultObserveGridRadius;
        this.defaultObserveGridWidth = defaultObserveGridRadius * 2 + 1;
        this.defaultObserveGridSize = defaultObserveGridWidth * defaultObserveGridWidth;
    }

    public String[][][] getZeroGrid(WorldObservation worldObservation) {
        return worldObservation.getGrid(getDefaultObserveGridName(), getDefaultObserveGridWidth(), 1, getDefaultObserveGridWidth());
    }

}
