package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.model.BrandCodeModel;
import jp.co.jun.edi.model.BrandCodesSearchConditionModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから、Service.
 */
@Service
public class JunpcCodmstBrandCodeByAccountNameService extends GenericListService<ListServiceParameter<BrandCodesSearchConditionModel>,
ListServiceResponse<BrandCodeModel>> {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    /**
     * アカウント名に紐づくブランドコードリストを取得する.
     * @param serviceParameter 検索パラメータ
     * @return ブランドコードリスト
     */
    @Override
    protected ListServiceResponse<BrandCodeModel>
    execute(final ListServiceParameter<BrandCodesSearchConditionModel> serviceParameter) {

        final List<BrandCodeModel> brandCodeList = new ArrayList<>();

        if (StringUtils.isEmpty(serviceParameter.getSearchCondition().getAccountName())) {
            // 全てのブランドコードとブランド名を取得
            brandCodeList.addAll(findAllBrandCode());
        } else {
            // アカウント名を指定してブランドコードを取得
            brandCodeList.addAll(findBrandCodeByAccountName(serviceParameter));
        }

        return ListServiceResponse.<BrandCodeModel>builder().items(brandCodeList).build();

    }

    /**
     *
     * 全ブランドコードを検索する.
     *
     * @return ブランドコードリスト
     */
    private List<BrandCodeModel> findAllBrandCode() {
        // 全ブランドコードリストを取得
        return mCodmstRepository.findByTblIdOrderByCode1(MCodmstTblIdType.BRAND.getValue(),
                                                  PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("code1")))).stream()
        .map(entity -> {

            BrandCodeModel brandCodeModel = new BrandCodeModel();
            brandCodeModel.setBrandCode(entity.getCode1());
            brandCodeModel.setBrandName(entity.getItem1());

            return brandCodeModel;
        }).collect(Collectors.toList());

    }


    /**
     *
     * アカウント名を指定してブランドコードを検索する.
     *
     * @param serviceParameter 検索条件
     * @return ブランドコードリスト
     */
    private List<BrandCodeModel> findBrandCodeByAccountName(final ListServiceParameter<BrandCodesSearchConditionModel> serviceParameter) {
        // アカウント名に紐づくブランドコードリストを取得
        List<BrandCodeModel> brandCodeList = new ArrayList<>();
        List<String> result = mCodmstRepository.findBrandCodeByAccountName(
                MCodmstTblIdType.STAFF.getValue(),
                MCodmstTblIdType.BRAND.getValue(),
                serviceParameter.getSearchCondition().getAccountName());
        for (final String brandCode: result) {
            BrandCodeModel brandCodeModel = new BrandCodeModel();
            brandCodeModel.setBrandCode(brandCode);
            brandCodeList.add(brandCodeModel);
        }

        return brandCodeList;
    }
}
