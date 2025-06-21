package minecraft.game.advantage.figures;

import minecraft.game.advantage.advisee.IMinecraft;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GCDSensSystem implements IMinecraft {

    /**
     * Рассчитывает чувствительность мыши, используя текущие настройки и угол поворота.
     *
     * @param rot Угол поворота.
     * @return Изменение чувствительности для заданного угла.
     */
    public static float getSensitivity(float rot) {
        return getDeltaMouse(rot) * getGCDValue();
    }

    /**
     * Получает значение GCD (Greatest Common Divisor), используемое для вычислений чувствительности.
     *
     * @return Значение GCD, умноженное на коэффициент 0.15.
     */
    public static float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    /**
     * Рассчитывает значение GCD для настройки чувствительности мыши на основе текущих настроек.
     *
     * @return Рассчитанное значение GCD.
     */
    public static float getGCD() {
        float mouseSensitivity = (float) mc.gameSettings.mouseSensitivity;
        return (float) ((mouseSensitivity * 0.6 + 0.2) * (mouseSensitivity * 0.6 + 0.2) * 8);
    }

    /**
     * Рассчитывает изменение мыши на основе делты (изменения) мыши и значения GCD.
     *
     * @param delta Изменение положения мыши.
     * @return Результат округления изменения мыши.
     */
    public static float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}
