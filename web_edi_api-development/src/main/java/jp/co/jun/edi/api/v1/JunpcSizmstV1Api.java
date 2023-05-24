package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcSizmstModel;
import jp.co.jun.edi.model.JunpcSizmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcSizemstListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注生産システムのサイズマスタを取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/sizmst")
@Slf4j
public class JunpcSizmstV1Api {
    @Autowired
    private JunpcSizemstListService listService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 発注生産システムのサイズマスタをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcSizmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<JunpcSizmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcSizmstSearchConditionModel searchCondition) {
        final JunpcSizmstSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<JunpcSizmstModel> serviceResponse = listService
                .call(ListServiceParameter.<JunpcSizmstSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<JunpcSizmstModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * モデルを取得します.
     *
     * @param searchCondition {@link JunpcSizmstSearchConditionModel} instance
     * @return {@link JunpcSizmstSearchConditionModel} instance
     */
    private JunpcSizmstSearchConditionModel getSearchCondition(
            final JunpcSizmstSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final JunpcSizmstSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        JunpcSizmstSearchConditionModel.class);

                log.info(localSearchCondition.toString());

                return localSearchCondition;
            } catch (IOException e) {
                log.warn("IOException.", e);
            }
        }

        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link JunpcSizmstSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final JunpcSizmstSearchConditionModel searchCondition,
            final boolean nextPage) {
        if (nextPage) {
            searchCondition.setPageToken(null);
            searchCondition.setPage(searchCondition.getPage() + 1);

            try {
                return Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException.", e);
            }
        }

        return "";
    }
}
