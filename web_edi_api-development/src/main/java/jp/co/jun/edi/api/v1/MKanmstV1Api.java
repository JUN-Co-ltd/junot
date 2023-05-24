package jp.co.jun.edi.api.v1;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.MKanmstModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.MKanmstGetService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;

/**
 * 発注生産システムの管理マスタAPI.
 */
@RestController
@RequestMapping("/api/v1/m/kanmst")
public class MKanmstV1Api {

    @Autowired
    private MKanmstGetService getService;

    /**
     * 発注生産システムの管理マスタから情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @return {@link OrderMKanmstModelModel} instance
     */
    @GetMapping
    public MKanmstModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).build()).getItem();
    }


}
