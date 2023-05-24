package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.constants.NumberConstants;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.PurchaseDivisionModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.model.PurchaseSkuModel;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.CollectionUtils;

/**
 * 仕入関連のコンポーネント.
 */
@Component
public class PurchaseComponent extends GenericComponent {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ShipmentComponent shipmentComponent;

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private TWmsLinkingFileRepository wmsLinkingFileRepository;

    /**
     * @param resultList 取得結果リスト
     * @return PurchaseModel
     */
    public PurchaseModel toPurchaseModel(final List<TPurchaseEntity> resultList) {
        if (resultList.isEmpty()) {
            return null;
        }

        final PurchaseModel res = new PurchaseModel();
        // 共通項目コピー
        BeanUtils.copyProperties(resultList.get(0), res);

        // 仕入SKU
        final List<PurchaseSkuModel> purchaseSkus = resultList
                .stream()
                .filter(CollectionUtils.distinctByKey(result -> result.getColorCode() + result.getSize()))  // SKUの重複除去
                .map(result -> toPurchaseSkuModel(result, resultList))
                .collect(Collectors.toList());
        res.setPurchaseSkus(purchaseSkus);

        return res;
    }

    /**
     * @param entity 処理中の仕入情報
     * @param resultList resultEntityList
     * @return PurchaseSkuModel
     */
    private PurchaseSkuModel toPurchaseSkuModel(final TPurchaseEntity entity, final List<TPurchaseEntity> resultList) {
        final PurchaseSkuModel model = new PurchaseSkuModel();
        // 仕入SKUリストコピー
        BeanUtils.copyProperties(entity, model);

        final List<PurchaseDivisionModel> purchaseDivisions = resultList
                .stream()
                .filter(innerEntity -> isSameSku(innerEntity, entity))  // 処理中の仕入情報と同じSKU抽出
                .map(this::toPurchaseDivisionModel)
                .collect(Collectors.toList());

        model.setPurchaseDivisions(purchaseDivisions);

        return model;
    }

    /**
     * @param entity entity
     * @return PurchaseDivisionModel
     */
    private PurchaseDivisionModel toPurchaseDivisionModel(final TPurchaseEntity entity) {
        final PurchaseDivisionModel model = new PurchaseDivisionModel();
        // 仕入配分課リストコピー
        BeanUtils.copyProperties(entity, model);

        return model;
    }

    /**
     * @param entity1 TPurchaseEntity
     * @param entity2 TPurchaseEntity
     * @return true:同一SKU
     */
    private boolean isSameSku(final TPurchaseEntity entity1, final TPurchaseEntity entity2) {
        return entity1.getColorCode().equals(entity2.getColorCode())
               && entity1.getSize().equals(entity2.getSize());
    }

    /**
     * 仕入登録・更新バリデーションチェックを行う.
     * @param purchaseRequest 仕入フォーム値
     * @param delivery 納品情報
     * @return ResultMessages
     */
    public ResultMessages checkValidate(
            final PurchaseModel purchaseRequest,
            final DeliveryModel delivery) {
        final ResultMessages rsltMsg = ResultMessages.warning();

        // 入荷日が当日より未来の場合、エラー
        if (purchaseRequest.getArrivalAt().after(new Date())) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_001));
        }

        // 入荷数量が納品数量超えた場合、エラー
        purchaseRequest.getPurchaseSkus().forEach(psku -> psku.getPurchaseDivisions().forEach(div -> {
            if (isArrivalCountOver(delivery.getDeliveryDetails(), psku, div)) {
                rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_002,
                        getMessage("code.400_PC_02", div.getDivisionCode(), psku.getColorCode(), psku.getSize())));
            }
        }));

        return rsltMsg;
    }


    /**
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    public String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    /**
     * @param deliveryDetails DeliveryDetailリスト
     * @param psku PurchaseSku
     * @param div PurchaseDivision
     * @return true:入荷数量が納品依頼数超え
     */
    private boolean isArrivalCountOver(final List<DeliveryDetailModel> deliveryDetails, final PurchaseSkuModel psku, final PurchaseDivisionModel div) {
        return deliveryDetails
                .stream()
                .filter(d -> d.getDivisionCode().equals(div.getDivisionCode()))
                .anyMatch(d -> d.getDeliverySkus()
                        .stream()
                        .filter(s -> s.getColorCode().equals(psku.getColorCode()) && s.getSize().equals(psku.getSize()))
                        .anyMatch(s -> div.getArrivalCount() > s.getDeliveryLot()));
    }

    /**
     * @param purchaseRequest 仕入フォーム値
     * @return Upsert用のentityリスト
     */
    public List<TPurchaseEntity> toPurchaseEntitiesForUpsert(final PurchaseModel purchaseRequest) {
        final List<TPurchaseEntity> entities = purchaseRequest
                .getPurchaseSkus()
                .stream()
                .flatMap(purchaseSku -> purchaseSku
                        .getPurchaseDivisions()
                        .stream()
                        .filter(this::isInputtedArrivalCount)
                        .map(purchaseDivision -> toPurchaseEntity(purchaseRequest, purchaseSku, purchaseDivision)))
                .collect(Collectors.toList());

        return entities;
    }

    /**
     * @param purchaseDivisionRequest 仕入配分課フォーム値
     * @return true:入荷数の入力がある
     */
    private boolean isInputtedArrivalCount(final PurchaseDivisionModel purchaseDivisionRequest) {
        final Integer arrivalCount = purchaseDivisionRequest.getArrivalCount();
        return arrivalCount != null;
    }

    /**
     * @param purchaseRequest 仕入フォーム値
     * @param purchaseSku 仕入SKUフォーム値
     * @param purchaseDivision 仕入配分課フォーム値
     * @return TPurchaseEntity
     */
    private TPurchaseEntity toPurchaseEntity(
            final PurchaseModel purchaseRequest,
            final PurchaseSkuModel purchaseSku,
            final PurchaseDivisionModel purchaseDivision) {
        // Entityにコピー
        final TPurchaseEntity entity = new TPurchaseEntity();
        BeanUtils.copyProperties(purchaseRequest, entity);
        BeanUtils.copyProperties(purchaseSku, entity);
        BeanUtils.copyProperties(purchaseDivision, entity);

        // 入荷場所
        entity.setArrivalPlace(shipmentComponent.extraxtOldLogisticsCode(purchaseRequest.getArrivalShop()));

        // LG送信区分
        entity.setLgSendType(LgSendType.NO_INSTRUCTION);
        // 会計連携ステータス
        entity.setAccountLinkingStatus(FileInfoStatusType.FILE_UNPROCESSED);

        return entity;
    }

    /**
     * 倉庫連携ファイル情報登録.
     * 直送確定(仕入)用
     * @param businessType 業務区分
     * @return 登録された倉庫連携ファイル情報
     */
    public TWmsLinkingFileEntity insertWmsLinkingFile(final BusinessType businessType) {

        // 管理Noを取得
        String manageNo = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_PURCHASE,
                                                                          MNumberColumnNameType.SQ_MANAGE_NUMBER,
                                                                          NumberConstants.CONTROL_NUMBER_LENGTH);

        final TWmsLinkingFileEntity entity = new TWmsLinkingFileEntity();
        entity.setBusinessType(businessType);
        entity.setManageNumber(manageNo);
        entity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_NOT_CREATE);

        wmsLinkingFileRepository.save(entity);

        return entity;
    }

    /**
     * 更新用データの作成.
     * @param purchases 更新用の仕入情報Entityリスト(リクエストのキーで抽出したDBの仕入情報リスト)
     * @param wmsLinkingFileEntity 登録済の倉庫連携ファイル情報Entity
     * @param userId ログインユーザID
     */
    public void prepareSaveData(final List<TPurchaseEntity> purchases,
                                  final TWmsLinkingFileEntity wmsLinkingFileEntity,
                                  final BigInteger userId) {

        final Date currentDate = new Date();

        TPurchaseEntity prePurchase = null; // 前回ループで処理した仕入情報
        int seqCnt = 1; // リクエスト単位の連番
        int seqCntByNumber = 1; // キー単位の連番

        String purchaseVoucherNo = StringUtils.EMPTY;
        String instructNo = StringUtils.EMPTY;

        for (final TPurchaseEntity p: purchases) {
            if (notMatchLgKey(prePurchase, p)) { // 新しいキー.新規採番
                purchaseVoucherNo = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_PURCHASE,
                                                                               MNumberColumnNameType.PURCHASE_VOUCHER_NUMBER,
                                                                               NumberConstants.VOUCHER_NUMBER_LENGTH);
                instructNo = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_PURCHASE,
                                                                        MNumberColumnNameType.INSTRUCT_NUMBER,
                                                                        NumberConstants.INSTRUCT_NUMBER_LENGTH);
                seqCntByNumber = 1;
            }

            // 日時
            p.setSqManageDate(currentDate);
            p.setSqManageAt(currentDate);

            // 倉庫連携ファイルID
            p.setWmsLinkingFileId(wmsLinkingFileEntity.getId());

            // 採番
            p.setSqManageNumber(wmsLinkingFileEntity.getManageNumber());
            p.setPurchaseVoucherNumber(purchaseVoucherNo);
            p.setInstructNumber(instructNo);

            // 連番
            p.setLineNumber(seqCnt);
            p.setPurchaseVoucherLine(seqCntByNumber);
            p.setInstructNumberLine(seqCntByNumber);

            // LG
            p.setLgSendType(LgSendType.INSTRUCTION);

            // 更新ユーザ
            p.setUpdatedUserId(userId);

            seqCnt = seqCnt + 1;
            seqCntByNumber = seqCntByNumber + 1;
            prePurchase = p;
        }
    }

    /**
     * 更新順にソート.
     * @param list LG送信対象リスト
     */
    public void sortForUpdate(final List<TPurchaseEntity> list) {
        Collections.sort(list, Comparator.comparing(TPurchaseEntity::getPurchaseCount)
                .thenComparing(TPurchaseEntity::getDivisionCode)
                .thenComparing(TPurchaseEntity::getId));
    }

    /**
     * @param prePurchase 前回のループで処理した仕入情報
     * @param purchase 現在のループで処理中の仕入情報
     * @return true:LG送信のキーが不一致
     */
    private boolean notMatchLgKey(final TPurchaseEntity prePurchase, final TPurchaseEntity purchase) {
        return prePurchase == null
                || !prePurchase.getDeliveryId().equals(purchase.getDeliveryId())
                || !prePurchase.getDivisionCode().equals(purchase.getDivisionCode());
    }

    //PRD_0134 #10654 add JEF start
    /**
     * 発注ID、伝票番号をキーに重複除去.
     * @param tpurchaseModels 仕入情報リスト
     * @return 発注ID、伝票番号で重複除去した仕入情報リスト
     */
    public List<TPurchaseEntity> distinctPurchaseByOrderIsAndVoucherNo(final List<TPurchaseEntity> tPurchaseEntitys) {
    	return tPurchaseEntitys.stream()
    			.filter(jp.co.jun.edi.util.CollectionUtils.distinctByKey(model -> model.getOrderId() + model.getPurchaseVoucherNumber()))
    			.collect(Collectors.toList());
    }
    //PRD_0134 #10654 add JEF end
}
