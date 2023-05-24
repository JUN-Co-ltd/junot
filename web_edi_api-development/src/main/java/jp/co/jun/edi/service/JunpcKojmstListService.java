package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.model.JunpcKojmstModel;
import jp.co.jun.edi.model.JunpcKojsmstSearchConditionModel;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MKojmstSearchType;

/**
 * 発注生産システムの工場マスタから工場を検索するService.
 */
@Service
public class JunpcKojmstListService extends GenericListService<ListServiceParameter<JunpcKojsmstSearchConditionModel>, ListServiceResponse<JunpcKojmstModel>> {
    @Autowired
    private MKojmstRepository mKojmstRepository;

    @Override
    protected ListServiceResponse<JunpcKojmstModel> execute(final ListServiceParameter<JunpcKojsmstSearchConditionModel> serviceParameter) {
        final Page<MKojmstEntity> page = find(serviceParameter);

        return ListServiceResponse.<JunpcKojmstModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> toItem(entity)).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param serviceParameter {@link ListServiceParameter} instance
     * @return {@link Page} instance
     */
    private Page<MKojmstEntity> find(final ListServiceParameter<JunpcKojsmstSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        if (StringUtils.isEmpty(serviceParameter.getSearchCondition().getSearchText())) {
            // 検索文字列が空の場合、全検索
            return mKojmstRepository.findBySireAndSirkbnOrderByKojcd(
                    serviceParameter.getSearchCondition().getSire(),
                    serviceParameter.getSearchCondition().getSirkbn(),
                    pageRequest);
        }

        switch (MKojmstSearchType.findByValue(serviceParameter.getSearchCondition().getSearchType()).orElse(MKojmstSearchType.CODE_OR_NAME)) {
        case CODE_OR_NAME:
            // 値がなければor検索
            return mKojmstRepository.findBySireAndSirkbnAndKojcdLikeOrNameLikeOrderByKojcd(
                    serviceParameter.getSearchCondition().getSire(),
                    serviceParameter.getSearchCondition().getSirkbn(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        case CODE:
            // コード検索
            return mKojmstRepository.findBySireAndSirkbnAndKojcdLikeOrderByKojcd(
                    serviceParameter.getSearchCondition().getSire(),
                    serviceParameter.getSearchCondition().getSirkbn(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        case NAME:
            // 工場名検索
            return mKojmstRepository.findBySireAndSirkbnAndNameLikeOrderByKojcd(
                    serviceParameter.getSearchCondition().getSire(),
                    serviceParameter.getSearchCondition().getSirkbn(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        default:
            return null;
        }
    }

    /**
     * Item に変換する.
     * 発注生産システムの工場マスタは、空文字をnullに変換し、APIのレスポンスサイズを削減する。
     *
     * @param entity {@link MKojmstEntity} instance
     * @return {@link JunpcKojmstModel} instance
     */
    private JunpcKojmstModel toItem(final MKojmstEntity entity) {
        final JunpcKojmstModel item = new JunpcKojmstModel();

        item.setId(entity.getId());
        //        item.setSire(StringUtils.defaultIfEmpty(entity.getSire(), null));
        item.setKojcd(StringUtils.defaultIfEmpty(entity.getKojcd(), null));
        //        item.setReckbn(StringUtils.defaultIfEmpty(entity.getReckbn(), null));
        //        item.setSirkbn(StringUtils.defaultIfEmpty(entity.getSirkbn(), null));
        //        item.setSname(StringUtils.defaultIfEmpty(entity.getSname(), null));
        item.setName(StringUtils.defaultIfEmpty(entity.getName(), null));
        //        item.setYubin(StringUtils.defaultIfEmpty(entity.getYubin(), null));
        //        item.setAdd1(StringUtils.defaultIfEmpty(entity.getAdd1(), null));
        //        item.setAdd2(StringUtils.defaultIfEmpty(entity.getAdd2(), null));
        //        item.setAdd3(StringUtils.defaultIfEmpty(entity.getAdd3(), null));
        //        item.setTel1(StringUtils.defaultIfEmpty(entity.getTel1(), null));
        //        item.setFax1(StringUtils.defaultIfEmpty(entity.getFax1(), null));
        //        item.setHsofkbn(StringUtils.defaultIfEmpty(entity.getHsofkbn(), null));
        //        item.setHfax(StringUtils.defaultIfEmpty(entity.getHfax(), null));
        //        item.setHemail1(StringUtils.defaultIfEmpty(entity.getHemail1(), null));
        //        item.setNsofkbn(StringUtils.defaultIfEmpty(entity.getNsofkbn(), null));
        //        item.setNfax(StringUtils.defaultIfEmpty(entity.getNfax(), null));
        //        item.setNemail1(StringUtils.defaultIfEmpty(entity.getNemail1(), null));
        //        item.setYsofkbn(StringUtils.defaultIfEmpty(entity.getYsofkbn(), null));
        //        item.setYfax(StringUtils.defaultIfEmpty(entity.getYfax(), null));
        //        item.setYemail1(StringUtils.defaultIfEmpty(entity.getYemail1(), null));
        //        item.setHkiji(StringUtils.defaultIfEmpty(entity.getHkiji(), null));
        //        item.setHseihin(StringUtils.defaultIfEmpty(entity.getHseihin(), null));
        //        item.setHnefuda(StringUtils.defaultIfEmpty(entity.getHnefuda(), null));
        //        item.setHfuzoku(StringUtils.defaultIfEmpty(entity.getHfuzoku(), null));
        //        item.setBrand1(StringUtils.defaultIfEmpty(entity.getBrand1(), null));
        //        item.setBrand2(StringUtils.defaultIfEmpty(entity.getBrand2(), null));
        //        item.setBrand3(StringUtils.defaultIfEmpty(entity.getBrand3(), null));
        //        item.setBrand4(StringUtils.defaultIfEmpty(entity.getBrand4(), null));
        //        item.setBrand5(StringUtils.defaultIfEmpty(entity.getBrand5(), null));
        //        item.setBrand6(StringUtils.defaultIfEmpty(entity.getBrand6(), null));
        //        item.setBrand7(StringUtils.defaultIfEmpty(entity.getBrand7(), null));
        //        item.setBrand8(StringUtils.defaultIfEmpty(entity.getBrand8(), null));
        //        item.setBrand9(StringUtils.defaultIfEmpty(entity.getBrand9(), null));
        //        item.setBrand10(StringUtils.defaultIfEmpty(entity.getBrand10(), null));
        //        item.setBrand11(StringUtils.defaultIfEmpty(entity.getBrand11(), null));
        //        item.setBrand12(StringUtils.defaultIfEmpty(entity.getBrand12(), null));
        //        item.setBrand13(StringUtils.defaultIfEmpty(entity.getBrand13(), null));
        //        item.setBrand14(StringUtils.defaultIfEmpty(entity.getBrand14(), null));
        //        item.setBrand16(StringUtils.defaultIfEmpty(entity.getBrand16(), null));
        //        item.setBrand17(StringUtils.defaultIfEmpty(entity.getBrand17(), null));
        //        item.setBrand18(StringUtils.defaultIfEmpty(entity.getBrand18(), null));
        //        item.setBrand19(StringUtils.defaultIfEmpty(entity.getBrand19(), null));
        //        item.setBrand20(StringUtils.defaultIfEmpty(entity.getBrand20(), null));
        //        item.setBrand21(StringUtils.defaultIfEmpty(entity.getBrand21(), null));
        //        item.setBrand22(StringUtils.defaultIfEmpty(entity.getBrand22(), null));
        //        item.setBrand23(StringUtils.defaultIfEmpty(entity.getBrand23(), null));
        //        item.setBrand24(StringUtils.defaultIfEmpty(entity.getBrand24(), null));
        //        item.setBrand25(StringUtils.defaultIfEmpty(entity.getBrand25(), null));
        //        item.setBrand26(StringUtils.defaultIfEmpty(entity.getBrand26(), null));
        //        item.setBrand27(StringUtils.defaultIfEmpty(entity.getBrand27(), null));
        //        item.setBrand28(StringUtils.defaultIfEmpty(entity.getBrand28(), null));
        //        item.setBrand29(StringUtils.defaultIfEmpty(entity.getBrand29(), null));
        //        item.setBrand30(StringUtils.defaultIfEmpty(entity.getBrand30(), null));
        //        item.setSdenflg(StringUtils.defaultIfEmpty(entity.getSdenflg(), null));
        //        item.setSouflg(StringUtils.defaultIfEmpty(entity.getSouflg(), null));
        //        item.setMntflg(StringUtils.defaultIfEmpty(entity.getMntflg(), null));

        return item;
    }
}
