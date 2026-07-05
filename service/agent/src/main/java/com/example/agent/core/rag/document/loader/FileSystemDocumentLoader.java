package com.example.agent.core.rag.document.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentParser;
import com.example.agent.core.rag.document.parser.TextDocumentParser;

public final class FileSystemDocumentLoader {
    private static final PathMatcher DEFAULT_TEXT_MATCHER = path -> {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        String name = fileName.toString()
                .toLowerCase(Locale.ROOT);
        return name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".markdown");
    };

    private FileSystemDocumentLoader() {
    }

    public static Document loadDocument(
            Path filePath,
            DocumentParser documentParser) {

        if (filePath == null) {
            throw new IllegalArgumentException("filePath 不能为空");
        }

        if (documentParser == null) {
            throw new IllegalArgumentException("documentParser 不能为空");
        }

        Path normalizedPath = filePath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(normalizedPath)) {
            throw new IllegalArgumentException("指定路径不是文件：" + normalizedPath);
        }

        try (
                InputStream inputStream = Files.newInputStream(normalizedPath);) {
            Document parsedDocument = documentParser.parse(inputStream);

            Map<String, String> metadata = new LinkedHashMap<String, String>(parsedDocument.metadata());

            metadata.put(
                    Document.FILE_NAME,
                    normalizedPath.getFileName().toString());

            Path parent = normalizedPath.getParent();

            if (parent != null) {
                metadata.put(
                        Document.ABSOLUTE_DIRECTORY_PATH,
                        parent.toString());
            }

            return new Document(parsedDocument.id(), parsedDocument.text(), Map.copyOf(metadata));

        } catch (IOException e) {
            throw new DocumentLoadingException(
                    "加载文档失败：" + normalizedPath, e);
        }
    }

    public static Document loadDocument(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "filePath 不能为空");
        }

        if (!DEFAULT_TEXT_MATCHER.matches(filePath)) {
            throw new IllegalArgumentException(
                    "默认解析器只支持 txt、md、markdown: "
                            + filePath);
        }

        return loadDocument(
                filePath,
                new TextDocumentParser());
    }

    public static Document loadDocument(String filePath, DocumentParser documentParser) {
        return loadDocument(toPath(filePath, "filePath"), documentParser);
    }

    public static Document loadDocument(String filePath) {
        return loadDocument(toPath(filePath, "filePath"));
    }

    public static List<Document> loadDocuments(
            Path directoryPath,
            DocumentParser documentParser) {
        return loadDocuments(directoryPath, path -> true, documentParser);
    }

    public static List<Document> loadDocuments(Path directoryPath) {
        return loadDocuments(
                directoryPath,
                DEFAULT_TEXT_MATCHER,
                new TextDocumentParser());
    }

    public static List<Document> loadDocuments(String directoryPath, DocumentParser documentParser) {
        return loadDocuments(toPath(directoryPath, "directoryPath"), documentParser);
    }

    public static List<Document> loadDocuments(String directory) {
        return loadDocuments(toPath(directory, "directoryPath"));
    }

    public static List<Document> loadDocuments(Path directory, PathMatcher pathMatcher) {
        return loadDocuments(
                directory,
                pathMatcher,
                new TextDocumentParser());
    }

    public static List<Document> loadDocuments(String directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocuments(
                toPath(directoryPath, "directoryPath"),
                pathMatcher,
                documentParser);
    }

    public static List<Document> loadDocuments(
            Path directoryPath,
            PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsInternal(
                directoryPath,
                pathMatcher,
                documentParser,
                false);
    }

    public static List<Document> loadDocuments(String directoryPath, PathMatcher pathMatcher) {

        return loadDocuments(toPath(directoryPath, "directoryPath"), pathMatcher);
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath,
            DocumentParser documentParser) {

        return loadDocumentsRecursively(
                directoryPath,
                path -> true,
                documentParser);
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath) {

        return loadDocumentsRecursively(
                directoryPath,
                DEFAULT_TEXT_MATCHER,
                new TextDocumentParser());
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath, PathMatcher pathMatcher) {

        return loadDocumentsRecursively(
                directoryPath,
                pathMatcher,
                new TextDocumentParser());
    }

    public static List<Document> loadDocumentsRecursively(
            Path directoryPath,
            PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsInternal(
                directoryPath,
                pathMatcher,
                documentParser,
                true);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"));
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, DocumentParser documentParser) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"), documentParser);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, PathMatcher pathMatcher) {
        return loadDocumentsRecursively(toPath(directoryPath, "directoryPath"), pathMatcher);
    }

    public static List<Document> loadDocumentsRecursively(String directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser) {

        return loadDocumentsRecursively(
                toPath(directoryPath, "directoryPath"),
                pathMatcher,
                documentParser);
    }

    private static List<Document> loadDocumentsInternal(Path directoryPath, PathMatcher pathMatcher,
            DocumentParser documentParser, boolean recursive) {

        if (directoryPath == null) {
            throw new IllegalArgumentException("directory 不能为空");
        }

        if (pathMatcher == null) {
            throw new IllegalArgumentException("pathMatcher 不能为空");
        }

        if (documentParser == null) {
            throw new IllegalArgumentException("documentParser 不能为空 ");
        }

        Path normalizePath = directoryPath.toAbsolutePath().normalize();

        if (!Files.isDirectory(normalizePath)) {
            throw new IllegalArgumentException("指定路径不是目录:" + normalizePath);
        }

        try (Stream<Path> paths = recursive
                ? Files.walk(normalizePath)
                : Files.list(normalizePath)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pathMatcher
                            .matches(
                                    normalizePath
                                            .relativize(path)))
                    .sorted(Comparator.comparing(
                            path -> normalizePath
                                    .relativize(path)
                                    .toString()))

                    .map(path -> loadDocument(
                            path,
                            documentParser))
                    .toList();

        } catch (IOException e) {
            throw new DocumentLoadingException("加载文档失败：" + normalizePath, e);
        }
    }

    private static Path toPath(String path,
            String parameterName) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(parameterName + " 不能为空");
        }

        return Path.of(path);
    }

}
