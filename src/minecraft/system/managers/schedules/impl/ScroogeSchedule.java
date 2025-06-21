package minecraft.system.managers.schedules.impl;

import minecraft.system.managers.schedules.Schedule;
import minecraft.system.managers.schedules.TimeType;

public class ScroogeSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Скрудж";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.FIFTEEN_HALF};
    }
}
