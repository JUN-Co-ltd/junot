package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.util.List;

import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.security.CustomLoginUser;
import lombok.Data;

/**
 *
 * {@link DeliveryUpsertModel} の パラメータモデル.
 * @param <T> 更新対象のリクエストモデルリスト
 * @param <P> 親のエンティティ
 */
@Data
public class DeliveryUpsertModel<T, P> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 更新対象のリクエストモデルリスト(必須). */
    private List<T> modelForUpdateList;

    /** 親のエンティティ(必須). */
    private P parentEntity;

    /** 更新ユーザー(必須). */
    private CustomLoginUser loginUser;

    /** 現時点でDBに登録されている納品依頼明細リストの先頭値(任意). */
    private TDeliveryDetailEntity registeredFirstDeliveryDetailEntity;

    /** 店舗配分画面からの登録フラグ(任意). */
    private boolean fromStoreScreen;

    // PRD_0044 add SIT start
    /** 店舗配分画面からの一時保存フラグ(任意). */
    private boolean storeScreenSaveCorrect = false;
    // PRD_0044 add SIT end
}
