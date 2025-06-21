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

package minecraft.system.via.platform;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import minecraft.system.via.ViaLoadingBase;

import java.io.File;
import java.util.logging.Logger;

public class ViaBackwardsPlatformImpl implements ViaBackwardsPlatform {
    private final File directory;

    public ViaBackwardsPlatformImpl(final File directory) {
        this.init(new File(this.directory = directory, "00a2e9d88245ef09548222707686af7c6095edad"));
    }

    @Override
    public Logger getLogger() {
        return ViaLoadingBase.LOGGER;
    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public void disable() {}

    @Override
    public File getDataFolder() {
        return directory;
    }
}
