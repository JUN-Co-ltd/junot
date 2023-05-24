package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DirectDistributionShipmentConfirmLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.csv.DistributionShipmentConfirmCsvFileEntity;
import jp.co.jun.edi.repository.csv.DistributionShipmentConfirmCsvFileRepository;
import jp.co.jun.edi.type.BusinessType;

/**
 * 直送配分出荷確定ファイル作成スケジュールのコンポーネント.
 */
@Component
public class DirectDistributionShipmentConfirmFileLinkingScheduleComponent
extends LinkingScheduleComponent<DistributionShipmentConfirmCsvFileEntity> {

    @Autowired
    private DirectDistributionShipmentConfirmLinkingCreateCsvFileComponent directDistributionShipmentConfirmLinkingCreateCsvFileComponent;

    @Autowired
    private DistributionShipmentConfirmCsvFileRepository distributionShipmentConfirmCsvFileRepository;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DIRECT_DISTRIBUTION_SHIPMENT_CONFIRM;
    }

    @Override
    List<DistributionShipmentConfirmCsvFileEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return distributionShipmentConfirmCsvFileRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<DistributionShipmentConfirmCsvFileEntity> entities,
            final BigInteger wmsLinkingFileId) {
    }

    @Override
    File createInstructionFile(final List<DistributionShipmentConfirmCsvFileEntity> entities) throws Exception {
        return directDistributionShipmentConfirmLinkingCreateCsvFileComponent.createCsvFile(entities, BusinessType.DISTRIBUTION_SHIPMENT_CONFIRM);
    }

}
