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

package de.articdive.enum_to_configuration.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Set;

/**
 * @author Lukas Mansour
 */
public final class CustomYamlFormat implements ConfigFormat<CommentedConfig> {
    static {
        FormatDetector.registerExtension("yaml", YamlFormat::defaultInstance);
        FormatDetector.registerExtension("yml", YamlFormat::defaultInstance);
    }
    
    final Yaml yaml;
    
    private CustomYamlFormat(Yaml yaml) {
        this.yaml = yaml;
    }
    
    @Override
    public ConfigWriter createWriter() {
        return new CustomYamlWriter(yaml);
    }
    
    @Override
    public ConfigParser<CommentedConfig> createParser() {
        return new CustomYamlParser(this);
    }
    
    @Override
    public CommentedConfig createConfig() {
        return CommentedConfig.of(this);
    }
    
    @Override
    public CommentedConfig createConcurrentConfig() {
        return CommentedConfig.ofConcurrent(this);
    }
    
    @Override
    public boolean supportsComments() {
        return true;
    }
    
    @Override
    public boolean supportsType(Class<?> type) {
        return type == null
                || type.isEnum()
                || type == Boolean.class
                || type == String.class
                || type == java.util.Date.class
                || type == java.sql.Date.class
                || type == java.sql.Timestamp.class
                || type == byte[].class
                || type == Object[].class
                || Number.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Config.class.isAssignableFrom(type);
    }
    
    /**
     * Creates an instance of YamlFormat, set with the specified Yaml object.
     *
     * @param yaml the Yaml object to use
     * @return a new instance of YamlFormat
     */
    public static CustomYamlFormat configuredInstance(Yaml yaml) {
        return new CustomYamlFormat(yaml);
    }
}
