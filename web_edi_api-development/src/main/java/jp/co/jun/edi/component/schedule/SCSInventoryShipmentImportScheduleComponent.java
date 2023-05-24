package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.SCSInventoryShipmentInstructionFileComponent;
import jp.co.jun.edi.component.model.InventoryShipmentImportCsvModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.TInventoryShipmentEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TInventoryShipmentRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.InstructorSystemType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.PsType;
import jp.co.jun.edi.type.ShippingCategoryType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;


/**
 * SCS・ZOZO 在庫出荷指示データ取込スケジュールのコンポーネント.
 */
@Component
public class SCSInventoryShipmentImportScheduleComponent {

    @Autowired
    private SCSInventoryShipmentInstructionFileComponent scsInventoryShipmentInstructionFileComponent;

    @Autowired
    private TInventoryShipmentRepository inventoryShipmentRepository;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    private static final String NON_CONFORMING_PRODUCT_TYPE = "B";
    private static final String ALLOCATION_PRODUCT = "1";
    private static final String ALLOCATION_STORE = "2";
    private static final String PROPER = "P";
    private static final String SALE = "S";
    private static final List<String> TARGET_INSTRUCTOR_SYSTEM_TYPES = Arrays.asList("00", "01", "02");

    /**
     * SCS・ZOZO 在庫出荷指示ファイル(取寄データ)ごとに処理実行.
     * ・S3よりCSVファイルをダウンロード
     * ・CSVファイルの読み込み
     * ・在庫出荷指示情報テーブルの更新
     * ・倉庫連携ファイル情報の更新
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param userId システムユーザID
     * @throws Exception 例外
     */
      @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
      public void executeByInventoryConfirmFile(final TWmsLinkingFileEntity wmsLinkingFileEntity,
              final BigInteger userId) throws Exception {
          // SCS・ZOZO 在庫出荷指示ファイルをS3よりダウンロード
          final File inventoryConfirmFile = scsInventoryShipmentInstructionFileComponent.downloadCsvFile(wmsLinkingFileEntity);

          // CSVファイルを読み込み、Modelに変換
          final List<InventoryShipmentImportCsvModel> inventoryShipmentImportCsvModel = scsInventoryShipmentInstructionFileComponent
                  .readCsvData(inventoryConfirmFile);

          // inventoryShipmentImportCsvModelが空の場合は取込エラー
          if (CollectionUtils.isEmpty(inventoryShipmentImportCsvModel)) {
              throw new ResourceNotFoundException(ResultMessages.warning().add(
                      MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                              .message("inventory confirm file no data.")
                              .value("s3_key", wmsLinkingFileEntity.getS3Key())
                              .build()));
          }

          // DB更新
          insertInventory(inventoryShipmentImportCsvModel, userId);
          // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
          updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
      }

      /**
       * SCS・ZOZO 在庫出荷指示情報の取込.
       *
       * @param inventoryShipmentImportCsvModels SCS・ZOZO在庫出荷指示データのリスト
       * @param userId 登録ユーザID
       * @throws Exception 例外
       */
      private void insertInventory(final List<InventoryShipmentImportCsvModel> inventoryShipmentImportCsvModels,
              final BigInteger userId) throws Exception {

          final List<TInventoryShipmentEntity> entities = inventoryShipmentImportCsvModels.stream()
                  .map(model -> toEntity(model, userId)).collect(Collectors.toList());
          inventoryShipmentRepository.saveAll(entities);
      }

      /**
       * Entityに変換.
       *
       * @param model SCS・ZOZO在庫出荷指示データのリスト.
       * @param userId 登録ユーザID.
       * @return TInventoryShipmentEntity.
       */
      private TInventoryShipmentEntity toEntity(final InventoryShipmentImportCsvModel model, final BigInteger userId) {

         final TInventoryShipmentEntity entity = new TInventoryShipmentEntity();

         entity.setManageDate(DateUtils.stringToDate(model.getManageDate()));
         entity.setManageAt(DateUtils.stringToTime(model.getManageAt()));
         entity.setManageNumber(model.getManageNumber());
         entity.setSequence(Integer.parseInt(model.getSequence()));
         entity.setInstructorSystem(getInstructorSystemType(model.getShopCode()));
         if (StringUtils.equals(model.getShippingCategory(), ALLOCATION_PRODUCT)) {
             entity.setShippingCategory(ShippingCategoryType.ALLOCATION_PRODUCT);
         } else if (StringUtils.equals(model.getShippingCategory(), ALLOCATION_STORE)) {
             entity.setShippingCategory(ShippingCategoryType.ALLOCATION_STORE);
         }
         entity.setCargoPlace(StringUtils.substring(model.getCargoPlace(), 0, 1));
         if (StringUtils.equals(StringUtils.substring(model.getCargoPlace(), 1, 2), NON_CONFORMING_PRODUCT_TYPE)) {
             entity.setNonConformingProductType(BooleanType.TRUE);
         } else {
             entity.setNonConformingProductType(BooleanType.FALSE);
         }
         entity.setAllocationRank(Integer.parseInt(model.getAllocationRank()));
         entity.setShopCode(model.getShopCode());
         entity.setCargoAt(DateUtils.stringToDate(model.getCargoAt()));
         entity.setPartNo(model.getPartNo());
         entity.setColorCode(model.getColorCode());
         entity.setSize(model.getSize());
         entity.setShippingInstructionLot(Integer.parseInt(model.getShippingInstructionLot()));
         entity.setFixShippingInstructionLot(0);
         entity.setRetailPrice(generateIntegerToBigDecimal(Integer.parseInt(model.getRetailPrice())));
         entity.setRate(generateIntegerToBigDecimal(Integer.parseInt(model.getRate())));
         entity.setWholesalePrice(generateIntegerToBigDecimal(Integer.parseInt(model.getWholesalePrice())));
         if (StringUtils.equals(model.getProperSaleType(), PROPER)) {
             entity.setProperSaleType(PsType.PROPER);
         } else if (StringUtils.equals(model.getProperSaleType(), SALE)) {
             entity.setProperSaleType(PsType.SALE);
         }
         entity.setSaleRetailPrice(generateIntegerToBigDecimal(Integer.parseInt(model.getSaleRetailPrice())));
         entity.setOffPercent(generateIntegerToBigDecimal(Integer.parseInt(model.getOffPercent())));
         entity.setInstructionManageUserCode(model.getInstructionManageUserCode());
         entity.setInstructionManageUserDate(DateUtils.stringToDate(model.getInstructionManageUserDate()));
         entity.setInstructionManageUserAt(DateUtils.stringToTime(model.getInstructionManageUserAt()));
         entity.setInstructionManageNumber(model.getInstructionManageNumber());
         entity.setInstructionManageShopCode(model.getInstructionManageShopCode());
         entity.setInstructionManagePartNo(model.getInstructionManagePartNo());
         entity.setInstructionManageDivisionCode(model.getInstructionManageDivisionCode());
         entity.setLgSendType(LgSendType.NO_INSTRUCTION);
         entity.setCreatedUserId(userId);
         entity.setUpdatedUserId(userId);

         return entity;
       }

      /**
       * 店舗コードから指示元システムを取得.
       * @param tenpo 店舗コード
       * @return InstructorSystemType
       */
      private InstructorSystemType getInstructorSystemType(final String tenpo) {

          final MCodmstEntity entity = mCodmstRepository.findByTblidAndCode1sAndItem1(MCodmstTblIdType.CONVERSION_SHOP_CODE.getValue(),
                                                                                       TARGET_INSTRUCTOR_SYSTEM_TYPES,
                                                                                       tenpo).
                  orElse(new MCodmstEntity());

          final Integer value = Integer.parseInt(StringUtils.left(entity.getCode1(), 2));

          return InstructorSystemType.convertToType(value);
      }

    /**
       * Integer型をBigDecimal型に変換する.
       * 値がない場合はnullを返却
       *
       * @param value Integer型の値
       * @return BigDecimal型の値
       */
      public BigDecimal generateIntegerToBigDecimal(final Integer value) {
          if (value == null) {
              return null;
          } else {
              return BigDecimal.valueOf(value);
          }
      }

      /**
       * 倉庫連携ファイル情報のWMS連携ステータスを更新する.
       *
       * @param type WMS連携ステータス
       * @param wmsLinkingFileEntity 倉庫連携ファイル情報
       * @param updatedUserId 更新ユーザID
       * @return 更新件数
       */
      @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
      public int updateWmsLinkingStatus(final WmsLinkingStatusType type,
              final TWmsLinkingFileEntity wmsLinkingFileEntity,
              final BigInteger updatedUserId) {
          return tWmsLinkingFileRepository.updateWmsLinkingStatus(type, wmsLinkingFileEntity.getId(), updatedUserId);
      }

      /**
       * 倉庫連携ファイル情報のWMS連携ステータスをファイル取込中に更新する.
       *
       * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
       * @param updatedUserId 更新ユーザID
       */
      @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
      public void updateWmsLinkingStatusFileImporting(final List<TWmsLinkingFileEntity> wmsLinkingFiles,
              final BigInteger updatedUserId) {
          final List<BigInteger> ids = wmsLinkingFiles.stream().map(entity -> entity.getId()).collect(Collectors.toList());
          tWmsLinkingFileRepository.updateWmsLinkingStatusByIds(WmsLinkingStatusType.FILE_IMPORTING, ids, updatedUserId);
      }
}
