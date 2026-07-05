package com.example.agent.core.rag.retriever;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.agent.core.rag.document.TextSegment;

public class RagPromptAugmentor {
    private static final Logger log = LoggerFactory.getLogger(RagPromptAugmentor.class);

    private final ContentRetriever contentRetriever;

    public RagPromptAugmentor(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    /**
     * augment content
     * 
     * @param userMessage userMessage
     * @return augment content
     */
    public String augment(String userMessage) {
        if (contentRetriever == null) {
            log.warn("RAG augment skipped because ContentRetriever is null");
            return userMessage;
        }

        List<TextSegment> segments = contentRetriever.retrieve(userMessage);

        if (segments == null || segments.isEmpty()) {
            log.info("RAG augment returned no segments for question='{}'", userMessage);
            return userMessage;
        }

        log.info("RAG augment retrieved {} segments for question='{}'", segments.size(), userMessage);
        if (log.isDebugEnabled()) {
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                log.debug("segment[{}]=id={},kb_id={},snippet={}",
                        i,
                        segment.id(),
                        segment.metadata().get("kb_id"),
                        segment.text().substring(0, Math.min(120, segment.text().length())).replaceAll("\n", " "));
            }
        }

        StringBuffer builder = new StringBuffer();

        builder.append("请基于以下资料回答用户问题。\n\n");
        builder.append("资料:\n");

        for (int i = 0; i < segments.size(); i++) {
            builder.append("[")
                    .append(i + 1)
                    .append("]")
                    .append(segments.get(i).text())
                    .append("\n");
        }

        builder.append("\n用户问题:\n");
        builder.append(userMessage);

        String augmented = builder.toString();
        log.debug("RAG augmented user message length={}, snippet={}", augmented.length(),
                augmented.substring(0, Math.min(240, augmented.length())).replaceAll("\n", " "));
        return augmented;
    }

}
