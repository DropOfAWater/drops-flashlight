package drop.flashlight.client.translations;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class EnUSTranslationsProvider extends FabricLanguageProvider {

    public EnUSTranslationsProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider holderLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("key.drops-flashlight.toggle_flashlight", "Toggle flashlight");
        translationBuilder.add("key.category.drops-flashlight.drops_flashlight", "Drop's Flashlight");
    }
}
