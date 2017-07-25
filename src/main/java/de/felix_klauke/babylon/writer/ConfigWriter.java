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

package de.felix_klauke.babylon.writer;

import de.felix_klauke.babylon.annotation.Comment;
import de.felix_klauke.babylon.annotation.Name;
import de.felix_klauke.babylon.annotation.Skip;
import de.felix_klauke.babylon.config.Config;
import de.felix_klauke.babylon.converter.Converter;
import de.felix_klauke.babylon.converter.ConverterManager;

import java.io.*;
import java.lang.reflect.Field;

/**
 * @author Felix 'SasukeKawaii' Klauke
 */
public class ConfigWriter {

    private static final String LINE_BREAK = "\n";
    private static final String IDENT_CHAR = "\t";
    private static final String DELIMITER = ": ";

    private final ConverterManager converterManager;
    private final StringBuilder stringBuilder;
    private byte currentIdent;

    public ConfigWriter(ConverterManager converterManager) {
        this.converterManager = converterManager;
        this.stringBuilder = new StringBuilder();
        this.currentIdent = 0;
    }

    public void write(Config config, File file) throws Exception {
        System.out.println(config + " to " + file);

        this.write(config);

        String result = this.stringBuilder.toString();

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(Config config) throws Exception {
        stringBuilder.append("{").append(LINE_BREAK);
        currentIdent++;

        for (Field field : config.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Skip.class)) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(config);

            this.insertIdent();

            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                stringBuilder.append("# ").append(comment.value());

                stringBuilder.append(LINE_BREAK);
                this.insertIdent();
            }


            String fieldName = field.getName();

            Name name = field.getAnnotation(Name.class);
            if (name != null) {
                fieldName = name.value();
            }

            stringBuilder.append(fieldName);
            stringBuilder.append(DELIMITER);

            Converter converter = this.converterManager.getConverter(value.getClass());
            stringBuilder.append(converter.toConfigValue(value));

            stringBuilder.append(LINE_BREAK);
        }

        this.currentIdent = 0;
        this.insertIdent();

        stringBuilder.append("}");
    }

    private void insertIdent() {
        for (int i = 0; i < this.currentIdent; ++i) {
            stringBuilder.append(IDENT_CHAR);
        }
    }
}
