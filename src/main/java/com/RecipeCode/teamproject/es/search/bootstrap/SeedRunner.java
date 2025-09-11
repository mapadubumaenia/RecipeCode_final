package com.RecipeCode.teamproject.es.search.bootstrap;

import com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedRunner implements CommandLineRunner {
    private final ElasticsearchOperations es;

    @Override public void run(String... args) {
        var indexOps = es.indexOps(RecipeSearchDoc.class);
        if (!indexOps.exists()) {
            indexOps.create();                          // 인덱스 생성
            indexOps.putMapping(indexOps.createMapping()); // 엔티티 매핑 적용
            indexOps.refresh();
        }

        // 이미 문서가 있으면 스킵
        long count = es.count(NativeQuery.builder().withQuery(q -> q.matchAll(m -> m)).build(),
                RecipeSearchDoc.class);
        if (count > 0) return;

        Faker faker = new Faker(new Locale("ko"));
        List<String> tagPool = List.of("매운","비건","한식","면","디저트","간단","집밥","단백질","건강","후식");

        List<RecipeSearchDoc> docs = new ArrayList<>();
        for (int i=0; i<300; i++) {
            RecipeSearchDoc d = new RecipeSearchDoc();
            d.setId(UUID.randomUUID().toString());
            d.setTitle(faker.food().dish() + " 레시피");
            d.setBody(faker.lorem().paragraph(3));
            d.setTags(faker.collection(() -> tagPool.get(faker.random().nextInt(tagPool.size())))
                    .len(1, 3).generate());
            d.setAuthorId("u" + faker.number().digits(4));
            d.setAuthorNick(faker.name().firstName());
            d.setLikes(faker.number().numberBetween(0, 500));
            d.setCreatedAt(Instant.now().minusSeconds(faker.number().numberBetween(0, 60*60*24*30)));
            d.setVisibility("PUBLIC");
            docs.add(d);
        }
        es.save(docs);
        System.out.println("Seeded fake recipes: " + docs.size());
    }
}