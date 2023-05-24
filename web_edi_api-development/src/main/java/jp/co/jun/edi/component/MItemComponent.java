package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import jp.co.jun.edi.component.model.MItemModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MItemPartsEntity;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.MCodmstStaffRepository;
import jp.co.jun.edi.repository.MItemPartsRepository;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.repository.MSizmstRepository;
import jp.co.jun.edi.repository.specification.JunpcCodmstStaffSpecification;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 品番のマスタデータ取得用のコンポーネント.
 */
@Component
public class MItemComponent extends GenericComponent {
    /** コードマスタ初期化フラグ.  */
    private static final String M_CODMST_INITIAL_FLAG = "1";

    /** 丸井 - 丸井品番のデフォルト値.  */
    private static final String MARUI_DEFAULT_VALUE = "000000";

    /** 統計情報 - ゾーンのデフォルト値.  */
    private static final String ZONE_DEFAULT_VALUE = "90";

    /** 統計情報 - 展開のデフォルト値.  */
    private static final String OUTLET_DEFAULT_VALUE = "00";

    /** 工場マスタの検索対象の仕入先区分.  */
    private static final String M_KOJMST_SIRKBN = "10";

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private MCodmstStaffRepository mCodmstStaffRepository;

    @Autowired
    private JunpcCodmstStaffSpecification staffSpec;

    @Autowired
    private MSizmstRepository mSizmstRepository;

    @Autowired
    private MSirmstRepository mSirmstRepository;

    @Autowired
    private MKojmstRepository mKojmstRepository;

    @Autowired
    private MItemPartsRepository mItemPartsRepository;

    /**
     * マスタデータを取得する.
     *
     * @return {@link MItemModel} instance
     */
    public MItemModel getMasterData() {
        final MItemModel data = new MItemModel();

        // 原産国リストを取得
        data.setOriginCountries(getOriginCountries());

        // 色リストを取得
        data.setColors(getColors());

        // 組成リストを取得
        data.setCompositions(getCompositions());

        // 組成マップを取得（頻繁に使用するため、性能を考慮しマップ化）
        data.setCompositionMap(data.getCompositions().stream().collect(Collectors.toMap(v -> v.getCode1(), v -> v)));

        // 統計情報 - 素材リストを取得
        data.setMaterials(getMaterials());

        // 統計情報 - 展開リストを取得
        data.setOutlets(getOutlets());

        return data;
    }

    /**
     * 検索キーが変更された場合、それぞれの検索キーに関連するマスタデータを取得して設定する.
     *
     * @param data {@link MItemModel} instance
     */
    public void setMasterDataIfSearchKeyWasChanged(final MItemModel data) {
        // ブランドが変更された場合、ブランド関連のマスタデータを設定
        setBrandMasterDataIfSearchKeyWasChanged(data);

        // 品種（ブランド、アイテム）が変更された場合、アイテム関連のマスタデータを設定
        setItemMasterDataIfSearchKeyWasChanged(data);

        // 生産メーカーが変更された場合、生産メーカー関連のマスタデータを設定
        setMdfMakerMasterDataIfSearchKeyWasChanged(data);

        // 担当が変更された場合、担当関連のマスタデータを設定
        setStaffMasterDataIfSearchKeyWasChanged(data);

        data.setPreBrandCode(data.getBrandCode());
        data.setPreItemCode(data.getItemCode());
        data.setPreMdfMakerFactoryCode(data.getMdfMakerFactoryCode());
        data.setPreMdfMakerCode(data.getMdfMakerCode());
        data.setPrePlannerCode(data.getPlannerCode());
        data.setPreMdfStaffCode(data.getMdfStaffCode());
        data.setPrePatanerCode(data.getPatanerCode());
    }

    /**
     * コードマスタから初期値フラグが設定されているレコードを取得する.
     *
     * @param stream コードマスタ
     * @return 初期値レコード
     */
    public Optional<MCodmstEntity> findByInitial(final Stream<MCodmstEntity> stream) {
        return stream.filter(v -> StringUtils.equals(v.getItem30(), M_CODMST_INITIAL_FLAG)).findFirst();
    }

    /**
     * ブランドが変更された場合、ブランド関連のマスタデータを取得して設定する.
     *
     * @param data {@link MItemModel} instance
     */
    private void setBrandMasterDataIfSearchKeyWasChanged(final MItemModel data) {
        if (StringUtils.equals(data.getPreBrandCode(), data.getBrandCode())) {
            return;
        }

        // 丸井 - Voi区分リストを取得
        data.setVoiSections(getVoiSections(data.getBrandCode()));

        // 統計情報 - ゾーンリストを取得
        data.setZones(getZones(data.getBrandCode()));

        // 統計情報 - サブブランドリストを取得
        data.setSubBrands(getSubBrands(data.getBrandCode()));

        // 統計情報 - テイストリストを取得
        data.setTastes(getTastes(data.getBrandCode()));

        // 統計情報 - タイプ3リストを取得
        data.setType3s(getType3s(data.getBrandCode()));
    }

    /**
     * 品種（ブランド、アイテム）が変更された場合、アイテム関連のマスタデータを取得して設定する.
     *
     * @param data {@link MItemModel} instance
     */
    private void setItemMasterDataIfSearchKeyWasChanged(final MItemModel data) {
        if (!StringUtils.equals(data.getPreBrandCode(), data.getBrandCode())
                || !StringUtils.equals(data.getPreItemCode(), data.getItemCode())) {
            // アイテムを取得
            data.setItem(getItem(data.getBrandCode(), data.getItemCode()));

            // サイズリストを取得
            data.setSizes(getSizes(data.getBrandCode(), data.getItemCode()));

            // 丸井 - 丸井品番リストを取得
            data.setMaruiItems(getMaruiItems(data.getBrandCode(), data.getItemCode()));
        }

        if (!StringUtils.equals(data.getPreItemCode(), data.getItemCode())) {
            // パーツリストを取得
            data.setItemParts(getItemParts(data.getItemCode()));

            // パーツマップを取得（頻繁に使用するため、性能を考慮しマップ化）
            data.setItemPartMap(data.getItemParts().stream().collect(Collectors.toMap(v -> v.getId().toString(), v -> v)));

            // 統計情報 - タイプ1リストを取得
            data.setType1s(getType1s(data.getItemCode()));

            // 統計情報 - タイプ2リストを取得
            data.setType2s(getType2s(data.getItemCode()));
        }
    }

    /**
     * 生産メーカーが変更された場合、生産メーカー関連のマスタデータを取得して設定する.
     *
     * @param data {@link MItemModel} instance
     */
    private void setMdfMakerMasterDataIfSearchKeyWasChanged(final MItemModel data) {
        if (!StringUtils.equals(data.getPreMdfMakerCode(), data.getMdfMakerCode())
                || !StringUtils.equals(data.getPreMdfMakerFactoryCode(), data.getMdfMakerFactoryCode())) {
            // 生産工場を取得
            data.setMdfMakerFactory(getMdfMakerFactory(data.getMdfMakerCode(), data.getMdfMakerFactoryCode()));
        }

        if (!StringUtils.equals(data.getPreMdfMakerCode(), data.getMdfMakerCode())) {
            // 生産メーカーを取得
            data.setMdfMaker(getMdfMaker(data.getMdfMakerCode()));
        }
    }

    /**
     * 担当が変更された場合、担当関連のマスタデータを取得して設定する.
     *
     * @param data {@link MItemModel} instance
     */
    private void setStaffMasterDataIfSearchKeyWasChanged(final MItemModel data) {
        if (!StringUtils.equals(data.getPrePlannerCode(), data.getPlannerCode())) {
            // 企画担当を取得
            data.setPlanner(getStaff(data.getPlannerCode()));
        }

        if (!StringUtils.equals(data.getPreMdfStaffCode(), data.getMdfStaffCode())) {
            // 製造担当を取得
            data.setMdfStaff(getStaff(data.getMdfStaffCode()));
        }

        if (!StringUtils.equals(data.getPrePatanerCode(), data.getPatanerCode())) {
            // パターンナーを取得
            data.setPataner(getStaff(data.getPatanerCode()));
        }
    }

    /**
     * アイテムのマスタデータを取得する.
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @return アイテムのマスタデータ
     */
    private MCodmstEntity getItem(final String brandCode, final String itemCode) {
        return mCodmstRepository.findByTblidAndCode1AndCode2OrderById(
                MCodmstTblIdType.ITEM.getValue(),
                StringUtils.defaultString(brandCode),
                StringUtils.defaultString(itemCode),
                createOnePageRequest()).stream().findFirst().orElse(null);
    }

    /**
     * 色のマスタデータを取得する.
     *
     * @return 色のマスタデータ
     */
    private List<MCodmstEntity> getColors() {
        return mCodmstRepository.findByTblIdOrderByCode1(
                MCodmstTblIdType.COLOR.getValue(),
                createMaxPageRequest()).getContent();
    }

    /**
     * サイズのマスタデータを取得する.
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @return サイズのマスタデータ
     */
    private List<MSizmstEntity> getSizes(final String brandCode, final String itemCode) {
        return mSizmstRepository.findByHscdOrderByJun(
                StringUtils.defaultString(brandCode) + StringUtils.defaultString(itemCode),
                createMaxPageRequest()).getContent();
    }

    /**
     * パーツのマスタデータを取得する.
     *
     * @param itemCode アイテムコード
     * @return パーツのマスタデータ
     */
    private List<MItemPartsEntity> getItemParts(final String itemCode) {
        return mItemPartsRepository.findByParts(
                StringUtils.defaultString(itemCode),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder")))).getContent();
    }

    /**
     * 組成のマスタデータを取得する.
     *
     * @return 組成のマスタデータ
     */
    private List<MCodmstEntity> getCompositions() {
        return mCodmstRepository.findByTblIdOrderByCode1(
                MCodmstTblIdType.COMPOSITION.getValue(),
                createMaxPageRequest()).getContent();
    }

    /**
     * 生産メーカーのマスタデータを取得する.
     *
     * @param mdfMakerCode 生産メーカーコード
     * @return 生産メーカーのマスタデータ
     */
    private MSirmstEntity getMdfMaker(final String mdfMakerCode) {
        if (StringUtils.isEmpty(mdfMakerCode)) {
            return null;
        }

        return mSirmstRepository.findBySire(
                mdfMakerCode)
                .orElse(null);
    }

    /**
     * 生産工場のマスタデータを取得する.
     *
     * @param mdfMakerCode 生産メーカーコード
     * @param mdfMakerFactoryCode 生産工場コード
     * @return 生産工場のマスタデータ
     */
    private MKojmstEntity getMdfMakerFactory(final String mdfMakerCode, final String mdfMakerFactoryCode) {
        if (StringUtils.isEmpty(mdfMakerCode) && StringUtils.isEmpty(mdfMakerFactoryCode)) {
            return null;
        }

        return mKojmstRepository.findBySireAndSirkbnAndKojcd(
                StringUtils.defaultString(mdfMakerCode),
                M_KOJMST_SIRKBN,
                StringUtils.defaultString(mdfMakerFactoryCode))
                .orElse(null);
    }

    /**
     * 原産国のマスタデータを取得する.
     *
     * @return 原産国のマスタデータ
     */
    private List<MCodmstEntity> getOriginCountries() {
        return mCodmstRepository.findByTblIdOrderByCode1(
                MCodmstTblIdType.ORIGIN_COUNTRY.getValue(),
                createMaxPageRequest()).getContent();
    }

    /**
     * 担当関連のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、検索条件からブランドコード、職種を除外する.
     * </pre>
     *
     * @param staffCode 担当コード
     * @return 担当関連のマスタデータ
     */
    private MCodmstEntity getStaff(final String staffCode) {
        if (StringUtils.isEmpty(staffCode)) {
            return null;
        }

        return mCodmstStaffRepository.findAll(Specification
                .where(staffSpec.mntflgContains())
                .and(staffSpec.tblidContains(MCodmstTblIdType.STAFF.getValue()))
                .and(staffSpec.staffCodeMatchContains(staffCode))
                .and(staffSpec.junotSearchTargetContains())
                .and(staffSpec.notDeleteContains()),
                createOnePageRequest())
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 丸井 - 丸井品番のマスタデータを取得する.
     *
     * <pre>
     * - 1 初期値設定処理
     *  - 1-1 ブランドコードに紐づく丸井品番が存在しない場合、
     *        「000000」を初期値として追加する。
     *
     *  - 1-2 ブランドコード・アイテムコードに紐づく丸井品番が存在する場合
     *        以下の条件に該当する丸井品番を初期値として設定する。
     *
     *        (1) item30に1が設定されている丸井品番
     *        (2) 一番コード値が小さい丸井品番
     * </pre>
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @return 丸井 - 丸井品番のマスタデータ
     */
    private List<MCodmstEntity> getMaruiItems(final String brandCode, final String itemCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByMaruiItem(
                MCodmstTblIdType.MARUI_ITEM.getValue(),
                brandCode,
                createMaxPageRequest()));

        if (list.isEmpty()) {
            // レコードがない場合は、初期値のレコードを追加
            final MCodmstEntity entity = new MCodmstEntity();

            entity.setCode1(brandCode);
            entity.setCode2(itemCode);
            entity.setCode3(MARUI_DEFAULT_VALUE);
            entity.setItem30(M_CODMST_INITIAL_FLAG);

            final List<MCodmstEntity> addList = new ArrayList<>(list.size() + 1);

            addList.addAll(list);
            addList.add(entity);

            return addList;
        }

        // アイテムコードで絞り込む
        final List<MCodmstEntity> filtered = list.stream().filter(v -> StringUtils.equals(v.getCode2(), itemCode)).collect(Collectors.toList());

        if (!findByInitial(filtered.stream()).isPresent()) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            filtered.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 丸井 - Voi区分のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @param brandCode ブランドコード
     * @return 丸井 - Voi区分のマスタデータ
     */
    private List<MCodmstEntity> getVoiSections(final String brandCode) {
        // コードマスタ（Voi展開ブランド）に対象ブランドのレコードが入っているか確認
        final boolean hasContent = mCodmstRepository.findByTblidAndCode1AndItem1OrderById(
                MCodmstTblIdType.VOI_BRAND.getValue(),
                brandCode,
                "1",
                createOnePageRequest()).hasContent();

        if (hasContent) {
            // レコードがある場合は、Voi区分リストを取得
            final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblIdOrderByCode1(
                    MCodmstTblIdType.VOI_SECTION.getValue(),
                    createMaxPageRequest()));

            // 先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
            return list;
        }

        // レコードがない場合は、空のリストを返却
        return Collections.emptyList();
    }

    /**
     * 統計情報 - 素材のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @return 統計情報 - 素材のマスタデータ
     */
    private List<MCodmstEntity> getMaterials() {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblIdOrderByCode1(
                MCodmstTblIdType.MATERIAL.getValue(),
                createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - ゾーンのマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値レコードを追加する。
     * </pre>
     *
     * @param brandCode ブランドコード
     * @return 統計情報 - ゾーンのマスタデータ
     */
    private List<MCodmstEntity> getZones(final String brandCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findZoneByTblidAndCode1AheadLikeOrderByCode1(
                MCodmstTblIdType.ZONE.getValue(),
                brandCode,
                createMaxPageRequest()));

        boolean addDefaultRecord = false;

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合、デフォルト値を検索
            final Optional<MCodmstEntity> optional = list.stream()
                    .filter(v -> StringUtils.equals(v.getCode1(), ZONE_DEFAULT_VALUE))
                    .findFirst();

            if (optional.isPresent()) {
                // デフォルト値のレコードが存在した場合、初期値レコードを設定
                optional.get().setItem30(M_CODMST_INITIAL_FLAG);
                return list;
            } else {
                // レコ―ドがない場合、初期値レコード追加
                addDefaultRecord = true;
            }
        } else if (list.isEmpty()) {
            // レコ―ドがない場合、初期値レコード追加
            addDefaultRecord = true;
        }

        if (addDefaultRecord) {
            // レコードがない場合、または初期値レコードがない場合は、初期値のレコードを追加
            final MCodmstEntity entity = new MCodmstEntity();

            entity.setCode1(ZONE_DEFAULT_VALUE);
            entity.setItem30(M_CODMST_INITIAL_FLAG);

            final List<MCodmstEntity> addList = new ArrayList<>(list.size() + 1);

            addList.addAll(list);
            addList.add(entity);

            return addList;
        }

        return list;
    }

    /**
     * 統計情報 - サブブランドのマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * code2の数字型の昇順でソートする。
     * </pre>
     *
     * @param brandCode ブランドコード
     * @return 統計情報 - サブブランドのマスタデータ
     */
    private List<MCodmstEntity> getSubBrands(final String brandCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblidAndCode1OrderByCode2(
            MCodmstTblIdType.SUB_BRAND.getValue(),
            brandCode,
            createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - テイストのマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @param brandCode ブランドコード
     * @return 統計情報 - テイストのマスタデータ
     */
    private List<MCodmstEntity> getTastes(final String brandCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblidAndCode1OrderByCode1Code2(
                MCodmstTblIdType.TASTE.getValue(),
                brandCode,
                createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - タイプ1のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @param itemCode アイテムコード
     * @return 統計情報 - タイプ1のマスタデータ
     */
    private List<MCodmstEntity> getType1s(final String itemCode) {

        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2LikeOrderByCode1(
                MCodmstTblIdType.TYPE_1.getValue(),
                itemCode,
                createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - タイプ2のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @param itemCode アイテムコード
     * @return 統計情報 - タイプ2のマスタデータ
     */
    private List<MCodmstEntity> getType2s(
            final String itemCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2LikeOrderByCode1(
                MCodmstTblIdType.TYPE_2.getValue(),
                itemCode,
                createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - タイプ3のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値のレコードがない場合、先頭レコードを初期値レコードとする。
     * </pre>
     *
     * @param brandCode ブランドコード
     * @return 統計情報 - タイプ3のマスタデータ
     */
    private List<MCodmstEntity> getType3s(final String brandCode) {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2OrderByCode1(
                MCodmstTblIdType.TYPE_3.getValue(),
                brandCode,
                createMaxPageRequest()));

        if (isNotEmptyAndNonInitial(list)) {
            // 初期値レコードがない場合は、先頭のレコードに初期値フラグを設定
            list.stream().findFirst().ifPresent(v -> v.setItem30(M_CODMST_INITIAL_FLAG));
        }

        return list;
    }

    /**
     * 統計情報 - 展開のマスタデータを取得する.
     *
     * <pre>
     * 品番画面に合わせて、初期値レコードを先頭に追加する。
     * </pre>
     *
     * @return 統計情報 - 展開のマスタデータ
     */
    private List<MCodmstEntity> getOutlets() {
        final List<MCodmstEntity> list = copyToList(mCodmstRepository.findByTblIdOrderByCode1(
                MCodmstTblIdType.OUTLET.getValue(),
                createMaxPageRequest()));

        if (!list.isEmpty()) {
            final Optional<MCodmstEntity> optional = list.stream()
                    .filter(v -> StringUtils.equals(v.getCode1(), OUTLET_DEFAULT_VALUE))
                    .findFirst();
            if (optional.isPresent()) {
                optional.get().setItem30(M_CODMST_INITIAL_FLAG);
                return list;
            }
        }

        // レコードがない、または初期値レコードがない場合は、初期値のレコードを追加
        final MCodmstEntity entity = new MCodmstEntity();

        entity.setCode1(OUTLET_DEFAULT_VALUE);
        entity.setItem30(M_CODMST_INITIAL_FLAG);

        final List<MCodmstEntity> addList = new ArrayList<>(list.size() + 1);

        // 先頭に追加
        addList.add(entity);
        addList.addAll(list);

        return addList;
    }

    /**
     * リストはあるが、初期値レコードがないか判定する.
     * @param list コードマスタリスト
     * @return true:リストはあるが、初期値レコードがない
     */
    private boolean isNotEmptyAndNonInitial(final List<MCodmstEntity> list) {
        return !list.isEmpty() && !findByInitial(list.stream()).isPresent();
    }

    /**
     * @return {@link PageRequest} instance
     */
    private PageRequest createOnePageRequest() {
        return PageRequest.of(0, 1);
    }

    /**
     * @return {@link PageRequest} instance
     */
    private PageRequest createMaxPageRequest() {
        return PageRequest.of(0, Integer.MAX_VALUE);
    }

    /**
     * リストのコピー.
     * EntityManagerの管理対象から外れたオブジェクトを生成するため、取得したリストをコピーする。
     *
     * @param page コードマスタリスト
     * @return コピー後のコードマスタリスト
     */
    private List<MCodmstEntity> copyToList(final Page<MCodmstEntity> page) {
        return page.stream().map(fromEntity -> {
            final MCodmstEntity toEntity = new MCodmstEntity();
            BeanUtils.copyProperties(fromEntity, toEntity);
            return toEntity;
        }).collect(Collectors.toList());
    }
}
