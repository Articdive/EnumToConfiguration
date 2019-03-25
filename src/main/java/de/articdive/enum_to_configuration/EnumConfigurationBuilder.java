/*
 * Copyright (C) 2019 Lukas Mansour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.articdive.enum_to_configuration;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lukas Mansour
 */
public class EnumConfigurationBuilder {
    private ConfigurationType type = ConfigurationType.YAML;
    private File file;
    private List<ConfigurationNode> configurationNodes;
    
    public <T extends Enum<T> & ConfigurationNode> EnumConfigurationBuilder(File file, Class<T> enumClass) {
        this.file = file;
        this.configurationNodes = new LinkedList<>(Arrays.asList(enumClass.getEnumConstants()));
    }
    
    public EnumConfigurationBuilder setType(ConfigurationType type) {
        this.type = type;
        return this;
    }
    
    public <T extends Enum<T> & ConfigurationNode> EnumConfigurationBuilder setConfigurationNodes(Class<T> enumClass) {
        this.configurationNodes = new LinkedList<>(Arrays.asList(enumClass.getEnumConstants()));
        return this;
    }
    
    public EnumConfigurationBuilder setFile(File file) {
        this.file = file;
        return this;
    }
    
    public EnumConfiguration build() {
        return new EnumConfiguration(file, type, configurationNodes);
    }
}
