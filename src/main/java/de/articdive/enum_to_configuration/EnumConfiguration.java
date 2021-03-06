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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.hocon.HoconFormat;
import com.electronwill.nightconfig.toml.TomlFormat;
import de.articdive.enum_to_configuration.yaml.CustomYamlFormat;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lukas Mansour
 */
public class EnumConfiguration {
    private static final Logger LOGGER = Logger.getLogger(EnumConfiguration.class.getName());
    private final File saveFile;
    private final List<ConfigurationNode> configurationNodes;
    private final ConfigurationType type;
    private CommentedConfig commentedConfig;
    
    EnumConfiguration(File loadFile, File saveFile, ConfigurationType type, List<ConfigurationNode> configurationNodes) {
        this.saveFile = saveFile;
        this.configurationNodes = configurationNodes;
        this.type = type;
        if (isFileInvalid(loadFile) || isFileInvalid(saveFile)) {
            return;
        }
        FileConfig oldConfiguration = FileConfig.of(loadFile);
        if (loadFile.length() != 0) {
            oldConfiguration.load();
        }
        
        ConfigFormat<?> newConfigurationFormat;
        switch (type) {
            default:
            case YAML: {
                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setPrettyFlow(true);
                dumperOptions.setIndent(4);
                dumperOptions.setWidth(10000);
                dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                dumperOptions.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
                newConfigurationFormat = CustomYamlFormat.configuredInstance(new Yaml(dumperOptions));
                break;
            }
            case TOML: {
                newConfigurationFormat = TomlFormat.instance();
                break;
            }
            case HOCON: {
                newConfigurationFormat = HoconFormat.instance();
                break;
            }
        }
        CommentedConfig newConfiguration = CommentedConfig.wrap(new LinkedHashMap<>(), newConfigurationFormat);
        buildConfig(oldConfiguration, newConfiguration);
        
    }
    
    private void buildConfig(Config oldConfiguration, CommentedConfig newConfiguration) {
        for (ConfigurationNode configurationNode : configurationNodes) {
            if (configurationNode.getComments().length > 0) {
                if (configurationNode.getComments().length == 1) {
                    if (!configurationNode.getComments()[0].isEmpty()) {
                        if (type.equals(ConfigurationType.YAML)) {
                            newConfiguration.setComment(configurationNode.getPath(), getOneCommentString(configurationNode.getComments()));
                        } else {
                            newConfiguration.setComment(configurationNode.getPath(), String.join("\n", configurationNode.getComments()));
                        }
                    }
                } else {
                    if (type.equals(ConfigurationType.YAML)) {
                        newConfiguration.setComment(configurationNode.getPath(), getOneCommentString(configurationNode.getComments()));
                    } else {
                        newConfiguration.setComment(configurationNode.getPath(), String.join("\n", configurationNode.getComments()).replaceAll("#", ""));
                    }
                }
            }
            if (oldConfiguration.get(configurationNode.getPath()) != null) {
                if (oldConfiguration.get(configurationNode.getPath()) instanceof Config) {
                    newConfiguration.set(configurationNode.getPath(), CommentedConfig.wrap(new LinkedHashMap<>(), newConfiguration.configFormat()));
                } else {
                    newConfiguration.set(configurationNode.getPath(), oldConfiguration.get(configurationNode.getPath()));
                }
            } else {
                if (configurationNode.getDefaultValue() instanceof ConfigurationSection) {
                    newConfiguration.set(configurationNode.getPath(), CommentedConfig.wrap(new LinkedHashMap<>(), newConfiguration.configFormat()));
                } else {
                    newConfiguration.set(configurationNode.getPath(), configurationNode.getDefaultValue());
                }
            }
        }
        commentedConfig = newConfiguration;
        save();
    }
    
    private void save() {
        commentedConfig.configFormat().createWriter().write(commentedConfig.checked(), saveFile, WritingMode.REPLACE);
    }
    
    private String getOneCommentString(String[] comments) {
        StringBuilder commentString = new StringBuilder();
        int j = 0;
        for (String comment : comments) {
            if (!comment.startsWith("#")) {
                if (comment.length() > 1 && comment.trim().length() > 1) {
                    comment = "#" + comment;
                }
            }
            commentString.append(comment);
            j = j + 1;
            if (j != (comments.length)) {
                commentString.append(System.getProperty("line.separator")).append("\uE000");
            }
        }
        return commentString.toString();
    }
    
    private boolean isFileInvalid(File file) {
        if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
            LOGGER.log(Level.SEVERE, "Parent folder was a File, not a directory.");
            return true;
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.log(Level.SEVERE, "File could not be created.");
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File could not be created.");
            e.printStackTrace();
            return true;
        }
    }
    
    public <T extends Enum<T> & ConfigurationNode> void addConfigurationEnumeration(Class<T> enumClass) {
        this.configurationNodes.addAll(new LinkedList<>(Arrays.asList(enumClass.getEnumConstants())));
    }
    
    public Object get(ConfigurationNode node) {
        return get(node.getPath());
    }
    
    public Object get(String path) {
        return commentedConfig.get(path);
    }
    
    public void set(ConfigurationNode node, Object o) {
        set(node.getPath(), o);
    }
    
    public void set(String path, Object o) {
        commentedConfig.set(path, o);
        save();
    }
}
