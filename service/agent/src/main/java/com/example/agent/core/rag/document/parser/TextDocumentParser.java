package com.example.agent.core.rag.document.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.example.agent.core.rag.document.Document;
import com.example.agent.core.rag.document.DocumentParseException;
import com.example.agent.core.rag.document.DocumentParser;

public final class TextDocumentParser implements DocumentParser {
    private final Charset charset;

    public TextDocumentParser() {
        this(StandardCharsets.UTF_8);
    }

    public TextDocumentParser(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset 不能为空");
        }
        this.charset = charset;
    }

    @Override
    public Document parse(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream 不能为空");
        }

        try {
            String text = new String(
                    inputStream.readAllBytes(),
                    charset);

            if (text.isBlank()) {
                throw new DocumentParseException("文档内容不能为空");
            }

            return Document.from(text);
        } catch (IOException e) {
            throw new DocumentParseException("读取文档内容失败", e);
        }

    }

}
