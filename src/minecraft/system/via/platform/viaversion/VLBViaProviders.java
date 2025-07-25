/*
 * This file is part of ViaLoadingBase - https://github.com/FlorianMichael/ViaLoadingBase
 * Copyright (C) 2020-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package minecraft.system.via.platform.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import minecraft.system.via.ViaLoadingBase;
import minecraft.system.via.provider.VLBBaseVersionProvider;

public class VLBViaProviders implements ViaPlatformLoader {

    @Override
    public void load() {
        // Now, we can implement custom providers
        final ViaProviders providers = Via.getManager().getProviders();
        providers.use(VersionProvider.class, new VLBBaseVersionProvider());

        if (ViaLoadingBase.getInstance().getProviders() != null) ViaLoadingBase.getInstance().getProviders().accept(providers);
    }

    @Override
    public void unload() {
        // Nothing to do
    }
}
