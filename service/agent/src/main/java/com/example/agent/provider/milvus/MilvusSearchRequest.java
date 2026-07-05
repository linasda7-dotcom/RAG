package com.example.agent.provider.milvus;

public class MilvusSearchRequest {
    private final float[] queryVector;
    private final int maxResults;
    private final double minScore;
    private final String filter;

    private MilvusSearchRequest(Builder builder) {
        this.queryVector = builder.queryVector;
        this.maxResults = builder.maxResults;
        this.minScore = builder.minScore;
        this.filter = builder.filter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private float[] queryVector;
        private int maxResults = 3;
        private double minScore = 0.7;
        private String filter;

        public Builder queryVector(float[] queryVector) {
            this.queryVector = queryVector;
            return this;
        }

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder minScore(double minScore) {
            this.minScore = minScore;
            return this;
        }

        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        public MilvusSearchRequest build() {
            if (queryVector == null) {
                throw new IllegalArgumentException("queryVector 不能为空");
            }
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults 不能为空");
            }
            if (minScore < 0 || minScore > 1) {
                throw new IllegalArgumentException("minScore 只能在0-1之间");
            }
            return new MilvusSearchRequest(this);
        }
    }

    public float[] queryVector() {
        return queryVector;
    }

    public int maxResults() {
        return maxResults;
    }

    public double minScore() {
        return minScore;
    }

    public String filter() {
        return filter;
    }

}
