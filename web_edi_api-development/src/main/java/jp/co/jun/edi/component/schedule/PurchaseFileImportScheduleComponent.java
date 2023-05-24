package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.MakerReturnComponent;
import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.component.PurchaseConfirmComponent;
import jp.co.jun.edi.component.PurchaseLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.PurchaseLinkingImportFromSqCsvFileComponent;
import jp.co.jun.edi.component.model.PurchaseImportModel;
import jp.co.jun.edi.component.model.PurchaseLinkingImportCsvModel;
import jp.co.jun.edi.component.model.PurchaseLinkingImportFromSqCsvModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TPurchasesVoucherEntity;
import jp.co.jun.edi.entity.TReturnVoucherEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.repository.TPurchasesVoucherRepository;
import jp.co.jun.edi.repository.TReturnVoucherRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.DivisionCodeType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.BooleanUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 仕入確定データファイル取込スケジュールのコンポーネント.
 */
@Component
public class PurchaseFileImportScheduleComponent {

    @Autowired
    private PurchaseConfirmComponent purchaseConfirmComponent;

    @Autowired
    private PurchaseLinkingImportCsvFileComponent purchaseLinkingImportCsvFileComponent;

    // PRD_0071 add SIT start
    @Autowired
    private PurchaseLinkingImportFromSqCsvFileComponent purchaseLinkingImportFromSqCsvFileComponent;
    // PRD_0071 add SIT end

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private TItemRepository itemRepository;

    // PRD_0073 add SIT start
    @Autowired
    private MakerReturnComponent makerReturnComponent;

    @Autowired
    private TReturnVoucherRepository returnVoucherRepository;

    //PRD_0134 #10654 add JEF start
    @Autowired
    private TPurchasesVoucherRepository purchasesVoucherRepository;

    @Autowired
    private PurchaseComponent purchaseComponent;
    //PRD_0134 #10654 add JEF end

    @Autowired
    private TMakerReturnRepository makerReturnRepository;
    // PRD_0073 add SIT end


    // PRD_0071 add SIT start
    /** SQ CSVファイル先頭2文字. */
    private static final String SQ = "SQ";
    // PRD_0071 add SIT end

    /**
     * 仕入確定CSVファイルごとに処理実行.
     * ・S3よりCSVファイルをダウンロード
     * ・CSVファイルの読み込み
     * ・出荷関連テーブルの更新
     * ・倉庫連携ファイル情報の更新
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeByPurchaseConfirmFile(
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) throws Exception {
        // 仕入確定CSVファイルをS3よりダウンロード
        final File purchaseConfirmFile = purchaseLinkingImportCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // PRD_0071 mod SIT start
        //// CSVファイルを読み込み、Modelに変換
        //final List<PurchaseLinkingImportCsvModel> purchaseImportCsvModels = purchaseLinkingImportCsvFileComponent.readCsvData(purchaseConfirmFile);
        //
        // purchaseImportCsvModelsが空の場合は取込エラー
        //if (CollectionUtils.isEmpty(purchaseImportCsvModels)) {
        //    throw new ResourceNotFoundException(ResultMessages.warning().add(
        //            MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
        //                    .message("purchase confirm file does not exist.")
        //                    .value("s3_key", wmsLinkingFileEntity.getS3Key())
        //                    .build()));
        //}
        //
        // DB更新
        //updatePurchaseConfirmData(purchaseImportCsvModels, wmsLinkingFileEntity, userId);

        // ファイル名称を取得（先頭2文字）
        final String fileName = purchaseConfirmFile.getName().substring(0, 2);

        // CSVファイル読み込みModel変換用変数
        final List<PurchaseLinkingImportFromSqCsvModel> purchaseImportFromSqCsvModels;
        final List<PurchaseLinkingImportCsvModel> purchaseImportCsvModels;

        if(fileName.equals(SQ))
        {
            // SQ用のCSVファイル読み込み、Model変換処理
            purchaseImportFromSqCsvModels = purchaseLinkingImportFromSqCsvFileComponent.readCsvData(purchaseConfirmFile);
            purchaseImportCsvModels = null;

            // purchaseImportCsvModelsが空の場合は取込エラー
            if (CollectionUtils.isEmpty(purchaseImportFromSqCsvModels)) {
                throw new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                                .message("purchase confirm file does not exist.")
                                .value("s3_key", wmsLinkingFileEntity.getS3Key())
                                .build()));
            }
        } else {

            // 通常CSVファイルを読み込み、Modelに変換
            purchaseImportCsvModels = purchaseLinkingImportCsvFileComponent.readCsvData(purchaseConfirmFile);
            purchaseImportFromSqCsvModels = null;

            // purchaseImportCsvModelsが空の場合は取込エラー
            if (CollectionUtils.isEmpty(purchaseImportCsvModels)) {
                throw new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                                .message("purchase confirm file does not exist.")
                                .value("s3_key", wmsLinkingFileEntity.getS3Key())
                                .build()));
            }
        }

        // DB更新
        updatePurchaseConfirmData(purchaseImportCsvModels, purchaseImportFromSqCsvModels, fileName, wmsLinkingFileEntity, userId);
        // PRD_0071 mod SIT end

        // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
        updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
    }

    /**
     * 仕入確定関連のデータ更新.
     * ・仕入情報 t_purchase
     * ・納品明細情報 t_delivery_detail
     * ・納品SKU t_delivery_sku
     * ・発注情報 t_order
     * ・発注SKU t_order_sku
     *
     * @param purchaseImportCsvModels 仕入確定データのリスト
     * @param purchaseImportFromSqCsvModels 仕入確定データ（SQ）のリスト
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param userId システムユーザID
     */
    private void updatePurchaseConfirmData(
            final List<PurchaseLinkingImportCsvModel> purchaseImportCsvModels,
            // PRD_0071 add SIT end
            final List<PurchaseLinkingImportFromSqCsvModel> purchaseImportFromSqCsvModels,
            final String fileName,
            // PRD_0071 add SIT end
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) {

        // 計上日
        final Date accountingDate = new Date();
        //PRD_0134 #10654 add JEF start
        List<TPurchaseEntity> tPurchaseEntitys = new ArrayList<>();
        //PRD_0134 #10654 add JEF end
        // PRD_0073 add SIT start
        List<MakerReturnModel> makerReturnModels = new ArrayList<MakerReturnModel>();
        // PRD_0073 add SIT end

        // PRD_0071 add SIT start
        if(fileName.equals(SQ))
        {
            // SQ用のCSVファイル読み込み時 仕入確定関連のデータ更新.

            // 仕入情報の更新
            updatePurchasesFromSq(purchaseImportCsvModels, purchaseImportFromSqCsvModels, fileName, wmsLinkingFileEntity, accountingDate, userId);

            // PRD_0073 add SIT start
            // 返品情報の更新
            for (final PurchaseLinkingImportFromSqCsvModel importModel : purchaseImportFromSqCsvModels) {
                // メーカー返品情報更新
                // PRD_0178 #10654 mod JEF start
                //if (importModel.getPurchaseType().equals("3"))
                if (importModel.getPurchaseType().equals("3") &&
                        !(importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("18")) &&
                        !(importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("19")))
                // PRD_0178 #10654 mod JEF end
                {
                    final TMakerReturnEntity makerReturnEntity = updateTMakerReturnFromSq(importModel, userId);

                    if (Objects.nonNull(makerReturnEntity.getId()))
                    {
                        MakerReturnModel makerReturnModel = new MakerReturnModel();
                        makerReturnModel.setOrderId(makerReturnEntity.getOrderId());
                        makerReturnModel.setVoucherNumber(makerReturnEntity.getVoucherNumber());
                        makerReturnModels.add(makerReturnModel);
                    }
                    //PRD_0134 #10654 add JEF start
                    // PRD_0178 #10654 mod JEF start
                    //else {
                }
                    if (importModel.getPurchaseType().equals("1") || importModel.getPurchaseType().equals("6") || importModel.getPurchaseType().equals("7") ||
                            (importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("19")) ||
                            (importModel.getPurchaseType().equals("9") && importModel.getArrivalPlace().equals("19"))) {
                    // PRD_0178 #10654 mod JEF end
                    	TPurchaseEntity tPurchaseEntity =findPurchaseByManageNumAndVoucherNumAndVoucherLine(null, importModel, fileName, wmsLinkingFileEntity, accountingDate);
                    	tPurchaseEntitys.add(tPurchaseEntity);

                    }
                    //PRD_0134 #10654 add JEF end
                // PRD_0178 #10654 del JEF start
                //}
                // PRD_0178 #10654 del JEF end
            }
            // PRD_0073 add SIT end

        } else {
        // PRD_0071 add SIT end

            // SQ用のCSVファイル読み込み時 仕入確定関連のデータ更新

            // 仕入情報の更新
            // PRD_0071 mod SIT start
            //final List<PurchaseImportModel> purchases = updatePurchases(purchaseImportCsvModels, wmsLinkingFileEntity,
            //                                                         accountingDate, userId);
            final List<PurchaseImportModel> purchases = updatePurchases(purchaseImportCsvModels, purchaseImportFromSqCsvModels, fileName, wmsLinkingFileEntity,
                                                                     accountingDate, userId);
            // PRD_0071 mod SIT end

            // 納品明細情報更新対象抽出
            final List<PurchaseImportModel> filterPurchaseImportsForDelivery = purchases.stream()
                    .filter(purchaseConfirmComponent::isDeliveryUpdateTarget)
                    .collect(Collectors.toList());

            // 納品依頼明細の更新
            updateDeliveryDetails(filterPurchaseImportsForDelivery, userId);

            // 発注情報更新対象抽出
            final List<PurchaseImportModel> filterPurchaseImportsForOrder = purchases.stream()
                .filter(purchaseConfirmComponent::isOrderUpdateTarget)
                .collect(Collectors.toList());

            // 発注情報の更新
            updateOrders(filterPurchaseImportsForOrder, accountingDate, userId);

            // 納品SKUと発注SKUの更新
            updateDeliverySkuAndOrderSku(filterPurchaseImportsForOrder, accountingDate, userId);

            // PRD_0073 add SIT start
            // 返品情報の更新
            for (final PurchaseLinkingImportCsvModel importModel : purchaseImportCsvModels) {
                // メーカー返品情報更新
                // PRD_0178 #10654 mod JEF start
                //if (importModel.getPurchaseType().equals("3"))
                if (importModel.getPurchaseType().equals("3") &&
                        !(importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("18")) &&
                        !(importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("19")))
                // PRD_0178 #10654 mod JEF end
                {
                    final TMakerReturnEntity makerReturnEntity = updateTMakerReturn(importModel, userId);

                    if (Objects.nonNull(makerReturnEntity.getId()))
                    {
                        MakerReturnModel makerReturnModel = new MakerReturnModel();
                        makerReturnModel.setOrderId(makerReturnEntity.getOrderId());
                        makerReturnModel.setVoucherNumber(makerReturnEntity.getVoucherNumber());
                        makerReturnModels.add(makerReturnModel);
                    }
                }
                    //PRD_0134 #10654 add JEF start
                    // PRD_0178 #10654 mod JEF start
                    //else {
                    if (importModel.getPurchaseType().equals("1") || importModel.getPurchaseType().equals("6") || importModel.getPurchaseType().equals("7") ||
                            (importModel.getPurchaseType().equals("3") && importModel.getArrivalPlace().equals("19")) ||
                            (importModel.getPurchaseType().equals("9") && importModel.getArrivalPlace().equals("19"))) {
                    // PRD_0178 #10654 mod JEF end
                    	TPurchaseEntity tPurchaseEntity =findPurchaseByManageNumAndVoucherNumAndVoucherLine(importModel, null, fileName, wmsLinkingFileEntity, accountingDate);
                    	tPurchaseEntitys.add(tPurchaseEntity);

                    }
                    //PRD_0134 #10654 add JEF end
            }
            // PRD_0073 add SIT end
        }

        //PRD_0134 #10654 add JEF start
        if (tPurchaseEntitys.size() != 0) {
        	purchaseComponent.distinctPurchaseByOrderIsAndVoucherNo(tPurchaseEntitys)
        	.forEach(model ->{
        		//仕入伝票管理情報登録
                //PRD_0158 #10181 JFE mod start
//        		if(model.getPurchaseVoucherNumber() != null || model.getOrderId() != null) {
        		if(model.getPurchaseVoucherNumber() != null && model.getOrderId() != null) {
                //PRD_0158 #10181 JFE mod end
        			insertPurchasesVoucher(model.getPurchaseVoucherNumber(), model.getPurchaseVoucherLine(), model.getOrderId(), userId,model.getSupplierCode());
        		}
        	});
        }
        //PRD_0134 #10654 add JEF end

        // PRD_0073 add SIT start
        // 返品伝票管理情報 重複除去
        if (makerReturnModels.size() != 0)
        {
            makerReturnComponent.distinctMakerReturnByOrderIsAndVoucherNo(makerReturnModels)
            .forEach(model ->{
                // 返品伝票管理情報情報登録
                insertReturnsVoucher(model.getVoucherNumber(), model.getOrderId(), userId);
            });
        }
        // PRD_0073 add SIT end
    }

    /**
     * 仕入情報更新.
     * @param purchaseImportCsvModels 仕入確定データのリスト
     * @param purchaseImportFromSqCsvModels 仕入確定データ（SQ）のリスト
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     * @return 取込用仕入情報リスト
     */
    private List<PurchaseImportModel> updatePurchases(
            final List<PurchaseLinkingImportCsvModel> purchaseImportCsvModels,
            // PRD_0071 add SIT end
            final List<PurchaseLinkingImportFromSqCsvModel> purchaseImportFromSqCsvModels,
            final String fileName,
            // PRD_0071 add SIT end
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        return purchaseImportCsvModels.stream()
                .map(purchaseImportCsvModel -> {
                    // PRD_0071 mod SIT start
                    //final TPurchaseEntity purchaseEntity = upsertPurchase(purchaseImportCsvModel, wmsLinkingFileEntity,
                    //                                                        accountingDate, updatedUserId);
                    final TPurchaseEntity purchaseEntity = upsertPurchase(purchaseImportCsvModel, null, fileName, wmsLinkingFileEntity,accountingDate, updatedUserId);
                    // PRD_0071 mod SIT end
                    return generatePurchaseImportModel(purchaseEntity, purchaseImportCsvModel);
                }).collect(Collectors.toList());
    }

    // PRD_0071 add SIT start
    /**
     * 仕入情報更新.(SQデータ用)
     * @param purchaseImportCsvModels 仕入確定データのリスト
     * @param purchaseImportFromSqCsvModels 仕入確定データ（SQ）のリスト
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    private void updatePurchasesFromSq(
            final List<PurchaseLinkingImportCsvModel> purchaseImportCsvModels,
            final List<PurchaseLinkingImportFromSqCsvModel> purchaseImportFromSqCsvModels,
            final String fileName,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        purchaseImportFromSqCsvModels.stream().map(purchaseImportFromSqCsvModel -> {
            final TPurchaseEntity purchaseEntity =  upsertPurchase(null, purchaseImportFromSqCsvModel, fileName, wmsLinkingFileEntity,
                                                                            accountingDate, updatedUserId);
                return generatePurchaseImportFromSqModel(purchaseEntity, purchaseImportFromSqCsvModel);
                }).collect(Collectors.toList());
    }
    // PRD_0071 add SIT end

    // PRD_0073 add SIT start
    /**
     * メーカー返品情報更新.
     *
     * @param model 確定データ
     * @param updatedUserId ID
     * @return 更新情報
     */
    private TMakerReturnEntity updateTMakerReturn(
            final PurchaseLinkingImportCsvModel model,
            final BigInteger updatedUserId) {

//      #6783 mod JFE start
        // メーカー返品情報取得
        TMakerReturnEntity makerReturnEntity =
                makerReturnRepository.findByManageColumnAndSequence(
                        model.getPurchaseVoucherNumber(),
                        NumberUtils.createInteger(model.getPurchaseVoucherLine()))
                .orElse(new TMakerReturnEntity());
//      #6783 mod JFE end

        // 返品確定数、更新ユーザーIDをセット
        if (Objects.nonNull(makerReturnEntity.getId()))
        {
            makerReturnEntity.setFixReturnLot(NumberUtils.createInteger(model.getFixArrivalCount()));
            makerReturnEntity.setUpdatedUserId(updatedUserId);
            makerReturnRepository.save(makerReturnEntity);
        }
        return makerReturnEntity;
    }
    // PRD_0073 add SIT end

    // PRD_0073 add SIT start
    /**
     * メーカー返品情報更新.(SQデータ用)
     *
     * @param model 確定データ
     * @param updatedUserId ID
     * @return 更新情報
     */
    private TMakerReturnEntity updateTMakerReturnFromSq(
            final PurchaseLinkingImportFromSqCsvModel model,
            final BigInteger updatedUserId) {

//      #6783 mod JFE start
        // メーカー返品情報取得
        TMakerReturnEntity makerReturnEntity =
                makerReturnRepository.findByManageColumnAndSequence(
                        model.getPurchaseVoucherNumber(),
                        NumberUtils.createInteger(model.getPurchaseVoucherLine()))
                .orElse(new TMakerReturnEntity());
//      #6783 mod JFE end

        // 返品確定数、更新ユーザーIDをセット
        if (Objects.nonNull(makerReturnEntity.getId()))
        {
            makerReturnEntity.setFixReturnLot(NumberUtils.createInteger(model.getFixArrivalCount()));
            makerReturnEntity.setUpdatedUserId(updatedUserId);
            makerReturnRepository.save(makerReturnEntity);
        }
        return makerReturnEntity;
    }
    // PRD_0073 add SIT end

    //PRD_0134 #10654 add JEF start
    /**
     * 返品伝票管理情報情報登録.
     * @param voucherNumber 伝票場合
     * @param purchaseVoucherLine 仕入伝票行
     * @param orderId 発注ID
     */
    private void insertPurchasesVoucher(
            final String purchaseVoucherNumber,
            final Integer purchaseVoucherLine,
            final BigInteger orderId,
            final BigInteger userId,
            final String supplierCode) {
        final TPurchasesVoucherEntity entity = new TPurchasesVoucherEntity();

        entity.setPurchaseVoucherNumber(purchaseVoucherNumber);
        entity.setPurchaseVoucherLine(purchaseVoucherLine);
        entity.setOrderId(orderId);
        entity.setStatus(SendMailStatusType.UNPROCESSED);
        entity.setCreatedUserId(userId);
        entity.setSupplierCode(supplierCode);
        entity.setUpdatedUserId(userId);

        purchasesVoucherRepository.save(entity);
    }
    //PRD_0134 #10654 add JEF end

    // PRD_0073 add SIT start
    /**
     * 返品伝票管理情報情報登録.
     * @param voucherNumber 伝票場合
     * @param orderId 発注ID
     */
    private void insertReturnsVoucher(
            final String voucherNumber,
            final BigInteger orderId,
            final BigInteger userId) {
        final TReturnVoucherEntity entity = new TReturnVoucherEntity();

        entity.setVoucherNumber(voucherNumber);
        entity.setOrderId(orderId);
        entity.setStatus(SendMailStatusType.UNPROCESSED);
        entity.setCreatedUserId(userId);
        entity.setUpdatedUserId(userId);

        returnVoucherRepository.save(entity);
    }
    // PRD_0073 add SIT end

    /**
     * 仕入情報登録・更新.
     * modelに対応するデータが無い場合は登録を行う.
     * @param purchaseImportCsvModel 仕入確定データ
     * @param purchaseImportFromSqCsvModel 仕入確定データ（SQ）
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     * @return 登録・更新した仕入情報
     */
    private TPurchaseEntity upsertPurchase(
            final PurchaseLinkingImportCsvModel purchaseImportCsvModel,
            // PRD_0071 add SIT end
            final PurchaseLinkingImportFromSqCsvModel purchaseImportFromSqCsvModel,
            final String fileName,
            // PRD_0071 add SIT end
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        // PRD_0071 mod SIT start
        //final TPurchaseEntity purchaseEntity = findPurchaseByManageNumAndVoucherNumAndVoucherLine(purchaseImportCsvModel, wmsLinkingFileEntity, accountingDate);
        //
        // 計上日
        //purchaseEntity.setRecordAt(accountingDate);
        //
        //purchaseEntity.setFixArrivalCount(Integer.parseInt(purchaseImportCsvModel.getFixArrivalCount()));
        final TPurchaseEntity purchaseEntity = findPurchaseByManageNumAndVoucherNumAndVoucherLine(purchaseImportCsvModel, purchaseImportFromSqCsvModel, fileName, wmsLinkingFileEntity, accountingDate);
        final String purchaseType;
        final String arrivalPlace;
        final String recordAt;
        final String divisionCode;
        final String partNo;
        final String purchaseUnitPrice;
        final String fixArrivalCount;

        if(fileName.equals(SQ))
        {
            // SQ用のCSVファイル読み込み時
            purchaseType = purchaseImportFromSqCsvModel.getPurchaseType();
            arrivalPlace = purchaseImportFromSqCsvModel.getArrivalPlace();
            recordAt     = purchaseImportFromSqCsvModel.getRecordAt();
            divisionCode = purchaseImportFromSqCsvModel.getDivisionCode();
            partNo       = purchaseImportFromSqCsvModel.getPartNo();
            purchaseUnitPrice = purchaseImportFromSqCsvModel.getPurchaseUnitPrice();
            fixArrivalCount = purchaseImportFromSqCsvModel.getFixArrivalCount();
            // 計上日
            if((arrivalPlace.equals("18") || arrivalPlace.equals("19"))) {
                purchaseEntity.setRecordAt(DateUtils.stringToDate(recordAt));
                //// 各種テーブル更新処理引数用に計上日を保持
                //localAccountDate = DateUtils.stringToDate(recordAt);
            } else {
                purchaseEntity.setRecordAt(accountingDate);
            }

            // 課コード
            //PRD_0158 #10181 JFE mod start
//            if(Integer.parseInt(purchaseType) == 9) {
//                purchaseEntity.setDivisionCode(divisionCode);
//            }
            IntStream stream = IntStream.of(2,4,5,9);
            boolean result = stream.anyMatch(i -> i == Integer.parseInt(purchaseType));
            if(result) {
                purchaseEntity.setDivisionCode(divisionCode);
            }
            // 引取回数
            IntStream stream2 = IntStream.of(2,4,5,9);  //RPD_0208 TEAM_ALBUS-17 mod
            boolean result2 = stream2.anyMatch(i -> i == Integer.parseInt(purchaseType));
            if(result2) {
                purchaseEntity.setPurchaseCount(Integer.parseInt(purchaseImportFromSqCsvModel.getPurchaseCount()));
            }
            //PRD_0158 #10181 JFE mod end

            // 品番情報（仕入単価取得用）
            TItemEntity itemEntity = itemRepository.findByPartNo(partNo).orElse(new TItemEntity());

            // 仕入単価
            if(arrivalPlace.equals("18")) {
                purchaseEntity.setPurchaseUnitPrice(Integer.parseInt(purchaseUnitPrice));
            } else if(arrivalPlace.equals("19")) {
                if(itemRepository.existsByPartNo(partNo)) {
                    purchaseEntity.setPurchaseUnitPrice(itemEntity.getOtherCost().intValue());
                } else {
                    throw new ResourceNotFoundException(ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("updatePurchaseUnitPrice")
                                    .message("t_item does not exist.")
                                    .build()));
                }
            } else {
                purchaseEntity.setPurchaseUnitPrice(itemComponent.sumCosts(itemEntity).intValue());
            }

        } else {
            // 通常のCSVファイル読み込み時
            fixArrivalCount = purchaseImportCsvModel.getFixArrivalCount();
            // 計上日
            purchaseEntity.setRecordAt(accountingDate);
        }

        // 入荷確定数
        purchaseEntity.setFixArrivalCount(Integer.parseInt(fixArrivalCount));
        // PRD_0071 mod SIT end

        if (Objects.isNull(purchaseEntity.getCreatedUserId())) {
            purchaseEntity.setCreatedUserId(updatedUserId);
        }
        purchaseEntity.setUpdatedUserId(updatedUserId);
        purchaseRepository.save(purchaseEntity);
        return purchaseEntity;
    }

    /**
     * 仕入確定データ取得.
     * 仕入確定データが取得でない場合は、modelから登録用のentityを作成する.
     *
     * @param purchaseImportCsvModel 仕入確定データ
     * @param purchaseImportFromSqCsvModel 仕入確定データ（SQ）
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param accountingDate 計上日
     * @return 仕入情報
     */
    private TPurchaseEntity findPurchaseByManageNumAndVoucherNumAndVoucherLine(
            final PurchaseLinkingImportCsvModel purchaseImportCsvModel,
            // PRD_0071 add SIT end
            final PurchaseLinkingImportFromSqCsvModel purchaseImportFromSqCsvModel,
            final String fileName,
            // PRD_0071 add SIT end
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date accountingDate) {
        // PRD_0071 mod SIT start
        //// PRD_0017 mod SIT start
        ////return purchaseRepository.findByManageNumAndVoucherNumAndVoucherLine(
        ////        DateUtils.stringToDate(purchaseImportCsvModel.getArrivalAt()),
        ////        purchaseImportCsvModel.getPurchaseVoucherNumber(),
        ////        Integer.parseInt(purchaseImportCsvModel.getPurchaseVoucherLine()))
        ////        .orElse(generatePurchaseEntity(purchaseImportCsvModel, wmsLinkingFileEntity, accountingDate));
        ////
        //return purchaseRepository.findByVoucherNumAndVoucherLine(
        //        purchaseImportCsvModel.getPurchaseVoucherNumber(),
        //        Integer.parseInt(purchaseImportCsvModel.getPurchaseVoucherLine()))
        //        .orElse(generatePurchaseEntity(purchaseImportCsvModel, wmsLinkingFileEntity, accountingDate));
        //// PRD_0017 mod SIT end
        if(fileName.equals(SQ))
        {
            // PRD_0201 mod JFE start
            //return purchaseRepository.findByVoucherNumAndVoucherLine(
            return purchaseRepository.findByVoucherNumAndVoucherLineAndSupplierCodeAndRecordAt(
            // PRD_0201 mod JFE end
                    purchaseImportFromSqCsvModel.getPurchaseVoucherNumber(),
                    // PRD_0201 mod JFE start
                    //Integer.parseInt(purchaseImportFromSqCsvModel.getPurchaseVoucherLine()))
                    Integer.parseInt(purchaseImportFromSqCsvModel.getPurchaseVoucherLine()),
                    purchaseImportFromSqCsvModel.getSupplierCode(),
                    DateUtils.stringToDate(purchaseImportFromSqCsvModel.getRecordAt()))
                    // PRD_0201 mod JFE end
                    .orElse(generatePurchaseEntity(purchaseImportCsvModel, purchaseImportFromSqCsvModel, fileName, wmsLinkingFileEntity, accountingDate));
        }
        else
        {
            return purchaseRepository.findByVoucherNumAndVoucherLine(
                    purchaseImportCsvModel.getPurchaseVoucherNumber(),
                    Integer.parseInt(purchaseImportCsvModel.getPurchaseVoucherLine()))
                    .orElse(generatePurchaseEntity(purchaseImportCsvModel, purchaseImportFromSqCsvModel, fileName, wmsLinkingFileEntity, accountingDate));
        }
        // PRD_0071 mod SIT end
    }

    /**
     * 登録用仕入情報の作成.
     * @param model 仕入確定データ
     * @param sqModel 仕入確定データ（SQ）
     * @param fileName ファイル名称先頭2文字
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param accountingDate 計上日
     * @return 仕入情報
     */
    private TPurchaseEntity generatePurchaseEntity(
            final PurchaseLinkingImportCsvModel model,
            // PRD_0071 add SIT end
            final PurchaseLinkingImportFromSqCsvModel sqModel,
            final String fileName,
            // PRD_0071 add SIT end
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date accountingDate) {

        final TPurchaseEntity entity = new TPurchaseEntity();

        // PRD_0071 add SIT start
        if(fileName.equals(SQ)) {
            // 品番情報
            TItemEntity itemEntity = itemRepository.findByPartNo(sqModel.getPartNo()).orElse(new TItemEntity());

            // 倉庫連携ファイルID
            entity.setWmsLinkingFileId(wmsLinkingFileEntity.getId());
            // 日付
            entity.setSqManageDate(DateUtils.stringToDate(sqModel.getManageDate()));
            // 時間
            entity.setSqManageAt(DateUtils.stringToTime(sqModel.getManageAt()));
            // 管理No
            entity.setSqManageNumber(sqModel.getManageNumber());
            // 行No
            entity.setLineNumber(Integer.parseInt(sqModel.getLineNumber()));
            // データ種別
            entity.setDataType(PurchaseDataType.convertToType(sqModel.getDataType()));
            // 仕入区分
            entity.setPurchaseType(PurchaseType.convertToType(Integer.parseInt(sqModel.getPurchaseType())));
            // 入荷場所
            entity.setArrivalPlace(sqModel.getArrivalPlace());
            // 入荷店舗
            entity.setArrivalShop(sqModel.getArrivalShop());
            // 仕入先
            entity.setSupplierCode(sqModel.getSupplierCode());
            // 製品工場
            entity.setMdfMakerFactoryCode(sqModel.getMakerFactoryCode());
            // 入荷日
            entity.setArrivalAt(DateUtils.stringToDate(sqModel.getArrivalAt()));
            // 計上日
            entity.setRecordAt(accountingDate);
            // 仕入相手伝票No
            entity.setMakerVoucherNumber(sqModel.getMakerVoucherNumber());
            // 仕入伝票No
            entity.setPurchaseVoucherNumber(sqModel.getPurchaseVoucherNumber());
            // 仕入伝票行
            entity.setPurchaseVoucherLine(Integer.parseInt(sqModel.getPurchaseVoucherLine()));
            // 品番ID
            entity.setPartNoId(itemEntity.getId());
            // 品番
            entity.setPartNo(sqModel.getPartNo());
            // 色
            entity.setColorCode(sqModel.getColorCode());
            // サイズ
            entity.setSize(sqModel.getSize());
            // 入荷数
            entity.setArrivalCount(Integer.parseInt(sqModel.getArrivalCount()));
            // 入荷確定数
            entity.setFixArrivalCount(Integer.parseInt(sqModel.getFixArrivalCount()));
            // 良品・不用品区分
            entity.setNonConformingProductType(BooleanType.convertToType(BooleanUtils.toBoolean(sqModel.getNonConformingProductType())));
            // 指示番号
            entity.setInstructNumber(sqModel.getInstructNumber());
            // 指示番号行
            entity.setInstructNumberLine(Integer.parseInt(sqModel.getInstructNumberLine()));
            // 発注ID
            entity.setOrderId(jp.co.jun.edi.util.NumberUtils.createBigInteger(sqModel.getOrderId()));
            // 発注番号
            entity.setOrderNumber(null);
            //PRD_0158 #10181 JFE mod start
            // 引取回数
//            entity.setPurchaseCount(Integer.parseInt(sqModel.getPurchaseCount()));
//            // 課コード
//            entity.setDivisionCode(DivisionCodeType.TOKYO_DIVISION_ONE.getValue());
            IntStream stream = IntStream.of(2,4,5,9);
            boolean result = stream.anyMatch(i -> i == Integer.parseInt(sqModel.getPurchaseType()));
            if(result) {
            	entity.setDivisionCode(sqModel.getDivisionCode());
            }
            // 引取回数
            IntStream stream2 = IntStream.of(2,4,5,9);    //RPD_0208 TEAM_ALBUS-17 mod
            boolean result2 = stream2.anyMatch(i -> i == Integer.parseInt(sqModel.getPurchaseType()));
            if(result2) {
            entity.setPurchaseCount(Integer.parseInt(sqModel.getPurchaseCount()));
            }
            //PRD_0158 #10181 JFE mod end
            // 仕入単価
            entity.setPurchaseUnitPrice(itemComponent.sumCosts(itemEntity).intValue());
            // 納品ID
            entity.setDeliveryId(null);
            // LG送信区分　1:LG送信指示済
            entity.setLgSendType(LgSendType.INSTRUCTION);
            // 会計連携ステータス　0:ファイル未処理
            entity.setAccountLinkingStatus(FileInfoStatusType.FILE_UNPROCESSED);
        } else {
        // PRD_0071 add SIT end
            // 品番情報
            TItemEntity itemEntity = itemRepository.findByPartNo(model.getPartNo()).orElse(new TItemEntity());

            // 倉庫連携ファイルID
            entity.setWmsLinkingFileId(wmsLinkingFileEntity.getId());
            // 日付
            entity.setSqManageDate(DateUtils.stringToDate(model.getManageDate()));
            // 時間
            entity.setSqManageAt(DateUtils.stringToTime(model.getManageAt()));
            // 管理No
            entity.setSqManageNumber(model.getManageNumber());
            // 行No
            entity.setLineNumber(Integer.parseInt(model.getLineNumber()));
            // データ種別
            entity.setDataType(PurchaseDataType.convertToType(model.getDataType()));
            // 仕入区分
            entity.setPurchaseType(PurchaseType.convertToType(Integer.parseInt(model.getPurchaseType())));
            // 入荷場所
            entity.setArrivalPlace(model.getArrivalPlace());
            // 入荷店舗
            entity.setArrivalShop(null);
            // 仕入先
            entity.setSupplierCode(null);
            // 製品工場
            entity.setMdfMakerFactoryCode(null);
            // 入荷日
            entity.setArrivalAt(DateUtils.stringToDate(model.getArrivalAt()));
            // 計上日
            entity.setRecordAt(accountingDate);
            // 仕入相手伝票No
            entity.setMakerVoucherNumber(null);
            // 仕入伝票No
            entity.setPurchaseVoucherNumber(model.getPurchaseVoucherNumber());
            // 仕入伝票行
            entity.setPurchaseVoucherLine(Integer.parseInt(model.getPurchaseVoucherLine()));
            // 品番ID
            entity.setPartNoId(itemEntity.getId());
            // 品番
            entity.setPartNo(model.getPartNo());
            // 色
            entity.setColorCode(model.getColorCode());
            // サイズ
            entity.setSize(model.getSize());
            // 入荷数
            entity.setArrivalCount(Integer.parseInt(model.getArrivalCount()));
            // 入荷確定数
            entity.setFixArrivalCount(Integer.parseInt(model.getFixArrivalCount()));
            // 良品・不用品区分
            entity.setNonConformingProductType(BooleanType.convertToType(BooleanUtils.toBoolean(model.getNonConformingProductType())));
            // 指示番号
            entity.setInstructNumber(model.getInstructNumber());
            // 指示番号行
            entity.setInstructNumberLine(Integer.parseInt(model.getInstructNumberLine()));
            // 発注ID
            entity.setOrderId(null);
            // 発注番号
            entity.setOrderNumber(null);
            // 引取回数
            entity.setPurchaseCount(null);
            // 課コード
            entity.setDivisionCode(DivisionCodeType.TOKYO_DIVISION_ONE.getValue());
            // 仕入単価
            entity.setPurchaseUnitPrice(itemComponent.sumCosts(itemEntity).intValue());
            // 納品ID
            entity.setDeliveryId(null);
            // LG送信区分　1:LG送信指示済
            entity.setLgSendType(LgSendType.INSTRUCTION);
            // 会計連携ステータス　0:ファイル未処理
            entity.setAccountLinkingStatus(FileInfoStatusType.FILE_UNPROCESSED);
        // PRD_0071 add SIT start
        }
        // PRD_0071 add SIT end

        return entity;

    }

    /**
     * 取込用の仕入情報の作成.
     * @param purchaseEntity DBに登録されている仕入情報
     * @param purchaseImportCsvModel 仕入確定データ
     * @return 取込用の仕入情報
     */
    private PurchaseImportModel generatePurchaseImportModel(
            final TPurchaseEntity purchaseEntity,
            final PurchaseLinkingImportCsvModel purchaseImportCsvModel) {
        final PurchaseImportModel purchaseImportModel = new PurchaseImportModel();
        BeanUtils.copyProperties(purchaseEntity, purchaseImportModel);

        // ※入荷確定数はセット済み
        purchaseImportModel.setSqManageNumber(purchaseImportCsvModel.getManageNumber()); // 管理No
        purchaseImportModel.setArrivalPlace(purchaseImportCsvModel.getArrivalPlace()); // 入荷場所
        final PurchaseType purchaseType = PurchaseType.convertToType(Integer.parseInt(purchaseImportCsvModel.getPurchaseType()));
        purchaseImportModel.setPurchaseType(purchaseType); // 仕入区分

        return purchaseImportModel;
    }

    // PRD_0071 add SIT start
    /**
     * 取込用の仕入情報の作成.
     * @param purchaseEntity DBに登録されている仕入情報
     * @param purchaseImportCsvModel 仕入確定データ
     * @return 取込用の仕入情報
     */
    private PurchaseImportModel generatePurchaseImportFromSqModel(
            final TPurchaseEntity purchaseEntity,
            final PurchaseLinkingImportFromSqCsvModel purchaseImportFromSqCsvModel) {
        final PurchaseImportModel purchaseImportModel = new PurchaseImportModel();
        BeanUtils.copyProperties(purchaseEntity, purchaseImportModel);

        // ※入荷確定数はセット済み
        purchaseImportModel.setSqManageNumber(purchaseImportFromSqCsvModel.getManageNumber()); // 管理No
        purchaseImportModel.setArrivalPlace(purchaseImportFromSqCsvModel.getArrivalPlace()); // 入荷場所
        final PurchaseType purchaseType = PurchaseType.convertToType(Integer.parseInt(purchaseImportFromSqCsvModel.getPurchaseType()));
        purchaseImportModel.setPurchaseType(purchaseType); // 仕入区分

        return purchaseImportModel;
    }
    // PRD_0071 add SIT end

    /**
     * 納品依頼明細情報更新.
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param updatedUserId 更新ユーザID
     */
    private void updateDeliveryDetails(
            final List<PurchaseImportModel> purchaseImportModels,
            final BigInteger updatedUserId) {
        // 取込用仕入情報Modelを課単位で重複除去してループ
        purchaseConfirmComponent.distinctPurchaseByDivision(purchaseImportModels)
        .forEach(purchaseImportModel -> {
            // 納品明細情報の更新
            final TDeliveryDetailEntity updateDeliveryDetail = purchaseConfirmComponent.generateUpdateDeliveryDetail(purchaseImportModel, updatedUserId);
            deliveryDetailRepository.save(updateDeliveryDetail);
        });
    }

    /**
     * 発注情報更新.
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    private void updateOrders(
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        if (CollectionUtils.isEmpty(purchaseImportModels)) {
            return;
        }

        final List<TOrderEntity> updateOrders = purchaseConfirmComponent.generateUpdateOrders(
                purchaseImportModels, accountingDate, updatedUserId);

        orderRepository.saveAll(updateOrders);
    }

    /**
     * 納品SKU情報と発注SKU情報更新.
     *
     * @param purchaseImportModels 取込用の仕入情報Modelリスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    private void updateDeliverySkuAndOrderSku(
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        // 取込用仕入情報Modelを発注SKU単位で重複除去してループ
        purchaseConfirmComponent.distinctPurchaseByOrderSku(purchaseImportModels)
        .forEach(distinctByOrderSkuPurchase -> {
            final TOrderSkuEntity orderSkuEntity = purchaseConfirmComponent.findByOrderIdAndColorAndSize(distinctByOrderSkuPurchase);

            purchaseConfirmComponent.filterOrderSkuPurchaseByOrderSku(purchaseImportModels, distinctByOrderSkuPurchase)
            .forEach(orderSkuPurchase -> {
                // PRD_0098 mod SIT start
                //final TDeliverySkuEntity deliverySkuEntity = purchaseConfirmComponent.findDeliverySkuEntity(orderSkuPurchase);
                //// 納品依頼SKU更新
                //purchaseConfirmComponent.generateUpdateDeliverySku(orderSkuPurchase, deliverySkuEntity, updatedUserId);
                //deliverySkuRepository.save(deliverySkuEntity);
                TDeliverySkuEntity deliverySkuEntity = new TDeliverySkuEntity();
                if (purchaseConfirmComponent.isDeliveryUpdateTarget(orderSkuPurchase)) {
                    deliverySkuEntity = purchaseConfirmComponent.findDeliverySkuEntity(orderSkuPurchase);
                    // 納品依頼SKU更新
                    purchaseConfirmComponent.generateUpdateDeliverySku(orderSkuPurchase, deliverySkuEntity, updatedUserId);
                    deliverySkuRepository.save(deliverySkuEntity);
                }
                // PRD_0098 mod SIT end
                // 発注SKU更新
                purchaseConfirmComponent.generateUpdateOrderSku(orderSkuPurchase, orderSkuEntity, deliverySkuEntity, accountingDate, updatedUserId);
                orderSkuRepository.save(orderSkuEntity);
            });
        });
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
    public int updateWmsLinkingStatus(
            final WmsLinkingStatusType type,
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
    public void updateWmsLinkingStatusFileImporting(
            final List<TWmsLinkingFileEntity> wmsLinkingFiles,
            final BigInteger updatedUserId) {
        final List<BigInteger> ids = wmsLinkingFiles.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tWmsLinkingFileRepository.updateWmsLinkingStatusByIds(WmsLinkingStatusType.FILE_IMPORTING, ids, updatedUserId);
    }
}
