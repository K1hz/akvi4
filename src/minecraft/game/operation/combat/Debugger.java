package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.transactions.EventPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;

@Defuse(name = "AC Debbuger", description = "MitroxSigma",brand = Category.Misc)
public class Debugger extends Module {
    private int ticks;
    CheckBoxSetting debug = new CheckBoxSetting("AcDetector", false);
    final ModeListSetting targets = new ModeListSetting("Что Показывать", new CheckBoxSetting[]{new CheckBoxSetting("Транзакции", true), new CheckBoxSetting("Флаги ач", true), new CheckBoxSetting("Велосити", false)});
    public Debugger() {
        addSettings(debug,targets);
    }
    @Override
    public void onDisable() {
        super.onDisable();
        this.ticks = 0;
    }
    @Subscribe
    public void OnPacket(EventPacket e) {
        if (debug.getValue()) {
            if (targets.is("Транзакции").getValue()) {
                if (e.getPacket() instanceof SConfirmTransactionPacket p) {
                    if (!debug.getValue()) {
                        print("transaction: " + p.getActionNumber());
                    }
                    if (debug.getValue() ) {
                        ticks++;
                        print("AC: " + NeetACBoost(p.getActionNumber()));
                    }
                }
                if (ticks > 300) {
                    print("Античит не найден оффаю");
                    toggle();
                }
            }
        }
    }

    public String NeetACBoost(int id) { //хз поч это посчитают нейронкой но это писалось человеком
        if (id < 0 && id > -100 && ticks < 30) {
            return "GrimAC";
        }
        if (ticks > 30 && id < -100) {
            return "Polar"; //тк он являеться сильно измененой версией GrimAC и отправляет 2 транзакции за 1 секунду как и Intave если надо допишу его сюда
        }
        if (id >= -18000 && id < -20000 && ticks < 14) {
            return "Matrix";
        }
        if (id >= 100 && id < 500) {
            return "Matrix";
        }
        if (id == 1488) {
            return "NeetAC обнаружен врубаю анхук тк это сложно обойти даже нурик не может";
        }
        if (id < -23764 && id > -10000000) {
            return "Vulcan";
        }
        return "Не удалось обнаружить античит";
    }
}