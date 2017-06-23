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

import com.minecraft.moonlake.module.ModuleBase;
import com.minecraft.moonlake.module.ModuleDescription;
import com.minecraft.moonlake.module.ModuleLoader;
import com.minecraft.moonlake.module.ModuleLogger;
import com.minecraft.moonlake.validate.Validate;

import java.io.File;
import java.util.logging.Logger;

public abstract class JavaModule extends ModuleBase {

    private File moduleFile;
    private boolean isEnable;
    private ModuleLoader loader;
    private ModuleLogger logger;
    private ModuleDescription description;
    private ClassLoader classLoader;

    public JavaModule() {
        ClassLoader classLoader = getClass().getClassLoader();
        if(!(classLoader instanceof JavaModuleClassLoader))
            throw new IllegalStateException("模块需要模块类加载器才能加载.");
        ((JavaModuleClassLoader) classLoader).initialize(this);
    }

    protected JavaModule(JavaModuleLoader loader, ModuleDescription description, File file) {
        ClassLoader classLoader = getClass().getClassLoader();
        if(classLoader instanceof JavaModuleClassLoader)
            throw new IllegalStateException("在运行时无法使用此初始化构造函数.");
        initialize(loader, description, file, classLoader);
    }

    final void initialize(JavaModuleLoader loader, ModuleDescription description, File file, ClassLoader classLoader) {
        this.loader = loader;
        this.moduleFile = file;
        this.description = description;
        this.classLoader = classLoader;
        this.logger = new ModuleLogger(this);
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public final ModuleDescription getDescription() {
        return description;
    }

    @Override
    public final Logger getLogger() {
        return logger;
    }

    @Override
    public final ModuleLoader getLoader() {
        return loader;
    }

    @Override
    public final boolean isEnable() {
        return isEnable;
    }

    protected final File getFile() {
        return moduleFile;
    }

    protected final ClassLoader getClassLoader() {
        return classLoader;
    }

    protected final void setEnable(boolean enable) {
        if(isEnable != enable) {
            isEnable = enable;
            if(isEnable()) onEnable();
            else onDisable();
        }
    }

    public static <T extends JavaModule> T getModule(Class<T> clazz) {
        Validate.notNull(clazz, "类对象不能为 null 值.");
        if(!JavaModule.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("类没有继承 JavaModule 类.");
        ClassLoader classLoader = clazz.getClassLoader();
        if(!(classLoader instanceof JavaModuleClassLoader))
            throw new IllegalArgumentException("类不是由模块类加载器加载.");
        JavaModule module = ((JavaModuleClassLoader) classLoader).module;
        if(module == null)
            throw new IllegalStateException("不能获取模块从 '" + clazz + "' 来自静态初始化.");
        return clazz.cast(module);
    }

    public static JavaModule getProvidingModule(Class<?> clazz) {
        Validate.notNull(clazz, "类对象不能为 null 值.");
        ClassLoader classLoader = clazz.getClassLoader();
        if(!(classLoader instanceof JavaModuleClassLoader))
            throw new IllegalArgumentException("类不是由模块类加载器加载.");
        JavaModule module = ((JavaModuleClassLoader) classLoader).module;
        if(module == null)
            throw new IllegalStateException("不能获取模块从 '" + clazz + "' 来自静态初始化.");
        return module;
    }
}
