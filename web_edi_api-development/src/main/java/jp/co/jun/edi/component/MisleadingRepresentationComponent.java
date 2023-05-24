package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.ItemChangeStateModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TMisleadingRepresentationEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.MCodmstTargetType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MSirmstYugaikbnType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.MisleadingRepresentationType;
import jp.co.jun.edi.type.QualityApprovalType;

/**
 * 優良誤認承認情報関連のコンポーネント.
 */
@Component
public class MisleadingRepresentationComponent extends GenericComponent {

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private MSirmstRepository mSirmstRepository;

    @Autowired
    private TMisleadingRepresentationRepository misleadingRepresentationRepository;

    /**
     * 優良誤認(組成)対象チェック.
     * 組成(混率)リストがない場合は非対象
     *
     * @param itemModel 品番情報
     * @return true : 対象, false : 非対象
     */
    public boolean isQualityCompositionTarget(final ItemModel itemModel) {
        if (itemModel.getCompositions().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 優良誤認(国)対象チェック.
     * 原産国がある
     * かつ
     * コードマスタの原産国(tblid='05')のitem4が1
     * の場合、対象とする
     *
     * @param cooCode 原産国コード
     * @return true : 対象, false : 非対象
     */
    public boolean isQualityCooTarget(final String cooCode) {
        if (StringUtils.isEmpty(cooCode)) {
            return false;
        }

        // 原産国コードを基にコードマスタ情報を取得する
        final List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.ORIGIN_COUNTRY.getValue(),
                cooCode,
                PageRequest.of(0, 1)).getContent();

        // 原産国コードを取得できなかった場合はエラー
        if (mCodmstEntityList.isEmpty()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_I_08));
        }

        // item4のデータに空白スペースが含まれてる可能性があるため、空白除去が必要
        // ※コードマスタのテーブル定義でカラム値にnullを許容してないので、item4がnullであることはないと見る
        return MCodmstTargetType.TARGET.getValue().equals(mCodmstEntityList.get(0).getItem4().replaceAll("\\h", ""));
    }

    /**
     * 優良誤認(有害物質)対象チェック.
     * 仕入れ先マスタの有害物質対応区分が提出済み(1)または未提出(9)
     * の場合は、「対象」とする
     *
     * @param makerCode 仕入先コード
     * @return true : 対象, false : 非対象
     */
    public boolean isQualityHarmfulTarget(final String makerCode) {
        // 仕入先コードが存在しない場合はエラー
        final MSirmstEntity mSirmstEntity = mSirmstRepository.findBySire(makerCode).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_I_13)));

        // 仕入先コードを基に仕入先マスタ情報を取得する
        return MSirmstYugaikbnType.SUBMITTED.getValue().equals(mSirmstEntity.getYugaikbn())
                || MSirmstYugaikbnType.UNSUBMITTED.getValue().equals(mSirmstEntity.getYugaikbn());
    }

    /**
     * 優良誤認対象チェック.
     * 優良誤認(組成、国、有害物質)
     * 対象(1)、一部承認(5)、承認(9)のいずれかであれば対象とする.
     *
     * @param itemEntity 品番情報
     * @return true : 対象, false : 対象外
     */
    public boolean isMisleadingRepresentationTarget(final TItemEntity itemEntity) {
        return (QualityApprovalType.isMisleadingRepresentation(itemEntity.getQualityCompositionStatus())
                || QualityApprovalType.isMisleadingRepresentation(itemEntity.getQualityCooStatus())
                || QualityApprovalType.isMisleadingRepresentation(itemEntity.getQualityHarmfulStatus()));
    }

    /**
     * 優良誤認承認情報をupsertする.
     *
     * @param item リクエストパラメータ
     * @param itemChangeState 品番変更状況
     * @param loginUser CustomLoginUser
     */
    public void upsertMisleadingRepresentation(final ItemModel item, final ItemChangeStateModel itemChangeState,
            final CustomLoginUser loginUser) {
        final BigInteger partNoId = item.getId();

        final Page<TMisleadingRepresentationEntity> page = misleadingRepresentationRepository.findByPartNoId(partNoId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id"))));

        if (page.getTotalPages() == 0) {
            // 優良誤認承認テーブル未登録の場合、insert
            misleadingRepresentationRepository.saveAll(generateMisleadingRepresentationEntityForInsert(item));
            return;
        }

        misleadingRepresentationRepository
                .saveAll(generateMisleadingRepresentationEntityForUpsert(item, page.getContent(), itemChangeState));
    }

    /**
     * insert用の優良誤認承認データリストを作成.
     *
     * @param item リクエストパラメータ
     * @return insert用の優良誤認承認データリスト
     */
    private List<TMisleadingRepresentationEntity> generateMisleadingRepresentationEntityForInsert(
            final ItemModel item) {
        final BigInteger partNoId = item.getId();

        // 組成(SKUのカラーコード)
        final List<TMisleadingRepresentationEntity> list = item.getSkus().stream()
                .map(sku -> sku.getColorCode())
                .distinct()
                .map(colorCode -> generateCompositionMisleadingRepresentationEntity(partNoId, colorCode))
                .collect(Collectors.toList());

        // 原産国
        list.add(0, generateCooMisleadingRepresentationEntity(partNoId, item.getCooCode()));

        // 有害物質
        list.add(generateHarmfulMisleadingRepresentationEntity(partNoId, item.getOrderSuppliers().get(0).getSupplierCode()));

        return list;
    }

    /**
     * 優良誤認承認登録用の原産国検査データを作成する.
     *
     * @param partNoId 品番ID
     * @param cooCode 原産国コード
     * @return 優良誤認承認登録用の原産国検査データ
     */
    private TMisleadingRepresentationEntity generateCooMisleadingRepresentationEntity(final BigInteger partNoId,
            final String cooCode) {
        final TMisleadingRepresentationEntity entity = new TMisleadingRepresentationEntity();
        entity.setMisleadingRepresentationType(MisleadingRepresentationType.COUNTRY_OF_ORIGIN);
        entity.setPartNoId(partNoId);
        entity.setCooCode(cooCode);
        return entity;
    }

    /**
     * 優良誤認承認登録用の有害物質検査データを作成する.
     *
     * @param partNoId 品番ID
     * @param mdfMakerCode 生産メーカーコード
     * @return 優良誤認承認登録用の有害物質検査データ
     */
    private TMisleadingRepresentationEntity generateHarmfulMisleadingRepresentationEntity(final BigInteger partNoId,
            final String mdfMakerCode) {
        final TMisleadingRepresentationEntity entity = new TMisleadingRepresentationEntity();
        entity.setMisleadingRepresentationType(MisleadingRepresentationType.HARMFUL_STATUS);
        entity.setPartNoId(partNoId);
        entity.setMdfMakerCode(mdfMakerCode);
        return entity;
    }

    /**
     * 優良誤認承認登録用の組成検査データを作成する.
     *
     * @param partNoId 品番ID
     * @param colorCode カラーコード
     * @return 優良誤認承認登録用の組成検査データ
     */
    private TMisleadingRepresentationEntity generateCompositionMisleadingRepresentationEntity(final BigInteger partNoId,
            final String colorCode) {
        final TMisleadingRepresentationEntity entity = new TMisleadingRepresentationEntity();
        entity.setMisleadingRepresentationType(MisleadingRepresentationType.COMPOSITION);
        entity.setPartNoId(partNoId);
        entity.setColorCode(colorCode);
        return entity;
    }

    /**
     * upsert用の優良誤認承認データリストを作成する.
     *
     * @param item リクエストパラメータ
     * @param dbRegisteredList 登録済優良誤認承認情報
     * @param itemChangeState 品番変更状態
     * @return upsert用の優良誤認承認データリスト
     */
    private List<TMisleadingRepresentationEntity> generateMisleadingRepresentationEntityForUpsert(final ItemModel item,
            final List<TMisleadingRepresentationEntity> dbRegisteredList, final ItemChangeStateModel itemChangeState) {
        // ※dbRegisteredListはサイズが決まっているのでdbRegisteredListにaddするとエラーになります
        final List<TMisleadingRepresentationEntity> listForUpsert = new ArrayList<>();

        // 原産国が変更された場合、原産国の優良誤認の承認済を取り消し原産国コードをupdate
        if (itemChangeState.isCooCodeChanged()) {
            dbRegisteredList.stream()
            .filter(dbRecord -> MisleadingRepresentationType.COUNTRY_OF_ORIGIN == dbRecord.getMisleadingRepresentationType())
            .findFirst()
            .ifPresent(cooRecord -> listForUpsert.add(generateUpdateCooRecord(cooRecord, item.getCooCode())));
        }

        // 仕入先メーカーが変更された場合、有害物質の優良誤認の承認済を取り消し仕入先コードをupdate
        // ※発注承認時、発注承認後に変更される可能性がある
        if (itemChangeState.isMdfMakerCodeChanged()) {
            dbRegisteredList.stream()
            .filter(dbRecord -> MisleadingRepresentationType.HARMFUL_STATUS == dbRecord.getMisleadingRepresentationType())
            .findFirst()
            .ifPresent(harmfulRecord -> listForUpsert.add(generateUpdateHarmfulRecord(harmfulRecord,
                    item.getOrderSuppliers().get(0).getSupplierCode())));
        }

        // SKUが追加された場合、追加されたSKUのカラーコードを登録
        final List<String> addedColorCodes = extractAddedColorCodes(item, dbRegisteredList);
        addedColorCodes.forEach(colorCode -> listForUpsert.add(generateCompositionMisleadingRepresentationEntity(item.getId(), colorCode)));

        // 組成が変更された場合、変更された組成のカラーコードの承認済を取り消す
        final List<String> changedCompositionsColorCodeList = itemChangeState.getChangedCompositionsColors();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(changedCompositionsColorCodeList)) {
            dbRegisteredList.stream()
            .filter(dbRecord -> changedCompositionsColorCodeList.contains(dbRecord.getColorCode())
                    && !addedColorCodes.contains(dbRecord.getColorCode()))  // ※新規にSKUを追加したカラーコードはinsertするので除外
            .forEach(compChangedRecord -> listForUpsert.add(generateApprovalCancelUpdateRecord(compChangedRecord)));
        }

        return listForUpsert;
    }

    /**
     * @param item リクエストパラメータ
     * @param dbRegisteredList 登録済優良誤認承認情報
     * @return SKUに追加されたカラーコードリスト
     */
    private List<String> extractAddedColorCodes(final ItemModel item,
            final List<TMisleadingRepresentationEntity> dbRegisteredList) {

        // 優良誤認承認情報テーブルに登録済のカラーコードリスト
        final List<String> dbRegisteredColorCodeList = dbRegisteredList.stream()
                .filter(dbRecord -> MisleadingRepresentationType.COMPOSITION == dbRecord
                .getMisleadingRepresentationType())
                .map(dbRecord -> dbRecord.getColorCode())
                .collect(Collectors.toList());

        // 発注承認時、承認後の編集時はSKUがnullでくるので要nullチェック
        if (item.getSkus() == null) {
            return new ArrayList<>(0);
        }

        return item.getSkus().stream().map(sku -> sku.getColorCode())
                .distinct()
                .filter(reqColorCode -> !dbRegisteredColorCodeList.contains(reqColorCode))
                .collect(Collectors.toList());
    }

    /**
     * 原産国更新用の優良誤認承認データを作成する.
     *
     * @param cooDbRecord DB登録済の原産国の優良誤認承認データ
     * @param cooCode 原産国コード
     * @return 原産国更新用の優良誤認承認データ
     */
    private TMisleadingRepresentationEntity generateUpdateCooRecord(final TMisleadingRepresentationEntity cooDbRecord,
            final String cooCode) {
        final TMisleadingRepresentationEntity entity = generateApprovalCancelUpdateRecord(cooDbRecord);
        entity.setCooCode(cooCode);
        return entity;
    }

    /**
     * 有害物質更新用の優良誤認承認データを作成する.
     *
     * @param harmfulDbRecord DB登録済の有害物質の優良誤認承認データ
     * @param supplierCode 仕入先コード
     * @return 有害物質更新用の優良誤認承認データ
     */
    private TMisleadingRepresentationEntity generateUpdateHarmfulRecord(final TMisleadingRepresentationEntity harmfulDbRecord,
            final String supplierCode) {
        final TMisleadingRepresentationEntity entity = generateApprovalCancelUpdateRecord(harmfulDbRecord);
        entity.setMdfMakerCode(supplierCode);
        return entity;
    }

    /**
     * 承認取り消し更新用の優良誤認承認データを作成する.
     *
     * @param dbRecord DB登録済の優良誤認承認データ
     * @return 承認取り消し更新用の優良誤認承認データ
     */
    private TMisleadingRepresentationEntity generateApprovalCancelUpdateRecord(
            final TMisleadingRepresentationEntity dbRecord) {
        final TMisleadingRepresentationEntity entity = new TMisleadingRepresentationEntity();
        BeanUtils.copyProperties(dbRecord, entity);
        entity.setApprovalAt(null);
        entity.setApprovalUserAccountName(null);
        return entity;
    }

    /**
     * 原産国が優良誤認対象であれば「対象」を返す.
     * 非対象であれば「非」を返す.
     *
     * @param cooCode 原産国コード
     * @return 優良誤認承認ステータス
     */
    public QualityApprovalType decideQualityCooStatus(final String cooCode) {
        if (isQualityCooTarget(cooCode)) {
            return QualityApprovalType.TARGET;
        }

        return QualityApprovalType.NON_TARGET;
    }

    /**
     * 仕入先が優良誤認対象であれば「対象」を返す.
     * 非対象であれば「非」を返す.
     *
     * @param supplierCode 仕入先コード
     * @return 優良誤認承認ステータス
     */
    public QualityApprovalType decideQualityHarmfulStatus(final String supplierCode) {
        if (isQualityHarmfulTarget(supplierCode)) {
            return QualityApprovalType.TARGET;
        }

        return QualityApprovalType.NON_TARGET;
    }
}
