package com.example.agent.core.rag.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocumentByCharacterSplitter implements DocumentSplitter {
    private final int maxSegmentSize;
    private final int overlapSize;

    public DocumentByCharacterSplitter(int maxSegmentSize) {
        this(maxSegmentSize, 0);
    }

    public DocumentByCharacterSplitter(int maxSegmentSize, int overlapSize) {

        if (maxSegmentSize <= 0) {
            throw new IllegalArgumentException(
                    "maxSegmentSize 必须大于0");
        }

        if (overlapSize < 0) {
            throw new IllegalArgumentException(
                    "overlapSize 不能小于0");
        }

        if (overlapSize >= maxSegmentSize) {
            throw new IllegalArgumentException(
                    "overlapSize 必须小于maxSegmentSize");
        }

        this.maxSegmentSize = maxSegmentSize;
        this.overlapSize = overlapSize;
    }

    @Override
    public List<TextSegment> split(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document 不能为空");
        }

        List<TextSegment> segments = new ArrayList<TextSegment>();
        String text = document.text();

        int start = 0;
        int segmentIndex = 0;

        while (start < text.length()) {
            int end = Math.min(
                    start + maxSegmentSize,
                    text.length());

            String segmentText = text.substring(start, end);

            if (!segmentText.isBlank()) {
                Map<String, String> metadata = new LinkedHashMap<>(document.metadata());

                metadata.put("document_id", document.id());
                metadata.put(
                        "segment_index",
                        String.valueOf(segmentIndex));

                TextSegment textSegment = new TextSegment(
                        null,
                        segmentText,
                        Map.copyOf(metadata));

                segments.add(textSegment);

                segmentIndex++;
            }

            if (end == text.length()) {
                break;
            }
            start = end - overlapSize;
        }
        return List.copyOf(segments);
    }

}
