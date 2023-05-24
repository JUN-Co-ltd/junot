package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.InventoryShipmentLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.schedule.ExtendedInventoryShipmentScheduleEntity;
import jp.co.jun.edi.repository.extended.ExtendedInventoryShipmentScheduleRepository;
import jp.co.jun.edi.type.BusinessType;

/**
 * 在庫出荷ファイル作成スケジュールのコンポーネント.
 */
@Component
public class InventoryShipmentFileLinkingScheduleComponent
extends LinkingScheduleComponent<ExtendedInventoryShipmentScheduleEntity> {

    @Autowired
    private InventoryShipmentLinkingCreateCsvFileComponent inventoryShipmentLinkingCreateCsvFileComponent;

    @Autowired
    private ExtendedInventoryShipmentScheduleRepository extendedInventoryShipmentScheduleRepository;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.INVENTORY_INSTRUCTION;
    }

    @Override
    List<ExtendedInventoryShipmentScheduleEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return extendedInventoryShipmentScheduleRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<ExtendedInventoryShipmentScheduleEntity> entities,
            final BigInteger wmsLinkingFileId) {
    }

    @Override
    File createInstructionFile(final List<ExtendedInventoryShipmentScheduleEntity> entities) throws Exception {
        return inventoryShipmentLinkingCreateCsvFileComponent.createCsvFile(entities);
    }

}
