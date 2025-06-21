package minecraft.game.advantage.figures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Vector4i {

    public int x,y,z,w;
    public static Vector4i copy(int value) {
        return new Vector4i(value, value, value, value);
    }
}
