//PRD_0137 #10669 add start
package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.text.SimpleDateFormat;

import jp.co.jun.edi.repository.MSizmstRepository;
import jp.co.jun.edi.repository.custom.DynamicMaintSizeRepositoryCustom;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.StringUtils;

/**
 * マスタコードRepository実装クラス.
 */
public class DynamicMaintSizeRepositoryImpl implements DynamicMaintSizeRepositoryCustom {
	//PRD_0205 add JFE start
	/** 6桁. */
    private static final int DIGIT_6 = 6;
    //PRD_0205 add JFE end
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private MSizmstRepository mSizmstRepository;

    /**
     * メンテナンスコード情報を登録する.
     */
    @Override
    public int insertByMaintCode(final BigInteger userId, final Map<String, String> data,String hscd) {


        // SQL組み立て
        final String sql = generatedInsertQuery();

        // パラメータ設定
        final Date d = DateUtils.createNow();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        final String crtymd = df.format(d);
        final Query query = entityManager.createNativeQuery(sql);
        final String szkg = data.get("szkg");
        final String jun = data.get("jun");
        //PRD_0205 add JFE start
        final String tanto = StringUtils.toStringPadding0(userId, DIGIT_6);
        //PRD_0205 add JFE end

        query.setParameter("hscd", hscd);
        query.setParameter("szkg", szkg);
        query.setParameter("jun", jun);
        query.setParameter("crtymd", crtymd);
        query.setParameter("updymd", crtymd);
        //PRD_0205 mod JFE start
        //query.setParameter("tanto", userId);
        query.setParameter("tanto", tanto);
        //PRD_0205 mod JFE end
        query.setParameter("created_at", d);
        query.setParameter("created_user_id", userId);
        query.setParameter("updated_at", d);
        query.setParameter("updated_user_id", userId);

        // クエリの実行
        return query.executeUpdate();

    }

    /**
     * メンテナンスコード情報を検索する.
     */
    @Override
    public int searchByMaintCode(final BigInteger userId, final Map<String, String> data,String hscd) {

        // SQL組み立て
        final String sql = generatedSearchQuery();

        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql);


        query.setParameter("hscd", hscd);
        query.setParameter("szkg", data.get("szkg"));
        // クエリの実行
        List<?> list = query.getResultList();
        int count = list.size();

         return count;
    }

    /**
     * 品種に1件でも登録があるか検索する.
     */
    @Override
    public int searchHscdMaintCode(final BigInteger userId,String hscd) {

        // SQL組み立て
        final String sql = generatedHscdSearchQuery();
        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql);
        query.setParameter("hscd", hscd);
        // クエリの実行
        List<?> list = query.getResultList();
        int count = list.size();
         return count;
    }


    //検索情報作成
    private String generatedSearchQuery() {
        final StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" *");
        sql.append(" FROM");
        sql.append(" m_sizmst ");
        sql.append(" WHERE ");
        sql.append(" hscd = :hscd ");
        sql.append(" AND szkg = :szkg ");
        return sql.toString();
    }


    //検索情報作成
    private String generatedHscdSearchQuery() {
        final StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" *");
        sql.append(" FROM");
        sql.append(" m_sizmst ");
        sql.append(" WHERE ");
        sql.append(" hscd = :hscd ");
        sql.append(" AND deleted_at IS NULL");
        return sql.toString();
    }



    /**
     * メンテナンスコード情報を更新する.
     */
    @Override
    public int updateByMaintCode(final BigInteger userId, final Map<String, String> data,String hscd) {

        // SQL組み立て
        final String sql = generatedUpdateQuery();

        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql);

        final Date d = DateUtils.createNow();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        final String updymd = df.format(d);
        //PRD_0205 add JFE start
        final String tanto = StringUtils.toStringPadding0(userId, DIGIT_6);
        //PRD_0205 add JFE end

        query.setParameter("id", data.get("id"));
        query.setParameter("szkg", data.get("szkg"));
        query.setParameter("jun", data.get("jun"));
        //PRD_0154 #10669 add start
        query.setParameter("mntflg", data.get("mntflg"));
        //PRD_0154 #10669 add end
        query.setParameter("updymd", updymd);
        //PRD_0205 mod JFE start
        //query.setParameter("tanto", userId);
        query.setParameter("tanto", tanto);
        //PRD_0205 mod JFE end
        query.setParameter("souymd", "0");
        query.setParameter("updated_at", DateUtils.createNow());
        query.setParameter("updated_user_id", userId);

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
    public int deletedByMaintCode(final BigInteger userId, final Map<String, String> data) {
        // SQL組み立て
        final StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE ");
        sql.append(" m_sizmst");
        sql.append(" SET deleted_at = :deleted_at ");
        sql.append(" , mntflg = 3 ");
        sql.append(" , souflg = 1");
        sql.append(" , updated_at = :updated_at ");
        sql.append(" , updated_user_id = :updated_user_id ");

        sql.append(" WHERE id = :id ");

        final Date d = DateUtils.createNow();
        // パラメータ設定
        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("id", data.get("id"));
        query.setParameter("deleted_at", d);
        query.setParameter("updated_at", d);
        query.setParameter("updated_user_id", userId);

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
    private String generatedInsertQuery() {
        final StringBuilder sql = new StringBuilder();
        sql.append(" INSERT INTO ");
        sql.append(" m_sizmst	 ");
        sql.append(" ( ");
        sql.append(" hscd ");
        sql.append(" , szkg ");
        sql.append(" , jun  ");
        sql.append(" , mntflg ");
        sql.append(" , crtymd ");
        sql.append(" , updymd ");
        sql.append(" , tanto ");
        sql.append(" , souflg ");
        sql.append(" , souymd");
        sql.append(" , created_at ");
        sql.append(" , created_user_id ");
        sql.append(" , updated_at ");
        sql.append(" , updated_user_id ");

        sql.append(" ) VALUES ( ");
        sql.append(" :hscd ");
        sql.append(" , :szkg ");
        sql.append(" , :jun  ");
        sql.append(" , 1 "); //メンテ区分は固定
        sql.append(" , :crtymd ");
        sql.append(" , :updymd ");
        sql.append(" , :tanto ");
        sql.append(" , 1 "); //送信区分は固定
        sql.append(" , 00000000 "); //送信日付は固定
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
    private String generatedUpdateQuery() {
        final StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE ");
        sql.append(" m_sizmst");
        sql.append(" SET ");
        sql.append(" szkg = :szkg ");
        sql.append(" ,jun = :jun ");
        //PRD_0154 #10699 mod start
        sql.append(" ,mntflg = :mntflg ");
        //PRD_0154 #10669 mod end
        sql.append(" ,updymd = :updymd ");
        sql.append(" ,tanto = :tanto ");
        sql.append(" ,souflg = 1 ");
        sql.append(" ,souymd = :souymd ");
        sql.append(" ,updated_at = :updated_at ");
        sql.append(" ,updated_user_id = :updated_user_id ");
        //PRD_0154 #10699 add start
        sql.append(" ,deleted_at = null ");
        //PRD_0154 #10699 add end
        sql.append(" WHERE id = :id");
        return sql.toString();
    }

}
//PRD_0137 #10669 add end