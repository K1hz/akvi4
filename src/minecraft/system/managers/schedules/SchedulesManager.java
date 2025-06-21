package minecraft.system.managers.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minecraft.system.managers.schedules.impl.*;
import minecraft.game.advantage.advisee.IMinecraft;

public class SchedulesManager
        implements IMinecraft {
    private final List<Schedule> schedules = new ArrayList<>();

    public SchedulesManager() {
        this.schedules.addAll(Arrays.asList(new AirDropSchedule(), new ScroogeSchedule(), new SecretMerchantSchedule(), new MascotSchedule(), new CompetitionSchedule()));
    }

    public List<Schedule> getSchedules() {
        return this.schedules;
    }
}