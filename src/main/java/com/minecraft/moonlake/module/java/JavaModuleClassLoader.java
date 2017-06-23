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

import com.minecraft.moonlake.module.ModuleDescription;
import com.minecraft.moonlake.module.exception.InvalidModuleException;
import com.minecraft.moonlake.validate.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class JavaModuleClassLoader extends URLClassLoader {

    private final JavaModuleLoader loader;
    private final Map<String, Class<?>> classes;
    private final ModuleDescription description;
    private final File moduleFile;
    private JavaModule moduleInitialize;

    final JavaModule module; // instance

    public JavaModuleClassLoader(JavaModuleLoader loader, ClassLoader parent, ModuleDescription description, File moduleFile) throws InvalidModuleException, MalformedURLException {
        super(new URL[] { moduleFile.toURI().toURL() }, parent);
        Validate.notNull(loader, "模块加载器不能为 null 值.");
        this.loader = loader;
        this.description = description;
        this.moduleFile = moduleFile;
        this.classes = new ConcurrentHashMap<>();

        try {
            Class<?> mainClass = null;
            Class<? extends JavaModule> moduleClass = null;
            try {
                mainClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException e) {
                throw new InvalidModuleException("无法查找到模块主类 '" + description.getMain() + "'", e);
            }
            try {
                moduleClass = mainClass.asSubclass(JavaModule.class);
            } catch (ClassCastException e) {
                throw new InvalidModuleException("模块的主类 '" + description.getMain() + "' 没有继承 JavaModule 类.", e);
            }
            this.module = moduleClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new InvalidModuleException("模块中的构造函数不是 public 修饰.", e);
        } catch (InstantiationException e) {
            throw new InvalidModuleException("不正常的模块类型.", e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(String name, boolean global) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if(result == null) {
            if(global)
                result = loader.getClassByName(name);
            if(result == null) {
                result = super.findClass(name);
                if(result != null)
                    loader.setClass(name, result);
            }
            classes.put(name, result);
        }
        return result;
    }

    Set<String> getClasses() {
        return classes.keySet();
    }

    synchronized void initialize(JavaModule module) {
        Validate.notNull(module, "初始化的模块对象不能为 null 值.");
        Validate.isTrue(module.getClass().getClassLoader() == this, "无法初始化此模块, 因为它不是当前模块类加载器对象.");
        if(this.module != null || moduleInitialize != null)
            throw new IllegalArgumentException("模块已经被初始化.");
        this.moduleInitialize = module;
        module.initialize(loader, description, moduleFile, this);
    }
}
