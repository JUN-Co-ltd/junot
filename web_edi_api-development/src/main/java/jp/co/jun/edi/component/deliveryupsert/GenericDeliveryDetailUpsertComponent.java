package jp.co.jun.edi.component.deliveryupsert;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.model.DeliveryStoreModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 納品明細情報の登録・更新処理.
 */
public abstract class GenericDeliveryDetailUpsertComponent extends GenericDeliveryUpsertComponent<DeliveryDetailModel, TDeliveryDetailEntity, TDeliveryEntity> {
    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private DeliveryStoreUpsertComponent deliveryStoreUpsert;

    @Autowired
    private DeliverySkuUpsertComponent deliverySkuUpsert;

    @Override
    protected TDeliveryDetailEntity generateEntityForInsert(final DeliveryDetailModel deliveryDetailModelForUpdate,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {

        // PRD_0044 mod SIT start
        //return deliveryComponent.generateDeliveryDetailForInsert(deliveryUpsertModel.getLoginUser(),
        //        deliveryDetailModelForUpdate, deliveryUpsertModel.getParentEntity().getId(), deliveryUpsertModel.isFromStoreScreen());
        return deliveryComponent.generateDeliveryDetailForInsert(deliveryUpsertModel.getLoginUser(), deliveryDetailModelForUpdate,
                deliveryUpsertModel.getParentEntity().getId(), deliveryUpsertModel.isFromStoreScreen(), deliveryUpsertModel.isStoreScreenSaveCorrect());
        // PRD_0044 mod SIT end
    }

    @Override
    protected TDeliveryDetailEntity generateEntityForUpdate(final DeliveryDetailModel deliveryDetailModelForUpdate,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {
        // 現時点でDBに登録されている納品明細を取得(取得できない場合はエラー)
        final TDeliveryDetailEntity registeredDbDeliveryDetail =
                deliveryDetailRepository.findByIdAndDeletedAtIsNull(deliveryDetailModelForUpdate.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        /* 画面側で設定される項目 */
        // 修正納期
        registeredDbDeliveryDetail.setCorrectionAt(deliveryDetailModelForUpdate.getCorrectionAt());
        // 配分率ID
        registeredDbDeliveryDetail.setDistributionRatioId(deliveryDetailModelForUpdate.getDistributionRatioId());
        // ファックス送信フラグ
        registeredDbDeliveryDetail.setFaxSend(deliveryDetailModelForUpdate.getFaxSend());
        // キャリー区分
        registeredDbDeliveryDetail.setCarryType(deliveryDetailModelForUpdate.getCarryType());
        //PRD_0123 #7054 add JFE start
        //物流コード
        registeredDbDeliveryDetail.setLogisticsCode(deliveryDetailModelForUpdate.getLogisticsCode());
        //PRD_0123 #7054 add JFE end

        // 店舗配分画面からの登録・更新(一時保存以外)の場合は店舗別登録済フラグにtrueをセット
        // PRD_0044 mod SIT start
        //if (deliveryUpsertModel.isFromStoreScreen()) {
        // PRD_0130　#9756 mod JFE start
		//        if (deliveryUpsertModel.isFromStoreScreen() && !deliveryUpsertModel.isStoreScreenSaveCorrect()) {
		//        // PRD_0044 mod SIT end
		//            registeredDbDeliveryDetail.setStoreRegisteredFlg(BooleanType.TRUE);
        //        }
        registeredDbDeliveryDetail.setStoreRegisteredFlg(deliveryUpsertModel.isFromStoreScreen() && !deliveryUpsertModel.isStoreScreenSaveCorrect() ? BooleanType.TRUE : BooleanType.FALSE);
        // PRD_0130　#9756 mod JFE end
        /* API側で設定される項目 */
        // 納期
        geneateDeliveryAt(registeredDbDeliveryDetail, registeredDbDeliveryDetail.getDeliveryAt(),
                deliveryDetailModelForUpdate.getCorrectionAt());

        // 連携入力者
        registeredDbDeliveryDetail.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(deliveryUpsertModel.getLoginUser()));
        return registeredDbDeliveryDetail;
    }

    @Override
    protected BigInteger save(final TDeliveryDetailEntity deliveryDetailEntityForUpsert) {
        final TDeliveryDetailEntity resultTDeliveryDetailEntity = deliveryDetailRepository.save(deliveryDetailEntityForUpsert);
        return resultTDeliveryDetailEntity.getId();
    }

    @Override
    protected boolean existsId(final DeliveryDetailModel deliveryDetailModelForUpdate) {
        return Objects.nonNull(deliveryDetailModelForUpdate.getId());
    }

    @Override
    protected void setIdToModelForUpdate(final TDeliveryDetailEntity resultTDeliveryDetailEntity, final BigInteger id) {
        resultTDeliveryDetailEntity.setId(id);
    }

    @Override
    protected void upsertChild(final TDeliveryDetailEntity deliveryDetailEntityForUpsert, final DeliveryDetailModel deliveryDetailModelForUpdate,
        final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {

        final CustomLoginUser loginUser = deliveryUpsertModel.getLoginUser();

        // 納品得意先の登録・更新
        final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> storeModel = generateDeliveryStoreUpsertModel(
                deliveryDetailModelForUpdate, deliveryDetailEntityForUpsert, loginUser);
        deliveryStoreUpsert.upsert(storeModel);

        // 課別画面からの場合は納品SKUの登録・更新
        if (!deliveryUpsertModel.isFromStoreScreen()) {
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> skuModel = generateDeliverySkuUpsertModel(
                    deliveryDetailModelForUpdate, deliveryDetailEntityForUpsert, loginUser);
            deliverySkuUpsert.upsert(skuModel);
        }
    }

    /**
     * 納品SKUUpsert用のモデル作成.
     * @param deliveryDetailModelForUpdate 親モデル
     * @param deliveryDetailEntityForUpsert 親エンティティ
     * @param loginUser ログインユーザー
     * @return 納品SKUUpsert用のモデル
     */
    protected DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> generateDeliverySkuUpsertModel(
            final DeliveryDetailModel deliveryDetailModelForUpdate, final TDeliveryDetailEntity deliveryDetailEntityForUpsert,
            final CustomLoginUser loginUser) {
        final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> skuModel = new DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity>();
        skuModel.setModelForUpdateList(deliveryDetailModelForUpdate.getDeliverySkus());
        skuModel.setLoginUser(loginUser);
        skuModel.setParentEntity(deliveryDetailEntityForUpsert);
        return skuModel;
    }

    /**
     * 納品得意先Upsert用のモデル作成.
     * @param deliveryDetailModelForUpdate 親モデル
     * @param deliveryDetailEntityForUpsert 親エンティティ
     * @param loginUser ログインユーザー
     * @return 納品得意先Upsert用のモデル
     */
    protected DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> generateDeliveryStoreUpsertModel(
            final DeliveryDetailModel deliveryDetailModelForUpdate,
            final TDeliveryDetailEntity deliveryDetailEntityForUpsert, final CustomLoginUser loginUser) {

        final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> storeModel = new DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity>();
        storeModel.setModelForUpdateList(deliveryDetailModelForUpdate.getDeliveryStores());
        storeModel.setLoginUser(loginUser);
        storeModel.setParentEntity(deliveryDetailEntityForUpsert);

        return storeModel;
    }

    /**
     * 納期の設定.
     *
     * DBに登録されている納期 > 画面から入力した修正納期の場合、納期に修正納期を設定する.
     * DBに登録されている納期 <= 画面から入力した修正納期の場合、納期に設定なし.
     * (納期が早くなったときだけ更新)
     *
     * @param insertDeliveryDetailEntity 値をセットする納品明細情報
     * @param registedDeliveryAt DBに登録されている納期
     * @param correctionAt  画面から入力した修正納期
     */
    protected void geneateDeliveryAt(final TDeliveryDetailEntity insertDeliveryDetailEntity, final Date registedDeliveryAt, final Date correctionAt) {
        // 画面から入力した修正納期の時分秒を切り捨てる
        final Date formatCorrectionAt = DateUtils.truncateDate(correctionAt);

        if (registedDeliveryAt.after(formatCorrectionAt)) {
            // DBに登録されている納期 > 画面から入力した修正納期の場合、納期に修正納期（時分秒を除く）を設定
            insertDeliveryDetailEntity.setDeliveryAt(formatCorrectionAt);
        }
    }

    @Override
    protected void deleteExceptUpsertIds(final List<BigInteger> deliveryDetailUpsertResultIds,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {
        final BigInteger deliveryId = deliveryUpsertModel.getParentEntity().getId();
        final BigInteger loginUserId = deliveryUpsertModel.getLoginUser().getUserId();

        // 登録・更新した納品明細のID以外のレコードを論理削除(登録・更新されなかった明細は数量入力がないもの＝削除)

        // 納品得意先画面からの場合は納品明細・納品SKUは削除しない
        if (deliveryUpsertModel.isFromStoreScreen()) {
            return;
        }

        // 納品明細論理削除
        deliveryDetailRepository.updateDetailDeletedAtByDeliveryIdAndExclusionIds(
                deliveryId, deliveryDetailUpsertResultIds, loginUserId);

        // 納品SKU論理削除
        deliverySkuRepository.updateSkuDeletedAtByDeliveryIdAndExclusionIds(deliveryId, deliveryDetailUpsertResultIds, loginUserId);
    }
}
