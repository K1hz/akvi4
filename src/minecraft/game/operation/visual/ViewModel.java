package minecraft.game.operation.visual;

import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


@Defuse(name = "View Model",description = "Изменяет положение рук", brand = Category.Visual)
public class ViewModel extends Module {

    public CheckBoxSetting swingAnim = new CheckBoxSetting("SwingAnim", true);
    public ModeSetting animationMode = new ModeSetting(
            "Мод",
            "Back",
            "Block",
            "Block Down",
            "Custom",
            "Reverse",
            "Smooth",
            "Smooth Down",
            "Spin",
            "Swipe Back",
            "Swing Wide",
            "Sway",
            "Wave",
            "Default"
    ).visibleIf(() -> swingAnim.getValue());
    public CheckBoxSetting spinValue = new CheckBoxSetting("Вращение с аурой", false).visibleIf(() -> animationMode.is("Spin"));
    public ModeSetting spinanimationMode = new ModeSetting(
            "Режим при вкл ауре",
            "Spin",
            "Block",
            "Block Down",
            "Reverse",
            "Smooth",
            "Smooth Down",
            "Spin",
            "Swipe Back"
    ).visibleIf(() -> spinValue.getValue() && animationMode.is("Spin"));



    public SliderSetting swingPower = new SliderSetting("Сила", 5, 1.0f, 15.0f, 0.05f).visibleIf(() -> swingAnim.getValue());
    public SliderSetting swingSpeed = new SliderSetting("Скорость", 8, 3.0f, 11, 1.0f).visibleIf(() -> swingAnim.getValue());
    public SliderSetting scale = new SliderSetting("Размер", 1, 0.5f, 2.5f, 0.05f).visibleIf(() -> swingAnim.getValue());
    public final CheckBoxSetting DDONLY = new CheckBoxSetting("Только с Аttаck-Аurа", false).visibleIf(() -> swingAnim.getValue());
    public final CheckBoxSetting PIZDEC = new CheckBoxSetting("Пиздатая Анимация", false).visibleIf(() -> swingAnim.getValue());
    final SliderSetting customtypeYP = new SliderSetting("YP", 90f, -360f, 360f, 1f).visibleIf(() -> animationMode.is("Custom"));
    final SliderSetting customtypeZP = new SliderSetting("ZP", -60F, -360f, 360f, 1f).visibleIf(() -> animationMode.is("Custom"));
    final SliderSetting customtypeXP = new SliderSetting("XP", -90f, -360f, 360f, 1f).visibleIf(() -> animationMode.is("Custom"));
    private float spinAngle = 0.0f; // Глобальная переменная для угла вращения

    public CheckBoxSetting vmeste = new CheckBoxSetting("Общее значение", false);
    public final SliderSetting right_x = new SliderSetting("RightX", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting right_y = new SliderSetting("RightY", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting right_z = new SliderSetting("RightZ", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting left_x = new SliderSetting("LeftX", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting left_y = new SliderSetting("LeftY", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting left_z = new SliderSetting("LeftZ", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> !vmeste.getValue());
    public final SliderSetting x = new SliderSetting("X", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> vmeste.getValue());
    public final SliderSetting y = new SliderSetting("Y", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> vmeste.getValue());
    public final SliderSetting z = new SliderSetting("Z", 0.0F, -2.0f, 2.0f, 0.1F).visibleIf(() -> vmeste.getValue());

    public DDATTACK DDATTACK;

    public ViewModel(DDATTACK DDATTACK) {
        this.DDATTACK = DDATTACK;
        addSettings(swingAnim, animationMode, swingPower, swingSpeed,spinValue, spinanimationMode, scale, DDONLY,customtypeYP, customtypeZP, customtypeXP, vmeste,x,y,z, right_x, right_y, right_z, left_x, left_y, left_z);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);
        float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

        if (DDONLY.getValue() && (DDATTACK.getTarget() == null)) {
            return;
        }

        switch (animationMode.getValue()) {
            case "Swipe Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.1);
                stack.rotate(Vector3f.YP.rotationDegrees(60));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                stack.rotate(Vector3f.XP.rotationDegrees(-10 - (swingPower.getValue() * 10) * anim));
                stack.rotate(Vector3f.XP.rotationDegrees(-60));
            }

            case "Block Down" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.1);
                stack.translate(0.5, -0.1, 0);
                stack.rotate(Vector3f.XP.rotationDegrees(sin2 * -45));
                stack.translate(-0.5, 0.1, 0);

                stack.translate(0.5, -0.1, 0);
                stack.rotate(Vector3f.YP.rotationDegrees(sin2 * -20));
                stack.translate(-0.5, 0.1, 0);

                stack.rotate(Vector3f.YP.rotationDegrees(50));
                stack.rotate(Vector3f.XP.rotationDegrees(-90));
                stack.rotate(Vector3f.YP.rotationDegrees(50));
            }

            case "Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
            }
            case "Custom" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(customtypeYP.getValue()));
                stack.rotate(Vector3f.ZP.rotationDegrees(customtypeZP.getValue()));
                stack.rotate(Vector3f.XP.rotationDegrees(customtypeXP.getValue() - (swingPower.getValue() * 10) * anim));
            }
            case "Reverse" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, -0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(110));
                stack.rotate(Vector3f.ZP.rotationDegrees(120));
                stack.rotate(Vector3f.XP.rotationDegrees(-130 - (swingPower.getValue() * 10) * anim));
            }
            case "Smooth Down" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0, 0, 0);
                stack.rotate(Vector3f.YP.rotationDegrees(15 * anim));

                stack.rotate(Vector3f.ZP.rotationDegrees(-60 * anim));
                stack.rotate(Vector3f.XP.rotationDegrees((-90 - (swingPower.getValue())) * anim));
            }
            case "Block" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0, 0f);
                stack.rotate(Vector3f.YP.rotationDegrees(70));
                stack.rotate(Vector3f.ZP.rotationDegrees(-20));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
            }
            case "Block12" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(50));
                stack.rotate(Vector3f.ZP.rotationDegrees(-20));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
            }
            case "Wave" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0f + MathHelper.sin(swingProgress * 2 * (float) Math.PI) * 0.2f, 0f);
                stack.rotate(Vector3f.XP.rotationDegrees(-60));
                stack.rotate(Vector3f.ZP.rotationDegrees(sin2 * 45));
            }
            case "Spin" -> {
                if (spinValue.getValue()) {
                    if (DDATTACK.getTarget() ==null) {
                        stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                        stack.translate(0, 0f, 0f);

                        spinAngle += swingSpeed.getValue() / 2  * 1.5f; // Используем swingSpeed для регулировки скорости вращения
                        if (spinAngle >= 360.0f) spinAngle -= 360.0f; // Ограничиваем угол до 360 градусов
                        stack.rotate(Vector3f.YP.rotationDegrees(0)); // Постоянное вращение вокруг оси Y
                        stack.rotate(Vector3f.XP.rotationDegrees(-spinAngle)); // Ориентация по X
                        stack.rotate(Vector3f.ZP.rotationDegrees(0));
                    }
                    else {
                        //            "Spin",
                        //            "Block Down",
                        //            "Reverse",
                        //            "Smooth",
                        //            "Smooth Down",
                        //            "Spin",
                        //            "Swipe Back"
                        if (spinanimationMode.is("Spin")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0, 0f, 0f);

                            spinAngle += swingSpeed.getValue() / 2  * 1.5f; // Используем swingSpeed для регулировки скорости вращения
                            if (spinAngle >= 360.0f) spinAngle -= 360.0f; // Ограничиваем угол до 360 градусов
                            stack.rotate(Vector3f.YP.rotationDegrees(0)); // Постоянное вращение вокруг оси Y
                            stack.rotate(Vector3f.XP.rotationDegrees(-spinAngle)); // Ориентация по X
                            stack.rotate(Vector3f.ZP.rotationDegrees(0));
                        }
                        if (spinanimationMode.is("Block Down")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0f, 0f, 0);
                            stack.translate(0.5, -0.1, 0);
                            stack.rotate(Vector3f.XP.rotationDegrees(sin2 * -45));
                            stack.translate(-0.5, 0.1, 0);

                            stack.translate(0.5, -0.1, 0);
                            stack.rotate(Vector3f.YP.rotationDegrees(sin2 * -20));
                            stack.translate(-0.5, 0.1, 0);

                            stack.rotate(Vector3f.YP.rotationDegrees(50));
                            stack.rotate(Vector3f.XP.rotationDegrees(-90));
                            stack.rotate(Vector3f.YP.rotationDegrees(50));
                        }
                        if (spinanimationMode.is("Reverse")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0f, 0f, 0);
                            stack.rotate(Vector3f.YP.rotationDegrees(110));
                            stack.rotate(Vector3f.ZP.rotationDegrees(120));
                            stack.rotate(Vector3f.XP.rotationDegrees(-130 - (swingPower.getValue() * 10) * anim));
                        }
                        if (spinanimationMode.is("Block")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0f, 0, 0f);
                            stack.rotate(Vector3f.YP.rotationDegrees(70));
                            stack.rotate(Vector3f.ZP.rotationDegrees(-20));
                            stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
                        }
                        if (spinanimationMode.is("Smooth")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            runnable.run();
                        }
                        if (spinanimationMode.is("Smooth Down")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0.0, 0, 0);
                            stack.rotate(Vector3f.YP.rotationDegrees(15 * anim));

                            stack.rotate(Vector3f.ZP.rotationDegrees(-60 * anim));
                            stack.rotate(Vector3f.XP.rotationDegrees((-90 - (swingPower.getValue())) * anim));
                        }
                        if (spinanimationMode.is("Swipe Back")) {
                            stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                            stack.translate(0f, 0f, 0);
                            stack.rotate(Vector3f.YP.rotationDegrees(60));
                            stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                            stack.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                            stack.rotate(Vector3f.XP.rotationDegrees(-10 - (swingPower.getValue() * 10) * anim));
                            stack.rotate(Vector3f.XP.rotationDegrees(-60));
                        }
                    }
                }
                else {
                    stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                    stack.translate(0, 0f, 0f);

                    spinAngle += swingSpeed.getValue() / 2  * 1.5f; // Используем swingSpeed для регулировки скорости вращения
                    if (spinAngle >= 360.0f) spinAngle -= 360.0f; // Ограничиваем угол до 360 градусов
                    stack.rotate(Vector3f.YP.rotationDegrees(0)); // Постоянное вращение вокруг оси Y
                    stack.rotate(Vector3f.XP.rotationDegrees(-spinAngle)); // Ориентация по X
                    stack.rotate(Vector3f.ZP.rotationDegrees(0));
                }
            }

            case "Swing Wide" -> {
                // Широкий размах
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.5f * MathHelper.sin(swingProgress * 2 * (float) Math.PI), - 0.1f, -0.5f);
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - anim * 45));
                stack.rotate(Vector3f.YP.rotationDegrees(sin2 * 60));
            }

            case "Sway" -> {
                // Покачивание как маятник
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                float swayAngle = MathHelper.sin(swingProgress * 2 * (float) Math.PI) * 50;
                stack.translate(0, 0f, 0f);
                stack.rotate(Vector3f.XP.rotationDegrees(0));
                stack.rotate(Vector3f.YP.rotationDegrees(swayAngle));
            }
            case "Default" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0f + MathHelper.sin(swingProgress * (float) -Math.PI) * 0.5, 0f);
                stack.rotate(Vector3f.XP.rotationDegrees(0));
                stack.rotate(Vector3f.ZP.rotationDegrees(0));
            }


            default -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                runnable.run();
            }
        }
    }

}
