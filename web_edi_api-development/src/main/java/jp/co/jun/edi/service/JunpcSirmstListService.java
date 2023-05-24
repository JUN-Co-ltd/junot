package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.model.JunpcSirmstModel;
import jp.co.jun.edi.model.JunpcSirmstSearchConditionModel;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MSirmstSearchType;

/**
 * 発注生産システムの仕入先マスタから仕入先を検索するService.
 */
@Service
public class JunpcSirmstListService extends GenericListService<ListServiceParameter<JunpcSirmstSearchConditionModel>, ListServiceResponse<JunpcSirmstModel>> {
    @Autowired
    private MSirmstRepository mSirmstRepository;

    @Override
    protected ListServiceResponse<JunpcSirmstModel> execute(final ListServiceParameter<JunpcSirmstSearchConditionModel> serviceParameter) {
        final Page<MSirmstEntity> page = find(serviceParameter);

        return ListServiceResponse.<JunpcSirmstModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> toItem(entity)).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param serviceParameter {@link ListServiceParameter} instance
     * @return {@link Page} instance
     */
    private Page<MSirmstEntity> find(final ListServiceParameter<JunpcSirmstSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());
        // final String sirkbn = serviceParameter.getSearchCondition().getSirkbn(); // 生地縫製の機能実装時にコメント外す
        final String searchText = serviceParameter.getSearchCondition().getSearchText();

        if (StringUtils.isEmpty(serviceParameter.getSearchCondition().getSearchText())) {
            // 検索文字列が空の場合、全検索
            return mSirmstRepository.findBySirkbnOrderBySire(
                    // sirkbn,  // 生地縫製の機能実装時にコメント外す
                    pageRequest);
        }

        switch (MSirmstSearchType.findByValue(serviceParameter.getSearchCondition().getSearchType()).orElse(MSirmstSearchType.CODE_OR_NAME)) {
        case CODE_OR_NAME:
            // 値がなければor検索
            return mSirmstRepository.findBySirkbnAndSireLikeOrNameLikeOrderBySire(
                    // sirkbn,  // 生地縫製の機能実装時にコメント外す
                    searchText,
                    searchText,
                    pageRequest);
        case CODE:
            // コード検索
            return mSirmstRepository.findBySirkbnAndSireLikeOrderBySire(
                    // sirkbn,  // 生地縫製の機能実装時にコメント外す
                    searchText,
                    pageRequest);
        case NAME:
            // 名称検索
            return mSirmstRepository.findBySirkbnAndNameLikeOrderBySire(
                    // sirkbn,  // 生地縫製の機能実装時にコメント外す
                    searchText,
                    pageRequest);
        default:
            return null;
        }
    }

    /**
     * Item に変換する.
     * 発注生産システムの工場マスタは、空文字をnullに変換し、APIのレスポンスサイズを削減する。
     *
     * @param entity {@link MSirmstEntity} instance
     * @return {@link JunpcSirmstModel} instance
     */
    private JunpcSirmstModel toItem(final MSirmstEntity entity) {
        final JunpcSirmstModel item = new JunpcSirmstModel();

        item.setId(entity.getId());
        item.setSire(StringUtils.defaultIfEmpty(entity.getSire(), null));
        //        item.setSirkbn(StringUtils.defaultIfEmpty(entity.getSirkbn(), null));
        //        item.setSname(StringUtils.defaultIfEmpty(entity.getSname(), null));
        item.setName(StringUtils.defaultIfEmpty(entity.getName(), null));
        //        item.setYubin(StringUtils.defaultIfEmpty(entity.getYubin(), null));
        //        item.setAdd1(StringUtils.defaultIfEmpty(entity.getAdd1(), null));
        //        item.setAdd2(StringUtils.defaultIfEmpty(entity.getAdd2(), null));
        //        item.setAdd3(StringUtils.defaultIfEmpty(entity.getAdd3(), null));
        //        item.setTel1(StringUtils.defaultIfEmpty(entity.getTel1(), null));
        //        item.setFax1(StringUtils.defaultIfEmpty(entity.getFax1(), null));
        //        item.setBank(StringUtils.defaultIfEmpty(entity.getBank(), null));
        //        item.setSiten(StringUtils.defaultIfEmpty(entity.getSiten(), null));
        //        item.setKozsyu(StringUtils.defaultIfEmpty(entity.getKozsyu(), null));
        //        item.setKozno(StringUtils.defaultIfEmpty(entity.getKozno(), null));
        //        item.setMeigij(StringUtils.defaultIfEmpty(entity.getMeigij(), null));
        //        item.setMeigik(StringUtils.defaultIfEmpty(entity.getMeigik(), null));
        //        item.setCity(StringUtils.defaultIfEmpty(entity.getCity(), null));
        //        item.setDummy1(StringUtils.defaultIfEmpty(entity.getDummy1(), null));
        //        item.setBrand1(StringUtils.defaultIfEmpty(entity.getBrand1(), null));
        //        item.setHimok1(StringUtils.defaultIfEmpty(entity.getHimok1(), null));
        //        item.setBrand2(StringUtils.defaultIfEmpty(entity.getBrand2(), null));
        //        item.setHimok2(StringUtils.defaultIfEmpty(entity.getHimok2(), null));
        //        item.setBrand3(StringUtils.defaultIfEmpty(entity.getBrand3(), null));
        //        item.setHimok3(StringUtils.defaultIfEmpty(entity.getHimok3(), null));
        //        item.setBrand4(StringUtils.defaultIfEmpty(entity.getBrand4(), null));
        //        item.setHimok4(StringUtils.defaultIfEmpty(entity.getHimok4(), null));
        //        item.setBrand5(StringUtils.defaultIfEmpty(entity.getBrand5(), null));
        //        item.setHimok5(StringUtils.defaultIfEmpty(entity.getHimok5(), null));
        //        item.setBrand6(StringUtils.defaultIfEmpty(entity.getBrand6(), null));
        //        item.setHimok6(StringUtils.defaultIfEmpty(entity.getHimok6(), null));
        //        item.setBrand7(StringUtils.defaultIfEmpty(entity.getBrand7(), null));
        //        item.setHimok7(StringUtils.defaultIfEmpty(entity.getHimok7(), null));
        //        item.setBrand8(StringUtils.defaultIfEmpty(entity.getBrand8(), null));
        //        item.setHimok8(StringUtils.defaultIfEmpty(entity.getHimok8(), null));
        //        item.setBrand9(StringUtils.defaultIfEmpty(entity.getBrand9(), null));
        //        item.setHimok9(StringUtils.defaultIfEmpty(entity.getHimok9(), null));
        //        item.setBrand10(StringUtils.defaultIfEmpty(entity.getBrand10(), null));
        //        item.setHimok10(StringUtils.defaultIfEmpty(entity.getHimok10(), null));
        //        item.setBrand11(StringUtils.defaultIfEmpty(entity.getBrand11(), null));
        //        item.setHimok11(StringUtils.defaultIfEmpty(entity.getHimok11(), null));
        //        item.setBrand12(StringUtils.defaultIfEmpty(entity.getBrand12(), null));
        //        item.setHimok12(StringUtils.defaultIfEmpty(entity.getHimok12(), null));
        //        item.setBrand13(StringUtils.defaultIfEmpty(entity.getBrand13(), null));
        //        item.setHimok13(StringUtils.defaultIfEmpty(entity.getHimok13(), null));
        //        item.setBrand14(StringUtils.defaultIfEmpty(entity.getBrand14(), null));
        //        item.setHimok14(StringUtils.defaultIfEmpty(entity.getHimok14(), null));
        //        item.setBrand15(StringUtils.defaultIfEmpty(entity.getBrand15(), null));
        //        item.setHimok15(StringUtils.defaultIfEmpty(entity.getHimok15(), null));
        //        item.setBrand16(StringUtils.defaultIfEmpty(entity.getBrand16(), null));
        //        item.setHimok16(StringUtils.defaultIfEmpty(entity.getHimok16(), null));
        //        item.setBrand17(StringUtils.defaultIfEmpty(entity.getBrand17(), null));
        //        item.setHimok17(StringUtils.defaultIfEmpty(entity.getHimok17(), null));
        //        item.setBrand18(StringUtils.defaultIfEmpty(entity.getBrand18(), null));
        //        item.setHimok18(StringUtils.defaultIfEmpty(entity.getHimok18(), null));
        //        item.setBrand19(StringUtils.defaultIfEmpty(entity.getBrand19(), null));
        //        item.setHimok19(StringUtils.defaultIfEmpty(entity.getHimok19(), null));
        //        item.setBrand20(StringUtils.defaultIfEmpty(entity.getBrand20(), null));
        //        item.setHimok20(StringUtils.defaultIfEmpty(entity.getHimok20(), null));
        //        item.setBrand21(StringUtils.defaultIfEmpty(entity.getBrand21(), null));
        //        item.setHimok21(StringUtils.defaultIfEmpty(entity.getHimok21(), null));
        //        item.setBrand22(StringUtils.defaultIfEmpty(entity.getBrand22(), null));
        //        item.setHimok22(StringUtils.defaultIfEmpty(entity.getHimok22(), null));
        //        item.setBrand23(StringUtils.defaultIfEmpty(entity.getBrand23(), null));
        //        item.setHimok23(StringUtils.defaultIfEmpty(entity.getHimok23(), null));
        //        item.setBrand24(StringUtils.defaultIfEmpty(entity.getBrand24(), null));
        //        item.setHimok24(StringUtils.defaultIfEmpty(entity.getHimok24(), null));
        //        item.setBrand25(StringUtils.defaultIfEmpty(entity.getBrand25(), null));
        //        item.setHimok25(StringUtils.defaultIfEmpty(entity.getHimok25(), null));
        //        item.setBrand26(StringUtils.defaultIfEmpty(entity.getBrand26(), null));
        //        item.setHimok26(StringUtils.defaultIfEmpty(entity.getHimok26(), null));
        //        item.setBrand27(StringUtils.defaultIfEmpty(entity.getBrand27(), null));
        //        item.setHimok27(StringUtils.defaultIfEmpty(entity.getHimok27(), null));
        //        item.setBrand28(StringUtils.defaultIfEmpty(entity.getBrand28(), null));
        //        item.setHimok28(StringUtils.defaultIfEmpty(entity.getHimok28(), null));
        //        item.setBrand29(StringUtils.defaultIfEmpty(entity.getBrand29(), null));
        //        item.setHimok29(StringUtils.defaultIfEmpty(entity.getHimok29(), null));
        //        item.setBrand30(StringUtils.defaultIfEmpty(entity.getBrand30(), null));
        //        item.setHimok30(StringUtils.defaultIfEmpty(entity.getHimok30(), null));
        //        item.setFtesury(StringUtils.defaultIfEmpty(entity.getFtesury(), null));
        //        item.setBubkbn(StringUtils.defaultIfEmpty(entity.getBubkbn(), null));
        //        item.setSofkbn(StringUtils.defaultIfEmpty(entity.getSofkbn(), null));
        //        item.setHkiji(StringUtils.defaultIfEmpty(entity.getHkiji(), null));
        //        item.setHseihin(StringUtils.defaultIfEmpty(entity.getHseihin(), null));
        //        item.setHnefuda(StringUtils.defaultIfEmpty(entity.getHnefuda(), null));
        //        item.setHfuzoku(StringUtils.defaultIfEmpty(entity.getHfuzoku(), null));
        //        item.setSofhou(StringUtils.defaultIfEmpty(entity.getSofhou(), null));
        //        item.setDummy2(StringUtils.defaultIfEmpty(entity.getDummy2(), null));
        //        item.setLokkbn(StringUtils.defaultIfEmpty(entity.getLokkbn(), null));
        item.setSouflg(StringUtils.defaultIfEmpty(entity.getSouflg(), null));
        //        item.setMntflg(StringUtils.defaultIfEmpty(entity.getMntflg(), null));
        //        item.setTanto(StringUtils.defaultIfEmpty(entity.getTanto(), null));
        //        item.setCrtymd(StringUtils.defaultIfEmpty(entity.getCrtymd(), null));
        //        item.setUpdymd(StringUtils.defaultIfEmpty(entity.getUpdymd(), null));
        //        item.setPgid(StringUtils.defaultIfEmpty(entity.getPgid(), null));
        //        item.setSouflga(StringUtils.defaultIfEmpty(entity.getSouflga(), null));
        //        item.setSouymda(StringUtils.defaultIfEmpty(entity.getSouymda(), null));
        item.setYugaikbn(StringUtils.defaultIfEmpty(entity.getYugaikbn(), null));
        //        item.setYugaiymd(StringUtils.defaultIfEmpty(entity.getYugaiymd(), null));

        return item;
    }
}
