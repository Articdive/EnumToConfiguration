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

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WriterOutput;
import com.electronwill.nightconfig.core.utils.FakeUnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.Yaml;

import java.io.Writer;
import java.util.Map;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * @author Lukas Mansour
 */
final class CustomYamlWriter implements ConfigWriter {
    private final Yaml yaml;
    
    CustomYamlWriter(Yaml yaml) {
        this.yaml = yaml;
    }
    
    @Override
    public void write(UnmodifiableConfig config, Writer writer) {
        UnmodifiableCommentedConfig commentedConfig;
        if (config instanceof UnmodifiableCommentedConfig) {
            commentedConfig = (UnmodifiableCommentedConfig) config;
        } else {
            commentedConfig = new FakeUnmodifiableCommentedConfig(config);
        }
        writeObject(commentedConfig, new WriterOutput(writer));
    }
    
    private void writeObject(UnmodifiableCommentedConfig config, CharacterOutput output) {
        Map<String, Object> unwrappedMap = unwrap(config);
        String data = yaml.dump(unwrappedMap);
        
        if (data.equals("{}\n")) {
            data = "";
        }
        
        String[] yamlContents = data.split(System.getProperty("line.separator"));
        // This will hold the newly formatted line
        StringBuilder newContents = new StringBuilder();
        // This holds the current path the lines are at in the config
        String currentPath = "";
        // The depth of the path. (number of words separated by periods - 1)
        int depth = 0;
        // Loop through the config lines
        for (String line : yamlContents) {
            // This flags if the line is a node or unknown text.
            boolean node = false;
            // If the line is a node (and not something like a list value)
            if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
                // This is a node so flag it as one
                node = true;
                // Grab the index of the end of the node name
                int index;
                index = line.indexOf(": ");
                if (index < 0) {
                    index = line.length() - 1;
                }
                // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                if (currentPath.isEmpty()) {
                    currentPath = line.substring(0, index);
                } else {
                    // Calculate the whitespace preceding the node name
                    int whiteSpace = 0;
                    for (int n = 0; n < line.length(); n++) {
                        if (line.charAt(n) == ' ') {
                            whiteSpace++;
                        } else {
                            break;
                        }
                    }
                    if (whiteSpace / 4 > depth) {
                        currentPath = currentPath + ('.' + line.substring(whiteSpace, index));
                        depth++;
                    } else if (whiteSpace / 4 < depth) {
                        int newDepth = whiteSpace / 4;
                        for (int i = 0; i < depth - newDepth; i++) {
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                        }
                        int lastIndex = currentPath.lastIndexOf('.');
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = "";
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath = currentPath + ".";
                        }
                        // Add the new node name to the path
                        currentPath = currentPath + line.substring(whiteSpace, index);
                        // Reset the depth
                        depth = newDepth;
                    } else {
                        // Path is same depth, replace the last path node name to the current node name
                        int lastIndex = currentPath.lastIndexOf(".");
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = "";
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath = currentPath.replaceAll(currentPath.substring(currentPath.lastIndexOf(".")) + "$", "");
                            currentPath = currentPath + ".";
                        }
                        //currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                        currentPath = currentPath + line.substring(whiteSpace, index);
                    }
                }
            }
            if (node && config.getComment(currentPath) != null) {
                // Add the comment to the beginning of the current line, replace custom Unicode character with a new line
                line = new String(new char[depth]).replace("\0", "    ") + config.getComment(currentPath).replaceAll("\\uE000", new String(new char[depth]).replace("\0", "    ")) + System.getProperty("line.separator") + line;
            }
            newContents.append(line).append(System.getProperty("line.separator"));
            
        }
        output.write(newContents.toString());
    }
    
    private static Map<String, Object> unwrap(UnmodifiableConfig config) {
        return new TransformingMap<>(config.valueMap(), CustomYamlWriter::unwrap, v -> v, v -> v);
    }
    
    private static Object unwrap(Object value) {
        if (value instanceof UnmodifiableConfig) {
            return unwrap((UnmodifiableConfig) value);
        }
        if (value == NULL_OBJECT) {
            return null;
        }
        return value;
    }
}