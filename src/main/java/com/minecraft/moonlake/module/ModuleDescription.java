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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class ModuleDescription {

    private String name;
    private String main;
    private String version;
    private List<String> authors;

    public ModuleDescription(InputStream inputStream) throws InvalidModuleDescriptionException {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            this.name = properties.getProperty("name");
            this.main = properties.getProperty("main");
            this.version = properties.getProperty("version", "1.0.0");
            {
                String property = properties.getProperty("author", null);
                if(property != null && !property.trim().isEmpty())
                    this.authors = Arrays.asList(property.split(","));
            }
        } catch (Exception e) {
            throw new InvalidModuleDescriptionException(e);
        }
    }

    public ModuleDescription(String name, String main, String version) {
        this.name = name.replace(" ", "_");
        this.main = main;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getMain() {
        return main;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getFullName() {
        return String.format("%s v%s", name, version);
    }
}
