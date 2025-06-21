package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventKey;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Chаt Неlpеr",description = "Помощь в чате", brand = Category.Misc)
public class DDCHTHELPER extends Module {

    public final CheckBoxSetting autoAuth = new CheckBoxSetting("Авто логин", true);
    public final ModeSetting loginMode = new ModeSetting("Пароль", "tryhackmecom1337", "tryhackmecom1337", "1t_M4kes_N0n_S3ns3", "Custom").visibleIf(() -> autoAuth.getValue());
    public final StringSetting customPassword = new StringSetting("Кастомный пароль", "123123123qwe", "Укажите текст для вашего пароля").visibleIf(() -> autoAuth.getValue() && loginMode.is("Custom"));

    public final CheckBoxSetting autoText = new CheckBoxSetting("Авто текст", false);
    public final BindSetting textBind = new BindSetting("Кнопка", -1).visibleIf(() -> autoText.getValue());
    public final ModeSetting textMode = new ModeSetting("Текст", "Корды", "Корды", "ezz", "Custom").visibleIf(() -> autoText.getValue());
    public final StringSetting customText = new StringSetting("Кастомный текст", "жди сват", "Укажите текст").visibleIf(() -> autoText.getValue() && textMode.is("Custom"));
    public final CheckBoxSetting globalChat = new CheckBoxSetting("Писать в глобал", true).visibleIf(() -> autoText.getValue());

    public final CheckBoxSetting spammer = new CheckBoxSetting("Cпаммер", false);
    public final SliderSetting spamDelay = new SliderSetting("Задержка в секундах", 5, 1, 30, 0.5f).visibleIf(() -> spammer.getValue());
    public final StringSetting spammerText = new StringSetting("Кастомный текст", "жди сват", "Укажите текст").visibleIf(() -> spammer.getValue());
    public final CheckBoxSetting emoji = new CheckBoxSetting("Эмодзи", true);
    public List<String> lastMessages = new ArrayList<>();
    public String password;
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    public DDCHTHELPER() {
        addSettings(autoAuth, loginMode, customPassword, autoText, textBind, textMode, customText, globalChat, spammer, spamDelay, spammerText, emoji);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!autoAuth.getValue() && !autoAuth.getValue() && !autoAuth.getValue() && !spammer.getValue() && !emoji.getValue()) {
            toggle();
        }

        if (loginMode.is("tryhackmecom1337")) {
            password = "tryhackmecom1337";
        } else if (loginMode.is("1t_M4kes_N0n_S3ns3")) {
            password = "1t_M4kes_N0n_S3ns3";
        } else if (loginMode.is("Custom")) {
            password = customPassword.getValue();
        }

        if (spammer.getValue()) {
            if (TimeCounterSetting.isReached(spamDelay.getValue().longValue() * 1000)) {
                mc.player.sendChatMessage(spammerText.getValue() + " " + MathSystem.random(1, 100));
                TimeCounterSetting.reset();
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (mc.player == null) return;
            if (e.getPacket() instanceof SChatPacket wrapper) {
                if (lastMessages.size() > 10) lastMessages.remove(0);
                lastMessages.add(wrapper.getChatComponent().getString());

                List<String> acceptWords = List.of("reg", "register", "Зарегистрируйтесь", "/reg Пароль ПовторитеПароль");

                List<String> loginWords = List.of("login", "/l", "Авторизуйтесь", "/login Пароль", "/login <password>");


                boolean containsWords = false;

                for (String lastMessage : lastMessages) {
                    for (String acceptWord : acceptWords) {
                        if (!containsWords) {
                            containsWords = lastMessage.contains(acceptWord);
                        }
                    }
                }

                boolean containsLoginWords = false;

                for (String lastMessage : lastMessages) {
                    for (String acceptWord : loginWords) {
                        if (!containsWords) {
                            containsLoginWords = lastMessage.contains(acceptWord);
                        }
                    }
                }

                boolean containsRegister = containsWords;

                boolean containsLogin = containsLoginWords;

                String emptyField = "Вы не указали пароль, регистрация под паролем " + TextFormatting.GREEN + "qweasdzxc";
                String success = "Ваш аккаунт был успешно зарегистрирован";
                String successLogin = "Авторизация успешно пройдена";
                if (containsLogin || containsRegister) {
                    if (password == null || password.equals("")) {
                        assert mc.player != null;
                        if (containsRegister) {
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                print(emptyField);
                                mc.player.sendChatMessage("/register " + "qweasdzxc123 qweasdzxc123");
                            }
                        }
                        if (containsLogin){
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                print(getName() + ": Я не знаю ваш пароль!");
                            }
                        }
                        lastMessages.clear();
                    } else {
                        assert mc.player != null;
                        if (containsRegister) {
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                mc.player.sendChatMessage("/register " + password + " " + password);

                            }
                        }
                        if (containsLogin){
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                mc.player.sendChatMessage("/login " + password);
                            }
                        }
                        lastMessages.clear();
                        GLFW.glfwSetClipboardString(Minecraft.getInstance().getMainWindow().getHandle(), password);
                        print("Пароль скопировам в буфер обмена!");
                    }
                }
            }
        }
    }

    @Subscribe
    public void onKeyPress(EventKey e) {
        if (e.getKey() == textBind.getValue() && autoText.getValue()) {
            String text = globalChat.getValue() ? "!" : "";
            String pos = (int) (mc.player.getPosX()) + ", " + (int) (mc.player.getPosY()) + ", " + (int) (mc.player.getPosZ());
            if (textMode.is("Корды")) {
                mc.player.sendChatMessage(text + pos);
            } if (textMode.is("ezz")) {
                mc.player.sendChatMessage(text + "ezz");
            } if (textMode.is("Custom")) {
                mc.player.sendChatMessage(text + customText.getValue());
            }
        }
    }
}
