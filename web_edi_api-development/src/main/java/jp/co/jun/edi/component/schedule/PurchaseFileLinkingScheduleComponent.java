package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PurchaseLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.schedule.ExtendedTPurchaseLinkingCreateCsvFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.extended.ExtendedTPurchaseRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * 仕入指示データファイル作成スケジュールのコンポーネント.
 */
@Component
public class PurchaseFileLinkingScheduleComponent
extends LinkingScheduleComponent<ExtendedTPurchaseLinkingCreateCsvFileEntity> {

    @Autowired
    private PurchaseLinkingCreateCsvFileComponent purchaseLinkingCreateCsvFileComponent;

    @Autowired
    private ExtendedTPurchaseRepository exTPurchaseRepository;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.PURCHASE_INSTRUCTION;
    }

    @Override
    List<ExtendedTPurchaseLinkingCreateCsvFileEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return exTPurchaseRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<ExtendedTPurchaseLinkingCreateCsvFileEntity> entities,
            final BigInteger wmsLinkingFileId) {
        // 一つでも発注情報がない場合はファイル作成エラー
        if (entities.stream().anyMatch(entity -> entity.getCheckOrderId() == null)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("getOrderInfo")
                    .message("t_order not found.")
                    .value("wmsLinkingFileId", wmsLinkingFileId)
                    .build()));
        }
    }

    @Override
    File createInstructionFile(final List<ExtendedTPurchaseLinkingCreateCsvFileEntity> entities) throws Exception {
        return purchaseLinkingCreateCsvFileComponent.createCsvFile(entities);
    }
}
