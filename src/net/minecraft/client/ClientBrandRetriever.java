package net.minecraft.client;

import minecraft.game.operation.misc.CustomClientBrand;
import minecraft.system.AG;

public class ClientBrandRetriever {
    public static String getClientModName() {
        if (AG.getInst() == null) {
            return "vanilla";
        }

        // Retrieve the CustomClientBrand module and check if it's enabled
        CustomClientBrand customClientBrand = AG.getInst().getModuleManager().getCustomClientBrand();
        if (customClientBrand != null && customClientBrand.isEnabled()) {
            return customClientBrand.get();
        }
        return "vanilla";
    }
}
