package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.MakerReturnLinkingCreateCsvFileComponent;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnLinkingCsvFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMakerReturnLinkingCsvFileRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * メーカー返品指示データファイル作成スケジュールのコンポーネント.
 */
@Component
public class MakerReturnFileLinkingScheduleComponent
extends LinkingScheduleComponent<ExtendedTMakerReturnLinkingCsvFileEntity> {

    @Autowired
    private MakerReturnLinkingCreateCsvFileComponent makerReturnLinkingCreateCsvFileComponent;

    @Autowired
    private ExtendedTMakerReturnLinkingCsvFileRepository exTMakerReturnLinkingCsvFileRepository;

    @Autowired
    private TMakerReturnRepository tMakerReturnRepository;
    // PRD_0089 add SIT start
    @Autowired
    private TPurchaseRepository purchaseRepository;

    //PRD_0126#9251 del JFE start
//    private static final BigDecimal ZERO = new BigDecimal("0");
    //PRD_0126#9251 del JFE end

    // PRD_0089 del SIT start
    ///** 入荷場所接尾辞. */
    //private static final String ARRIVAL_PLACE_SUFFIX = "B";
    // PRD_0089 del SIT end

    private static final Integer addInstructNumber = 400000;
    // PRD_0089 add SIT end
    @Override
    BusinessType getBusinessType() {
        // PRD_0089 add SIT start
        // return BusinessType.RETURN_INSTRUCTION;
        return BusinessType.PURCHASE_INSTRUCTION;
        // PRD_0089 add SIT end
    }

    @Override
    List<ExtendedTMakerReturnLinkingCsvFileEntity> generateByWmsLinkingFileIdEntities(final BigInteger wmsLinkingFileId) {
        return exTMakerReturnLinkingCsvFileRepository.findByWmsLinkingFileId(wmsLinkingFileId);
    }

    @Override
    void appendValidateCheck(
            final List<ExtendedTMakerReturnLinkingCsvFileEntity> entities,
            final BigInteger wmsLinkingFileId) {
        // 倉庫連携ファイルIDに該当するメーカー返品情報の件数取得
        final int cnt = tMakerReturnRepository.countByWmsLinkingFileId(wmsLinkingFileId);
        // 送信対象のレコード数との一致を確認(一致しない場合はエラー)
        if (cnt != entities.size()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("getCount.")
                    .message("MakerReturn record number is insufficient.")
                    .value("wmsLinkingFileId", wmsLinkingFileId)
                    .build()));
        }
    }

    @Override
    File createInstructionFile(final List<ExtendedTMakerReturnLinkingCsvFileEntity> entities) throws Exception {
        return makerReturnLinkingCreateCsvFileComponent.createCsvFile(entities);
    }

    /**
     * 仕入情報作成.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void createPurchases(final TWmsLinkingFileEntity wmsLinkingFileEntity, final BigInteger userId) {
        final List<ExtendedTMakerReturnLinkingCsvFileEntity> exTMakerReturnLinkingCsvFileEntities
            = exTMakerReturnLinkingCsvFileRepository.findByWmsLinkingFileId(wmsLinkingFileEntity.getId());

        for(ExtendedTMakerReturnLinkingCsvFileEntity exTMakerReturnLinkingCsvFileEntity : exTMakerReturnLinkingCsvFileEntities ) {
            final TPurchaseEntity purchaseEntity = new TPurchaseEntity();

            String arrivalPlace = exTMakerReturnLinkingCsvFileEntity.getLogisticsCode();
            if(arrivalPlace != null) {
                arrivalPlace = arrivalPlace.substring(0, 1);
            }

            BigDecimal unitPrice = exTMakerReturnLinkingCsvFileEntity.getUnitPrice(); // 単価;
            //PRD_0126#9251 del JFE start
//            final BigDecimal nonConformingProductUnitPrice = exTMakerReturnLinkingCsvFileEntity.getNonConformingProductUnitPrice();
//            if (nonConformingProductUnitPrice != null && ZERO.compareTo(nonConformingProductUnitPrice) == -1)  {
//                // 発注情報.B級品単価 > 0
//                // PRD_0089 del SIT start
//                //arrivalPlace = arrivalPlace + ARRIVAL_PLACE_SUFFIX;
//                // PRD_0089 del SIT end
//                unitPrice = nonConformingProductUnitPrice;
//            }
            //PRD_0126#9251 del JFE end
            // 倉庫連携ファイルID
            purchaseEntity.setWmsLinkingFileId(exTMakerReturnLinkingCsvFileEntity.getWmsLinkingFileId());
            // 日付
            purchaseEntity.setSqManageDate(exTMakerReturnLinkingCsvFileEntity.getManageDate());
            //PRD_0109 #7811 mod JFE start
            // 時間
            //purchaseEntity.setSqManageAt(exTMakerReturnLinkingCsvFileEntity.getManageDate());
            purchaseEntity.setSqManageAt(exTMakerReturnLinkingCsvFileEntity.getManageAt());
            //PRD_0109 #7811 mod JFE end
            // 管理No
            purchaseEntity.setSqManageNumber(exTMakerReturnLinkingCsvFileEntity.getManageNumber());
            // 行No
            purchaseEntity.setLineNumber(exTMakerReturnLinkingCsvFileEntity.getSequence());
            // データ種別
            purchaseEntity.setDataType(PurchaseDataType.SR);
            // 仕入区分
            purchaseEntity.setPurchaseType(PurchaseType.RETURN_PURCHASE);
            // 入荷場所
            purchaseEntity.setArrivalPlace(arrivalPlace);
            // 入荷店舗
            purchaseEntity.setArrivalShop(exTMakerReturnLinkingCsvFileEntity.getShpcd());
            // 仕入先
            purchaseEntity.setSupplierCode(exTMakerReturnLinkingCsvFileEntity.getSupplierCode());
            // 製品工場
            purchaseEntity.setMdfMakerFactoryCode(null);
            // 入荷日
            purchaseEntity.setArrivalAt(exTMakerReturnLinkingCsvFileEntity.getReturnAt());
            // 計上日
            purchaseEntity.setRecordAt(null);
            // 仕入相手伝票No
            purchaseEntity.setMakerVoucherNumber(null);
            // 仕入伝票No
            purchaseEntity.setPurchaseVoucherNumber(exTMakerReturnLinkingCsvFileEntity.getVoucherNumber());
            // 仕入伝票行
            purchaseEntity.setPurchaseVoucherLine(exTMakerReturnLinkingCsvFileEntity.getVoucherLine());
            // 品番ID
            purchaseEntity.setPartNoId(exTMakerReturnLinkingCsvFileEntity.getPartNoId());
            // 品番
            purchaseEntity.setPartNo(exTMakerReturnLinkingCsvFileEntity.getPartNo());
            // 色
            purchaseEntity.setColorCode(exTMakerReturnLinkingCsvFileEntity.getColorCode());
            // サイズ
            purchaseEntity.setSize(exTMakerReturnLinkingCsvFileEntity.getSize());
            // 入荷数
            purchaseEntity.setArrivalCount(exTMakerReturnLinkingCsvFileEntity.getReturnLot());
            // 入荷確定数
            purchaseEntity.setFixArrivalCount(null);
            // 良品・不良品区分
            purchaseEntity.setNonConformingProductType(BooleanType.TRUE);
            // 指示番号
            String instructNumber = exTMakerReturnLinkingCsvFileEntity.getInstructNumber();
            if (instructNumber != null) {
                instructNumber = String.valueOf(Integer.parseInt(instructNumber) + addInstructNumber);
            }
            purchaseEntity.setInstructNumber(instructNumber);
            // 指示番号行
            purchaseEntity.setInstructNumberLine(exTMakerReturnLinkingCsvFileEntity.getInstructNumberLine());
            // 発注ID
            purchaseEntity.setOrderId(exTMakerReturnLinkingCsvFileEntity.getOrderId());
            // 発注番号
            purchaseEntity.setOrderNumber(exTMakerReturnLinkingCsvFileEntity.getOrderNumber());
            // 引取回数
            purchaseEntity.setPurchaseCount(null);
            // 課コード
            purchaseEntity.setDivisionCode(null);
            // 仕入単価
            purchaseEntity.setPurchaseUnitPrice(unitPrice.intValue());
            // 納品ID
            purchaseEntity.setDeliveryId(null);
            // LG送信区分　1:LG送信指示済
            purchaseEntity.setLgSendType(LgSendType.INSTRUCTION);
            // 会計連携ステータス　0:ファイル未処理
            purchaseEntity.setAccountLinkingStatus(FileInfoStatusType.FILE_UNPROCESSED);
            //登録ユーザID
            purchaseEntity.setCreatedUserId(userId);
            //更新ユーザID
            purchaseEntity.setUpdatedUserId(userId);

            purchaseRepository.save(purchaseEntity);
        }
    }
}
