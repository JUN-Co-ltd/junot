package jp.co.jun.edi.component.maint;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ScreenSettingStructureModel;
import jp.co.jun.edi.model.maint.code.MaintCodeScreenSettingModel;
import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;
import jp.co.jun.edi.repository.DynamicMaintCodeRepository;
import jp.co.jun.edi.repository.MScreenStructureRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * コードマスタ用コンポーネント.
 */
@Component
public class MaintCodeComponent {

    @Autowired
    private MScreenStructureRepository mScreenStructureRepository;
    @Autowired
    private DynamicMaintCodeRepository dynamicMaintCodeRepository;

    /**
     * テーブル区分から画面構成マスタ情報を取得し、MaintCodeScreenSettingModelに格納し返す.
     *
     * @param tblId
     *            テーブル区分
     * @return MaintCodeScreenSettingModel
     * @exception ResourceNotFoundException
     *                テーブル区分に該当する情報が画面構成マスタテーブルに存在しない
     */
    public MaintCodeScreenSettingModel generateMaintCodeScreenSetting(final MCodmstTblIdType tblId) {
        final MScreenStructureEntity entity = getMScreenStructureEntity(tblId);

        final List<ScreenSettingStructureModel> listStructure = entity.getStructure().stream().map(structure -> {
            final ScreenSettingStructureModel model = new ScreenSettingStructureModel();
            BeanUtils.copyProperties(structure, model);
            return model;
        }).collect(Collectors.toList());

        final MaintCodeScreenSettingModel model = new MaintCodeScreenSettingModel();
        model.setId(entity.getId());
        model.setTableId(entity.getTableId());
        model.setStructure(listStructure);
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedUserId(entity.getCreatedUserId());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedUserId(entity.getUpdatedUserId());

        return model;
    }

    /**
     * 画面構成情報取得.
     *
     * @param tblId
     *            テーブル区分
     * @return 画面構成情報
     */
    public MScreenStructureEntity getMScreenStructureEntity(final MCodmstTblIdType tblId) {
        final MScreenStructureEntity entity = mScreenStructureRepository.findByTblidAndDeletedAtIsNull(tblId)
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
        return entity;
    }

    /**
     * コードマスタ検索.
     *
     * @param entity
     *            画面構成情報
     * @param searchCondition
     *            検索条件
     * @param pageRequest
     *            ページャー
     * @return コードマスタの検索結果
     */
    public Page<Map<String, Object>> maintCodeSearch(final MScreenStructureEntity entity, final MaintCodeSearchConditionModel searchCondition,
            final PageRequest pageRequest) {
        final Page<Map<String, Object>> page = dynamicMaintCodeRepository.findByMaintCode(searchCondition, entity);
        return page;
    }

    /**
     * メンテコード登録.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            登録情報
     * @param entity
     *            画面構成情報
     * @return 登録件数
     */
    public int insertMaintCode(final BigInteger userId, final Map<String, String> record, final MScreenStructureEntity entity) {
        return dynamicMaintCodeRepository.insertByMaintCode(userId, record, entity);
    }

    /**
     * メンテコード更新.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            更新情報
     * @param entity
     *            画面構成情報
     * @return 更新件数
     */
    public int updateMaintCode(final BigInteger userId, final Map<String, String> record, final MScreenStructureEntity entity) {
        return dynamicMaintCodeRepository.updateByMaintCode(userId, record, entity);
    }

    /**
     * メンテコード削除.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            削除情報
     * @param entity
     *            画面構成情報
     * @return 削除件数
     */
    public int deletedMaintCode(final BigInteger userId, final Map<String, String> record, final MScreenStructureEntity entity) {
        return dynamicMaintCodeRepository.deletedByMaintCode(userId, record, entity);
    }
}
