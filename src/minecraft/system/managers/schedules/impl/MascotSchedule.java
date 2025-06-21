package minecraft.system.managers.schedules.impl;

import minecraft.system.managers.schedules.Schedule;
import minecraft.system.managers.schedules.TimeType;

public class MascotSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Талисман";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINETEEN_HALF};
    }
}
