package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcHrtmstModel;
import jp.co.jun.edi.model.JunpcHrtmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcHrtmstListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムの配分率マスタを取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/hrtmsts")
public class JunpcHrtmstV1Api {
    @Autowired
    private JunpcHrtmstListService listService;

    /**
     * 発注生産システムの配分率マスタをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcHrtmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<JunpcHrtmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcHrtmstSearchConditionModel searchCondition) {

        final ListServiceResponse<JunpcHrtmstModel> serviceResponse = listService
                .call(ListServiceParameter.<JunpcHrtmstSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<JunpcHrtmstModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
