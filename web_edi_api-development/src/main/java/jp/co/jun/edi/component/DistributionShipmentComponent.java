package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.constants.NumberConstants;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.repository.extended.ExtendedDeliveryStoreConfirmRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.WmsLinkingStatusType;

/**
 * 出荷配分関連のコンポーネント.
 */
@Component
public class DistributionShipmentComponent extends GenericComponent {

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private ExtendedDeliveryStoreConfirmRepository extendedDeliveryStoreConfirmRepository;

    @Autowired
    private TWmsLinkingFileRepository wmsLinkingFileRepository;

    /**
     * 倉庫連携ファイル情報登録.
     * 直送確定(配分出荷)用
     * @param businessType 業務区分
     * @return 登録された倉庫連携ファイル情報
     */
    public TWmsLinkingFileEntity insertWmsLinkingFile(final BusinessType businessType) {
        final TWmsLinkingFileEntity entity = new TWmsLinkingFileEntity();

        // 管理Noを取得
        String manageNumber = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_DELIVERY_STORE_SKU,
                                                                          MNumberColumnNameType.MANAGE_NUMBER,
                                                                          NumberConstants.CONTROL_NUMBER_LENGTH);

        entity.setBusinessType(businessType);
        entity.setManageNumber(manageNumber);
        entity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_NOT_CREATE);
        wmsLinkingFileRepository.save(entity);

        return entity;
    }

    /**
     * 更新順にソート.
     * @param list 送信対象リスト
     */
    public void sortForUpdate(final List<TDeliveryDetailEntity> list) {
        Collections.sort(list, Comparator
                .comparing(TDeliveryDetailEntity::getId)
                .thenComparing(TDeliveryDetailEntity::getDeliveryId));
    }

    /**
     * 更新用データの作成.
     * @param deliveryDetails 更新用の納品明細情報Entityリスト(リクエストのキーで抽出したDBの納品明細情報リスト)
     * @param sendEntities LG送信用の得意先SKU情報リスト
     * @param wmsLinkingFileEntity 登録済の倉庫連携ファイル情報Entity
     * @param userId ログインユーザID
     */
    public void prepareSaveData(final List<TDeliveryDetailEntity> deliveryDetails,
            final List<TDeliveryStoreSkuEntity> sendEntities,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) {

        final Date currentDate = new Date();
        final List<BigInteger> ids = new ArrayList<BigInteger>();
        int lineNumber = 1;

        // 更新用納品明細のセット.
        for (final TDeliveryDetailEntity p: deliveryDetails) {
            // 納品指示フラグ
            p.setShippingInstructionsFlg(BooleanType.TRUE);

            // 納品指示日
            p.setShippingInstructionsAt(currentDate);

            p.setUpdatedUserId(userId);

            // ※ 納品明細ID.
            ids.add(p.getId());
            // 更新用納品得意先SKUのセット.
            // ↓こっちでループ(in構文実行するとなぜか最初のIDの結果のみ参照になるため).
            final List<TDeliveryStoreSkuEntity> page
            = extendedDeliveryStoreConfirmRepository.findByDeliveryDetailIds(
                    org.apache.commons.lang3.StringUtils.join(ids, ", "));
            for (final TDeliveryStoreSkuEntity entity : page) {
                entity.setWmsLinkingFileId(wmsLinkingFileEntity.getId());
                entity.setLineNumber(lineNumber);
                entity.setManageNumber(wmsLinkingFileEntity.getManageNumber());
                entity.setManageAt(currentDate);
                entity.setManageDate(currentDate);
                entity.setUpdatedUserId(userId);
                lineNumber = 1 + lineNumber;
                sendEntities.add(entity);
            }
            ids.clear();
        }
    }

    /**
     * 確定用納品得意先SKU情報作成.
     *
     * @param deliveryStoreSkus 納品得意先SKU情報リスト
     * @param deliveryStores 納品得意先情報リスト
     */
    public void generateConfirmDeliveryStoreSku(
            final List<TDeliveryStoreSkuEntity> deliveryStoreSkus,
            final List<TDeliveryStoreEntity> deliveryStores) {
        // 納品得意先情報を店舗コード昇順でソート
        sortDeliveryStore(deliveryStores);

        deliveryStores.forEach(store -> {
            // 出荷伝票Noを採番
            final String voucherNumber = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_DELIVERY_STORE_SKU,
                                                                              MNumberColumnNameType.SHIPMENT_VOUCHER_NUMBER,
                                                                              NumberConstants.VOUCHER_NUMBER_LENGTH);
            // 納品得意先IDに紐づく納品得意先SKU情報を抽出、確定データセット(色コード昇順、サイズ昇順)
            final List<TDeliveryStoreSkuEntity> updateDeliveryStoreSkus = deliveryStoreSkus.stream()
                    .filter(storeSku -> storeSku.getDeliveryStoreId().equals(store.getId()))
                    .sorted(Comparator.comparing(TDeliveryStoreSkuEntity::getColorCode).thenComparing(TDeliveryStoreSkuEntity::getSize))
                    .collect(Collectors.toList());
            int lineNumber = 1;
            for (TDeliveryStoreSkuEntity updateSku : updateDeliveryStoreSkus) {
                updateSku.setArrivalLot(updateSku.getDeliveryLot()); // 入荷数
                updateSku.setShipmentVoucherNumber(voucherNumber); // 出荷伝票No
                updateSku.setShipmentVoucherLine(lineNumber); // 出荷伝票行
                lineNumber = 1 + lineNumber;
            }
        });

    }

    /**
     * 納品得意先情報をソート(店舗コード昇順).
     *
     * @param list 納品得意先情報リスト
     */
    private void sortDeliveryStore(final List<TDeliveryStoreEntity> list) {
        Collections.sort(list, Comparator
                .comparing(TDeliveryStoreEntity::getStoreCode));
    }
}
