package jp.co.jun.edi.component;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.csv.DistributionShipmentConfirmCsvFileEntity;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 直送配分出荷確定データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class DirectDistributionShipmentConfirmLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<DistributionShipmentConfirmCsvFileEntity> {

    private final Integer shopkindNumber = 1;
    private final Integer shopfmtNumber = 130;
    private final Integer distrikindNumber = 1;
    private final Integer shipmentType = 1;
    private final Integer shipmentPlace = 1;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DIRECT_DISTRIBUTION_SHIPMENT_CONFIRM;
    }

    /**
     * 配分区分  =0 の場合、0を設定.
     * 配分区分!=0 の場合、1を設定.
     *  ただし、以下の条件の場合は、0をセットする.
     *  - 店舗マスタ.店舗区分＝1:本部
     *  - AND 店舗マスタ.店舗形態＝130:倉庫
     *  - AND 店舗マスタ.配分区分＝1:保留
     * @param distrikind 配分区分
     * @param shopkind 店舗区分
     * @param shopfmt 店舗形態
     * @return 保留区分
     */
    public Integer setSuspendType(final Integer distrikind, final Integer shopkind, final Integer shopfmt) {
        if (Objects.equals(distrikind, 0)
                ||  Objects.equals(shopkind, shopkindNumber) && Objects.equals(shopfmt, shopfmtNumber) && Objects.equals(distrikind, distrikindNumber)) {
            return 0;
        }
        return 1;
    }

    @Override
    void createRecord(final CSVPrinter printer, final DistributionShipmentConfirmCsvFileEntity entity) throws Exception {
        final Date shipmentAt = new Date();

        printer.printRecord(
                DateUtils.formatFromDate(entity.getManageDate(), "yyyyMMdd"), // 管理情報 日付
                DateUtils.formatFromDate(entity.getManageAt(), "HHmmss"), // 管理情報 時間
                entity.getManageNumber(), // 管理情報 管理No
                entity.getLineNumber(), // 管理情報 行No
                BusinessType.DISTRIBUTION_SHIPMENT_CONFIRM.getValue(), // データ種別("KH"固定)
                shipmentType, // 出荷区分
                shipmentPlace, // 出荷場所
                setSuspendType(entity.getDistrikind(), entity.getShopkind(), entity.getShopfmt()), // 保留区分
                entity.getStoreCode(), // 店舗コード
                DateUtils.formatFromDate(shipmentAt, "yyyyMMdd"), // 出荷日
                entity.getShipmentVoucherNumber(), // 出荷伝票 No
                entity.getShipmentVoucherLine(), // 出荷伝票  行
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                entity.getSize(), // サイズコード
                entity.getDeliveryLot(), //  出荷指示数
                entity.getArrivalLot(), //  出荷確定数
                "", // 箱№（梱包No.）
                entity.getOrderNumber(), // KEY 発注番号
                entity.getDeliveryCount(), // KEY 引取回数
                entity.getDivisionCode() // KEY 配分課
        );
    }
}
