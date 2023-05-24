//PRD_0137 #10669 add start
package jp.co.jun.edi.repository.custom;

import java.math.BigInteger;
import java.util.Map;

/**
 * サイズマスタRepositoryのカスタムインターフェース.
 */
public interface DynamicMaintSizeRepositoryCustom {

    /**
     * サイズ情報を登録する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            登録データ
     * @param hscd
     *            品種コード
     * @return 登録件数
     */
    int insertByMaintCode(BigInteger userId, Map<String, String> data,String hscd);

    /**
     * サイズ情報を検索する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            更新データ
     * @param hscd
     *            品種コード
     * @return 更新件数
     */
    int searchByMaintCode(BigInteger userId, Map<String, String> data,String hscd);


    /**
     * サイズ情報を更新する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            更新データ
     * @param hscd
     *            品種コード
     * @return 更新件数
     */
    int updateByMaintCode(BigInteger userId, Map<String, String> data,String hscd);

    /**
     * サイズ情報を削除する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            登録・更新データ
     * @param hscd
     *            品種コード
     * @return 削除件数
     */
    int deletedByMaintCode(BigInteger userId, Map<String, String> data);

    /**
     * サイズ情報をコピー新規する.
     *
     * @param userId
     *            ユーザID
     * @param data
     *            更新データ
     * @param hscd
     *            品種コード
     * @return 更新件数
     */
    int searchHscdMaintCode(BigInteger userId, String hscd);

}
//PRD_0137 #10669 add end