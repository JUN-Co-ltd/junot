package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.SpecialtyQubeCancelResponseModel;
import jp.co.jun.edi.model.SpecialtyQubeDeleteResponseModel;
import jp.co.jun.edi.model.SpecialtyQubeRequestModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.SpecialtyQubeCancelService;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;

/**
 * SpecialtyQubeAPI.
 */
@RestController
@RequestMapping("/api/v1/specialtyQubes")
@Secured({ "ROLE_EDI", "ROLE_DISTA" })
public class SpecialtyQubeV1Api {

    @Autowired
    private SpecialtyQubeCancelService cancelService;

    /**
     * 店舗配分をキャンセルします.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param sqRequest {@link SpecialtyQubeRequestModel} instance
     * @return {@link SpecialtyQubeDeleteResponseModel} instance
     */
    @PutMapping("/cancel")
    public SpecialtyQubeCancelResponseModel cancel(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final SpecialtyQubeRequestModel sqRequest) {
        return cancelService.call(UpdateServiceParameter.<SpecialtyQubeRequestModel>builder().loginUser(loginUser).item(sqRequest).build()).getItem();
    }
}
