package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TFAttentionAppendicesTermEntity;
import jp.co.jun.edi.entity.TFItemEntity;
import jp.co.jun.edi.entity.TFWashAppendicesTermEntity;
import jp.co.jun.edi.entity.TFWashPatternEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFItemEntity;
import jp.co.jun.edi.model.FukukitaruItemAttentionAppendicesTermModel;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.FukukitaruItemWashAppendicesTermModel;
import jp.co.jun.edi.model.FukukitaruItemWashPatternModel;
import jp.co.jun.edi.repository.TFAttentionAppendicesTermRepository;
import jp.co.jun.edi.repository.TFItemRepository;
import jp.co.jun.edi.repository.TFWashAppendicesTermRepository;
import jp.co.jun.edi.repository.TFWashPatternRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFAttentionAppendicesTermRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFWashAppendicesTermRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFWashPatternRepository;
import jp.co.jun.edi.util.DateUtils;

/**
 * フクキタル品番情報関連のコンポーネント.
 */
@Component
public class FukukitaruItemComponent extends GenericComponent {
    @Autowired
    private TFItemRepository tfItemRepository;
    @Autowired
    private TFWashAppendicesTermRepository tfWashAppendicesTermRepository;

    @Autowired
    private TFAttentionAppendicesTermRepository tfAttentionAppendicesTermRepository;

    @Autowired
    private TFWashPatternRepository tfWashPatternRepository;

    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;

    @Autowired
    private ExtendedTFItemRepository extendedTFItemRepository;

    @Autowired
    private ExtendedTFWashPatternRepository extendedTFWashPatternRepository;

    @Autowired
    private ExtendedTFWashAppendicesTermRepository extendedTFWashAppendicesTermRepository;

    @Autowired
    private ExtendedTFAttentionAppendicesTermRepository extendedTFAttentionAppendicesTermRepository;

    /**
     * フクキタル品番IDから以下の情報を取得し、FukukitaruItemModelに格納する.
     * ・フクキタル品番情報
     * ・フクキタル用洗濯ネーム付記用語情報
     * ・フクキタル用アテンション付記用語情報
     * ・フクキタル用洗濯マーク情報を
     * @param fItemId フクキタル品番ID
     * @return FukukitaruItemModel
     */
    public Optional<FukukitaruItemModel> generatedFukukitaruItemModelSearchFItemId(final BigInteger fItemId) {
        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        final Optional<ExtendedTFItemEntity> optionalExtendedTFItemEntity = extendedTFItemRepository.findByFItemId(fItemId);
        if (!optionalExtendedTFItemEntity.isPresent()) {
            // フクキタル品番情報が存在しない場合はNULLを返す
            return Optional.empty();
        }
        return generatedFukukitaruItemModel(optionalExtendedTFItemEntity.get());
    }

    /**
     * 品番IDから以下の情報を取得し、FukukitaruItemModelに格納する.
     * ・フクキタル品番情報
     * ・フクキタル用洗濯ネーム付記用語情報
     * ・フクキタル用アテンション付記用語情報
     * ・フクキタル用洗濯マーク情報
     * @param partNoId 品番ID
     * @return FukukitaruItemModel
     */
    public Optional<FukukitaruItemModel> generatedFukukitaruItemModelSearchPartNoId(final BigInteger partNoId) {
        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        final Optional<ExtendedTFItemEntity> optionalExtendedTFItemEntity = extendedTFItemRepository.findByPartNoId(partNoId);
        if (!optionalExtendedTFItemEntity.isPresent()) {
            // フクキタル品番情報が存在しない場合はNULLを返す
            return Optional.empty();
        }
        return generatedFukukitaruItemModel(optionalExtendedTFItemEntity.get());
    }

    /**
     * フクキタル品番情報から以下の情報を取得し、FukukitaruItemModelに格納する.
     * ・フクキタル用洗濯ネーム付記用語情報
     * ・フクキタル用アテンション付記用語情報
     * ・フクキタル用洗濯マーク情報
     * @param extendedTFItemEntity フクキタル品番情報
     * @return FukukitaruItemModel
     */
    private Optional<FukukitaruItemModel> generatedFukukitaruItemModel(final ExtendedTFItemEntity extendedTFItemEntity) {

        final BigInteger fItemId = extendedTFItemEntity.getId();

        final FukukitaruItemModel fukukitaruItemModel = new FukukitaruItemModel();
        fukukitaruItemModel.setId(extendedTFItemEntity.getId());
        fukukitaruItemModel.setPartNoId(extendedTFItemEntity.getPartNoId());
        fukukitaruItemModel.setCategoryCode(extendedTFItemEntity.getCategoryCode());
        fukukitaruItemModel.setNergyBillCode1(extendedTFItemEntity.getNergyBillCode1());
        fukukitaruItemModel.setNergyBillCode2(extendedTFItemEntity.getNergyBillCode2());
        fukukitaruItemModel.setNergyBillCode3(extendedTFItemEntity.getNergyBillCode3());
        fukukitaruItemModel.setNergyBillCode4(extendedTFItemEntity.getNergyBillCode4());
        fukukitaruItemModel.setNergyBillCode5(extendedTFItemEntity.getNergyBillCode5());
        fukukitaruItemModel.setNergyBillCode6(extendedTFItemEntity.getNergyBillCode6());
        fukukitaruItemModel.setPrintAppendicesTerm(extendedTFItemEntity.getPrintAppendicesTerm());
        fukukitaruItemModel.setPrintCoo(extendedTFItemEntity.getPrintCoo());
        fukukitaruItemModel.setPrintParts(extendedTFItemEntity.getPrintParts());
        fukukitaruItemModel.setPrintQrcode(extendedTFItemEntity.getPrintQrcode());
        fukukitaruItemModel.setPrintSize(extendedTFItemEntity.getPrintSize());
        fukukitaruItemModel.setPrintWashPattern(extendedTFItemEntity.getPrintWashPattern());
        fukukitaruItemModel.setRecycleMark(extendedTFItemEntity.getRecycleMark());
        fukukitaruItemModel.setReefurPrivateBrandCode(extendedTFItemEntity.getReefurPrivateBrandCode());
        fukukitaruItemModel.setSaturdaysPrivateNyPartNo(extendedTFItemEntity.getSaturdaysPrivateNyPartNo());
        fukukitaruItemModel.setStickerTypeCode(extendedTFItemEntity.getStickerTypeCode());
        fukukitaruItemModel.setTapeCode(extendedTFItemEntity.getTapeCode());
        fukukitaruItemModel.setTapeWidthCode(extendedTFItemEntity.getTapeWidthCode());
        fukukitaruItemModel.setCnProductCategory(extendedTFItemEntity.getCnProductCategory());
        fukukitaruItemModel.setCnProductType(extendedTFItemEntity.getCnProductType());
        fukukitaruItemModel.setTapeName(extendedTFItemEntity.getTapeName());
        fukukitaruItemModel.setTapeWidthName(extendedTFItemEntity.getTapeWidthName());
        fukukitaruItemModel.setRecycleName(extendedTFItemEntity.getRecycleName());
        fukukitaruItemModel.setSealName(extendedTFItemEntity.getSealName());
        fukukitaruItemModel.setProductCategoryName(extendedTFItemEntity.getProductCategoryName());
        fukukitaruItemModel.setProductTypeName(extendedTFItemEntity.getProductTypeName());
        fukukitaruItemModel.setPrintSustainableMark(extendedTFItemEntity.getPrintSustainableMark());
        fukukitaruItemModel.setListItemAttentionAppendicesTerm(generatedFukukitaruItemAttentionAppendicesTermModel(fItemId));
        fukukitaruItemModel.setListItemWashAppendicesTerm(generatedFukukitaruItemWashAppendicesTermModel(fItemId));
        fukukitaruItemModel.setListItemWashPattern(generatedFukukitaruItemWashPatternModel(fItemId));

        return Optional.of(fukukitaruItemModel);
    }

    /**
     * フクキタル品番IDからフクキタル用アテンション付記用語情報を取得し、FukukitaruItemAttentionAppendicesTermModelを生成する.
     * @param fItemId フクキタル品番ID
     * @return FukukitaruItemAttentionAppendicesTermModelリスト
     */
    private List<FukukitaruItemAttentionAppendicesTermModel> generatedFukukitaruItemAttentionAppendicesTermModel(final BigInteger fItemId) {
        return extendedTFAttentionAppendicesTermRepository
                .findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .map(entity -> {
                    final FukukitaruItemAttentionAppendicesTermModel model = new FukukitaruItemAttentionAppendicesTermModel();
                    model.setId(entity.getId());
                    model.setAppendicesTermId(entity.getAppendicesTermId());
                    model.setColorCode(entity.getColorCode());
                    model.setAppendicesTermCode(entity.getAppendicesTermCode());
                    model.setAppendicesTermCodeName(entity.getAppendicesTermCodeName());

                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * フクキタル品番IDからフクキタル用洗濯ネーム付記用語情報を取得し、FukukitaruItemAttentionAppendicesTermModelを生成する.
     * @param fItemId フクキタル品番ID
     * @return FukukitaruItemAttentionAppendicesTermModelリスト
     */
    private List<FukukitaruItemWashAppendicesTermModel> generatedFukukitaruItemWashAppendicesTermModel(final BigInteger fItemId) {
        return extendedTFWashAppendicesTermRepository
                .findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .map(entity -> {
                    final FukukitaruItemWashAppendicesTermModel model = new FukukitaruItemWashAppendicesTermModel();
                    model.setId(entity.getId());
                    model.setAppendicesTermId(entity.getAppendicesTermId());
                    model.setColorCode(entity.getColorCode());
                    model.setAppendicesTermCode(entity.getAppendicesTermCode());
                    model.setAppendicesTermCodeName(entity.getAppendicesTermCodeName());

                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * フクキタル品番IDからフクキタル用洗濯マーク情報を取得し、FukukitaruItemAttentionAppendicesTermModelを生成する.
     * @param fItemId フクキタル品番ID
     * @return FukukitaruItemAttentionAppendicesTermModelリスト
     */
    private List<FukukitaruItemWashPatternModel> generatedFukukitaruItemWashPatternModel(final BigInteger fItemId) {
        return extendedTFWashPatternRepository
                .findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .map(entity -> {
                    final FukukitaruItemWashPatternModel fukukitaruItemWashPatternModel = new FukukitaruItemWashPatternModel();
                    fukukitaruItemWashPatternModel.setId(entity.getId());
                    fukukitaruItemWashPatternModel.setWashPatternId(entity.getWashPatternId());
                    fukukitaruItemWashPatternModel.setColorCode(entity.getColorCode());
                    fukukitaruItemWashPatternModel.setWashPatternCode(entity.getWashPatternCode());
                    fukukitaruItemWashPatternModel.setWashPatternName(entity.getWashPatternName());
                    return fukukitaruItemWashPatternModel;
                }).collect(Collectors.toList());
    }

    /**
     * フクキタル品番情報の新規登録.
     * @param fukukitaruItemModel フクキタル品番情報モデル
     */
    public void save(final FukukitaruItemModel fukukitaruItemModel) {
        // IDは自動採番のためEntityにNULLを設定する
        fukukitaruItemModel.setId(null);

        // モデルからエンティティを生成する
        final TFItemEntity tFItemEntity = setValueForTFItemEntity(fukukitaruItemModel);

        // フクキタル品番情報の登録
        tfItemRepository.save(tFItemEntity);

        // レスポンスデータを設定する
        fukukitaruItemModel.setId(tFItemEntity.getId());

        // フクキタル洗濯ネーム付記用語の登録
        upsertAndOtherwiseDeleteTFWashAppendicesTermEntity(fukukitaruItemModel, tFItemEntity);

        // フクキタルアテンションタグ付記用語の登録
        upsertAndOtherwiseDeleteTFAttentionAppendicesTermEntity(fukukitaruItemModel, tFItemEntity);

        // フクキタル洗濯マークの登録
        upsertAndOtherwiseDeleteTFWashPatternEntity(fukukitaruItemModel, tFItemEntity);
    }

    /**
     * フクキタル品番情報の更新登録.
     * @param fukukitaruItemModel フクキタル品番情報モデル
     */
    public void update(final FukukitaruItemModel fukukitaruItemModel) {
        // モデルからエンティティを生成する
        final TFItemEntity tFItemEntity = setValueForTFItemEntity(fukukitaruItemModel);

        // フクキタル品番情報の登録
        tfItemRepository.save(tFItemEntity);

        // レスポンスデータを設定する
        fukukitaruItemModel.setId(tFItemEntity.getId());

        // フクキタル洗濯ネーム付記用語の登録
        upsertAndOtherwiseDeleteTFWashAppendicesTermEntity(fukukitaruItemModel, tFItemEntity);

        // フクキタルアテンションタグ付記用語の登録
        upsertAndOtherwiseDeleteTFAttentionAppendicesTermEntity(fukukitaruItemModel, tFItemEntity);

        // フクキタル洗濯マークの登録
        upsertAndOtherwiseDeleteTFWashPatternEntity(fukukitaruItemModel, tFItemEntity);
    }

    /**
     * TFWashPatternEntityをUPSERTする.
     * UPSERTされた情報以外の、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
     * @param fukukitaruItemModel FukukitaruItemModel
     * @param tFItemEntity フクキタル品番情報
     */
    private void upsertAndOtherwiseDeleteTFWashPatternEntity(final FukukitaruItemModel fukukitaruItemModel, final TFItemEntity tFItemEntity) {
        final List<TFWashPatternEntity> listTFWashPatternEntity = setValueForTFWashPatternEntity(fukukitaruItemModel);
        if (listTFWashPatternEntity.isEmpty()) {
            tfWashPatternRepository.updateDeleteAtByFItemId(tFItemEntity.getId(), DateUtils.createNow());
            return;
        }
        tfWashPatternRepository.saveAll(listTFWashPatternEntity);
        tfWashPatternRepository.updateDeleteByFItemIdNotInIds(tFItemEntity.getId(),
                listTFWashPatternEntity.stream().map(data -> data.getId()).collect(Collectors.toList()),
                DateUtils.createNow());
    }

    /**
     * TFAttentionAppendicesTermEntityをUPSERTする.
     * UPSERTされた情報以外の、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
     * @param fukukitaruItemModel FukukitaruItemModel
     * @param tFItemEntity フクキタル品番情報
     */
    private void upsertAndOtherwiseDeleteTFAttentionAppendicesTermEntity(final FukukitaruItemModel fukukitaruItemModel, final TFItemEntity tFItemEntity) {
        final List<TFAttentionAppendicesTermEntity> listTFAttentionAppendicesTermEntity = setValueForTFAttentionAppendicesTermEntity(fukukitaruItemModel);
        if (listTFAttentionAppendicesTermEntity.isEmpty()) {
            // 更新データが存在しない場合、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
            tfAttentionAppendicesTermRepository.updateDeleteAtByFItemId(tFItemEntity.getId(), DateUtils.createNow());
            return;
        }

        // 更新データ以外の、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
        tfAttentionAppendicesTermRepository.saveAll(listTFAttentionAppendicesTermEntity);
        tfAttentionAppendicesTermRepository.updateDeleteByFItemIdNotInIds(tFItemEntity.getId(),
                listTFAttentionAppendicesTermEntity.stream().map(data -> data.getId()).collect(Collectors.toList()),
                DateUtils.createNow());
    }

    /**
     * TFWashAppendicesTermEntityをUPSERTする.
     * UPSERTされた情報以外の、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
     * @param fukukitaruItemModel FukukitaruItemModel
     * @param tFItemEntity フクキタル品番情報
     */
    private void upsertAndOtherwiseDeleteTFWashAppendicesTermEntity(final FukukitaruItemModel fukukitaruItemModel, final TFItemEntity tFItemEntity) {
        final List<TFWashAppendicesTermEntity> listTFWashAppendicesTermEntity = setValueForTFWashAppendicesTermEntity(fukukitaruItemModel);
        if (listTFWashAppendicesTermEntity.isEmpty()) {
            // 更新データが存在しない場合、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
            tfWashAppendicesTermRepository.updateDeleteAtByFItemId(tFItemEntity.getId(),
                    DateUtils.createNow());
            return;
        }

        // 更新データ以外の、フクキタル品番IDに紐づく削除されていない情報は、全て削除する
        tfWashAppendicesTermRepository.saveAll(listTFWashAppendicesTermEntity);
        tfWashAppendicesTermRepository.updateDeleteByFItemIdNotInIds(tFItemEntity.getId(),
                listTFWashAppendicesTermEntity.stream().map(data -> data.getId()).collect(Collectors.toList()),
                DateUtils.createNow());

    }

    /**
     * フクキタル品番情報の削除.
     * @param tfItemEntity フクキタル品番情報
     */
    public void delete(final TFItemEntity tfItemEntity) {
        // フクキタル品番ID
        final BigInteger fItemId = tfItemEntity.getId();

        // フクキタル品番IDに紐づくフクキタル品番情報の削除
        tfItemRepository.updateDeleteAtByFItemId(fItemId);
        tfAttentionAppendicesTermRepository.updateDeleteAtByFItemId(fItemId, DateUtils.createNow());
        tfWashAppendicesTermRepository.updateDeleteAtByFItemId(fItemId, DateUtils.createNow());
        tfWashPatternRepository.updateDeleteAtByFItemId(fItemId, DateUtils.createNow());

        // フクキタル品番IDに紐づくフクキタル発注情報の削除
        fukukitaruOrderComponent.delete(fItemId);

    }

    /**
     * {@link FukukitaruItemModel} を {@link TFItemEntity} に詰め替え.
     * @param fukukitaruItemModel {@link FukukitaruItemModel}
     * @return {@link TFItemEntity}
     */
    private TFItemEntity setValueForTFItemEntity(final FukukitaruItemModel fukukitaruItemModel) {
        final TFItemEntity entity = new TFItemEntity();
        entity.setId(fukukitaruItemModel.getId());
        entity.setPartNoId(fukukitaruItemModel.getPartNoId());
        entity.setCategoryCode(fukukitaruItemModel.getCategoryCode());
        entity.setNergyBillCode1(fukukitaruItemModel.getNergyBillCode1());
        entity.setNergyBillCode2(fukukitaruItemModel.getNergyBillCode2());
        entity.setNergyBillCode3(fukukitaruItemModel.getNergyBillCode3());
        entity.setNergyBillCode4(fukukitaruItemModel.getNergyBillCode4());
        entity.setNergyBillCode5(fukukitaruItemModel.getNergyBillCode5());
        entity.setNergyBillCode6(fukukitaruItemModel.getNergyBillCode6());
        entity.setPrintAppendicesTerm(fukukitaruItemModel.getPrintAppendicesTerm());
        entity.setPrintCoo(fukukitaruItemModel.getPrintCoo());
        entity.setPrintParts(fukukitaruItemModel.getPrintParts());
        entity.setPrintQrcode(fukukitaruItemModel.getPrintQrcode());
        entity.setPrintSize(fukukitaruItemModel.getPrintSize());
        entity.setPrintWashPattern(fukukitaruItemModel.getPrintWashPattern());
        entity.setRecycleMark(fukukitaruItemModel.getRecycleMark());
        entity.setReefurPrivateBrandCode(fukukitaruItemModel.getReefurPrivateBrandCode());
        entity.setSaturdaysPrivateNyPartNo(fukukitaruItemModel.getSaturdaysPrivateNyPartNo());
        entity.setStickerTypeCode(fukukitaruItemModel.getStickerTypeCode());
        entity.setTapeCode(fukukitaruItemModel.getTapeCode());
        entity.setTapeWidthCode(fukukitaruItemModel.getTapeWidthCode());
        entity.setCnProductCategory(fukukitaruItemModel.getCnProductCategory());
        entity.setCnProductType(fukukitaruItemModel.getCnProductType());
        entity.setPrintSustainableMark(fukukitaruItemModel.getPrintSustainableMark());

        return entity;
    }

    /**
     * {@link List<FukukitaruWashAppendicesTermModel>} を {@link List<TFWashAppendicesTermEntity>} に詰め替え.
     * @param fukukitaruItemModel {@link FukukitaruItemModel}
     * @return {@link List<TFWashAppendicesTermEntity>}
     */
    private List<TFWashAppendicesTermEntity> setValueForTFWashAppendicesTermEntity(final FukukitaruItemModel fukukitaruItemModel) {
        if (!Optional.ofNullable(fukukitaruItemModel.getListItemWashAppendicesTerm()).isPresent()) {
            // NULLの場合からリストを返す
            return new ArrayList<TFWashAppendicesTermEntity>();
        }
        return fukukitaruItemModel.getListItemWashAppendicesTerm().stream().map(model -> {
            final TFWashAppendicesTermEntity entity = new TFWashAppendicesTermEntity();
            entity.setPartNoId(fukukitaruItemModel.getPartNoId());
            entity.setFItemId(fukukitaruItemModel.getId());
            entity.setId(model.getId());
            entity.setColorCode(model.getColorCode());
            entity.setAppendicesTermId(model.getAppendicesTermId());
            return entity;
        }).collect(Collectors.toList());
    }

    /**
     * {@link List<FukukitaruAttentionAppendicesTermModel>} を {@link List<TFAttentionAppendicesTermEntity>} に詰め替え.
     * @param fukukitaruItemModel {@link FukukitaruItemModel}
     * @return {@link List<TFAttentionAppendicesTermEntity>}
     */
    private List<TFAttentionAppendicesTermEntity> setValueForTFAttentionAppendicesTermEntity(final FukukitaruItemModel fukukitaruItemModel) {
        if (!Optional.ofNullable(fukukitaruItemModel.getListItemAttentionAppendicesTerm()).isPresent()) {
            // NULLの場合からリストを返す
            return new ArrayList<TFAttentionAppendicesTermEntity>();
        }
        return fukukitaruItemModel.getListItemAttentionAppendicesTerm().stream().map(model -> {
            final TFAttentionAppendicesTermEntity entity = new TFAttentionAppendicesTermEntity();
            entity.setPartNoId(fukukitaruItemModel.getPartNoId());
            entity.setFItemId(fukukitaruItemModel.getId());
            entity.setId(model.getId());
            entity.setColorCode(model.getColorCode());
            entity.setAppendicesTermId(model.getAppendicesTermId());
            return entity;
        }).collect(Collectors.toList());
    }

    /**
     * {@link List<FukukitaruWashPatternModel>} を {@link List<TFWashPatternEntity>} に詰め替え.
     * @param fukukitaruItemModel {@link FukukitaruItemModel}
     * @return {@link List<TFWashPatternEntity>}
     */
    private List<TFWashPatternEntity> setValueForTFWashPatternEntity(final FukukitaruItemModel fukukitaruItemModel) {
        if (!Optional.ofNullable(fukukitaruItemModel.getListItemWashPattern()).isPresent()) {
            // NULLの場合からリストを返す
            return new ArrayList<TFWashPatternEntity>();
        }
        return fukukitaruItemModel.getListItemWashPattern().stream().map(model -> {
            final TFWashPatternEntity entity = new TFWashPatternEntity();
            entity.setPartNoId(fukukitaruItemModel.getPartNoId());
            entity.setFItemId(fukukitaruItemModel.getId());
            entity.setId(model.getId());
            entity.setColorCode(model.getColorCode());
            entity.setWashPatternId(model.getWashPatternId());
            return entity;
        }).collect(Collectors.toList());
    }
}
