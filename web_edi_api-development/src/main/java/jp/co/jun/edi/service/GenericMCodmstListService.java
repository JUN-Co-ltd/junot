package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムのコードマスタを検索するService.
 */
/**
 * @author kashiwabara-d
 *
 */
public abstract class GenericMCodmstListService extends GenericListService<JunpcCodmstListServiceParameter, ListServiceResponse<JunpcCodmstModel>> {
    @Override
    protected ListServiceResponse<JunpcCodmstModel> execute(final JunpcCodmstListServiceParameter serviceParameter) {
        final Page<MCodmstEntity> page = find(serviceParameter);

        if (page != null) {
            return ListServiceResponse.<JunpcCodmstModel>builder()
                    .nextPage(page.hasNext())
                    .items(page.stream().map(entity -> toItem(entity)).collect(Collectors.toList()))
                    .build();
        } else {
            return ListServiceResponse.<JunpcCodmstModel>builder()
                    .nextPage(false)
                    .items(new ArrayList<>())
                    .build();
        }
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
        item.setItem4(StringUtils.defaultIfEmpty(entity.getItem4(), null));
        //        item.setItem5(StringUtils.defaultIfEmpty(entity.getItem5(), null));
        //        item.setItem6(StringUtils.defaultIfEmpty(entity.getItem6(), null));
        item.setItem7(StringUtils.defaultIfEmpty(entity.getItem7(), null));
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

    /**
     * @param serviceParameter {@link JunpcCodmstListServiceParameter} instance
     * @return {@link Page} instance
     */
    protected abstract Page<MCodmstEntity> find(JunpcCodmstListServiceParameter serviceParameter);
}
