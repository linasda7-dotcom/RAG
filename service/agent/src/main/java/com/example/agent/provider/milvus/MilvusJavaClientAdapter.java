package com.example.agent.provider.milvus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.SearchReq.SearchReqBuilder;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;

public class MilvusJavaClientAdapter {

    private final MilvusClientV2 client;
    private final MilvusConnectionConfig config;
    private final Gson gson = new Gson();

    public MilvusJavaClientAdapter(MilvusConnectionConfig config) {
        this.config = config;

        ConnectConfig comConfig = ConnectConfig.builder()
                .uri(config.uri())
                .token(config.token())
                .dbName(config.databaseName())
                .build();

        this.client = new MilvusClientV2(
                comConfig);
    }

    public List<String> insert(List<MilvusInsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<JsonObject> data = new ArrayList<JsonObject>();

        for (MilvusInsertRow row : rows) {
            validateRow(row);
            data.add(toJsonObject(row));
        }

        InsertReq insertReq = InsertReq.builder()
                .databaseName(config.databaseName())
                .collectionName(config.collectionName())
                .data(data)
                .build();

        InsertResp insertRes = client.insert(insertReq);

        if (config.flushAfterInsert()) {
            client.flush(FlushReq.builder()
                    .databaseName(config.databaseName())
                    .collectionNames(List.of(config.collectionName()))
                    .build());
        }

        return insertRes
                .getPrimaryKeys()
                .stream()
                .map(String::valueOf)
                .toList();
    }

    private void validateRow(MilvusInsertRow row) {
        if (row == null) {
            throw new IllegalArgumentException("row must 不能为空");
        }

        if (row.vector() == null) {
            throw new IllegalArgumentException("row vector 不能为空");
        }

        if (row.text() == null || row.text().isBlank()) {
            throw new IllegalArgumentException("row vector 不能为空");
        }

        if (row.vector().length != config.dimension()) {
            throw new IllegalArgumentException("vector dimension mismatch 维度不一致config向量维度：" + config.dimension()
                    + "!=vector维度：" + row.vector().length);
        }
    }

    private JsonObject toJsonObject(MilvusInsertRow row) {
        JsonObject object = new JsonObject();

        writePrimaryKey(object, row);
        object.addProperty(config.contentFieldName(), row.text());

        if (config.metadataFieldName() != null && !config.metadataFieldName().isBlank()) {
            object.add(config.metadataFieldName(), gson.toJsonTree(row.metadata()));
        }

        object.add(config.vectorFieldName(), gson.toJsonTree(row.vector()));

        return object;
    }

    private void writePrimaryKey(JsonObject object, MilvusInsertRow row) {
        switch (config.primaryKeyMode()) {
            case INT64_AUTO -> {

            }
            case INT64_MANUAL -> {
                object.addProperty(config.idFieldName(), Long.parseLong(row.id()));
            }
            case VARCHAR_MANUAL -> {
                object.addProperty(config.idFieldName(), row.id());
            }

        }
    }

    public List<MilvusSearchHit> search(MilvusSearchRequest request) {
        validateSearchRequest(request);

        ArrayList<String> outputFields = new ArrayList<String>();
        outputFields.add(config.idFieldName());
        outputFields.add(config.contentFieldName());

        if (config.metadataFieldName() != null && !config.metadataFieldName().isBlank()) {
            outputFields.add(config.metadataFieldName());
        }

        SearchReqBuilder builder = SearchReq.builder()
                .databaseName(config.databaseName())
                .collectionName(config.collectionName())
                .annsField(config.vectorFieldName())
                .data(Collections.singletonList(new FloatVec(request.queryVector())))
                .limit(request.maxResults())
                .outputFields(outputFields);

        SearchResp searchResp = client.search(builder.build());

        return toSearchHits(searchResp, request.minScore());
    }

    private List<MilvusSearchHit> toSearchHits(SearchResp searchResp, double minScore) {
        List<MilvusSearchHit> hits = new ArrayList<MilvusSearchHit>();

        if (searchResp == null || searchResp.getSearchResults().isEmpty()) {
            return hits;
        }

        List<SearchResult> results = searchResp.getSearchResults()
                .get(0);

        for (SearchResult result : results) {
            double score = result.getScore();

            if (score < minScore) {
                continue;
            }

            Object id = result.getId();
            if (id == null) {
                id = result.getEntity().get(config.idFieldName());
            }

            Object text = result.getEntity().get(config.contentFieldName());

            if (text == null || id == null) {
                continue;
            }

            hits.add(new MilvusSearchHit(
                    String.valueOf(id),
                    text.toString(),
                    readMetadata(result),
                    score));
        }

        return hits;

    }

    private Map<String, String> readMetadata(SearchResult result) {
        if (config.metadataFieldName() == null || config.metadataFieldName().isBlank()) {
            return Map.of();
        }

        Object metadata = result.getEntity().get(config.metadataFieldName());

        if (metadata == null) {
            return Map.of();
        }

        if (metadata instanceof Map<?, ?> map) {
            return map.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            entry -> String.valueOf(entry.getValue())));
        }

        if (metadata instanceof JsonObject jsonObject) {
            return jsonObject.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> entry.getValue().isJsonNull()
                                    ? ""
                                    : entry.getValue().getAsString()));
        }

        return Map.of();

    }

    private void validateSearchRequest(MilvusSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }

        if (request.queryVector() == null) {
            throw new IllegalArgumentException("queryVector 不能为空");
        }

        if (request.queryVector().length != config.dimension()) {
            throw new IllegalArgumentException(
                    "查询的向量维度与集合的向量维度不一致" + config.dimension() + "!=" + request.queryVector().length);
        }
    }

    public MilvusConnectionConfig config() {
        return config;
    }

}
