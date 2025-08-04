package de.tomalbrc.filamentcosmetics.gui.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;


public class UiResourceCreator {
    private static final String ITEM_TEMPLATE = """
            {
              "parent": "|BASE|",
              "textures": {
                "layer0": "|ID|"
              }
            }
            """.replace(" ", "").replace("\n", "");
    public static final Style STYLE = Style.EMPTY.withColor(0xFFFFFF).withFont(FilamentCosmetics.id("gui"));
    private static char character = 'a';
    private static final Char2IntMap SPACES = new Char2IntOpenHashMap();
    private static final List<Tuple<PolymerModelData, String>> SIMPLE_MODEL = new ArrayList<>();
    private static final List<FontTexture> FONT_TEXTURES = new ArrayList<>();
    private static final char CHEST_SPACE0 = character++;
    private static final char CHEST_SPACE1 = character++;

    public static Function<Component, Component> background(String path) {
        var builder = new StringBuilder().append(CHEST_SPACE0);
        var c = (character++);
        builder.append(c);
        builder.append(CHEST_SPACE1);

        var texture = new FontTexture(FilamentCosmetics.id("item/" + path), 13, 256, new char[][] { new char[] {c} });

        FONT_TEXTURES.add(texture);
        return new TextBuilders(Component.literal(builder.toString()).setStyle(STYLE));
    }

    public static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        for (var texture : SIMPLE_MODEL) {
            assetWriter.accept("assets/" + texture.getA().modelPath().getNamespace() + "/models/" + texture.getA().modelPath().getPath() + ".json",
                    ITEM_TEMPLATE.replace("|ID|", texture.getA().modelPath().toString()).replace("|BASE|", texture.getB()).getBytes(StandardCharsets.UTF_8));
        }

        var fontBase = new JsonObject();
        var providers = new JsonArray();

        {
            var spaces = new JsonObject();
            spaces.addProperty("type", "space");
            var advances = new JsonObject();
            SPACES.char2IntEntrySet().stream().sorted(Comparator.comparing(Char2IntMap.Entry::getCharKey)).forEach((c) -> advances.addProperty(Character.toString(c.getCharKey()), c.getIntValue()));
            spaces.add("advances", advances);
            providers.add(spaces);
        }


        FONT_TEXTURES.forEach((entry) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", entry.path + ".png");
            bitmap.addProperty("ascent", entry.ascent);
            bitmap.addProperty("height", entry.height);
            var chars = new JsonArray();

            for (var a : entry.chars) {
                var builder = new StringBuilder();
                for (var b : a) {
                    builder.append(b);
                }
                chars.add(builder.toString());
            }

            bitmap.add("chars", chars);
            providers.add(bitmap);
        });

        fontBase.add("providers", providers);

        assetWriter.accept("assets/" + FilamentCosmetics.MOD_ID + "/font/gui.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));
    }

    private record TextBuilders(Component base) implements Function<Component, Component> {
        @Override
        public Component apply(Component text) {
            return Component.empty().append(base).append(text);
        }
    }

    public static void setup() {
        SPACES.put(CHEST_SPACE0, -8);
        SPACES.put(CHEST_SPACE1, -168);
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((b) -> UiResourceCreator.generateAssets(b::addData));
    }
    public record FontTexture(ResourceLocation path, int ascent, int height, char[][] chars) {}
}
