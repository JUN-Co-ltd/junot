package jp.co.jun.edi.repository.custom;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;

/**
 * メンテナンスコードRepositoryのカスタムインターフェース.
 */
public interface DynamicMaintCodeRepositoryCustom {
    /**
     * メンテナンスコード情報を取得する.
     *
     * @param searchCondition
     *            発注検索条件
     * @param entity
     *            画面構成情報
     * @return コードマスタ情報
     */
    Page<Map<String, Object>> findByMaintCode(MaintCodeSearchConditionModel searchCondition, MScreenStructureEntity entity);

    /**
     * メンテナンスコード情報を登録する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            登録データ
     * @param entity
     *            画面構成情報
     * @return 登録件数
     */
    int insertByMaintCode(BigInteger userId, Map<String, String> data, MScreenStructureEntity entity);

    /**
     * メンテナンスコード情報を更新する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            更新データ
     * @param entity
     *            画面構成情報
     * @return 更新件数
     */
    int updateByMaintCode(BigInteger userId, Map<String, String> data, MScreenStructureEntity entity);

    /**
     * メンテナンスコード情報を削除する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            登録・更新データ
     * @param entity
     *            画面構成情報
     * @return 削除件数
     */
    int deletedByMaintCode(BigInteger userId, Map<String, String> data, MScreenStructureEntity entity);

}
