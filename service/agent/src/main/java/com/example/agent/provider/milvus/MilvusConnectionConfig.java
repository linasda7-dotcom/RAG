package com.example.agent.provider.milvus;

public class MilvusConnectionConfig {
    private final String uri;
    private final String token;
    private final String databaseName;
    private final String collectionName;
    private final String idFieldName;
    private final String contentFieldName;
    private final String vectorFieldName;
    private final int dimension;
    private final MilvusPrimaryKeyMode primaryKeyMode;
    private final boolean flushAfterInsert;
    private final String metadataFieldName;

    private MilvusConnectionConfig(Builder builder) {
        this.uri = builder.uri;
        this.token = builder.token;
        this.databaseName = builder.databaseName;
        this.collectionName = builder.collectionName;
        this.idFieldName = builder.idFieldName;
        this.contentFieldName = builder.contentFieldName;
        this.vectorFieldName = builder.vectorFieldName;
        this.dimension = builder.dimension;
        this.primaryKeyMode = builder.primaryKeyMode;
        this.flushAfterInsert = builder.flushAfterInsert;
        this.metadataFieldName = builder.metadataFieldName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uri = "http://localhost:19530";
        private String token;
        private String databaseName = "default";
        private String collectionName;
        private String idFieldName = "id";
        private String contentFieldName = "content";
        private String vectorFieldName = "vector";
        private int dimension = 1024;
        private MilvusPrimaryKeyMode primaryKeyMode = MilvusPrimaryKeyMode.INT64_AUTO;
        private String metadataFieldName = "metadata";

        public boolean flushAfterInsert = false;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;

        }

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder flushAfterInsert(boolean flushAfterInsert) {
            this.flushAfterInsert = flushAfterInsert;
            return this;
        }

        public Builder collectionName(String collectionName) {
            if (collectionName == null || collectionName.isBlank()) {
                throw new IllegalArgumentException("collectionName 不能为空");
            }
            this.collectionName = collectionName;
            return this;
        }

        public Builder idFieldName(String idFieldName) {
            this.idFieldName = idFieldName;
            return this;
        }

        public Builder contentFieldName(String contentFieldName) {
            this.contentFieldName = contentFieldName;
            return this;
        }

        public Builder vectorFieldName(String vectorFieldName) {
            this.vectorFieldName = vectorFieldName;
            return this;
        }

        public Builder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder primaryKeyMode(MilvusPrimaryKeyMode primaryKeyMode) {
            this.primaryKeyMode = primaryKeyMode;
            return this;
        }

        public Builder metadataFieldName(String metadataFieldName) {
            this.metadataFieldName = metadataFieldName;
            return this;
        }

        public MilvusConnectionConfig build() {
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException("uri 不能为空");
            }

            if (databaseName == null || databaseName.isBlank()) {
                throw new IllegalArgumentException("databaseName 不能为空");
            }

            if (collectionName == null || collectionName.isBlank()) {
                throw new IllegalArgumentException("collectionName 不能为空");
            }

            if (idFieldName == null || idFieldName.isBlank()) {
                throw new IllegalArgumentException("idFieldName 不能为空");
            }

            if (contentFieldName == null || contentFieldName.isBlank()) {
                throw new IllegalArgumentException("contentFieldName 不能为空");
            }

            if (vectorFieldName == null || vectorFieldName.isBlank()) {
                throw new IllegalArgumentException("vectorFieldName 不能为空");
            }

            if (dimension <= 0) {
                throw new IllegalArgumentException("dimension 必须大于0");
            }

            if (primaryKeyMode == null) {
                throw new IllegalArgumentException("primaryKeyMode 不能为空");
            }

            if (metadataFieldName == null || metadataFieldName.isBlank()) {
                throw new IllegalArgumentException("metadataFieldName 不能为空");
            }

            return new MilvusConnectionConfig(this);
        }

    }

    public String uri() {
        return uri;
    }

    public String token() {
        return token;
    }

    public String databaseName() {
        return databaseName;
    }

    public String collectionName() {
        return collectionName;
    }

    public String idFieldName() {
        return idFieldName;
    }

    public String contentFieldName() {
        return contentFieldName;
    }

    public String vectorFieldName() {
        return vectorFieldName;
    }

    public int dimension() {
        return dimension;
    }

    public MilvusPrimaryKeyMode primaryKeyMode() {
        return primaryKeyMode;
    }

    public boolean flushAfterInsert() {
        return flushAfterInsert;
    }

    public String metadataFieldName() {
        return metadataFieldName;
    }

}
