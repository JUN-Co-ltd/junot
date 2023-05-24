package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DistributionShipmentLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.schedule.ExtendedDistributionShipmentScheduleEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedDistributionShipmentScheduleRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * 配分出荷指示データファイル作成スケジュールのコンポーネント.
 */
@Component
public class DistributionShipmentFileLinkingScheduleComponent
extends LinkingScheduleComponent<ExtendedDistributionShipmentScheduleEntity> {

    @Autowired
    private DistributionShipmentLinkingCreateCsvFileComponent distributionShipmentLinkingCreateCsvFileComponent;

    @Autowired
    private ExtendedDistributionShipmentScheduleRepository exDistributionShipmentScheduleRepository;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DISTRIBUTION_SHIPMENT_INSTRUCTION;
    }

    @Override
    List<ExtendedDistributionShipmentScheduleEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return exDistributionShipmentScheduleRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<ExtendedDistributionShipmentScheduleEntity> entities,
            final BigInteger wmsLinkingFileId) {
        // 倉庫連携ファイルIDに該当する納品得意先SKU情報の件数取得
        final int cnt = deliveryStoreSkuRepository.countByWmsLinkingFileId(wmsLinkingFileId);

        // 送信対象のレコード数との一致を確認(一致しない場合はエラー)
        if (cnt != entities.size()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("getCount.")
                    .message("distribution shipment record number is insufficient.")
                    .value("wmsLinkingFileId", wmsLinkingFileId)
                    .build()));
        }
    }

    @Override
    File createInstructionFile(final List<ExtendedDistributionShipmentScheduleEntity> entities) throws Exception {
        return distributionShipmentLinkingCreateCsvFileComponent.createCsvFile(entities);
    }
}
