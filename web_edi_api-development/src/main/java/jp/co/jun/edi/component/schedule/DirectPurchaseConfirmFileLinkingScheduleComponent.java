package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DirectPurchaseConfirmLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.csv.PurchaseConfirmCsvFileEntity;
import jp.co.jun.edi.repository.csv.PurchaseConfirmCsvFileRepository;
import jp.co.jun.edi.type.BusinessType;

/**
 * 直送仕入確定ファイル作成スケジュールのコンポーネント.
 */
@Component
public class DirectPurchaseConfirmFileLinkingScheduleComponent
extends LinkingScheduleComponent<PurchaseConfirmCsvFileEntity> {

    @Autowired
    private DirectPurchaseConfirmLinkingCreateCsvFileComponent directPurchaseConfirmLinkingCreateCsvFileComponent;

    @Autowired
    private PurchaseConfirmCsvFileRepository purchaseConfirmCsvFileRepository;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DIRECT_PURCHASE_CONFIRM;
    }

    @Override
    List<PurchaseConfirmCsvFileEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return purchaseConfirmCsvFileRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<PurchaseConfirmCsvFileEntity> entities,
            final BigInteger wmsLinkingFileId) {
    }

    @Override
    File createInstructionFile(final List<PurchaseConfirmCsvFileEntity> entities) throws Exception {
        return directPurchaseConfirmLinkingCreateCsvFileComponent.createCsvFile(entities, BusinessType.PURCHASE_CONFIRM);
    }

}
