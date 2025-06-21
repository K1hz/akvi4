package minecraft.game.operation.wamost.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Defuse {
    String name();
    int key() default 0;
    String description();
    Category brand();

}
