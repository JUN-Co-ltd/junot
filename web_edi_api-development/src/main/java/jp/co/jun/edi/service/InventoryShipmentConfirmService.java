package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MNumberComponent;
import jp.co.jun.edi.entity.TInventoryShipmentEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.constants.NumberConstants;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.InventoryShipmentConfirmListModel;
import jp.co.jun.edi.model.InventoryShipmentConfirmModel;
import jp.co.jun.edi.repository.TInventoryShipmentRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;

/**
 * (在庫出荷指示送信)用Service.
 */
@Service
public class InventoryShipmentConfirmService
extends GenericUpdateService<ApprovalServiceParameter<InventoryShipmentConfirmListModel>, ApprovalServiceResponse> {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private TWmsLinkingFileRepository wmsLinkingFileRepository;
    @Autowired
    private TInventoryShipmentRepository inventoryShipmentRepository;
    @Autowired
    private MNumberComponent numberComponent;

    private static final int NUMBER_LENGTH = 6;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<InventoryShipmentConfirmListModel> serviceParameter) {

        final InventoryShipmentConfirmListModel srcModel = serviceParameter.getItem();
        final List<InventoryShipmentConfirmModel> models = srcModel.getInventoryShipmentConfirms();

        // リクエストのキーと合致するレコード抽出
        final List<TInventoryShipmentEntity> entities = new ArrayList<TInventoryShipmentEntity>();
        for (InventoryShipmentConfirmModel model: models) {
            entities.addAll(readEntities(model).stream().collect(Collectors.toList()));
        }

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(models, entities);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 倉庫連携ファイル情報登録
        final TWmsLinkingFileEntity wms = insertWmsLinkingFile();

        // 更新
        confirm(entities, wms, serviceParameter.getLoginUser());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * レコードを生成.
     * @param model 在庫出荷指示データ
     * @return 在庫出荷指示情報
     */
    private List<TInventoryShipmentEntity> readEntities(
            final InventoryShipmentConfirmModel model
            ) {

        List<TInventoryShipmentEntity> entities = inventoryShipmentRepository.findByCargoAtAndInstructorSystemAndDivisionCodeAndPartNo(
                model.getCargoAt(),
                model.getCargoPlace(),
                model.getInstructorSystem().getValue(),
                model.getDivisionCode(),
                model.getPartNo());
        return entities;
    }

    /**
     * バリデーションチェックを行う.
     *
     * @param models 在庫出荷指示データリスト
     * @param entities 在庫出荷指示情報情報リスト
     * @return ResultMessages
     */
    private ResultMessages checkValidate(
            final List<InventoryShipmentConfirmModel> models,
            final List<TInventoryShipmentEntity> entities
            ) {
        final ResultMessages rsltMsg = ResultMessages.warning();
        // DBから取得できなければエラー
        if (entities.isEmpty()) {
            return ResultMessages.warning().add(MessageCodeType.CODE_002);
        }
        addIfError(models, entities, rsltMsg);
        return rsltMsg;
    }

    /**
     * エラーがあればエラー情報を追加する.
     * @param models リクエストパラメータ
     * @param entities DBの納品明細情報リスト
     * @param rsltMsg エラーメッセージ
     */
    private void addIfError(
            final List<InventoryShipmentConfirmModel> models,
            final List<TInventoryShipmentEntity> entities,
            final ResultMessages rsltMsg) {
        // LG送信済み
        final Optional<TInventoryShipmentEntity> opt
        = entities.stream().filter(d -> d.getLgSendType().equals(LgSendType.INSTRUCTION)).findFirst();
        if (opt.isPresent()) {
            MessageCodeType type = MessageCodeType.CODE_IS_001;
            rsltMsg.add(ResultMessage.fromCode(type, getMessage("code." + type .getValue(),
                    opt.get().getCargoAt(), opt.get().getInstructorSystem(), opt.get().getPartNo()
                    )));
            return;
        }
    }

    /**
     * メッセージ取得.
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    private String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    /**
     * 在庫出荷指示情報更新.
     * LG送信指示済に更新.
     *
     * @param entities リクエストのキーで抽出したDBの在庫出荷指示情報リスト
     * @param wms 倉庫連携ファイル情報
     * @param loginUser ログインユーザ情報
     */
    private void confirm(
            final List<TInventoryShipmentEntity> entities,
            final TWmsLinkingFileEntity wms,
            final CustomLoginUser loginUser
            ) {

        sortForUpdate(entities);

        // 更新データの作成.
        prepareSaveData(entities, wms, loginUser);
        inventoryShipmentRepository.saveAll(entities);
    }

    /**
     * 更新用データの作成.
     * @param entities 更新対象の在庫出荷指示情報リスト
     * @param wms 倉庫連携ファイル情報
     * @param loginUser ログインユーザ情報
     */
    private void prepareSaveData(
            final List<TInventoryShipmentEntity> entities,
            final TWmsLinkingFileEntity wms,
            final CustomLoginUser loginUser
            ) {

        TInventoryShipmentEntity preInventoryShipment = null; // 前回ループで処理した在庫出荷指示情報

        int lineNumber = 1;                 // 管理番号行
        int instructionLineNumber = 1;     // 指示管理番号行

        String instructionManageNumber = StringUtils.EMPTY;    // 指示番号

        final Date currentDate = new Date();

        // 在庫出荷指示情報の更新.
        for (final TInventoryShipmentEntity entity: entities) {

            if (notMatchLgKey(preInventoryShipment, entity)) {

                // 指示管理番号採番
                instructionManageNumber = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_INVENTORY_SHIPMENT,
                                                                                     MNumberColumnNameType.INSTRUCT_MANAGE_NUMBER,
                                                                                     NumberConstants.INSTRUCT_NUMBER_LENGTH);
                // カウントを初期化
                instructionLineNumber = 1;
            }


            // wms連携情報の更新
            entity.setWmsLinkingFileId(wms.getId());
            entity.setSequence(lineNumber);
            entity.setManageNumber(wms.getManageNumber());
            entity.setManageAt(currentDate);
            entity.setManageDate(currentDate);
            // LG送信フラグ→送信済
            entity.setLgSendType(LgSendType.INSTRUCTION);

            // 指示管理
            entity.setInstructionManageUserCode(loginUser.getAccountName());
            entity.setInstructionManageNumber(instructionManageNumber);
            entity.setInstructionManageNumberLine(instructionLineNumber);

            entity.setUpdatedUserId(loginUser.getUserId());

            lineNumber = lineNumber + 1;
            instructionLineNumber = instructionLineNumber + 1;
            preInventoryShipment = entity;
        }
    }

    /**
     * 更新順にソート.
     * @param list LG送信対象リスト
     */
    private void sortForUpdate(final List<TInventoryShipmentEntity> list) {
        Collections.sort(list, Comparator.comparing(TInventoryShipmentEntity::getPartNo)
                .thenComparing(TInventoryShipmentEntity::getColorCode)
                .thenComparing(TInventoryShipmentEntity::getSize)
                .thenComparing(TInventoryShipmentEntity::getShopCode)
                );
    }

    /**
     * @param preInventoryShipment 前回のループで処理した在庫出荷指示情報
     * @param inventoryShipment 現在のループで処理中の在庫出荷指示情報
     * @return true:品番が不一致
     */
    private boolean notMatchLgKey(final TInventoryShipmentEntity preInventoryShipment, final TInventoryShipmentEntity inventoryShipment) {
        return preInventoryShipment == null
                || !StringUtils.equals(preInventoryShipment.getPartNo(), inventoryShipment.getPartNo());

    }

    /**
     * 倉庫連携ファイル情報登録.
     *
     * @return 倉庫連携ファイル情報
     */
    private TWmsLinkingFileEntity insertWmsLinkingFile() {
        final String manageNo
        = numberComponent.createNumberSetZeroPadding(
                MNumberTableNameType.T_INVENTORY_SHIPMENT,
                MNumberColumnNameType.MANAGE_NUMBER, NUMBER_LENGTH);

        final TWmsLinkingFileEntity entity = new TWmsLinkingFileEntity();
        entity.setBusinessType(BusinessType.INVENTORY_INSTRUCTION);
        entity.setManageNumber(manageNo);
        entity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_NOT_CREATE);
        wmsLinkingFileRepository.save(entity);
        return entity;
    }
}
