/*
 * Copyright (C) 2026 A_drop_of_water
 * SPDX-License-Identifier: GPL-3.0-only
 */
package drop.flashlight.client.translations;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class PlPLTranslationsProvider extends FabricLanguageProvider {

    public PlPLTranslationsProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "pl_pl", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider holderLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("key.drops-flashlight.toggle_flashlight", "Włącz/Wyłącz latarkę");
        translationBuilder.add("key.category.drops-flashlight.drops_flashlight", "Drop's Flashlight");
    }
}