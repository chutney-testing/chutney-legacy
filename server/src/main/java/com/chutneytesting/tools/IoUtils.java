/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.tools;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;

public final class IoUtils {

    private IoUtils() {}

    public static String toString(Resource resource) {
        try(InputStreamReader isr = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)) {
            return CharStreams.toString(isr);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isHidden(final Path path, final Path rootPath) {
        boolean isHidden = path.toFile().isHidden() || path.toFile().getName().startsWith(".");
        Path p = path;
        while (!isHidden && p.getParent() != null && !p.getParent().equals(rootPath)) {
            isHidden = p.getParent().toFile().isHidden() || p.getParent().toFile().getName().startsWith(".");
            p = p.getParent();
        }
        return isHidden;
    }

    public static boolean isHidden(Path path) {
        return isHidden(path, path.getRoot());
    }

}
