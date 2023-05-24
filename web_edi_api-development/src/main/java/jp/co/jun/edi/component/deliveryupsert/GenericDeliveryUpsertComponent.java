package jp.co.jun.edi.component.deliveryupsert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.model.DeliveryUpsertModel;

/**
 * 納品依頼関連テーブルの登録・更新処理テンプレート.
 * @param <T> 更新レコードのモデル
 * @param <E> 更新レコードのエンティティ
 * @param <P> 親のエンティティ
 */
public abstract class GenericDeliveryUpsertComponent<T, E, P> extends GenericComponent {
    /**
     * 登録・更新処理.
     * @param entityForUpsert 登録・更新用整形済のエンティティ
     * @return 登録・更新処理したID
     */
    protected abstract BigInteger save(E entityForUpsert);

    /**
     * 登録用のレコードを設定する.
     * @param modelForUpdate 登録用のレコード
     * @param deliveryUpsertModel 納品依頼登録更新用引数
     * @return 登録用のレコード
     */
    protected abstract E generateEntityForInsert(T modelForUpdate, DeliveryUpsertModel<T, P> deliveryUpsertModel);

    /**
     * 更新用のレコードを設定する.
     * @param modelForUpdate 更新用のレコード
     * @param deliveryUpsertModel 納品依頼登録更新用引数
     * @return 更新用のレコード
     */
    protected abstract E generateEntityForUpdate(T modelForUpdate, DeliveryUpsertModel<T, P> deliveryUpsertModel);

    /**
     * 更新用のレコードにIDが存在するか判定.
     * @param modelForUpdate 更新用のレコード
     * @return true:存在する
     */
    protected abstract boolean existsId(T modelForUpdate);

    /**
     * 更新したIDを登録・更新用整形済のエンティティにセットする.
     * @param entityForUpsert 登録・更新用整形済のエンティティ
     * @param id 更新したID
     */
    protected abstract void setIdToModelForUpdate(E entityForUpsert, BigInteger id);

    /**
     * 子テーブルの登録・更新処理を行う.
     * @param entityForUpsert 登録・更新用整形済のエンティティ
     * @param modelForUpsert 登録・更新用レコード
     * @param deliveryUpsertModel 納品依頼登録更新用引数
     */
    protected abstract void upsertChild(E entityForUpsert, T modelForUpsert, DeliveryUpsertModel<T, P> deliveryUpsertModel);

    /**
     * 登録・更新したID以外のレコードを論理削除.
     * @param resultIds 登録・更新したIDリスト
     * @param deliveryUpsertModel 納品依頼登録更新用引数
     */
    protected abstract void deleteExceptUpsertIds(List<BigInteger> resultIds, DeliveryUpsertModel<T, P> deliveryUpsertModel);

    /**
     * 登録・更新を行う.
     * @param deliveryUpsertModel 納品依頼登録更新用引数
     */
    public void upsert(final DeliveryUpsertModel<T, P> deliveryUpsertModel) {
        // 登録・更新した(これからする)IDリスト
        final List<BigInteger> resultIds = new ArrayList<>();

        for (T modelForUpdate : deliveryUpsertModel.getModelForUpdateList()) {
            E entityForUpsert = null;

            if (existsId(modelForUpdate)) {
                // IDがある場合、UPDATE
                entityForUpsert = generateEntityForUpdate(modelForUpdate, deliveryUpsertModel);
            } else {
                // IDがない場合、INSERT
                entityForUpsert = generateEntityForInsert(modelForUpdate, deliveryUpsertModel);
            }

            final BigInteger id = save(entityForUpsert);
            setIdToModelForUpdate(entityForUpsert, id);
            resultIds.add(id);

            upsertChild(entityForUpsert, modelForUpdate, deliveryUpsertModel);
        }

        // 登録・更新した件数が0件の場合は、以降の削除処理は行わない
        if (resultIds.isEmpty()) {
            return;
        }

        // 登録・更新したID以外のレコードを論理削除
        deleteExceptUpsertIds(resultIds, deliveryUpsertModel);
    }
}
