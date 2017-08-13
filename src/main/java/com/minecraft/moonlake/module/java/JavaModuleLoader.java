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


package com.minecraft.moonlake.module.java;

import com.minecraft.moonlake.module.*;
import com.minecraft.moonlake.module.exception.InvalidModuleDescriptionException;
import com.minecraft.moonlake.module.exception.InvalidModuleException;
import com.minecraft.moonlake.validate.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class JavaModuleLoader extends ModuleLoaderBase {

    private final Map<String, Class<?>> classes;
    private final List<JavaModuleClassLoader> loaders;

    public JavaModuleLoader() {
        this.classes = new ConcurrentHashMap<>();
        this.loaders = new CopyOnWriteArrayList<>();
    }

    @Override
    public Module loadModule(File moduleFile) throws InvalidModuleException {
        Validate.notNull(moduleFile, "模块文件对象不能为 null 值.");
        if(!moduleFile.exists())
            throw new InvalidModuleException(new FileNotFoundException(moduleFile.getPath() + " 模块文件不存在."));
        ModuleDescription description = null;
        try {
            description = getDescription(moduleFile);
        } catch (InvalidModuleDescriptionException e) {
            throw new InvalidModuleException(e);
        }
        JavaModuleClassLoader loader = null;
        try {
            loader = new JavaModuleClassLoader(this, getClass().getClassLoader(), description, moduleFile);
        } catch (InvalidModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidModuleException(e);
        }
        loaders.add(loader);
        return loader.module;
    }

    @Override
    public ModuleDescription getDescription(File moduleFile) throws InvalidModuleDescriptionException {
        Validate.notNull(moduleFile, "模块文件对象不能为 null 值.");
        JarFile jarFile = null;
        InputStream inputStream = null;

        try {
            jarFile = new JarFile(moduleFile);
            JarEntry entry = jarFile.getJarEntry("module.properties");
            if(entry == null)
                throw new InvalidModuleDescriptionException(new FileNotFoundException("模块 Jar 文件没有存在 module.properties 属性文件."));
            inputStream = jarFile.getInputStream(entry);
            return new ModuleDescription(inputStream);
        } catch (IOException e) {
            throw new InvalidModuleDescriptionException(e);
        } finally {
            if(jarFile != null) try {
                jarFile.close();
            } catch (Exception e) {
            }
            if(inputStream != null) try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void enableModule(Module module) {
        if(!(module instanceof JavaModule))
            throw new IllegalArgumentException("模块对象与当前模块类加载器无关.");
        if(!module.isEnable()) {
            module.getLogger().info("启用模块中 " + module.getDescription().getFullName());
            JavaModule javaModule = (JavaModule) module;
            JavaModuleClassLoader loader = (JavaModuleClassLoader) javaModule.getClassLoader();
            if(!loaders.contains(loader))
                loaders.add(loader);
            try {
                javaModule.setEnable(true);
            } catch (Exception e) {
                handlerException(e);
            }
        }
    }

    @Override
    public void disableModule(Module module) {
        if(!(module instanceof JavaModule))
            throw new IllegalArgumentException("模块对象与当前模块类加载器无关.");
        if(module.isEnable()) {
            module.getLogger().info("关闭模块中 " + module.getDescription().getFullName());
            JavaModule javaModule = (JavaModule) module;
            ClassLoader classLoader = javaModule.getClassLoader();
            try {
                javaModule.setEnable(false);
            } catch (Exception e) {
                handlerException(e);
            }
            if(classLoader instanceof JavaModuleClassLoader) {
                JavaModuleClassLoader loader = (JavaModuleClassLoader) classLoader;
                loaders.remove(loader);
                for(String name : loader.getClasses())
                    removeClass(name);
            }
        }
    }

    Class<?> getClassByName(String name) {
        Class<?> clazz = classes.get(name);
        if(clazz != null)
            return clazz;
        for(JavaModuleClassLoader loader : loaders) try {
            clazz = loader.findClass(name, false);
        } catch (Exception e) {
            if(clazz != null)
                return clazz;
        }
        return null;
    }

    void setClass(String name, Class<?> clazz) {
        if(!classes.containsKey(name))
            classes.put(name, clazz);
    }

    private void removeClass(String name) {
        classes.remove(name);
    }
}
