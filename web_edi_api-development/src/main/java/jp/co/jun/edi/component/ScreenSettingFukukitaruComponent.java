package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.ScreenSettingFukukitaruComponentSearchConditionModel;
import jp.co.jun.edi.entity.MFAppendicesTermEntity;
import jp.co.jun.edi.entity.MFBrandDestinationEntity;
import jp.co.jun.edi.entity.MFBrandMasterEntity;
import jp.co.jun.edi.entity.MFCategoryCodeEntity;
import jp.co.jun.edi.entity.MFCnProductCategoryEntity;
import jp.co.jun.edi.entity.MFCnProductTypeEntity;
import jp.co.jun.edi.entity.MFDestinationEntity;
import jp.co.jun.edi.entity.MFMaterialAttentionHangTagEntity;
import jp.co.jun.edi.entity.MFMaterialAttentionNameEntity;
import jp.co.jun.edi.entity.MFMaterialAttentionTagEntity;
import jp.co.jun.edi.entity.MFMaterialHangTagAuxiliaryEntity;
import jp.co.jun.edi.entity.MFMaterialHangTagEntity;
import jp.co.jun.edi.entity.MFMaterialHangTagNergyMeritEntity;
import jp.co.jun.edi.entity.MFMaterialWashAuxiliaryEntity;
import jp.co.jun.edi.entity.MFMaterialWashNameEntity;
import jp.co.jun.edi.entity.MFRecycleEntity;
import jp.co.jun.edi.entity.MFSealEntity;
import jp.co.jun.edi.entity.MFTapeEntity;
import jp.co.jun.edi.entity.MFTapeWidthEntity;
import jp.co.jun.edi.entity.MFWashPatternEntity;
import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.entity.TFFileInfoEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedMFInputAssistSetEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.entity.extended.ExtendedTSkuEntity;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.FukukitaruDestinationModel;
import jp.co.jun.edi.model.FukukitaruInputAssistSetDetailsModel;
import jp.co.jun.edi.model.FukukitaruInputAssistSetModel;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.FukukitaruMasterModel;
import jp.co.jun.edi.model.FukukitaruMaterialAppendicesTermModel;
import jp.co.jun.edi.model.FukukitaruMaterialAttentionNameModel;
import jp.co.jun.edi.model.FukukitaruMaterialAttentionTagModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.JunpcSizmstSearchConditionModel;
import jp.co.jun.edi.model.MaterialFileInfoModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.ScreenSettingFukukiatruModel;
import jp.co.jun.edi.model.ScreenSettingFukukitaruSkuModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.repository.MFAppendicesTermRepository;
import jp.co.jun.edi.repository.MFBrandDestinationRepository;
import jp.co.jun.edi.repository.MFBrandMasterRepository;
import jp.co.jun.edi.repository.MFCategoryCodeRepository;
import jp.co.jun.edi.repository.MFCnProductCategoryRepository;
import jp.co.jun.edi.repository.MFCnProductTypeRepository;
import jp.co.jun.edi.repository.MFDestinationRepository;
import jp.co.jun.edi.repository.MFMaterialAttentionHangTagRepository;
import jp.co.jun.edi.repository.MFMaterialAttentionNameRepository;
import jp.co.jun.edi.repository.MFMaterialAttentionTagRepository;
import jp.co.jun.edi.repository.MFMaterialHangTagAuxiliaryRepository;
import jp.co.jun.edi.repository.MFMaterialHangTagNergyMeritRepository;
import jp.co.jun.edi.repository.MFMaterialHangTagRepository;
import jp.co.jun.edi.repository.MFMaterialWashAuxiliaryRepository;
import jp.co.jun.edi.repository.MFMaterialWashNameRepository;
import jp.co.jun.edi.repository.MFRecycleRepository;
import jp.co.jun.edi.repository.MFSealRepository;
import jp.co.jun.edi.repository.MFTapeRepository;
import jp.co.jun.edi.repository.MFTapeWidthRepository;
import jp.co.jun.edi.repository.MFWashPatternRepository;
import jp.co.jun.edi.repository.TFFileInfoRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedMFInputAssistSetRepository;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
import jp.co.jun.edi.repository.specification.MFDestinationSpecification;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.type.FukukitaruMasterDestinationType;
import jp.co.jun.edi.type.FukukitaruMasterIdListType;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.FukukitaruMasterType;
import jp.co.jun.edi.type.ScreenSettingFukukitaruMasterType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * フクキタル用画面構成情報を取得するコンポーネント.
 */
@Slf4j
@Component
public class ScreenSettingFukukitaruComponent {

    private static final String[] BV_KOMONO_ITEMS = {"A", "I", "N", "R", "U", "W", "X", "Z" };

    @Autowired
    private MFBrandMasterRepository mfBrandMasterRepository;
    @Autowired
    private MFTapeWidthRepository mfTapeWidthRepository;
    @Autowired
    private MFTapeRepository mfTapeRepository;
    @Autowired
    private MFAppendicesTermRepository mfAppendicesTermRepository;
    @Autowired
    private MFCnProductCategoryRepository mfCnProductCategoryRepository;
    @Autowired
    private MFCnProductTypeRepository mfCnProductTypeRepository;
    @Autowired
    private MFRecycleRepository mfRecycleRepository;
    @Autowired
    private MFSealRepository mfSealRepository;
    @Autowired
    private MFWashPatternRepository mfWashPatternRepository;
    @Autowired
    private MFBrandDestinationRepository mfBrandDestinationRepository;
    @Autowired
    private MFDestinationRepository mfDestinationRepository;
    @Autowired
    private MFDestinationSpecification mfAddreessSpecification;
    @Autowired
    private JunpcSizemstListServiceDataComponent junpcSizemstListServiceDataComponent;
    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;
    @Autowired
    private ExtendedTSkuRepository extendedTSkuRepository;
    @Autowired
    private TOrderSkuRepository tOrderSkuRepository;
    @Autowired
    private ExtendedTCompositionRepository extendedTCompositionRepository;
    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;
    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;
    @Autowired
    private TFFileInfoRepository tfFileInfoRepository;
    @Autowired
    private MFCategoryCodeRepository mfCategoryCodeRepository;
    @Autowired
    private MFMaterialAttentionNameRepository mfMaterialAttentionNameRepository;
    @Autowired
    private MFMaterialAttentionTagRepository mfMaterialAttentionTagRepository;
    @Autowired
    private MFMaterialHangTagAuxiliaryRepository mfMaterialHangTagAuxiliaryRepository;
    @Autowired
    private MFMaterialHangTagNergyMeritRepository mfMaterialHangTagNergyMeritRepository;
    @Autowired
    private MFMaterialHangTagRepository mfMaterialHangTagRepository;
    @Autowired
    private MFMaterialWashAuxiliaryRepository mfMaterialWashAuxiliaryRepository;
    @Autowired
    private MFMaterialAttentionHangTagRepository mfMaterialAttentionHangTagRepository;
    @Autowired
    private MFMaterialWashNameRepository mfMaterialWashNameRepository;
    @Autowired
    private ExtendedMFInputAssistSetRepository extendedMFInputAssistSetRepository;
    @Autowired
    private ItemComponent itemComponent;
    @Autowired
    private OrderComponent orderComponent;

    /**
     * フクキタル関連の画面構成に必要な情報を取得する.
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     * @return {@link ScreenSettingFukukiatruModel} instance
     */
    public ScreenSettingFukukiatruModel execute(final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final ScreenSettingFukukiatruModel returnModel = new ScreenSettingFukukiatruModel();

        for (ScreenSettingFukukitaruMasterType masterType : paramModel.getListMasterType()) {
            switch (masterType) {
            // テープ巾(1)
            case TAPE_WIDTH:
                generateListTapeWidth(returnModel, paramModel);
                break;
            // テープ種類(2)
            case TAPE_TYPE:
                generateListTapeType(returnModel, paramModel);
                break;
            // 洗濯ネーム付記用語(3)
            case WASH_NAME_APPENDICES_TERM:
                generateListWashNameAppendicesTerm(returnModel, paramModel);
                break;
            // アテンションタグ付記用語(4)
            case ATTENTION_TAG_APPENDICES_TERM:
                generateListAttentionTagAppendicesTerm(returnModel, paramModel);
                break;
            // アテンションシールのシール種類(5)
            case ATTENTION_TAG_SEAL:
                generateListAttentionSealType(returnModel, paramModel);
                break;
            // リサイクルマーク(6)
            case RECYCL:
                generateListRecycle(returnModel, paramModel);
                break;
            // 中国内販情報製品分類(7)
            case CN_DERIVERY_PRODUCT_CATEGORY:
                generateListCnProductCategory(returnModel, paramModel);
                break;
            // 中国内販情報製品種別(8)
            case CN_DERIVERY_PRODUCT_TYPE:
                generateListCnProductType(returnModel, paramModel);
                break;
            // アテンションタグ(9)
            case ATTENTION_TAG:
                generateListAttentionTag(returnModel, paramModel);
                break;
            // アテンションネーム(10)
            case ATTENTION_NAME:
                generateListAttentionName(returnModel, paramModel);
                break;
            // 同封副資材(11)
            case AUXILIARY_MATERIAL:
                generateListAuxiliaryMaterial(returnModel, paramModel);
                break;
            // 下札類(12)
            case HANG_TAG:
                generateListHangTag(returnModel, paramModel);
                break;
            // 洗濯マーク(13)
            case WASH_PATTERN:
                generateListWashPattern(returnModel, paramModel);
                break;
            // 請求先(14)
            case BILLING_ADDRESS:
                generateBillingDestination(returnModel, paramModel);
                break;
            // 納品先(15)
            case DELIVERY_ADDRESS:
                generateDeliveryDestination(returnModel, paramModel);
                break;
            // 発注先(16)
            case SUPPLIER_ADDRESS:
                generateSupplierDestination(returnModel, paramModel);
                break;
            // SKU(17)
            case SKU:
                generateSku(returnModel, paramModel);
                break;
            // 品番情報(18)
            case ITEM:
                generateItem(returnModel, paramModel);
                break;
            // 発注情報(19)
            case ORDER:
                generateOrder(returnModel, paramModel);
                break;
            // フクキタル品番情報(20)
            case FUKUKITARU_ITEM:
                generateFukukitaruItem(returnModel, paramModel);
                break;
            // 発注種別(21)
            case ORDER_TYPE:
                generateOrderType(returnModel, paramModel);
                break;
            // フクキタル用ファイル情報(22)
            case MATERIAL_FILE:
                generateFukukitaruFileInfo(returnModel, paramModel);
                break;
            //洗濯ネーム情報(23)
            case WASH_NAME:
                generateListWashName(returnModel, paramModel);
                break;
            // アテンション下札(24)
            case ATTENTION_HANG_TAG:
                generateListAttentionHangTag(returnModel, paramModel);
                break;
            // カテゴリコード(25)
            case CATEGORY_CODE:
                generateListCategoryCode(returnModel, paramModel);
                break;
            // NERGYメリット下札(26)
            case HANG_TAG_NERGY_MERIT:
                generateListHangTagNergyMerit(returnModel, paramModel);
                break;
            // 入力補助セット(27)
            case INPUT_ASSIST_SET:
                generateListInputAssistSet(returnModel, paramModel);
                break;
            // サスティナブルマーク(28)
            case SUSTAINABLE_MARK:
                generateSustainableMarkDisplayFlg(returnModel, paramModel);
                break;
            default:
                log.error(LogStringUtil.of("execute")
                        .message("Not found ScreenSettingFukukitaruMasterType")
                        .value("listMasterType", masterType.getValue())
                        .build());
                break;
            }
        }

        return returnModel;
    }

    /**
     * 入力補助セットから{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListInputAssistSet(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        Page<ExtendedMFInputAssistSetEntity> pageExtendedMFInputAssistSetEntity = null;
        if (Optional.ofNullable(paramModel.getInputAssistId()).isPresent()) {
            // 入力補助セットIDをキーに、入力補助セット情報を取得する
            pageExtendedMFInputAssistSetEntity = extendedMFInputAssistSetRepository.findByInputAssistSetId(
                    paramModel.getInputAssistId(),
                    PageRequest.of(0, Integer.MAX_VALUE));

        } else {
            // ブランドコード、フクキタルデリバリ種別、フクキタル発注種別をキーに、入力補助セット情報を取得する
            pageExtendedMFInputAssistSetEntity = extendedMFInputAssistSetRepository.findByBrandCodeDeliveryTypeOrderType(
                    paramModel.getBrandCode(),
                    paramModel.getDeliveryType().convertToValue(),
                    paramModel.getOrderType().convertToValue(),
                    PageRequest.of(0, Integer.MAX_VALUE));
        }

        // 入力補助セットIDごとにグルーピングし、モデルを生成する
        final List<FukukitaruInputAssistSetModel> listInputAssistSet = pageExtendedMFInputAssistSetEntity.stream()
                .collect(Collectors.groupingBy(entity -> entity.getId().getInputAssistSetId()))
                .entrySet().stream().map(value -> {

                    // 入力補助セット詳細情報取得
                    final List<FukukitaruInputAssistSetDetailsModel> detailModel = new ArrayList<FukukitaruInputAssistSetDetailsModel>();
                    value.getValue()
                            .stream()
                            .forEach(entity -> {
                                detailModel.addAll(genaratedInputAssistSetDetails(entity.getMaterialType(), entity.getMaterialIdList()));
                            });

                    final ExtendedMFInputAssistSetEntity firstEntity = value.getValue().get(0);

                    // モデル生成
                    final FukukitaruInputAssistSetModel model = new FukukitaruInputAssistSetModel();
                    model.setId(firstEntity.getId().getInputAssistSetId());
                    model.setSetName(firstEntity.getSetName());
                    model.setListInputAssistSetDetails(detailModel);

                    return model;
                }).collect(Collectors.toList());

        returnModel.setListInputAssistSet(listInputAssistSet);
    }

    /**
     * 入力補助セット詳細情報を取得し、FukukitaruInputAssistSetModelに変換する.
     * @param materialType 資材種別
     * @param materialIdList 資材IDリスト
     * @return 入力補助セット詳細情報
     */
    private List<FukukitaruInputAssistSetDetailsModel> genaratedInputAssistSetDetails(final FukukitaruMasterMaterialType materialType,
            final String materialIdList) {

        List<FukukitaruInputAssistSetDetailsModel> retModel = null;

        // TODO 対象の資材種別が増える場合は、そのときに対応する
        switch (materialType) {
        // 下札(4)
        case HANG_TAG:
            retModel = generateMaterialHangTag(materialIdList, materialType).stream().map(entity -> {
                final FukukitaruInputAssistSetDetailsModel model = new FukukitaruInputAssistSetDetailsModel();
                model.setId(entity.getId());
                model.setCode(entity.getCode());
                model.setCodeName(entity.getCodeName());
                model.setMaterialType(FukukitaruMasterMaterialType.HANG_TAG);
                return model;
            }).collect(Collectors.toList());
            break;
        // 下札同封副資材(8)
        case HANG_TAG_AUXILIARY_MATERIAL:
            retModel = generateMaterialHangTagAuxiliary(materialIdList, materialType).stream().map(entity -> {
                final FukukitaruInputAssistSetDetailsModel model = new FukukitaruInputAssistSetDetailsModel();
                model.setId(entity.getId());
                model.setCode(entity.getCode());
                model.setCodeName(entity.getCodeName());
                model.setMaterialType(FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL);
                return model;
            }).collect(Collectors.toList());
            break;
        default:
            break;
        }

        return retModel;
    }

    /**
     * カテゴリコードから{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListCategoryCode(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.CATEGORY_CODE, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListWashNameAppendicesTerm(new ArrayList<>());
            return;
        }
        returnModel.setListCategoryCode(generateCategoryCode(resultEntity));
    }

    /**
     * ブランドコードから検索したフクキタルファイル情報から{@link MaterialFileInfoModel} を生成し、
     * ScreenSettingFukukiatruModel.listMaterialFileに格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateFukukitaruFileInfo(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        final List<TFFileInfoEntity> listTFFileInfoEntity = tfFileInfoRepository
                .findByBrandCode(paramModel.getBrandCode(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        returnModel.setListMaterialFile(listTFFileInfoEntity.stream()
                // デリバリ種別がNULLの場合、または、デリバリ種別が画面パラメータのデリバリ種別と同じ情報のみ抽出
                .filter(entity -> Objects.isNull(entity.getDeliveryType()) || entity.getDeliveryType() == paramModel.getDeliveryType())
                .map(entity -> {
                    // entityをmodelに変換する
                    final MaterialFileInfoModel model = new MaterialFileInfoModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList()));
    }

    /**
     * フクキタル発注画面に表示するSKUのUI情報を作成する.
     * ・品番SKUで画面の色、サイズの2次元テーブルつくる
     * ・発注SKUの数量入れた部分を活性化する
     * @param returnModel {@link SearchConditionFukukiatruOrderModel.class}
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel}
     */
    private void generateSku(final ScreenSettingFukukiatruModel returnModel, final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        if (Objects.isNull(paramModel.getPartNoId())) {
            // 品番IDがNULLの場合は、空のリストを設定する
            returnModel.setListScreenSku(new ArrayList<>());
            return;
        }
        if (Objects.isNull(paramModel.getOrderId())) {
            // 発注IDがNULLの場合は、空のリストを設定する
            returnModel.setListScreenSku(new ArrayList<>());
            return;
        }

        // 品番SKUを取得
        final List<ExtendedTSkuEntity> listExtendesTSkuEntity = extendedTSkuRepository
                .findByPartNoId(paramModel.getPartNoId(), PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code"))))
                .getContent();
        if (listExtendesTSkuEntity.isEmpty()) {
            // 品番SKUが存在しない場合は、空のリストを設定する
            returnModel.setListScreenSku(new ArrayList<ScreenSettingFukukitaruSkuModel>());
            return;
        }
        // 発注SKUを取得
        final List<TOrderSkuEntity> listTOrderEntity = tOrderSkuRepository
                .findByOrderId(paramModel.getOrderId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        if (listTOrderEntity.isEmpty()) {
            // 発注SKUが存在しない場合は、空のリストを設定する
            returnModel.setListScreenSku(new ArrayList<ScreenSettingFukukitaruSkuModel>());
            return;
        }

        // サイズマスタからサイズを取得する
        final JunpcSizmstSearchConditionModel junpcSizmstSearchConditionModel = new JunpcSizmstSearchConditionModel();
        junpcSizmstSearchConditionModel.setHscd(paramModel.getPartNoKind());
        final List<MSizmstEntity> listMSizmstEntity = junpcSizemstListServiceDataComponent.find(junpcSizmstSearchConditionModel).getContent();

        // SKU情報から一意のカラー情報を取得する
        final List<ExtendedTSkuEntity> listColorCode = listExtendesTSkuEntity.stream().filter(itemSku -> {
            final ExtendedTSkuEntity findItemSku = listExtendesTSkuEntity
                    .stream()
                    .filter(data -> data.getColorCode().equals(itemSku.getColorCode()))
                    .findFirst()
                    .get();
            if (itemSku == findItemSku) {
                return true;
            }

            return false;
        }).collect(Collectors.toList());

        final List<ScreenSettingFukukitaruSkuModel> list = new ArrayList<>();

        // サイズ×色の画面表示用SKUモデルを生成する
        listColorCode.stream().forEach(color -> {
            listMSizmstEntity.stream().forEach(msiz -> {
                final ScreenSettingFukukitaruSkuModel screenSkuModel = new ScreenSettingFukukitaruSkuModel();
                screenSkuModel.setColorCode(color.getColorCode());
                screenSkuModel.setColorName(color.getColorName());
                screenSkuModel.setSize(msiz.getSzkg());

                // サイズ×色に該当する発注SKUが存在する場合は、活性にする
                listTOrderEntity.stream().filter(findOrderSku -> {
                    if (findOrderSku.getColorCode().equals(color.getColorCode())
                            && findOrderSku.getSize().equals(msiz.getSzkg())) {
                        // 活性
                        screenSkuModel.setEnabled(true);
                        return true;
                    }
                    return false;
                }).findFirst();

                list.add(screenSkuModel);
            });
        });

        returnModel.setListScreenSku(list);

    }

    /**
     * 宛先情報から{@link FukukitaruDestinationModel}を生成する.
     * @param serviceParameter {@link GetServiceParameter} instance
     * @param mfBrandDestinationEntity {@link MFBrandDestinationEntity} instance
     * @return {@link FukukitaruDestinationModel} instance List
     */
    private List<FukukitaruDestinationModel> generateDestination(final ScreenSettingFukukitaruComponentSearchConditionModel serviceParameter,
            final MFBrandDestinationEntity mfBrandDestinationEntity) {

        Stream<MFDestinationEntity> streamEntity = null;
        if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandDestinationEntity.getDestinationIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfDestinationRepository.findAll(
                    Specification
                            .where(mfAddreessSpecification.notDeleteContains())
                            .and(mfAddreessSpecification.companyNameLikeContains(serviceParameter.getSearchCompanyName())),
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandDestinationEntity.getDestinationIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFDestinationEntity> listBeforSortData = mfDestinationRepository.findAll(
                    Specification
                            .where(mfAddreessSpecification.notDeleteContains())
                            .and(mfAddreessSpecification.idInContains(ids))
                            .and(mfAddreessSpecification.companyNameLikeContains(serviceParameter.getSearchCompanyName())),
                    PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFDestinationEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData.stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruDestinationModel destinationModel = new FukukitaruDestinationModel();
            destinationModel.setId(entity.getId());
            destinationModel.setCompanyName(entity.getCompanyName());
            destinationModel.setPostalCode(entity.getPostalCode());
            destinationModel.setAddress(entity.getAddress());
            destinationModel.setTel(entity.getTel());
            destinationModel.setFax(entity.getFax());
            destinationModel.setIsApprovalRequired(entity.getIsApprovalRequired());
            return destinationModel;
        }).collect(Collectors.toList());
    }

    /**
     * 付記用語情報から{@link FukukitaruMasterModel}を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMaterialAppendicesTermModel> generateAppendicesTerm(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFAppendicesTermEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfAppendicesTermRepository
                    .findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value)).collect(Collectors.toList());
            final List<MFAppendicesTermEntity> listBeforSortData = mfAppendicesTermRepository
                    .findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFAppendicesTermEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMaterialAppendicesTermModel codeModel = new FukukitaruMaterialAppendicesTermModel();
            codeModel.setId(entity.getId());
            codeModel.setAppendicesTermCode(entity.getAppendicesTermCode());
            codeModel.setAppendicesTermCodeName(entity.getAppendicesTermCodeName());
            codeModel.setAppendicesTermSentence(entity.getAppendicesTermSentence());
            codeModel.setCharacteristic(entity.getCharacteristic());
            codeModel.setSortOrder(entity.getSortOrder());
            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * シール情報から{@link FukukitaruMasterModel}を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateAttentionTagSeal(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFSealEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfSealRepository.findByAllDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFSealEntity> listBeforSortData = mfSealRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFSealEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getSealCode());
            codeModel.setCodeName(entity.getSealName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 中国内販情報製品分類情報から{@link FukukitaruMasterModel}を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateCNDeriveryProductCategory(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFCnProductCategoryEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfCnProductCategoryRepository.findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFCnProductCategoryEntity> listBeforSortData = mfCnProductCategoryRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFCnProductCategoryEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getProductCategoryCode());
            codeModel.setCodeName(entity.getProductCategoryName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 中国内販情報製品種別情報から{@link FukukitaruMasterModel}を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateCNDeriveryProductType(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFCnProductTypeEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfCnProductTypeRepository.findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFCnProductTypeEntity> listBeforSortData = mfCnProductTypeRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFCnProductTypeEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getProductTypeCode());
            codeModel.setCodeName(entity.getProductTypeName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 発注先情報から{@link MFBrandDestinationEntity}を生成し、{@link ScreenSettingFukukitaruComponentSearchConditionModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link GetServiceParameter} instance
     */
    private void generateSupplierDestination(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        final Optional<MFBrandDestinationEntity> optionalMFBrandDestinationEntity = mfBrandDestinationRepository.findByBrandCodeAndCompanyAndDestinationType(
                paramModel.getBrandCode(),
                paramModel.getCompany(),
                FukukitaruMasterDestinationType.SUPPLIER);
        if (!optionalMFBrandDestinationEntity.isPresent()) {
            // ブランド別宛先マスタ情報がない場合、空のリストを返す
            returnModel.setListSupplierAddress(new ArrayList<>());
            return;
        }

        returnModel.setListSupplierAddress(generateDestination(paramModel, optionalMFBrandDestinationEntity.get()));

    }

    /**
     * 請求先情報から{@link FukukitaruDestinationModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateBillingDestination(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        final Optional<MFBrandDestinationEntity> optionalMFBrandDestinationEntity = mfBrandDestinationRepository.findByBrandCodeAndCompanyAndDestinationType(
                paramModel.getBrandCode(),
                paramModel.getCompany(),
                FukukitaruMasterDestinationType.BILLING);
        if (!optionalMFBrandDestinationEntity.isPresent()) {
            // ブランド別宛先マスタ情報がない場合、空のリストを返す
            returnModel.setListBillingAddress(new ArrayList<>());
            return;
        }

        returnModel.setListBillingAddress(generateDestination(paramModel, optionalMFBrandDestinationEntity.get()));

    }

    /**
     * 納品先情報から{@link FukukitaruDestinationModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateDeliveryDestination(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {

        final Optional<MFBrandDestinationEntity> optionalMFBrandDestinationEntity = mfBrandDestinationRepository.findByBrandCodeAndCompanyAndDestinationType(
                paramModel.getBrandCode(),
                paramModel.getCompany(),
                FukukitaruMasterDestinationType.DELIVERY);
        if (!optionalMFBrandDestinationEntity.isPresent()) {
            // ブランド別宛先マスタ情報がない場合、空のリストを返す
            returnModel.setListDeriveryAddress(new ArrayList<>());
            return;
        }

        returnModel.setListDeriveryAddress(generateDestination(paramModel, optionalMFBrandDestinationEntity.get()));

    }

    /**
     * テープ巾情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListTapeWidth(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.TAPE_WIDTH, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListTapeWidth(new ArrayList<>());
            return;
        }
        returnModel.setListTapeWidth(generateTapeWidth(resultEntity));
    }

    /**
     * テープ種別情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListTapeType(final ScreenSettingFukukiatruModel returnModel, final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.TAPE_TYPE, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListTapeType(new ArrayList<>());
            return;
        }
        returnModel.setListTapeType(generateTapeType(resultEntity));
    }

    /**
     * 洗濯ネーム付記用語情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListWashNameAppendicesTerm(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.WASH_NAME_APPENDICES_TERM, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListWashNameAppendicesTerm(new ArrayList<>());
            return;
        }
        returnModel.setListWashNameAppendicesTerm(generateAppendicesTerm(resultEntity));
    }

    /**
     * アテンションタグ付記用語情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAttentionTagAppendicesTerm(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAttentionTagAppendicesTerm(new ArrayList<>());
            return;
        }
        returnModel.setListAttentionTagAppendicesTerm(generateAppendicesTerm(resultEntity));
    }

    /**
     * アテンションシールのシール種類情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAttentionSealType(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.ATTENTION_TAG_SEAL, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAttentionSealType(new ArrayList<>());
            return;
        }
        returnModel.setListAttentionSealType(generateAttentionTagSeal(resultEntity));
    }

    /**
     * リサイクルマーク情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListRecycle(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.RECYCL, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListRecycle(new ArrayList<>());
            return;
        }
        returnModel.setListRecycle(generateRecycl(resultEntity));
    }

    /**
     * 中国内販情報製品分類情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListCnProductCategory(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.CN_DERIVERY_PRODUCT_CATEGORY, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListCnProductCategory(new ArrayList<>());
            return;
        }
        returnModel.setListCnProductCategory(generateCNDeriveryProductCategory(resultEntity));
    }

    /**
     * 中国内販情報製品種別情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListCnProductType(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.CN_DERIVERY_PRODUCT_TYPE, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListCnProductType(new ArrayList<>());
            return;
        }
        returnModel.setListCnProductType(generateCNDeriveryProductType(resultEntity));
    }

    /**
     * アテンションネーム情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAttentionName(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getOrderType())) {
            // OrderTypeが設定されていない場合は、空のリストを設定する
            returnModel.setListAttentionName(new ArrayList<>());
            return;
        }

        final MFBrandMasterEntity resultEntity =
                generateMFBrandMasterEntityByCodeAndType(FukukitaruMasterType.ATTENTION_NAME, paramModel);

        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAttentionName(new ArrayList<>());
            return;
        }
        returnModel.setListAttentionName(generateMaterialAttentionName(resultEntity, FukukitaruMasterMaterialType.ATTENTION_NAME));
    }

    /**
     * Nergyメリット下札情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListHangTagNergyMerit(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getOrderType())) {
            // OrderTypeが設定されていない場合は、空のリストを設定する
            returnModel.setListAttentionName(new ArrayList<>());
            return;
        }

        final MFBrandMasterEntity resultEntity =
                generateMFBrandMasterEntityByCodeAndType(FukukitaruMasterType.ATTENTION_NAME, paramModel);

        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAttentionName(new ArrayList<>());
            return;
        }
        returnModel.setListHangTagNergyMerit(generateMaterialHangTagNergyMerit(resultEntity, FukukitaruMasterMaterialType.ATTENTION_NAME));
    }

    /**
     * 洗濯マーク情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListWashPattern(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.WASH_PATTERN, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListWashPattern(new ArrayList<>());
            return;
        }
        returnModel.setListWashPattern(generateWashPattern(resultEntity));
    }

    /**
     * アテンションタグ情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAttentionTag(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getOrderType())) {
            // OrderTypeが設定されていない場合は、空のリストを設定する
            returnModel.setListAttentionTag(new ArrayList<>());
            return;
        }

        final MFBrandMasterEntity resultEntity =
                generateMFBrandMasterEntityByCodeAndType(FukukitaruMasterType.ATTENTION_TAG, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAttentionTag(new ArrayList<>());
            return;
        }
        returnModel.setListAttentionTag(generateMaterialAttentionTag(resultEntity, FukukitaruMasterMaterialType.ATTENTION_TAG));
    }

    /**
     * 下札情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListHangTag(final ScreenSettingFukukiatruModel returnModel, final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getOrderType())) {
            // OrderTypeが設定されていない場合は、空のリストを設定する
            returnModel.setListBottomBill(new ArrayList<>());
            return;
        }

        final MFBrandMasterEntity resultEntity =
                generateMFBrandMasterEntityByCodeAndType(FukukitaruMasterType.HANG_TAG, paramModel);

        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListBottomBill(new ArrayList<>());
            return;
        }
        returnModel.setListBottomBill(generateMaterialHangTag(resultEntity.getMasterIdList(), FukukitaruMasterMaterialType.HANG_TAG));
    }

    /**
     * 同封副資材情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAuxiliaryMaterial(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getOrderType())) {
            // OrderTypeが設定されていない場合は、空のリストを設定する
            returnModel.setListAuxiliaryMaterial(new ArrayList<>());
            return;
        }

        final MFBrandMasterEntity resultEntity =
                generateMFBrandMasterEntityByCodeAndType(FukukitaruMasterType.AUXILIARY_MATERIAL, paramModel);

        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、空のリストを設定する
            returnModel.setListAuxiliaryMaterial(new ArrayList<>());
            return;
        }

        switch (paramModel.getOrderType()) {
        case WASH_NAME:
        case WASH_NAME_KOMONO:
            // 洗濯ネーム、洗濯ネーム（小物）の場合、洗濯同封副資材を取得し、設定する
            returnModel.setListAuxiliaryMaterial(
                    generateMaterialWashAuxiliary(
                            resultEntity,
                            FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL));
            break;
        case HANG_TAG:
        case HANG_TAG_KOMONO:
            // 下札、下札（小物）の場合、下札同封副資材を取得し、設定する
            returnModel.setListAuxiliaryMaterial(
                    generateMaterialHangTagAuxiliary(
                            resultEntity.getMasterIdList(),
                            FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL));
            break;
        default:
            // 該当しない場合、空のリストを設定する
            returnModel.setListAuxiliaryMaterial(new ArrayList<>());
            break;
        }

    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMaterialAttentionNameModel> generateMaterialAttentionName(final MFBrandMasterEntity mfBrandMasterEntity,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialAttentionNameEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialAttentionNameRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialAttentionNameEntity> listBeforSortData = mfMaterialAttentionNameRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialAttentionNameEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMaterialAttentionNameModel codeModel = new FukukitaruMaterialAttentionNameModel();
            codeModel.setId(entity.getId());
            codeModel.setMaterialType(entity.getMaterialType());
            codeModel.setMaterialTypeName(entity.getMaterialTypeName());
            codeModel.setMaterialCode(entity.getMaterialCode());
            codeModel.setMaterialCodeName(entity.getMaterialCodeName());
            codeModel.setMoq(entity.getMoq());
            codeModel.setSortOrder(entity.getSortOrder());
            codeModel.setType(entity.getType());
            codeModel.setProductName(entity.getProductName());
            codeModel.setTitle(entity.getTitle());
            codeModel.setSentence(entity.getSentence());
            codeModel.setOldMaterialCode(entity.getOldMaterialCode());
            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateMaterialHangTagNergyMerit(final MFBrandMasterEntity mfBrandMasterEntity,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialHangTagNergyMeritEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialHangTagNergyMeritRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialHangTagNergyMeritEntity> listBeforSortData = mfMaterialHangTagNergyMeritRepository
                    .findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialHangTagNergyMeritEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getMaterialCode());
            codeModel.setCodeName(entity.getMaterialCodeName());
            codeModel.setId(entity.getId());
            codeModel.setMoq(entity.getMoq());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMaterialAttentionTagModel> generateMaterialAttentionTag(final MFBrandMasterEntity mfBrandMasterEntity,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialAttentionTagEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialAttentionTagRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialAttentionTagEntity> listBeforSortData = mfMaterialAttentionTagRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialAttentionTagEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMaterialAttentionTagModel codeModel = new FukukitaruMaterialAttentionTagModel();
            codeModel.setId(entity.getId());
            codeModel.setMaterialType(entity.getMaterialType());
            codeModel.setMaterialTypeName(entity.getMaterialTypeName());
            codeModel.setMaterialCode(entity.getMaterialCode());
            codeModel.setMaterialCodeName(entity.getMaterialCodeName());
            codeModel.setMoq(entity.getMoq());
            codeModel.setSortOrder(entity.getSortOrder());
            codeModel.setType(entity.getType());
            codeModel.setProductName(entity.getProductName());
            codeModel.setTitle(entity.getTitle());
            codeModel.setSentence(entity.getSentence());
            codeModel.setOldMaterialCode(entity.getOldMaterialCode());
            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param masterIdList 資材IDリスト
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateMaterialHangTagAuxiliary(final String masterIdList,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialHangTagAuxiliaryEntity> streamEntity = null;
        if (FukukitaruMasterIdListType.ALL.getValue().equals(masterIdList)) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialHangTagAuxiliaryRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(masterIdList.split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialHangTagAuxiliaryEntity> listBeforSortData = mfMaterialHangTagAuxiliaryRepository
                    .findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialHangTagAuxiliaryEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getMaterialCode());
            codeModel.setCodeName(entity.getMaterialCodeName());
            codeModel.setId(entity.getId());
            codeModel.setMoq(entity.getMoq());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateMaterialWashAuxiliary(final MFBrandMasterEntity mfBrandMasterEntity,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialWashAuxiliaryEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialWashAuxiliaryRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialWashAuxiliaryEntity> listBeforSortData = mfMaterialWashAuxiliaryRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialWashAuxiliaryEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getMaterialCode());
            codeModel.setCodeName(entity.getMaterialCodeName());
            codeModel.setId(entity.getId());
            codeModel.setMoq(entity.getMoq());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 資材情報から{@link FukukitaruMasterModel} を生成する.
     * @param masterIdList 資材IDリスト
     * @param materialType {@link FukukitaruMasterMaterialType}
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateMaterialHangTag(final String masterIdList,
            final FukukitaruMasterMaterialType materialType) {
        Stream<MFMaterialHangTagEntity> streamEntity = null;
        if (FukukitaruMasterIdListType.ALL.getValue().equals(masterIdList)) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfMaterialHangTagRepository
                    .findByMaterialTypeAll(materialType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(masterIdList.split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFMaterialHangTagEntity> listBeforSortData = mfMaterialHangTagRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFMaterialHangTagEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getMaterialCode());
            codeModel.setCodeName(entity.getMaterialCodeName());
            codeModel.setId(entity.getId());
            codeModel.setMoq(entity.getMoq());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * リサイクル情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity MFBrandMasterEntity
     * @return FukukitaruMasterModelのリスト
     */
    private List<FukukitaruMasterModel> generateRecycl(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFRecycleEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfRecycleRepository.findByAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFRecycleEntity> listBeforSortData = mfRecycleRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFRecycleEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getRecycleCode());
            codeModel.setCodeName(entity.getRecycleName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * テープ情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateTapeType(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFTapeEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfTapeRepository.findByAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFTapeEntity> listBeforSortData = mfTapeRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFTapeEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getTapeCode());
            codeModel.setCodeName(entity.getTapeName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * テープ巾情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateTapeWidth(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFTapeWidthEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfTapeWidthRepository.findByAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFTapeWidthEntity> listBeforSortData = mfTapeWidthRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFTapeWidthEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getTapeWidthCode());
            codeModel.setCodeName(entity.getTapeWidthName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 洗濯マーク情報から{@link FukukitaruMasterModel} を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateWashPattern(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFWashPatternEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfWashPatternRepository.findByAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value))
                    .collect(Collectors.toList());
            final List<MFWashPatternEntity> listBeforSortData = mfWashPatternRepository.findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFWashPatternEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getWashPatternCode());
            codeModel.setCodeName(entity.getWashPatternName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * 品番情報から{@link ItemModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateItem(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final ItemModel item = new ItemModel();

        if (Objects.isNull(paramModel.getPartNoId())) {
            // 品番IDがNULLの場合は、空のリストを設定する
            returnModel.setItem(item);
            return;
        }

        final Optional<ExtendedTItemEntity> optionalItemEntity = extendedTItemRepository.findById(paramModel.getPartNoId());
        if (!optionalItemEntity.isPresent()) {
            //品番情報がない場合、空の品番情報モデルを投げる
            item.setId(paramModel.getPartNoId());
            returnModel.setItem(item);
            return;
        }
        final ExtendedTItemEntity entity = optionalItemEntity.get();

        // 品番情報のコピー
        BeanUtils.copyProperties(entity, item);
        item.setReadOnly(itemComponent.isReadOnly(entity.getExternalLinkingType()));

        // SKU情報を取得する
        item.setSkus(extendedTSkuRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code")))).stream()
                .map(tSku -> {
                    final SkuModel sku = new SkuModel();

                    // SKU情報のコピー
                    BeanUtils.copyProperties(tSku, sku);

                    return sku;
                }).collect(Collectors.toList()));

        // 組成情報を取得する
        item.setCompositions(extendedTCompositionRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code"), Order.asc("serial_number")))).stream()
                .map(tComposition -> {
                    final CompositionModel composition = new CompositionModel();

                    // 組成情報のコピー
                    BeanUtils.copyProperties(tComposition, composition);

                    return composition;
                }).collect(Collectors.toList()));

        returnModel.setItem(item);
    }

    /**
     * 発注情報から{@link OrderModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateOrder(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final OrderModel orderModel = new OrderModel();

        if (Objects.isNull(paramModel.getOrderId())) {
            //発注IDがない場合、空の発注情報モデルを設定する
            returnModel.setOrder(orderModel);
            return;
        }
        // 発注情報を取得し、データが存在しない場合は例外を投げる
        final Optional<ExtendedTOrderEntity> extendedTOrderEntity = extendedTOrderRepository.findById(paramModel.getOrderId());
        if (!extendedTOrderEntity.isPresent()) {
            //発注情報がない場合、空の発注情報モデルを設定する
            orderModel.setId(paramModel.getOrderId());
            returnModel.setOrder(orderModel);
            return;
        }

        final ExtendedTOrderEntity orderEntity = extendedTOrderEntity.get();

        BeanUtils.copyProperties(orderEntity, orderModel);

        // 読み取り専用設定
        orderModel.setReadOnly(orderComponent.isReadOnly(orderEntity.getExpenseItem()));

        returnModel.setOrder(orderModel);

    }

    /**
     * フクキタル品番情報から{@link ItemModel} を生成し、{@link FukukitaruItemComponent}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateFukukitaruItem(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        if (Objects.isNull(paramModel.getPartNoId())) {
            // 品番IDがNULLの場合は、空のリストを設定する
            returnModel.setFkItem(new FukukitaruItemModel());
            return;
        }

        // 品番情報からフクキタル品番情報を取得する
        returnModel.setFkItem(fukukitaruItemComponent.generatedFukukitaruItemModelSearchPartNoId(paramModel.getPartNoId())
                .orElseGet(() -> {
                    // フクキタル品番情報がない場合、空のフクキタル品番情報モデルを設定する
                    final FukukitaruItemModel fukukitaruItemModel = new FukukitaruItemModel();
                    fukukitaruItemModel.setPartNoId(paramModel.getPartNoId());
                    return fukukitaruItemModel;
                }));
    }

    /**
     * ブランドコード、アイテム、画面呼び出し元（洗濯ネーム、下札）によって、発注タイプを判別する.
     * ・ブランドコード「BV」の場合、アイテムが「A,I,N,R,U,W,X,Z」のいずれかの場合、「小物」と判別する
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateOrderType(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        returnModel.setOrderType(paramModel.getOrderType());
    }

    /**
     * ブランドコード、アイテム、画面呼び出し元（洗濯ネーム、下札）によって、発注タイプを判別する.
     * ・ブランドコード「BV」の場合、アイテムが「A,I,N,R,U,W,X,Z」のいずれかの場合、「小物」と判別する
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param parentOrderType 洗濯ネーム or 下札
     * @return ブランドコード
     */
    public FukukitaruMasterOrderType identifyOrderType(final String brandCode, final String itemCode, final FukukitaruMasterOrderType parentOrderType) {
        if (Objects.isNull(parentOrderType)) {
            return parentOrderType;
        }
        // ブランドコード「BV」
        if (brandCode.equals("BV")) {
            if (Arrays.asList(BV_KOMONO_ITEMS).stream().anyMatch(komonoItem -> komonoItem.equals(itemCode))) {
                switch (parentOrderType) {
                case WASH_NAME:
                    // 洗濯ネーム小物
                    return FukukitaruMasterOrderType.WASH_NAME_KOMONO;
                case HANG_TAG:
                    // 下札小物
                    return FukukitaruMasterOrderType.HANG_TAG_KOMONO;
                default:
                    break;
                }
            }
        }

        return parentOrderType;
    }

    /**
     * 洗濯ネーム情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListWashName(final ScreenSettingFukukiatruModel returnModel, final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final List<MFMaterialWashNameEntity> listMFMaterialEntity = mfMaterialWashNameRepository
                .findByMaterialTypeAll(FukukitaruMasterMaterialType.WASH_NAME, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                .getContent();

        returnModel.setListWashName(listMFMaterialEntity
                .stream()
                .map(entity -> {
                    final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
                    codeModel.setCode(entity.getMaterialCode());
                    codeModel.setCodeName(entity.getMaterialCodeName());
                    codeModel.setId(entity.getId());
                    codeModel.setMoq(entity.getMoq());

                    return codeModel;
                }).collect(Collectors.toList()));
    }

    /**
     * アテンション下札情報から{@link FukukitaruMasterModel} を生成し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateListAttentionHangTag(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final List<MFMaterialAttentionHangTagEntity> listMFMaterialEntity = mfMaterialAttentionHangTagRepository
                .findByMaterialTypeAll(FukukitaruMasterMaterialType.ATTENTION_HANG_TAG,
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                .getContent();

        returnModel.setListAttentionBottomBill(listMFMaterialEntity
                .stream()
                .map(entity -> {
                    final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
                    codeModel.setCode(entity.getMaterialCode());
                    codeModel.setCodeName(entity.getMaterialCodeName());
                    codeModel.setId(entity.getId());
                    codeModel.setMoq(entity.getMoq());

                    return codeModel;
                }).collect(Collectors.toList()));
    }

    /**
     * カテゴリコード情報から{@link FukukitaruMasterModel}を生成する.
     * @param mfBrandMasterEntity {@link MFBrandMasterEntity} instance
     * @return {@link FukukitaruMasterModel} instance List
     */
    private List<FukukitaruMasterModel> generateCategoryCode(final MFBrandMasterEntity mfBrandMasterEntity) {
        Stream<MFCategoryCodeEntity> streamEntity = null;
        if (StringUtils.isEmpty(mfBrandMasterEntity.getMasterIdList())) {
            return Collections.emptyList();
        } else if (FukukitaruMasterIdListType.ALL.getValue().equals(mfBrandMasterEntity.getMasterIdList())) {
            // 全ての情報をsort_orderの昇順で取得する
            streamEntity = mfCategoryCodeRepository
                    .findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))))
                    .stream();
        } else {
            // MasterIdListに定義されているID（カンマ区切り）の情報を取得する
            final List<BigInteger> ids = Stream.of(mfBrandMasterEntity.getMasterIdList().split(","))
                    .map(value -> new BigInteger(value)).collect(Collectors.toList());
            final List<MFCategoryCodeEntity> listBeforSortData = mfCategoryCodeRepository
                    .findByIds(ids, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            // MasterIdListに定義されている順番に並び替える
            final List<MFCategoryCodeEntity> sortData = new ArrayList<>();
            for (BigInteger sortId : ids) {
                listBeforSortData
                        .stream()
                        .filter(entity -> sortId.equals(entity.getId()))
                        .findFirst()
                        .ifPresent(entity -> sortData.add(entity));
            }

            streamEntity = sortData.stream();

        }
        // モデル変換
        return streamEntity.map(entity -> {
            final FukukitaruMasterModel codeModel = new FukukitaruMasterModel();
            codeModel.setCode(entity.getCategoryCode());
            codeModel.setCodeName(entity.getCategoryName());
            codeModel.setId(entity.getId());

            return codeModel;
        }).collect(Collectors.toList());
    }

    /**
     * サスティナブルマークから表示フラグを取得し、{@link ScreenSettingFukukiatruModel}に格納する.
     * @param returnModel {@link ScreenSettingFukukiatruModel} instance
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     */
    private void generateSustainableMarkDisplayFlg(final ScreenSettingFukukiatruModel returnModel,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        final MFBrandMasterEntity resultEntity = generateMFBrandMasterEntityByCode(FukukitaruMasterType.SUSTAINABLE_MARK, paramModel);
        if (Objects.isNull(resultEntity)) {
            // ブランド別マスタ情報が存在しない場合は、サスティナブルマーク非表示
            returnModel.setSustainableMarkDisplayFlg(false);
            return;
        }
        returnModel.setSustainableMarkDisplayFlg(resultEntity.getDisplayFlg().convertToValue());
    }

    /**
     * ブランドコードとアイテムコードで指定したフクキタルマスタ種別のフクキタルマスタ検索結果を取得する.
     *
     * アイテムコードに合致する結果がない場合、アイテムコードはNULLで再検索
     * @param fukukitaruMasterType フクキタルマスタ種別
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     * @return MFBrandMasterEntity 取得したフクキタルマスタ情報
     */
    private MFBrandMasterEntity generateMFBrandMasterEntityByCode(final FukukitaruMasterType fukukitaruMasterType,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        // アイテムコードありで検索
        final Optional<MFBrandMasterEntity> optionalResultExistsItemCode = mfBrandMasterRepository
                .findByBrandAndItemCodeAndMasterType(
                        paramModel.getBrandCode(),
                        paramModel.getItemCode(),
                        fukukitaruMasterType);

        if (optionalResultExistsItemCode.isPresent()) {
            return optionalResultExistsItemCode.get();
        }

        // アイテムコードNULLで再検索
        final Optional<MFBrandMasterEntity> optionalResultNotExistsItemCode = mfBrandMasterRepository
                .findByBrandAndItemCodeNullAndMasterType(
                        paramModel.getBrandCode(),
                        fukukitaruMasterType);

        if (optionalResultNotExistsItemCode.isPresent()) {
            return optionalResultNotExistsItemCode.get();
        } else {
            // 取得できなかった場合はNULLを返却
            return null;
        }
    }

    /**
     * ブランド・アイテムコード、デリバリ・発注種別で指定したフクキタルマスタ種別のフクキタルマスタ検索結果を取得する.
     *
     * アイテムコードに合致する結果がない場合、アイテムコードはNULLで再検索
     * @param fukukitaruMasterType フクキタルマスタ種別
     * @param paramModel {@link ScreenSettingFukukitaruComponentSearchConditionModel} instance
     * @return MFBrandMasterEntity 取得したフクキタルマスタ情報
     */
    private MFBrandMasterEntity generateMFBrandMasterEntityByCodeAndType(
            final FukukitaruMasterType fukukitaruMasterType,
            final ScreenSettingFukukitaruComponentSearchConditionModel paramModel) {
        // アイテムコードありで検索
        final Optional<MFBrandMasterEntity> optionalResultExistsItemCode = mfBrandMasterRepository
                .findByBrandAndItemCodeAndDeliveryAndOrderTypeAndMasterType(
                        paramModel.getBrandCode(),
                        paramModel.getItemCode(),
                        paramModel.getDeliveryType(),
                        paramModel.getOrderType(),
                        fukukitaruMasterType);

        if (optionalResultExistsItemCode.isPresent()) {
            return optionalResultExistsItemCode.get();
        }

        // アイテムコードNULLで再検索
        final Optional<MFBrandMasterEntity> optionalResultNotExistsItemCode = mfBrandMasterRepository
                .findByBrandAndItemCodeNullAndDeliveryeAndOrderTypeAndMasterType(
                        paramModel.getBrandCode(),
                        paramModel.getDeliveryType(),
                        paramModel.getOrderType(),
                        fukukitaruMasterType);

        if (optionalResultNotExistsItemCode.isPresent()) {
            return optionalResultNotExistsItemCode.get();
        } else {
            // 取得できなかった場合はNULLを返却
            return null;
        }
    }
}
