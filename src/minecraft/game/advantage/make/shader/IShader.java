package minecraft.game.advantage.make.shader;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}
