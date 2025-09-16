package com.RecipeCode.teamproject.es.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(indexName = "rc-search-logs-000001")
public class SearchLogDoc {
    @Id
    private String id = UUID.randomUUID().toString();

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant at;
    @Field(type = FieldType.Keyword) private String userId;   // 미로그인 시 "ANON"
    @Field(type = FieldType.Keyword) private String q;        // 원문 그대로 집계하려고 keyword
    @Field(type = FieldType.Keyword) private List<String> filters;
    @Field(type = FieldType.Keyword) private String sort;
    @Field(type = FieldType.Integer) private Integer page;
    @Field(type = FieldType.Integer) private Integer size;
    @Field(type = FieldType.Integer) private Integer total;
    @Field(type = FieldType.Boolean) private Boolean zeroHit;

    public SearchLogDoc() {}

    // getters/setters...
    public String getId() { return id; }
    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public List<String> getFilters() { return filters; }
    public void setFilters(List<String> filters) { this.filters = filters; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Boolean getZeroHit() { return zeroHit; }
    public void setZeroHit(Boolean zeroHit) { this.zeroHit = zeroHit; }
}
