package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.entity.extended.ExtendedMScreenStructureEntity;
import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;
import jp.co.jun.edi.repository.custom.DynamicMaintCodeRepositoryCustom;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.With;

/**
 * マスタコードRepository実装クラス.
 */
public class DynamicMaintCodeRepositoryImpl implements DynamicMaintCodeRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    /** テーブル名（m_codmst）. */
    private static final String TABLE_NAME_M_CODMST = "m_codmst";

    /** 除外カラムリスト. */
    private static final List<String> EXCLUDE_COLUMN = Arrays.asList("id", "updated_at", "updated_user_id", "created_at", "created_user_id", "deleted_at",
            "deleted_at_unix");

    /**
     * メンテナンスコード情報を登録する.
     */
    @Override
    public int insertByMaintCode(final BigInteger userId, final Map<String, String> data, final MScreenStructureEntity entity) {
        // 除外カラムリストに該当する情報を除く
        final List<ExtendedMScreenStructureEntity> excludeStructures = entity.getStructure().stream()
                .filter(structure -> !EXCLUDE_COLUMN.contains(structure.getKey())).collect(Collectors.toList());

        // SQL組み立て
        final String sql = generatedInsertQuery(entity, excludeStructures);

        // パラメータ設定
        final Date d = DateUtils.createNow();
        final Query query = entityManager.createNativeQuery(sql);
        excludeStructures.stream().forEach(With.index(0, (structure, index) -> {
            final String key = structure.getKey();
            query.setParameter(key, data.get(key));
        }));
        query.setParameter("created_at", d);
        query.setParameter("created_user_id", userId);
        query.setParameter("updated_at", d);
        query.setParameter("updated_user_id", userId);

        // クエリの実行
        return query.executeUpdate();

    }

    /**
     * メンテナンスコード情報を更新する.
     */
    @Override
    public int updateByMaintCode(final BigInteger userId, final Map<String, String> data, final MScreenStructureEntity entity) {
        // 固有カラム除外
        final List<ExtendedMScreenStructureEntity> excludeStructures = entity.getStructure().stream()
                .filter(structure -> !EXCLUDE_COLUMN.contains(structure.getKey())).collect(Collectors.toList());

        // SQL組み立て
        final String sql = generatedUpdateQuery(entity, excludeStructures);

        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql);
        excludeStructures.stream().forEach(With.index(0, (structure, index) -> {
            final String key = structure.getKey();
            query.setParameter(key, data.get(key));
        }));
        query.setParameter("id", data.get("id"));
        query.setParameter("updated_at", DateUtils.createNow());
        query.setParameter("updated_user_id", userId);
        if (TABLE_NAME_M_CODMST.equals(entity.getTableName())) {
            query.setParameter("tableId", entity.getTableId().getValue());
        }

        // クエリの実行
        return query.executeUpdate();
    }

    /**
     * メンテナンスコード情報を削除する（論理削除）.
     *
     * <pre>
     *   UPDATE テーブル
     *   SET deleted_at = 現在日時
     *   , updated_at = 現在日時
     *   , updated_user_id = ユーザID
     *   WHERE id = :id
     * </pre>
     */
    @Override
    public int deletedByMaintCode(final BigInteger userId, final Map<String, String> data, final MScreenStructureEntity entity) {
        // SQL組み立て
        final StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE ");
        sql.append(entity.getTableName());
        sql.append(" SET deleted_at = :deleted_at ");
        sql.append(" , updated_at = :updated_at ");
        sql.append(" , updated_user_id = :updated_user_id ");
        if (BooleanType.TRUE == entity.getDeletedAtUnixFlg()) {
            sql.append(" , deleted_at_unix = :deleted_at_unix ");
        }
        sql.append(" WHERE id = :id ");

        final Date d = DateUtils.createNow();
        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("id", data.get("id"));
        query.setParameter("deleted_at", d);
        query.setParameter("updated_at", d);
        query.setParameter("updated_user_id", userId);
        if (BooleanType.TRUE == entity.getDeletedAtUnixFlg()) {
            query.setParameter("deleted_at_unix", d);
        }

        return query.executeUpdate();
    }

    /**
     * INSERT句生成.
     *
     * <pre>
     *   INSERT
     *   INTO テーブル ( カラムX, カラムY, ... , created_at, created_user_id, updated_at, updated_user_id)
     *   VALUES ( :カラムX, :カラムY, ... , :created_at, :created_user_id, :updated_at, :updated_user_id);
     * </pre>
     *
     * @param entity
     *            画面構成情報
     * @param structures
     *            カラムリスト
     * @return INSERT句
     */
    private String generatedInsertQuery(final MScreenStructureEntity entity, final List<ExtendedMScreenStructureEntity> structures) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" INSERT INTO ");
        sql.append(entity.getTableName());
        sql.append(" ( ");
        // カラムリスト ( カラムX, カラムY, ... )
        structures.stream().forEach(With.index(0, (structure, index) -> {
            if (index != 0) {
                sql.append(" , ");
            }
            sql.append(structure.getKey());
        }));
        sql.append(" , created_at ");
        sql.append(" , created_user_id ");
        sql.append(" , updated_at ");
        sql.append(" , updated_user_id ");

        sql.append(" ) VALUES ( ");
        // データリスト ( :カラムX, :カラムY, ...)
        structures.stream().forEach(With.index(0, (structure, index) -> {
            if (index != 0) {
                sql.append(" , ");
            }
            sql.append(":" + structure.getKey());
        }));
        sql.append(" , :created_at ");
        sql.append(" , :created_user_id ");
        sql.append(" , :updated_at ");
        sql.append(" , :updated_user_id ");

        sql.append(" ) ");
        return sql.toString();
    }

    /**
     * UPDATE句生成.
     *
     * <pre>
     *   対象テーブルが m_codmst の場合
     *     UPDATE テーブル
     *     SET カラムX = :カラムX,  カラムY = :カラムY, ... , updated_at = :updated_at, updated_user_id = :updated_user_id
     *     WHERE id = :id
     *     AND tblid = :tableId
     *
     *   対象テーブルが m_codmst 以外
     *     UPDATE テーブル
     *     SET カラムX = :カラムX,  カラムY = :カラムY, ... , updated_at = :updated_at, updated_user_id = :updated_user_id
     *     WHERE id = :id
     * </pre>
     *
     * @param entity
     *            画面構成情報
     * @param structures
     *            カラムリスト
     * @return UPDATE句
     */
    private String generatedUpdateQuery(final MScreenStructureEntity entity, final List<ExtendedMScreenStructureEntity> structures) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE ");
        sql.append(entity.getTableName());
        sql.append(" SET ");
        structures.stream().forEach(With.index(0, (structure, index) -> {
            if (index != 0) {
                sql.append(" , ");
            }
            sql.append(structure.getKey());
            sql.append(" = :" + structure.getKey());
        }));
        sql.append(" ,updated_at = :updated_at ");
        sql.append(" ,updated_user_id = :updated_user_id ");
        //PRD_0147 #10671 JFE add start
        sql.append(" ,mntflg = 1 ");
        //PRD_0147 #10671 JFE add end
        sql.append(" WHERE id = :id");
        if (TABLE_NAME_M_CODMST.equals(entity.getTableName())) {
            sql.append(" AND tblid = :tableId ");
        }
        return sql.toString();
    }

    /**
     * メンテナンスコード情報を取得する.
     *
     * <pre>
     *   SELECT カラムX, カラムY
     *   FROM テーブル
     *   WHERE deleted_at IS NULL
     *   AND deleted_at_unix = 0
     *   AND ( ( カラムX LIKE :カラムX0 ) OR ( カラム LIKE :カラムX1 ) ... )
     *   AND ( ( カラムY LIKE :カラムY0 ) OR ( カラム LIKE :カラムY1 ) ... )
     *   ORDER BY 並び順用カラム
     * </pre>
     */
    @Override
    public PageImpl<Map<String, Object>> findByMaintCode(final MaintCodeSearchConditionModel searchCondition, final MScreenStructureEntity entity) {

        // カラム名をキーにし、値にLIKE検索用のリストデータを格納
        final Map<String, List<String>> likeValues = genaratedWhereMap(searchCondition);

        // SELECT 句 生成
        final String selectQuery = generatedSelectQuery(entity);
        // FROM 句 生成
        final String fromQuery = generatedFromQuery(entity);
        // WHERE 句 生成
        final String whereQuery = generatedWhereQuery(entity, likeValues);
        // ORDER BY 句 生成
        final String orderQuery = " ORDER BY id ";

        // SQL組み立て
        final StringBuilder sql = new StringBuilder();
        sql.append(selectQuery).append(fromQuery).append(whereQuery).append(orderQuery);

        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql.toString());
        generatedQueryParameters(query, entity, likeValues);

        query.setFirstResult(searchCondition.getPage() * searchCondition.getMaxResults());
        query.setMaxResults(searchCondition.getMaxResults());

        // クエリを実行し、MAP型の配列データに変換する
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> result = ((List<Object[]>) query.getResultList()).stream().map(values -> {
            final Map<String, Object> data = new HashMap<String, Object>();
            entity.getStructure().stream().forEach(With.index(0, (key, index) -> data.put(key.getKey(), values[index])));

            return data;
        }).collect(Collectors.toList());

        final PageRequest pageRequest = PageRequest.of(searchCondition.getPage(), searchCondition.getMaxResults());

        return new PageImpl<Map<String, Object>>(result, pageRequest, countBySpec(fromQuery, whereQuery, entity, likeValues));
    }

    /**
     * クエリパラメータ生成.
     *
     * @param query
     *            Queryオブジェクト
     * @param entity
     *            画面構成情報
     * @param likeValues
     *            Where句用データ
     */
    private void generatedQueryParameters(final Query query, final MScreenStructureEntity entity, final Map<String, List<String>> likeValues) {

        if (TABLE_NAME_M_CODMST.equals(entity.getTableName())) {
            // m_codmstテーブルの場合、条件tblidは必須
            query.setParameter("tableId", entity.getTableId().getValue());
        }

        likeValues.entrySet().stream().forEach(condition -> {
            condition.getValue().forEach(With.index(0, (likeValue, index) -> {
                final String key = condition.getKey() + "_" + index;
                final String value = "%" + likeValue + "%";
                query.setParameter(key, value);
            }));
            return;
        });
    }

    /**
     * 総件数取得.
     *
     * @param fromQuery
     *            FROM句
     * @param whereQuery
     *            WHERE句
     * @param entity
     *            画面構成情報
     * @param likeValues
     *            Where句用データ
     * @return 総件数
     */
    private int countBySpec(final String fromQuery, final String whereQuery, final MScreenStructureEntity entity, final Map<String, List<String>> likeValues) {
        final String selectQuery = " SELECT count(id) ";

        final StringBuilder sql = new StringBuilder();
        sql.append(selectQuery).append(fromQuery).append(whereQuery);

        final Query query = entityManager.createNativeQuery(sql.toString());
        generatedQueryParameters(query, entity, likeValues);

        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * WHERE句生成.
     *
     * @param entity
     *            画面構成情報
     * @param likeValues
     *            Where句用データ
     * @return WHERE句
     */
    private String generatedWhereQuery(final MScreenStructureEntity entity, final Map<String, List<String>> likeValues) {
        final StringBuilder sql = new StringBuilder();
        // WHERE句 固定
        sql.append(" WHERE deleted_at IS NULL ");
        if (TABLE_NAME_M_CODMST.equals(entity.getTableName())) {
            // m_codmst.tblid の条件は必須となるが、コードマスタテーブルを分解するときは不要
            sql.append(" AND tblid = :tableId ");
        }
        // WHERE句 動的
        final String sql3 = likeValues.entrySet().stream().map(condition -> {
            // 動的WHERE句を生成
            // sql2 = " AND ( ( カラム LIKE :カラム1 ) OR ( カラム LIKE :カラム2 ) ... ) "
            final StringBuilder sql2 = new StringBuilder();
            sql2.append(" AND ( ");
            condition.getValue().forEach(With.index(0, (likeValue, index) -> {
                if (index != 0) {
                    sql2.append(" OR ");
                }
                sql2.append(" ( ");
                sql2.append(condition.getKey());
                sql2.append(" LIKE ");
                sql2.append(":" + condition.getKey() + "_" + index);
                sql2.append(" ) ");
            }));
            sql2.append(" ) ");
            return sql2.toString();
        }).collect(Collectors.joining());

        if (!sql3.isEmpty()) {
            sql.append(sql3);
        }

        return sql.toString();
    }

    /**
     * カラム名をキーにし、値にLIKE検索用のリストデータを格納.
     *
     * @param searchCondition
     *            検索条件
     * @return カラム名をキーにし、値にLIKE検索用のリストデータを格納したデータ
     */
    private Map<String, List<String>> genaratedWhereMap(final MaintCodeSearchConditionModel searchCondition) {
        final Map<String, List<String>> likeValues = searchCondition.getConditions().entrySet().stream()
                // 半角空白、全角空白、タブを除いたうえで、値が空でないデータのみ絞り込む
                .filter(condition -> StringUtils.stripToNull(condition.getValue()) != null)
                // 値をスペース（半角空白、全角空白、タブ）で切り分け、LIKE条件値に組み立て、テーブル名をキーにMAPに変換
                .collect(Collectors.toMap(condition -> condition.getKey(), condition -> Arrays.asList(StringUtils.split(condition.getValue())),
                        (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        return likeValues;
    }

    /**
     * SELECT FROM句を生成.
     *
     * @param entity
     *            画面構成情報
     * @return SELECT FROM句
     */
    private String generatedFromQuery(final MScreenStructureEntity entity) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" FROM ");
        sql.append(entity.getTableName());
        return sql.toString();
    }

    /**
     * SELECT FROM句を生成.
     *
     * @param entity
     *            画面構成情報
     * @return SELECT FROM句
     */
    private String generatedSelectQuery(final MScreenStructureEntity entity) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(entity.getStructure().stream().map(data -> data.getKey()).collect(Collectors.joining(",")));
        return sql.toString();
    }
}
