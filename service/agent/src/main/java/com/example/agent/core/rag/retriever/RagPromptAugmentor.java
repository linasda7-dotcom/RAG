package com.example.agent.core.rag.retriever;

import java.util.List;

import com.example.agent.core.rag.document.TextSegment;

public class RagPromptAugmentor {
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
            return userMessage;
        }

        List<TextSegment> segments = contentRetriever.retrieve(userMessage);

        if (segments == null || segments.isEmpty()) {
            return userMessage;
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

        return builder.toString();
    }

}
