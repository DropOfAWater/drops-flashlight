package drop.flashlight.client;

import drop.flashlight.client.translations.EnUSTranslationsProvider;
import drop.flashlight.client.translations.PlPLTranslationsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenEntrypoint implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(EnUSTranslationsProvider::new);
        pack.addProvider(PlPLTranslationsProvider::new);
    }
}