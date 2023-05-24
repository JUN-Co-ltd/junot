package jp.co.jun.edi.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MTnpmstEntity;
import jp.co.jun.edi.model.JunpcTnpmstModel;
import jp.co.jun.edi.model.JunpcTnpmstSearchConditionModel;
import jp.co.jun.edi.model.SortModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.MTnpmstRepository;
import jp.co.jun.edi.repository.specification.JunpcTnpmstSpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムの店舗マスタから仕入先を検索するService.
 */
@Service
public class JunpcTnpmstSearchService extends GenericListService<ListServiceParameter<JunpcTnpmstSearchConditionModel>, ListServiceResponse<JunpcTnpmstModel>> {
    @Autowired
    private JunpcTnpmstSpecification tnpSpec;

    @Autowired
    private MTnpmstRepository mTnpmstRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected ListServiceResponse<JunpcTnpmstModel> execute(final ListServiceParameter<JunpcTnpmstSearchConditionModel> serviceParameter) {
        final Page<MTnpmstEntity> page = search(serviceParameter);

        return ListServiceResponse.<JunpcTnpmstModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param serviceParameter {@link ListServiceParameter} instance
     * @return {@link Page} instance
     */
    private Page<MTnpmstEntity> search(final ListServiceParameter<JunpcTnpmstSearchConditionModel> serviceParameter) {
        final JunpcTnpmstSearchConditionModel sc = serviceParameter.getSearchCondition();
        final SortModel sort = sc.getSort();
        final PageRequest pageRequest = PageRequest.of(sc.getPage(), sc.getMaxResults(), Sort.by(sort.getOrderByType(), sort.getSortColumnName()));

        // ブランドコード取得
        final String divisionCode = sc.getDivisionCode();
        List<String> brands = null;
        if (StringUtils.isNoneEmpty(divisionCode)) {
            brands = mCodmstRepository.findBrandCodesByDivisionCode(MCodmstTblIdType.BRAND.getValue(), divisionCode);
            // 取得できなければ空の結果を返す
            if (brands.size() == 0) {
                return new PageImpl<>(Collections.emptyList(), pageRequest, 0);
            }
        }

        return mTnpmstRepository.findAll(Specification
                .where(tnpSpec.notDeleteContains())
                .and(tnpSpec.shpcdAheadMatchContains(sc.getShpcdAhead()))
                .and(tnpSpec.shopkindMatchContains(sc.getShopkind()))
                .and(tnpSpec.shpcdMatchContains(sc.getShpcd()))
                .and(tnpSpec.nameMatchContains(sc.getName()))
                .and(tnpSpec.telbanMatchContains(sc.getTelban()))
                .and(tnpSpec.groupcdInContains(brands)),
                pageRequest);
    }

    /**
     * @param entity {@link MTnpmstEntity} instance
     * @return {@link JunpcTnpmstModel} instance
     */
    private JunpcTnpmstModel toModel(final MTnpmstEntity entity) {
        final JunpcTnpmstModel model = new JunpcTnpmstModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }
}
