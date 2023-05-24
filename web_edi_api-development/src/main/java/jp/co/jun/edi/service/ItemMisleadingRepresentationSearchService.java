package jp.co.jun.edi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationSearchResultEntity;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchConditionModel;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchResultModel;
import jp.co.jun.edi.repository.ItemMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.specification.JunpcCodmstCompositionSpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 画面で指定された検索条件を基に優良誤認検査承認一覧を取得するサービス.
 */
@Service
public class ItemMisleadingRepresentationSearchService extends GenericListService<ListServiceParameter<ItemMisleadingRepresentationSearchConditionModel>,
ListServiceResponse<ItemMisleadingRepresentationSearchResultModel>> {

    @Autowired
    private ItemMisleadingRepresentationRepository itemMisleadingRepresentationRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private JunpcCodmstCompositionSpecification spec;

    @Override
    protected ListServiceResponse<ItemMisleadingRepresentationSearchResultModel>
    execute(final ListServiceParameter<ItemMisleadingRepresentationSearchConditionModel> serviceParameter) {

        final Page<ExtendedItemMisleadingRepresentationSearchResultEntity> noNameRslt =
                itemMisleadingRepresentationRepository.findBySpec(serviceParameter.getSearchCondition());

        final Page<MCodmstEntity> mdfStaffList = findMdfStaffName(noNameRslt);
        final Page<MCodmstEntity> cooList = findCooName(noNameRslt);
        final Page<MCodmstEntity> compositionList = findCompositionName(noNameRslt);

        final List<ItemMisleadingRepresentationSearchResultModel> list =
                noNameRslt
                .stream()
                .map(record -> toModel(record, mdfStaffList, cooList, compositionList))
                .collect(Collectors.toList());

        return ListServiceResponse.<ItemMisleadingRepresentationSearchResultModel>builder().nextPage(noNameRslt.hasNext()).items(list).build();
    }

    /**
     * 製造担当者名を取得する.
     * @param noNameRsltPage 検索結果リスト
     * @return 製造担当者リスト.
     */
    private Page<MCodmstEntity> findMdfStaffName(final Page<ExtendedItemMisleadingRepresentationSearchResultEntity> noNameRsltPage) {
        final List<String> mdfStaffList =
                noNameRsltPage
                .stream()
                .map(record -> record.getMdfStaffCode())
                .distinct()
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());

        return mCodmstRepository.findAll(Specification
                .where(spec.tblidContains(MCodmstTblIdType.STAFF))
                .and(spec.compositionCodeInContains(mdfStaffList))
                .and(spec.mntflgContains())
                .and(spec.notDeleteContains()),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("code1"))));
    }

    /**
     * 原産国名を取得する.
     * @param noNameRsltPage 検索結果リスト
     * @return 原産国リスト.
     */
    private Page<MCodmstEntity> findCooName(final Page<ExtendedItemMisleadingRepresentationSearchResultEntity> noNameRsltPage) {
        final List<String> cooCodeList =
                noNameRsltPage
                .stream()
                .map(record -> record.getCooCode())
                .distinct()
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());

        return mCodmstRepository.findAll(Specification
                .where(spec.tblidContains(MCodmstTblIdType.ORIGIN_COUNTRY))
                .and(spec.compositionCodeInContains(cooCodeList))
                .and(spec.mntflgContains())
                .and(spec.notDeleteContains()),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("code1"))));
    }

    /**
     * 組成名を取得する.
     * @param noNameRsltPage 検索結果リスト
     * @return 組成リスト.
     */
    private Page<MCodmstEntity> findCompositionName(final Page<ExtendedItemMisleadingRepresentationSearchResultEntity> noNameRsltPage) {
        final List<String> compositionCodeList =
                noNameRsltPage
                .stream()
                .map(record -> record.getCompositionCode())
                .distinct()
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());

        // メンテ区分＝"3"（削除）分も取得する.
        return mCodmstRepository.findAll(Specification
                .where(spec.tblidContains(MCodmstTblIdType.COMPOSITION))
                .and(spec.compositionCodeInContains(compositionCodeList))
                .and(spec.notDeleteContains()),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("code1"))));
    }

    /**
     * 検索結果に名称を設定したModelを返す.
     * @param noNameRslt 検索結果
     * @param mdfStaffList 製造担当者リスト
     * @param cooList 原産国リスト
     * @param compositionList 組成リスト
     * @return ItemMisleadingRepresentationSearchResultModel
     */
    private ItemMisleadingRepresentationSearchResultModel toModel(final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt,
            final Page<MCodmstEntity> mdfStaffList, final Page<MCodmstEntity> cooList, final Page<MCodmstEntity> compositionList) {
        final ItemMisleadingRepresentationSearchResultModel model = new ItemMisleadingRepresentationSearchResultModel();
        toModelMdfStaffName(noNameRslt, mdfStaffList, model);
        toModelCooName(noNameRslt, cooList, model);
        toModelCompositionName(noNameRslt, compositionList, model);
        return model;
    }

    /**
     * 検索結果に製造担当者名をつけてmodelに設定する.
     * @param noNameRslt 検索結果
     * @param mdfStaffList 製造担当者リスト
     * @param model ItemMisleadingRepresentationSearchResultModel
     */
    private void toModelMdfStaffName(final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt,
            final Page<MCodmstEntity> mdfStaffList, final ItemMisleadingRepresentationSearchResultModel model) {
        final String mdfStaffName =
                mdfStaffList
                .stream()
                .filter(mdfStaff -> equalMdfStaffCode(mdfStaff, noNameRslt))
                .findFirst()
                .orElse(new MCodmstEntity())
                .getItem1();

        BeanUtils.copyProperties(noNameRslt, model);
        model.setMdfStaffName(mdfStaffName);
    }

    /**
     * 検索結果に原産国名をつけてmodelに設定する.
     * @param noNameRslt 検索結果
     * @param cooList 原産国リスト
     * @param model ItemMisleadingRepresentationSearchResultModel
     */
    private void toModelCooName(final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt,
            final Page<MCodmstEntity> cooList, final ItemMisleadingRepresentationSearchResultModel model) {
        final String cooName =
                cooList
                .stream()
                .filter(coo -> equalCooCode(coo, noNameRslt))
                .findFirst()
                .orElse(new MCodmstEntity())
                .getItem1();

        BeanUtils.copyProperties(noNameRslt, model);
        model.setCooName(cooName);
    }

    /**
     * 検索結果に組成名をつけてmodelに設定する.
     * @param noNameRslt 検索結果
     * @param compositionList 組成リスト
     * @param model ItemMisleadingRepresentationSearchResultModel
     */
    private void toModelCompositionName(final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt,
            final Page<MCodmstEntity> compositionList, final ItemMisleadingRepresentationSearchResultModel model) {
        final String compositionName =
                compositionList
                .stream()
                .filter(composition -> equalCompositionCode(composition, noNameRslt))
                .findFirst()
                .orElse(new MCodmstEntity())
                .getItem1();

        BeanUtils.copyProperties(noNameRslt, model);
        model.setCompositionName(compositionName);
    }

    /**
     * 製造担当者コードが等しいか判定する.
     * @param mdfStaff 製造担当者entity
     * @param noNameRslt 検索結果
     * @return true:等しい
     */
    private boolean equalMdfStaffCode(final MCodmstEntity mdfStaff, final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt) {
        return mdfStaff.getCode1().equals(noNameRslt.getMdfStaffCode());
    }

    /**
     * 原産国コードが等しいか判定する.
     * @param composition 原産国entity
     * @param noNameRslt 検索結果
     * @return true:等しい
     */
    private boolean equalCooCode(final MCodmstEntity composition, final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt) {
        return composition.getCode1().equals(noNameRslt.getCooCode());
    }

    /**
     * 組成コードが等しいか判定する.
     * @param composition 組成entity
     * @param noNameRslt 検索結果
     * @return true:等しい
     */
    private boolean equalCompositionCode(final MCodmstEntity composition, final ExtendedItemMisleadingRepresentationSearchResultEntity noNameRslt) {
        return composition.getCode1().equals(noNameRslt.getCompositionCode());
    }
}
