package minecraft.game.operation.wamost.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    Combat('G'),
    Movement('B'),
    Visual('C'),
    Player('D'),
    Misc('E');


    private final char iconChar;
}