/*
 * Copyright (c) 2017 Felix Klauke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.felix_klauke.babylon.parser;

import de.felix_klauke.babylon.annotation.Name;
import de.felix_klauke.babylon.annotation.Skip;
import de.felix_klauke.babylon.config.Config;
import de.felix_klauke.babylon.converter.Converter;
import de.felix_klauke.babylon.converter.ConverterManager;
import de.felix_klauke.babylon.node.Node;
import de.felix_klauke.babylon.node.NodeContainer;
import de.felix_klauke.babylon.node.NodeValue;
import de.felix_klauke.babylon.tokenizer.BabylonTokenizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Felix 'SasukeKawaii' Klauke
 */
public class ConfigParser {

    private static final char BEGIN_NODE = '{';
    private static final char END_NODE = '}';
    private static final char NODE_DELIMITER = ':';

    private final ConverterManager converterManager;
    private StreamTokenizer tokenizer;
    private int current;

    public ConfigParser(ConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    public void parse(Config config, File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            this.tokenizer = new BabylonTokenizer(reader);
            this.read(config);
        }
    }

    private void read(Config config) throws IOException, IllegalAccessException {
        this.next();

        NodeContainer nodeContainer = readContainer(null);

        for (Field field : config.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Skip.class)) {
                continue;
            }

            String fieldName = field.getName();

            Name name = field.getAnnotation(Name.class);
            if (name != null) {
                fieldName = name.value();
            }

            Node currentNode = nodeContainer.getNodes().get(fieldName);

            if (currentNode instanceof NodeValue) {
                Converter converter = this.converterManager.getConverter(field.getType());
                Object value = converter.fromConfigValue(((NodeValue) currentNode).getValue());

                field.setAccessible(true);
                field.set(config, value);
            }
        }
    }

    private NodeContainer readContainer(String ident) throws IOException {
        check(BEGIN_NODE);

        Map<String, Node> output = new HashMap<>();

        while (current != END_NODE) {

            String name = parseValIdent();
            this.check(NODE_DELIMITER);

            Node node = readNode(name);
            output.put(name, node);
        }

        check(END_NODE);
        return new NodeContainer(ident, output);
    }

    private Node readNode(String ident) throws IOException {
        return new NodeValue(ident, parseValIdent());
    }

    private int next() throws IOException {
        return (current = tokenizer.nextToken());
    }

    private int check(int charac) throws IOException {
        if (current == charac) next();
        return current;
    }

    private String parseValIdent() throws IOException {
        String ident = tokenizer.sval;
        next();
        return ident;
    }
}
