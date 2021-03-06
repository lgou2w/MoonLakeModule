/*
 * Copyright (C) 2017 The MoonLake Authors
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


package com.minecraft.moonlake.module;

import com.minecraft.moonlake.module.exception.InvalidModuleDescriptionException;
import com.minecraft.moonlake.module.exception.InvalidModuleException;

import java.io.File;

public interface ModuleManager {

    ModuleLoader getLoader();

    Module getModule(String name);

    Module[] getModules();

    int getModuleSize();

    boolean isEnable(String name);

    boolean isEnable(Module module);

    Module loadModule(File moduleFile) throws InvalidModuleException, InvalidModuleDescriptionException;

    Module[] loadModules(File rootDir);

    void enableModule(Module module);

    void enableModules();

    void disableModule(Module module);

    void disableModules();

    void clearModules();
}
