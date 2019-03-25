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
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.Map;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * @author Lukas Mansour
 */
final class CustomYamlParser implements ConfigParser<CommentedConfig> {
    private final Yaml yaml;
    private final ConfigFormat<CommentedConfig> configFormat;
    
    CustomYamlParser(CustomYamlFormat configFormat) {
        this.yaml = configFormat.yaml;
        this.configFormat = configFormat;
    }
    
    @Override
    public ConfigFormat<CommentedConfig> getFormat() {
        return configFormat;
    }
    
    @Override
    public CommentedConfig parse(Reader reader) {
        CommentedConfig config = configFormat.createConfig();
        parse(reader, config, ParsingMode.MERGE);
        return config;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
        try {
            Map<String, Object> wrappedMap = wrap(yaml.loadAs(reader, Map.class));
            parsingMode.prepareParsing(destination);
            if (parsingMode == ParsingMode.ADD) {
                for (Map.Entry<String, Object> entry : wrappedMap.entrySet()) {
                    destination.valueMap().putIfAbsent(entry.getKey(), entry.getValue());
                }
            } else {
                destination.valueMap().putAll(wrappedMap);
            }
        } catch (Exception e) {
            throw new ParsingException("YAML parsing failed", e);
        }
    }
    
    private Map<String, Object> wrap(Map<String, Object> map) {
        return new TransformingMap<>(map, this::wrap, v -> v, v -> v);
    }
    
    @SuppressWarnings("unchecked")
    private Object wrap(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = wrap((Map) value);
            return Config.wrap(map, configFormat);
        }
        if (value == null) {
            return NULL_OBJECT;
        }
        return value;
    }
}