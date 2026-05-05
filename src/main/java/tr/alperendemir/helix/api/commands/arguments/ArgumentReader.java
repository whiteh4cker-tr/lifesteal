package tr.alperendemir.helix.api.commands.arguments;

import com.mojang.brigadier.StringReader;

import java.util.function.BiPredicate;

public class ArgumentReader {

    public static String readString(StringReader reader, BiPredicate<StringReader, Character> allowed) {
        var start = reader.getCursor();

        while (reader.canRead() && allowed.test(reader, reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(start, reader.getCursor());
    }

}
