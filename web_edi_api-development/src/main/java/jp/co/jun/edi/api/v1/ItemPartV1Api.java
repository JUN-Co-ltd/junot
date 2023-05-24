package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemPartModel;
import jp.co.jun.edi.model.ItemPartSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ItemPartListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * パーツマスタからパーツ情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/itemParts")
public class ItemPartV1Api {
    @Autowired
    private ItemPartListService listService;

    /**
     * パーツマスタからブランド別のパーツ情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ItemPartSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<ItemPartModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated({ ItemPartSearchConditionModel.Item.class }) final ItemPartSearchConditionModel searchCondition) {
        final ListServiceResponse<ItemPartModel> serviceResponse = listService
                .call(ListServiceParameter.<ItemPartSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<ItemPartModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
