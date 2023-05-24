package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.model.JunpcCodmastTypeModel;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * パラメータの品番に基づきタイプ１～３を取得するService.
 */
@Service
public class JunpcCodmstTypeListService
        extends GenericListService<ListServiceParameter<JunpcCodmstSearchConditionModel>, ListServiceResponse<JunpcCodmastTypeModel>> {
    private static final int TYPE1_SEARCH_TEXT_BEGIN_INDEX = 2;
    private static final int TYPE1_SEARCH_TEXT_END_INDEX = 3;
    private static final int TYPE2_SEARCH_TEXT_BEGIN_INDEX = 2;
    private static final int TYPE2_SEARCH_TEXT_END_INDEX = 3;
    private static final int TYPE3_SEARCH_TEXT_BEGIN_INDEX = 0;
    private static final int TYPE3_SEARCH_TEXT_END_INDEX = 2;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected ListServiceResponse<JunpcCodmastTypeModel> execute(final ListServiceParameter<JunpcCodmstSearchConditionModel> serviceParameter) {
        final JunpcCodmastTypeModel item = new JunpcCodmastTypeModel();

        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        // タイプ1(末尾1文字検索)
        item.setType1s(
                mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2LikeOrderByCode1(
                        MCodmstTblIdType.TYPE_1.getValue(),
                        serviceParameter.getSearchCondition().getSearchText().substring(TYPE1_SEARCH_TEXT_BEGIN_INDEX, TYPE1_SEARCH_TEXT_END_INDEX),
                        pageRequest).stream().map(entity -> toItem(entity)).collect(Collectors.toList()));

        // タイプ2(末尾1文字検索)
        item.setType2s(
                mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2LikeOrderByCode1(
                        MCodmstTblIdType.TYPE_2.getValue(),
                        serviceParameter.getSearchCondition().getSearchText().substring(TYPE2_SEARCH_TEXT_BEGIN_INDEX, TYPE2_SEARCH_TEXT_END_INDEX),
                        pageRequest).stream().map(entity -> toItem(entity)).collect(Collectors.toList()));

        // タイプ3(先頭2文字検索)
        item.setType3s(
                mCodmstRepository.findByTblidAndItem2IsEmptyOrItem2OrderByCode1(
                        MCodmstTblIdType.TYPE_3.getValue(),
                        serviceParameter.getSearchCondition().getSearchText().substring(TYPE3_SEARCH_TEXT_BEGIN_INDEX, TYPE3_SEARCH_TEXT_END_INDEX),
                        pageRequest).stream().map(entity -> toItem(entity)).collect(Collectors.toList()));

        final List<JunpcCodmastTypeModel> items = new ArrayList<>(1);
        items.add(item);

        return ListServiceResponse.<JunpcCodmastTypeModel>builder().items(items).build();
    }

    /**
     * Item に変換する.
     * 発注生産システムのコードマスタは、空文字をnullに変換し、APIのレスポンスサイズを削減する。
     *
     * @param entity {@link MCodmstEntity} instance
     * @return {@link JunpcCodmstModel} instance
     */
    protected JunpcCodmstModel toItem(final MCodmstEntity entity) {
        final JunpcCodmstModel item = new JunpcCodmstModel();

        item.setCode1(StringUtils.defaultIfEmpty(entity.getCode1(), null));
        item.setCode2(StringUtils.defaultIfEmpty(entity.getCode2(), null));
        item.setCode3(StringUtils.defaultIfEmpty(entity.getCode3(), null));
        //        item.setCode4(StringUtils.defaultIfEmpty(entity.getCode4(), null));
        //        item.setCode5(StringUtils.defaultIfEmpty(entity.getCode5(), null));
        //        item.setCode6(StringUtils.defaultIfEmpty(entity.getCode6(), null));
        //        item.setCode7(StringUtils.defaultIfEmpty(entity.getCode7(), null));
        //        item.setCode8(StringUtils.defaultIfEmpty(entity.getCode8(), null));
        item.setItem1(StringUtils.defaultIfEmpty(entity.getItem1(), null));
        item.setItem2(StringUtils.defaultIfEmpty(entity.getItem2(), null));
        item.setItem3(StringUtils.defaultIfEmpty(entity.getItem3(), null));
        //        item.setItem4(StringUtils.defaultIfEmpty(entity.getItem4(), null));
        //        item.setItem5(StringUtils.defaultIfEmpty(entity.getItem5(), null));
        //        item.setItem6(StringUtils.defaultIfEmpty(entity.getItem6(), null));
        //        item.setItem7(StringUtils.defaultIfEmpty(entity.getItem7(), null));
        //        item.setItem8(StringUtils.defaultIfEmpty(entity.getItem8(), null));
        //        item.setItem9(StringUtils.defaultIfEmpty(entity.getItem9(), null));
        //        item.setItem10(StringUtils.defaultIfEmpty(entity.getItem10(), null));
        //        item.setItem11(StringUtils.defaultIfEmpty(entity.getItem11(), null));
        //        item.setItem12(StringUtils.defaultIfEmpty(entity.getItem12(), null));
        //        item.setItem13(StringUtils.defaultIfEmpty(entity.getItem13(), null));
        //        item.setItem14(StringUtils.defaultIfEmpty(entity.getItem14(), null));
        //        item.setItem15(StringUtils.defaultIfEmpty(entity.getItem15(), null));
        //        item.setItem16(StringUtils.defaultIfEmpty(entity.getItem16(), null));
        //        item.setItem17(StringUtils.defaultIfEmpty(entity.getItem17(), null));
        //        item.setItem18(StringUtils.defaultIfEmpty(entity.getItem18(), null));
        //        item.setItem19(StringUtils.defaultIfEmpty(entity.getItem19(), null));
        //        item.setItem20(StringUtils.defaultIfEmpty(entity.getItem20(), null));
        //        item.setItem21(StringUtils.defaultIfEmpty(entity.getItem21(), null));
        //        item.setItem22(StringUtils.defaultIfEmpty(entity.getItem22(), null));
        //        item.setItem23(StringUtils.defaultIfEmpty(entity.getItem23(), null));
        //        item.setItem24(StringUtils.defaultIfEmpty(entity.getItem24(), null));
        //        item.setItem25(StringUtils.defaultIfEmpty(entity.getItem25(), null));
        //        item.setItem26(StringUtils.defaultIfEmpty(entity.getItem26(), null));
        //        item.setItem27(StringUtils.defaultIfEmpty(entity.getItem27(), null));
        //        item.setItem28(StringUtils.defaultIfEmpty(entity.getItem28(), null));
        //        item.setItem29(StringUtils.defaultIfEmpty(entity.getItem29(), null));
        item.setItem30(StringUtils.defaultIfEmpty(entity.getItem30(), null));

        return item;
    }
}
