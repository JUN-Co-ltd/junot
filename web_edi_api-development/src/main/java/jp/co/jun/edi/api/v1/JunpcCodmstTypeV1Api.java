package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcCodmastTypeModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcCodmstTypeListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムのコードマスタからタイプ情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/types")
public class JunpcCodmstTypeV1Api {
    @Autowired
    private JunpcCodmstTypeListService listService;

    /**
     * 発注生産システムのコードマスタからタイプ情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcCodmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<JunpcCodmastTypeModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated({ JunpcCodmstSearchConditionModel.SearchText.class }) final JunpcCodmstSearchConditionModel searchCondition) {
        final ListServiceResponse<JunpcCodmastTypeModel> serviceResponse = listService
                .call(ListServiceParameter.<JunpcCodmstSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<JunpcCodmastTypeModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
