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


package com.minecraft.moonlake.module.test;

import com.minecraft.moonlake.module.ModuleManager;
import com.minecraft.moonlake.module.java.JavaModuleManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ModuleTest {

    public static void main(String[] args) {
    }

    private ModuleManager moduleManager;

    @Before
    public void init() {
        // 初始化模块加载器为 jar 包加载
        moduleManager = new JavaModuleManager();
    }

    @Test
    public void loadModules() {
        // 加载测试目录 modules 下的所有 jar 模块
        // 加载完毕后开启所有的模块
        moduleManager.loadModules(new File("src\\test\\resources"));
        moduleManager.enableModules();

        System.out.println("模块加载数量: " + moduleManager.getModuleSize());
    }

    @After
    public void close() {
        // 清除加载器的所有模块并关闭
        moduleManager.clearModules();
        moduleManager = null;
    }
}
