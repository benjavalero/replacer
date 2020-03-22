package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcUpdateWriter extends JdbcBatchItemWriter<ReplacementEntity> {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcUpdateWriter(NamedParameterJdbcTemplate jdbcTemplate) {
        setJdbcTemplate(jdbcTemplate);
        setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        setSql("UPDATE replacement2 "
            + "SET article_id=:articleId, type=:type, subtype=:subtype, position=:position, last_update=:lastUpdate "
            + "WHERE id=:id");

        afterPropertiesSet();
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void write(@NotNull List<? extends ReplacementEntity> replacements) throws Exception {
        int minId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).min().orElse(0);
        int maxId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).max().orElse(Integer.MAX_VALUE);
        List<ReplacementEntity> dbReps = findByArticles(minId, maxId);
        List<ReplacementEntity> toUpdate = new ArrayList<>();
        for (int i = 0; i < dbReps.size(); i++) {
            if (i % 5 == 0) {
                dbReps.get(i).setLastUpdate(LocalDate.now().plusDays(1));
                toUpdate.add(dbReps.get(i));
            }
        }
        super.write(toUpdate);
    }

    private List<ReplacementEntity> findByArticles(int minId, int maxId) {
        String sql = "SELECT * FROM replacement2 WHERE id BETWEEN :minId AND :maxId";
        Map<String, Object> params = new HashMap<>();
        params.put("minId", minId);
        params.put("maxId", maxId);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, params);

        List<ReplacementEntity> replacements = new ArrayList<>();
        for (Map<String, Object> map : list) {
            ReplacementEntity replacement = new ReplacementEntity();
            replacement.setId(((Integer) map.get("ID")).longValue());
            replacement.setArticleId((Integer) map.get("ARTICLE_ID"));
            replacement.setType((String) map.get("TYPE"));
            replacement.setSubtype((String) map.get("SUBTYPE"));
            replacement.setPosition((Integer) map.get("POSITION"));
            replacement.setLastUpdate(((Date) map.get("LAST_UPDATE")).toLocalDate());
            replacements.add(replacement);
        }
        return replacements;
    }
}
