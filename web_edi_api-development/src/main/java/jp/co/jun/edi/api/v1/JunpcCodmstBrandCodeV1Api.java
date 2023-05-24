package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.BrandCodeModel;
import jp.co.jun.edi.model.BrandCodesSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcCodmstBrandCodeByAccountNameService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムのコードマスタからブランドコードを取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/brandCodes")
public class JunpcCodmstBrandCodeV1Api {
    @Autowired
    private JunpcCodmstBrandCodeByAccountNameService listService;

    /**
     * 発注生産システムのコードマスタからブランド情報をリストで取得する.
     *
     * @param loginUser 認証情報
     * @param searchCondition 検索条件
     * @return ブランド情報のリスト
     */
    @GetMapping
    public GenericListMobel<BrandCodeModel> list(@AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final BrandCodesSearchConditionModel searchCondition) {
        final ListServiceResponse<BrandCodeModel> serviceResponse = listService
                .call(ListServiceParameter.<BrandCodesSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<BrandCodeModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
