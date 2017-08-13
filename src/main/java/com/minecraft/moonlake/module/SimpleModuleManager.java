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
import com.minecraft.moonlake.module.exception.ModuleException;
import com.minecraft.moonlake.validate.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleModuleManager extends ModuleManagerBase {

    private final List<Module> modules;
    private final ModuleLoaderFactory loaderFactory;
    private final Map<String, Module> lookupNames;

    public SimpleModuleManager(ModuleLoaderFactory factory) {
        this.loaderFactory = factory;
        this.modules = new ArrayList<>();
        this.lookupNames = new HashMap<>();
    }

    @Override
    public ModuleLoader getLoader() {
        return loaderFactory.getLoader();
    }

    @Override
    public synchronized Module getModule(String name) {
        Validate.notNull(name, "模块名字对象不能为 null 值.");
        return lookupNames.get(name.replace(" ", "_"));
    }

    @Override
    public synchronized Module[] getModules() {
        return modules.toArray(new Module[modules.size()]);
    }

    @Override
    public synchronized int getModuleSize() {
        return modules.size();
    }

    @Override
    public boolean isEnable(String name) {
        return isEnable(getModule(name));
    }

    @Override
    public boolean isEnable(Module module) {
        return module != null && modules.contains(module) && module.isEnable();
    }

    @Override
    public synchronized Module loadModule(File moduleFile) throws InvalidModuleException, InvalidModuleDescriptionException {
        Validate.notNull(moduleFile, "模块文件对象不能为 null 值.");
        ModuleLoader loader = loaderFactory.validate(moduleFile);
        if(loader == null)
            return null;
        Module module = loader.loadModule(moduleFile);
        modules.add(module);
        lookupNames.put(module.getDescription().getName(), module);
        return module;
    }

    @Override
    public Module[] loadModules(File rootDir) {
        Validate.notNull(rootDir, "模块文件目录对象不能为 null 值.");
        Validate.isTrue(rootDir.isDirectory(), "模块文件目录对象并不是目录文件.");
        File[] moduleFiles = rootDir.listFiles();
        if(moduleFiles == null || moduleFiles.length <= 0)
            return new Module[0];
        List<Module> modules = new ArrayList<>();
        for(File moduleFile : moduleFiles) {
            Module module = null;
            try {
                module = loadModule(moduleFile);
            } catch (ModuleException e) {
                handlerException(e);
            }
            if(module == null)
                continue;
            modules.add(module);
        }
        return modules.toArray(new Module[modules.size()]);
    }

    @Override
    public void enableModule(Module module) {
        if(module != null && !module.isEnable()) try {
            module.getLoader().enableModule(module);
        } catch (Exception e) {
            handlerException(e);
        }
    }

    @Override
    public void enableModules() {
        Module[] modules = getModules();
        for(int i = 0; i < modules.length; i++)
            enableModule(modules[i]);
    }

    @Override
    public void disableModule(Module module) {
        if(module != null && module.isEnable()) try {
            module.getLoader().disableModule(module);
        } catch (Exception e) {
            handlerException(e);
        }
    }

    @Override
    public void disableModules() {
        Module[] modules = getModules();
        for(int i = modules.length - 1; i >= 0; i--)
            disableModule(modules[i]);
    }

    @Override
    public void clearModules() {
        synchronized (this) {
            disableModules();
            modules.clear();
            lookupNames.clear();
        }
    }
}
