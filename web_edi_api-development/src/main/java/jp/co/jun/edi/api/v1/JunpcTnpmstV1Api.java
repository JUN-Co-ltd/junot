package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcSizmstSearchConditionModel;
import jp.co.jun.edi.model.JunpcTnpmstModel;
import jp.co.jun.edi.model.JunpcTnpmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcTnpmstSearchService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注生産システムの店舗マスタを取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/tnpmst")
@Slf4j
public class JunpcTnpmstV1Api {
    @Autowired
    private JunpcTnpmstSearchService searchService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 発注生産システムの店舗マスタを検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcTnpmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<JunpcTnpmstModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated final JunpcTnpmstSearchConditionModel searchCondition) {
        final JunpcTnpmstSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<JunpcTnpmstModel> serviceResponse = searchService
                .call(ListServiceParameter.<JunpcTnpmstSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<JunpcTnpmstModel> response = new GenericListMobel<>();
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
    private JunpcTnpmstSearchConditionModel getSearchCondition(
            final JunpcTnpmstSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final JunpcTnpmstSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        JunpcTnpmstSearchConditionModel.class);

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
     * @param searchCondition {@link JunpcTnpmstSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final JunpcTnpmstSearchConditionModel searchCondition,
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
